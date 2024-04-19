package me.anjoismysign.blobpets.event;

import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class BlobFloatingPetEvent extends Event {
    private final BlobFloatingPet floatingPet;
    private final int index;

    public BlobFloatingPetEvent(@NotNull BlobFloatingPet floatingPet,
                                int index,
                                boolean isAsync) {
        super(isAsync);
        this.index = index;
        this.floatingPet = floatingPet;
    }

    /**
     * Gets the BlobFloatingPet involved in this event
     *
     * @return the BlobFloatingPet involved in this event
     */
    @NotNull
    public BlobFloatingPet getFloatingPet() {
        return floatingPet;
    }

    /**
     * Gets the index of the BlobFloatingPet in the inventory.
     * If pet is being removed, it shows the index it had in the inventory.
     *
     * @return the index of the BlobFloatingPet in the inventory
     */
    public int getIndex() {
        return index;
    }
}
