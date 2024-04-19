package me.anjoismysign.blobpets.event;

import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a BlobFloatingPet is destroyed
 */
public class BlobFloatingPetDestroyEvent extends BlobFloatingPetEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    /**
     * Called before a BlobFloatingPet is destroyed
     *
     * @param pet   the BlobFloatingPet that will be destroyed
     * @param index the index of the BlobFloatingPet in the inventory
     */
    public BlobFloatingPetDestroyEvent(@NotNull BlobFloatingPet pet,
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
