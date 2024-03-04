package me.anjoismysign.blobpets.director.manager;

import me.anjoismysign.blobpets.director.PetsManager;
import me.anjoismysign.blobpets.director.PetsManagerDirector;
import me.anjoismysign.blobpets.settings.PetPacking;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import us.mytheria.bloblib.entities.ConfigDecorator;
import us.mytheria.bloblib.entities.ListenersSection;
import us.mytheria.bloblib.entities.TinyEventListener;

public class PetsConfigManager extends PetsManager {
    private boolean tinyDebug;
    private int applyDelay;
    private PetPacking petPacking;
    private TinyEventListener displayLevel;

    public PetsConfigManager(PetsManagerDirector managerDirector) {
        super(managerDirector);
        tinyDebug = false;
        reload();
    }

    @Override
    public void reload() {
        ConfigDecorator configDecorator = getPlugin().getConfigDecorator();
        ConfigurationSection settingsSection = configDecorator.reloadAndGetSection("Settings");
        tinyDebug = settingsSection.getBoolean("Tiny-Debug");
        applyDelay = settingsSection.getInt("Apply-Delay");
        ConfigurationSection petPackingSection = settingsSection.getConfigurationSection("Pet-Packing");
        ConfigurationSection pivotSection = petPackingSection.getConfigurationSection("Pivot");
        Vector pivot = new Vector(pivotSection.getDouble("Forward"), pivotSection.getDouble("Up"), pivotSection.getDouble("Right"));
        petPacking = PetPacking.of(petPackingSection.getDouble("Distance"),
                petPackingSection.getInt("Row-Size"),
                petPackingSection.getInt("Max-Pack-Size"),
                pivot);
        BlobPetOwnerManager manager = getManagerDirector().getBlobPetOwnerManager();
        if (manager != null)
            manager.getAll().forEach(owner -> owner
                    .petOwnerPackUber().thanks().apply(petPacking));
        ListenersSection listenersSection = configDecorator.reloadAndGetListeners();
        displayLevel = listenersSection.tinyEventListener("DisplayLevel");
    }

    public boolean tinyDebug() {
        return tinyDebug;
    }

    public int getApplyDelay() {
        return applyDelay;
    }

    public PetPacking getPetPacking() {
        return petPacking;
    }

    public TinyEventListener getDisplayLevel() {
        return displayLevel;
    }
}