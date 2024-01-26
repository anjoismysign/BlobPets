package me.anjoismysign.blobpets.director.manager;

import me.anjoismysign.blobpets.director.PetsManager;
import me.anjoismysign.blobpets.director.PetsManagerDirector;
import org.bukkit.configuration.ConfigurationSection;
import us.mytheria.bloblib.entities.ConfigDecorator;
import us.mytheria.bloblib.entities.ListenersSection;
import us.mytheria.bloblib.entities.TinyEventListener;

public class PetsConfigManager extends PetsManager {
    private boolean tinyDebug;
    private int applyDelay;
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
        ListenersSection listenersSection = configDecorator.reloadAndGetListeners();
        displayLevel = listenersSection.tinyEventListener("DisplayLevel");
    }

    public boolean tinyDebug() {
        return tinyDebug;
    }

    public int getApplyDelay() {
        return applyDelay;
    }

    public TinyEventListener getDisplayLevel() {
        return displayLevel;
    }
}