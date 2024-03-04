package me.anjoismysign.blobpets.listener;

import me.anjoismysign.blobpets.BlobPetsAPI;
import me.anjoismysign.blobpets.director.manager.PetsListenerManager;
import me.anjoismysign.blobpets.entity.petowner.BlobPetOwner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class QuitRemove extends BlobPetsListener {
    public QuitRemove(PetsListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        BlobPetOwner owner = Objects.requireNonNull((BlobPetOwner) BlobPetsAPI.getInstance()
                .getPetOwner(event.getPlayer()), "'owner' is null");
        if (owner.isHoldingAPet())
            owner.removeHeldPets();
    }

    @Override
    public boolean checkIfShouldRegister() {
        return true;
    }
}
