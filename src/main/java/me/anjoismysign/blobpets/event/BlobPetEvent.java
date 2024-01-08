package me.anjoismysign.blobpets.event;

import me.anjoismysign.blobpets.entity.BlobPet;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class BlobPetEvent extends Event {
    private final BlobPet pet;

    public BlobPetEvent(@NotNull BlobPet pet, boolean isAsync) {
        super(isAsync);
        this.pet = pet;
    }

    /**
     * Gets the BlobPet involved in this event
     *
     * @return the BlobPet involved in this event
     */
    @NotNull
    public BlobPet getPet() {
        return pet;
    }
}
