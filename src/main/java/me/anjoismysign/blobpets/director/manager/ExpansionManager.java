package me.anjoismysign.blobpets.director.manager;

import me.anjoismysign.blobpets.director.PetsManager;
import me.anjoismysign.blobpets.director.PetsManagerDirector;
import me.anjoismysign.blobpets.entity.*;
import me.anjoismysign.blobpets.entity.petexpansion.PetExpansionDirector;
import me.anjoismysign.blobpets.event.AsyncBlobPetsExpansionLoadEvent;
import me.anjoismysign.blobpets.event.AsyncBlobPetsLoadEvent;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.entities.ObjectManager;
import us.mytheria.bloblib.utilities.HandyDirectory;

import java.io.File;
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
        try {
            HandyDirectory handyDirectory = HandyDirectory.of(expansionDirectory);
            for (File file : handyDirectory.listFiles("zip")) {
                if (!loadExpansion(file))
                    getPlugin().getLogger().severe("Failed to load expansion: " + file.getName());
            }
            AsyncBlobPetsExpansionLoadEvent event = new AsyncBlobPetsExpansionLoadEvent(isFirstLoad);
            Bukkit.getPluginManager().callEvent(event);
            AsyncBlobPetsLoadEvent loadEvent = new AsyncBlobPetsLoadEvent(isFirstLoad,
                    Collections.unmodifiableCollection(getManagerDirector()
                            .getBlobPetDirector().getObjectManager().values()));
            Bukkit.getPluginManager().callEvent(loadEvent);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
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
            getPlugin().getLogger().warning("No assets found in expansion: " + expansion.getName());
            return true;
        }
        List<File> petMeasurements = assets.get(AssetType.PET_MEASUREMENTS);
        if (petMeasurements != null && !petMeasurements.isEmpty()) {
            ObjectManager<PetMeasurements> objectManager = director.getPetMeasurementsDirector().getObjectManager();
            petMeasurements.forEach(file -> {
                objectManager.loadFile(file, e -> {
                });
            });
            HandyDirectory.of(assetsDirectory.get(AssetType.PET_MEASUREMENTS))
                    .deleteRecursively();
        }
        List<File> petAnimations = assets.get(AssetType.PET_ANIMATIONS);
        if (petAnimations != null && !petAnimations.isEmpty()) {
            ObjectManager<PetAnimations> objectManager = director.getPetAnimationsDirector().getObjectManager();
            petAnimations.forEach(file -> {
                objectManager.loadFile(file, e -> {
                });
            });
            HandyDirectory.of(assetsDirectory.get(AssetType.PET_ANIMATIONS))
                    .deleteRecursively();
        }
        List<File> petData = assets.get(AssetType.PET_DATA);
        if (petData != null && !petData.isEmpty()) {
            ObjectManager<PetData> objectManager = director.getPetDataDirector().getObjectManager();
            petData.forEach(file -> {
                objectManager.loadFile(file, e -> {
                });
            });
            HandyDirectory.of(assetsDirectory.get(AssetType.PET_DATA))
                    .deleteRecursively();
        }
        List<File> blobPet = assets.get(AssetType.BLOB_PET);
        if (blobPet != null && !blobPet.isEmpty()) {
            ObjectManager<BlobPet> objectManager = director.getBlobPetDirector().getObjectManager();
            blobPet.forEach(file -> {
                objectManager.loadFile(file, e -> {
                });
            });
            HandyDirectory.of(assetsDirectory.get(AssetType.BLOB_PET))
                    .deleteRecursively();
        }
        List<File> attributePet = assets.get(AssetType.ATTRIBUTE_PET);
        if (attributePet != null && !attributePet.isEmpty()) {
            PetExpansionDirector<AttributePet> attributePetDirector = director.getAttributePetDirector();
            attributePet.forEach(attributePetDirector::addExpansion);
            attributePetDirector.setExpansionDirectory(assetsDirectory.get(AssetType.ATTRIBUTE_PET));
        }
        return true;
    }
}
