package me.anjoismysign.blobpets.listener;

import me.anjoismysign.blobpets.BlobPetsAPI;
import me.anjoismysign.blobpets.director.manager.PetsListenerManager;
import me.anjoismysign.blobpets.entity.AttributePet;
import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import me.anjoismysign.blobpets.event.BlobFloatingPetDestroyEvent;
import me.anjoismysign.blobpets.event.BlobFloatingPetSpawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class AttributePetsListener extends BlobPetsListener {
    public AttributePetsListener(PetsListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler
    public void onSpawn(BlobFloatingPetSpawnEvent event) {
        BlobFloatingPet floatingPet = event.getFloatingPet();
        String key = floatingPet.getKey();
        AttributePet pet = BlobPetsAPI.getInstance().isLinkedToAttributePet(key);
        if (pet == null)
            return;
        Player owner = floatingPet.getPetOwner();
        pet.apply(owner);
    }

    @EventHandler
    public void onDestroy(BlobFloatingPetDestroyEvent event) {
        BlobFloatingPet floatingPet = event.getFloatingPet();
        String key = floatingPet.getKey();
        AttributePet pet = BlobPetsAPI.getInstance().isLinkedToAttributePet(key);
        if (pet == null)
            return;
        Player owner = floatingPet.getPetOwner();
        pet.unapply(owner);
    }

    @Override
    public boolean checkIfShouldRegister() {
        return getConfigManager().getAttributePets().register();
    }
}
