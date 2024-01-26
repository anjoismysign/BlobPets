package me.anjoismysign.blobpets.director;

import me.anjoismysign.blobpets.BlobPets;
import me.anjoismysign.blobpets.command.BlobPetsCmd;
import me.anjoismysign.blobpets.director.manager.BlobPetOwnerManager;
import me.anjoismysign.blobpets.director.manager.PetsConfigManager;
import me.anjoismysign.blobpets.director.manager.PetsListenerManager;
import me.anjoismysign.blobpets.entity.*;
import me.anjoismysign.blobpets.entity.petexpansion.PetExpansionDirector;
import me.anjoismysign.blobpets.entity.petowner.BlobPetOwner;
import me.anjoismysign.blobpets.event.AsyncBlobPetsLoadEvent;
import org.bukkit.Bukkit;
import us.mytheria.bloblib.entities.GenericManagerDirector;
import us.mytheria.bloblib.entities.ObjectDirector;

import java.util.Collections;

public class PetsManagerDirector extends GenericManagerDirector<BlobPets> {
    private final BlobPetsCmd blobPetsCmd;

    public PetsManagerDirector(BlobPets plugin) {
        super(plugin);
        registerBlobInventory("View-Pets", "es_es/View-Pets");
        addManager("ConfigManager", new PetsConfigManager(this));
        addManager("ListenerManager", new PetsListenerManager(this));
        addManager("BlobPetOwner", new BlobPetOwnerManager(this,
                getConfigManager().tinyDebug()));
        addDirector("PetMeasurements", PetMeasurements::fromFile, false);
        getPetMeasurementsDirector().whenObjectManagerFilesLoad(a -> {
            addDirector("PetAnimations", PetAnimations::fromFile, false);
            getPetAnimationsDirector().whenObjectManagerFilesLoad(b -> {
                addDirector("PetData", PetData::fromFile, false);
                getPetDataDirector().whenObjectManagerFilesLoad(c -> {
                    addDirector("BlobPet", file -> BlobPet
                            .fromFile(file, this), false);
                    getBlobPetDirector().whenObjectManagerFilesLoad(d -> {
                        AsyncBlobPetsLoadEvent event = new AsyncBlobPetsLoadEvent(Collections.unmodifiableCollection(d.values()));
                        Bukkit.getPluginManager().callEvent(event);
                        addManager("AttributePetDirector",
                                PetExpansionDirector.of(this,
                                        "AttributePet",
                                        AttributePet::fromFile));
                    });
                });
            });
        });
        blobPetsCmd = BlobPetsCmd.of(this);
    }

    /**
     * From top to bottom, follow the order.
     */
    @Override
    public void reload() {
        getConfigManager().reload();
        getListenerManager().reload();
        getPetMeasurementsDirector().reload();
        getPetMeasurementsDirector().whenObjectManagerFilesLoad(a -> {
            getPetAnimationsDirector().reload();
            getPetAnimationsDirector().whenObjectManagerFilesLoad(b -> {
                getPetDataDirector().reload();
                getPetDataDirector().whenObjectManagerFilesLoad(c -> {
                    getBlobPetDirector().reload();
                    getBlobPetDirector().whenObjectManagerFilesLoad(d -> {
                        Bukkit.getScheduler().runTask(getPlugin(), () -> {
                            getBlobPetOwnerManager().getAll()
                                    .forEach(BlobPetOwner::reloadHeldPet);
                            Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
                                AsyncBlobPetsLoadEvent event = new AsyncBlobPetsLoadEvent(Collections.unmodifiableCollection(d.values()));
                                Bukkit.getPluginManager().callEvent(event);
                            });
                        });
                    });
                });
            });
        });
    }

    @Override
    public void unload() {
        getBlobPetOwnerManager().unload();
    }

    public final ObjectDirector<PetMeasurements> getPetMeasurementsDirector() {
        return getDirector("PetMeasurements", PetMeasurements.class);
    }

    public final ObjectDirector<PetAnimations> getPetAnimationsDirector() {
        return getDirector("PetAnimations", PetAnimations.class);
    }

    public final ObjectDirector<PetData> getPetDataDirector() {
        return getDirector("PetData", PetData.class);
    }

    public final ObjectDirector<BlobPet> getBlobPetDirector() {
        return getDirector("BlobPet", BlobPet.class);
    }

    @SuppressWarnings("unchecked")
    public final PetExpansionDirector<AttributePet> getAttributePetDirector() {
        return (PetExpansionDirector<AttributePet>) getManager("AttributePetDirector");
    }

    public final PetsConfigManager getConfigManager() {
        return getManager("ConfigManager", PetsConfigManager.class);
    }

    public final PetsListenerManager getListenerManager() {
        return getManager("ListenerManager", PetsListenerManager.class);
    }

    public final BlobPetOwnerManager getBlobPetOwnerManager() {
        return getManager("BlobPetOwner", BlobPetOwnerManager.class);
    }
}