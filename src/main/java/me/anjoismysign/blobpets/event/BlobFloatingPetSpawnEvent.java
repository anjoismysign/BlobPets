package me.anjoismysign.blobpets.event;

import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a BlobFloatingPet spawns
 */
public class BlobFloatingPetSpawnEvent extends BlobFloatingPetEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    /**
     * Called after a BlobFloatingPet spawns
     *
     * @param pet the BlobFloatingPet that spawned
     */
    public BlobFloatingPetSpawnEvent(@NotNull BlobFloatingPet pet) {
        super(pet, false);
    }
}
