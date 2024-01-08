package me.anjoismysign.blobpets.listener;

import me.anjoismysign.blobpets.BlobPetsAPI;
import me.anjoismysign.blobpets.director.manager.PetsListenerManager;
import me.anjoismysign.blobpets.entity.petowner.PetOwner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitRemove extends BlobPetsListener {
    public QuitRemove(PetsListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        PetOwner owner = BlobPetsAPI.getInstance()
                .getPetOwner(event.getPlayer());
        if (owner.getHeldPet() != null)
            owner.getHeldPet().remove();
    }

    @Override
    public boolean checkIfShouldRegister() {
        return true;
    }
}
