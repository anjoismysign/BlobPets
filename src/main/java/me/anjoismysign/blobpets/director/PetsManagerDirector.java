package me.anjoismysign.blobpets.director;

import me.anjoismysign.blobpets.BlobPets;
import me.anjoismysign.blobpets.command.BlobPetsCmd;
import me.anjoismysign.blobpets.director.manager.BlobPetOwnerManager;
import me.anjoismysign.blobpets.director.manager.ExpansionManager;
import me.anjoismysign.blobpets.director.manager.PetsConfigManager;
import me.anjoismysign.blobpets.director.manager.PetsListenerManager;
import me.anjoismysign.blobpets.entity.*;
import me.anjoismysign.blobpets.entity.petexpansion.PetExpansionDirector;
import me.anjoismysign.blobpets.entity.petowner.BlobPetOwner;
import me.anjoismysign.blobpets.event.AsyncBlobPetsLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.api.BlobLibInventoryAPI;
import us.mytheria.bloblib.entities.GenericManagerDirector;
import us.mytheria.bloblib.entities.ObjectDirector;
import us.mytheria.bloblib.entities.inventory.InventoryButton;
import us.mytheria.bloblib.entities.inventory.InventoryDataRegistry;

import java.util.logging.Logger;

public class PetsManagerDirector extends GenericManagerDirector<BlobPets>
        implements Listener {
    private final BlobPetsCmd blobPetsCmd;
    private final Logger logger;

    public PetsManagerDirector(BlobPets plugin) {
        super(plugin);
        logger = plugin.getLogger();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        registerBlobInventory("View-Pet-Storage", "es_es/View-Pet-Storage");
        registerBlobInventory("View-Pet-Inventory", "es_es/View-Pet-Inventory");
        registerBlobInventory("Manage-Pets", "es_es/Manage-Pets");
        addManager("ConfigManager", new PetsConfigManager(this));
        boolean tinyDebug = getConfigManager().tinyDebug();
        addManager("ListenerManager", new PetsListenerManager(this));
        addManager("BlobPetOwner", new BlobPetOwnerManager(this,
                getConfigManager().tinyDebug()));
        if (tinyDebug)
            logger.warning("Loading PetMeasurements");
        addDirector("PetMeasurements", PetMeasurements::fromFile, false);
        getPetMeasurementsDirector().whenReloaded(() -> {
            if (tinyDebug)
                logger.warning("PetMeasurements loaded");
        });
        if (tinyDebug)
            logger.warning("Loading PetAnimations");
        addDirector("PetAnimations", PetAnimations::fromFile, false);
        if (tinyDebug)
            logger.warning("Loading PetData");
        addDirector("PetData", PetData::fromFile, false);
        if (tinyDebug)
            logger.warning("Loading BlobPet");
        new BukkitRunnable() {
            @Override
            public void run() {
                if (independentAreReloading())
                    return;
                cancel();
                addDirector("BlobPet", file -> BlobPet
                        .fromFile(file, PetsManagerDirector.this), false);
                getBlobPetDirector().whenObjectManagerFilesLoad(blobPetObjectManager -> {
                    addManager("AttributePetDirector",
                            PetExpansionDirector.of(PetsManagerDirector.this,
                                    "AttributePet",
                                    AttributePet::fromFile));
                    getAttributePetDirector().whenObjectManagerFilesLoad(e -> {
                        addManager("ExpansionManager",
                                new ExpansionManager(PetsManagerDirector.this));
                    });
                });
            }
        }.runTaskTimerAsynchronously(getPlugin(), 1L, 1L);
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

    @EventHandler
    public void onLoad(AsyncBlobPetsLoadEvent event) {
        boolean tinyDebug = getConfigManager().tinyDebug();
        if (tinyDebug)
            logger.warning("BlobPets has loaded");
        Bukkit.getScheduler().runTask(getPlugin(), () -> {
            getBlobPetOwnerManager().getAll()
                    .forEach(BlobPetOwner::reloadHeldPets);
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
        getPetAnimationsDirector().reload();
        getPetDataDirector().reload();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (independentAreReloading())
                    return;
                cancel();
                getBlobPetDirector().reload();
                getBlobPetDirector().whenObjectManagerFilesLoad(d -> {
                    getAttributePetDirector().reload();
                    getAttributePetDirector().whenObjectManagerFilesLoad(e -> {
                        getExpansionManager().reload();
                    });
                });
            }
        }.runTaskTimerAsynchronously(getPlugin(), 1L, 1L);
    }

    @Override
    public void unload() {
        getBlobPetOwnerManager().unload();
    }

    @NotNull
    public final ObjectDirector<PetMeasurements> getPetMeasurementsDirector() {
        return getDirector("PetMeasurements", PetMeasurements.class);
    }

    @NotNull
    public final ObjectDirector<PetAnimations> getPetAnimationsDirector() {
        return getDirector("PetAnimations", PetAnimations.class);
    }

    @NotNull
    public final ObjectDirector<PetData> getPetDataDirector() {
        return getDirector("PetData", PetData.class);
    }

    @NotNull
    public final ObjectDirector<BlobPet> getBlobPetDirector() {
        return getDirector("BlobPet", BlobPet.class);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public final PetExpansionDirector<AttributePet> getAttributePetDirector() {
        return (PetExpansionDirector<AttributePet>) getManager("AttributePetDirector");
    }

    @NotNull
    public final ExpansionManager getExpansionManager() {
        return getManager("ExpansionManager", ExpansionManager.class);
    }

    @NotNull
    public final PetsConfigManager getConfigManager() {
        return getManager("ConfigManager", PetsConfigManager.class);
    }

    @NotNull
    public final PetsListenerManager getListenerManager() {
        return getManager("ListenerManager", PetsListenerManager.class);
    }

    @NotNull
    public final BlobPetOwnerManager getBlobPetOwnerManager() {
        return getManager("BlobPetOwner", BlobPetOwnerManager.class);
    }

    @Override
    public boolean isReloading() {
        return getPetMeasurementsDirector().isReloading() ||
                getPetAnimationsDirector().isReloading() ||
                getPetDataDirector().isReloading() ||
                getManager("BlobPetDirector") == null ||
                getBlobPetDirector().isReloading() ||
                getManager("AttributePetDirector") != null
                        && getAttributePetDirector().isReloading();
    }

    private boolean independentAreReloading() {
        return getPetMeasurementsDirector().isReloading() ||
                getPetAnimationsDirector().isReloading() ||
                getPetDataDirector().isReloading();
    }
}