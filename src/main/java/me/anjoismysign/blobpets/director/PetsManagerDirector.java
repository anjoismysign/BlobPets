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
import org.bukkit.entity.Player;
import us.mytheria.bloblib.api.BlobLibInventoryAPI;
import us.mytheria.bloblib.entities.GenericManagerDirector;
import us.mytheria.bloblib.entities.ObjectDirector;
import us.mytheria.bloblib.entities.inventory.InventoryButton;
import us.mytheria.bloblib.entities.inventory.InventoryDataRegistry;

import java.util.Collections;

public class PetsManagerDirector extends GenericManagerDirector<BlobPets> {
    private final BlobPetsCmd blobPetsCmd;

    public PetsManagerDirector(BlobPets plugin) {
        super(plugin);
        registerBlobInventory("View-Pet-Storage", "es_es/View-Pet-Storage");
        registerBlobInventory("View-Pet-Inventory", "es_es/View-Pet-Inventory");
        registerBlobInventory("Manage-Pets", "es_es/Manage-Pets");
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
        reloadInventories();
    }

    private void reloadInventories() {
        InventoryDataRegistry<InventoryButton> registry = BlobLibInventoryAPI.getInstance()
                .getInventoryDataRegistry("Manage-Pets");
        registry.onClick("Storage", event -> {
            Player player = (Player) event.getWhoClicked();
            BlobPetOwner owner = BlobPetOwner.by(player);
            if (owner == null)
                return;
            owner.openPetStorage();
        });
        registry.onClick("Inventory", event -> {
            Player player = (Player) event.getWhoClicked();
            BlobPetOwner owner = BlobPetOwner.by(player);
            if (owner == null)
                return;
            owner.openPetInventory();
        });
    }

    /**
     * From top to bottom, follow the order.
     */
    @Override
    public void reload() {
        reloadInventories();
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
                                    .forEach(BlobPetOwner::reloadHeldPets);
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