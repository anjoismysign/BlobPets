package me.anjoismysign.blobpets.event;

import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a BlobFloatingPet spawns
 */
public class BlobFloatingPetSpawnEvent extends BlobFloatingPetEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    /**
     * Called after a BlobFloatingPet spawns
     *
     * @param pet   the BlobFloatingPet that spawned
     * @param index the index of the BlobFloatingPet in the inventory
     */
    public BlobFloatingPetSpawnEvent(@NotNull BlobFloatingPet pet,
                                     int index) {
        super(pet, index, false);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
