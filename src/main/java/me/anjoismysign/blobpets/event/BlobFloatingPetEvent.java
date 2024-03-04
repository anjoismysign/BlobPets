package me.anjoismysign.blobpets.event;

import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class BlobFloatingPetEvent extends Event {
    private final BlobFloatingPet floatingPet;
    private final int holdIndex;
    private final int storageIndex;

    public BlobFloatingPetEvent(@NotNull BlobFloatingPet floatingPet,
                                int holdIndex,
                                int storageIndex,
                                boolean isAsync) {
        super(isAsync);
        this.holdIndex = holdIndex;
        this.storageIndex = storageIndex;
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
     * Gets the index of the BlobFloatingPet in the pack.
     * If pet is being removed, it shows the index it was in the pack.
     *
     * @return the index of the BlobFloatingPet in the pack
     */
    public int getHoldIndex() {
        return holdIndex;
    }

    /**
     * Gets the index of the BlobFloatingPet in the player's storage
     *
     * @return the index of the BlobFloatingPet in the player's storage
     */
    public int getStorageIndex() {
        return storageIndex;
    }
}
