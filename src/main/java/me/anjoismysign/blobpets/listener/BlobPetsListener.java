package me.anjoismysign.blobpets.listener;

import me.anjoismysign.blobpets.director.manager.PetsConfigManager;
import me.anjoismysign.blobpets.director.manager.PetsListenerManager;
import us.mytheria.bloblib.entities.BlobListener;

public abstract class BlobPetsListener implements BlobListener {
    private final PetsListenerManager listenerManager;

    public BlobPetsListener(PetsListenerManager listenerManager) {
        this.listenerManager = listenerManager;
    }

    public PetsListenerManager getListenerManager() {
        return listenerManager;
    }

    public PetsConfigManager getConfigManager() {
        return getListenerManager().getManagerDirector().getConfigManager();
    }
}
