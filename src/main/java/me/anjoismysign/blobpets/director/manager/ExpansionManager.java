package me.anjoismysign.blobpets.director.manager;

import me.anjoismysign.blobpets.director.PetsManager;
import me.anjoismysign.blobpets.director.PetsManagerDirector;
import me.anjoismysign.blobpets.entity.*;
import me.anjoismysign.blobpets.entity.petexpansion.PetExpansionDirector;
import me.anjoismysign.blobpets.event.AsyncBlobPetsLoadEvent;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.entities.ObjectManager;
import us.mytheria.bloblib.utilities.HandyDirectory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ExpansionManager extends PetsManager {
    private final File expansionDirectory, expansionOutputFile;

    public ExpansionManager(PetsManagerDirector managerDirector) {
        super(managerDirector);
        expansionDirectory = new File(getPlugin().getDataFolder(), "expansion");
        expansionOutputFile = new File(expansionDirectory, "output");
        if (!expansionDirectory.exists())
            expansionDirectory.mkdirs();
        if (!expansionOutputFile.exists())
            expansionOutputFile.mkdirs();
        reload(true);
    }

    @Override
    public void reload() {
        //this is only called by BlobLib when reloading BlobPets
        reload(false);
    }

    public void reload(boolean isFirstLoad) {
        HandyDirectory handyDirectory = HandyDirectory.of(expansionDirectory);
        for (File file : handyDirectory.listFiles("zip")) {
            if (!loadExpansion(file))
                getPlugin().getLogger().severe("Failed to load expansion: " + file.getName());
        }
        AsyncBlobPetsLoadEvent event = new AsyncBlobPetsLoadEvent(isFirstLoad,
                Collections.unmodifiableCollection(getManagerDirector()
                        .getBlobPetDirector().getObjectManager().values()));
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * Loads an expansion from a file.
     *
     * @param expansion The file to load the expansion from.
     * @return {@code true} if the expansion was loaded successfully, {@code false} otherwise.
     */
    public boolean loadExpansion(@NotNull File expansion) {
        Objects.requireNonNull(expansion);
        PetsManagerDirector director = getManagerDirector();
        if (!director.loadBlobLibExpansion(expansion))
            return false;
        HandyDirectory handyDirectory = HandyDirectory.of(expansionOutputFile);
        Map<AssetType, List<File>> assets = new HashMap<>();
        Map<AssetType, File> assetsDirectory = new HashMap<>();
        for (File directory : handyDirectory.listDirectories()) {
            if (directory.getName().equals(AssetType.PET_MEASUREMENTS.getDirectoryName())) {
                HandyDirectory subDirectory = HandyDirectory.of(directory);
                List<File> list = subDirectory.listRecursively("yml").stream().toList();
                assets.put(AssetType.PET_MEASUREMENTS, list);
                assetsDirectory.put(AssetType.PET_MEASUREMENTS, directory);
                continue;
            }
            if (directory.getName().equals(AssetType.PET_ANIMATIONS.getDirectoryName())) {
                HandyDirectory subDirectory = HandyDirectory.of(directory);
                List<File> list = subDirectory.listRecursively("yml").stream().toList();
                assets.put(AssetType.PET_ANIMATIONS, list);
                assetsDirectory.put(AssetType.PET_ANIMATIONS, directory);
                continue;
            }
            if (directory.getName().equals(AssetType.PET_DATA.getDirectoryName())) {
                HandyDirectory subDirectory = HandyDirectory.of(directory);
                List<File> list = subDirectory.listRecursively("yml").stream().toList();
                assets.put(AssetType.PET_DATA, list);
                assetsDirectory.put(AssetType.PET_DATA, directory);
                continue;
            }
            if (directory.getName().equals(AssetType.BLOB_PET.getDirectoryName())) {
                HandyDirectory subDirectory = HandyDirectory.of(directory);
                List<File> list = subDirectory.listRecursively("yml").stream().toList();
                assets.put(AssetType.BLOB_PET, list);
                assetsDirectory.put(AssetType.BLOB_PET, directory);
                continue;
            }
            if (directory.getName().equals(AssetType.ATTRIBUTE_PET.getDirectoryName())) {
                HandyDirectory subDirectory = HandyDirectory.of(directory);
                List<File> list = subDirectory.listRecursively("yml").stream().toList();
                assets.put(AssetType.ATTRIBUTE_PET, list);
                assetsDirectory.put(AssetType.ATTRIBUTE_PET, directory);
            }
        }
        if (assets.isEmpty()) {
            Bukkit.getLogger().warning("No assets found in expansion: " + expansion.getName());
            return true;
        }
        List<File> petMeasurements = assets.get(AssetType.PET_MEASUREMENTS);
        if (petMeasurements != null) {
            ObjectManager<PetMeasurements> objectManager = director.getPetMeasurementsDirector().getObjectManager();
            petMeasurements.forEach(file -> {
                objectManager.loadFile(file, e -> {
                });
            });
            try {
                FileUtils.deleteDirectory(assetsDirectory.get(AssetType.PET_MEASUREMENTS));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<File> petAnimations = assets.get(AssetType.PET_ANIMATIONS);
        if (petAnimations != null) {
            ObjectManager<PetAnimations> objectManager = director.getPetAnimationsDirector().getObjectManager();
            petAnimations.forEach(file -> {
                objectManager.loadFile(file, e -> {
                });
            });
            try {
                FileUtils.deleteDirectory(assetsDirectory.get(AssetType.PET_ANIMATIONS));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<File> petData = assets.get(AssetType.PET_DATA);
        if (petData != null) {
            ObjectManager<PetData> objectManager = director.getPetDataDirector().getObjectManager();
            petData.forEach(file -> {
                objectManager.loadFile(file, e -> {
                });
            });
            try {
                FileUtils.deleteDirectory(assetsDirectory.get(AssetType.PET_DATA));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<File> blobPet = assets.get(AssetType.BLOB_PET);
        if (blobPet != null) {
            ObjectManager<BlobPet> objectManager = director.getBlobPetDirector().getObjectManager();
            blobPet.forEach(file -> {
                objectManager.loadFile(file, e -> {
                });
            });
            try {
                FileUtils.deleteDirectory(assetsDirectory.get(AssetType.BLOB_PET));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<File> attributePet = assets.get(AssetType.ATTRIBUTE_PET);
        if (attributePet != null) {
            PetExpansionDirector<AttributePet> attributePetDirector = director.getAttributePetDirector();
            attributePet.forEach(file -> {
                attributePetDirector.getObjectManager().loadFile(file, e -> {
                });
            });
            try {
                FileUtils.deleteDirectory(assetsDirectory.get(AssetType.ATTRIBUTE_PET));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
