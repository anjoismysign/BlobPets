package me.anjoismysign.blobpets.event;

import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class BlobFloatingPetEvent extends Event {
    private final BlobFloatingPet floatingPet;

    public BlobFloatingPetEvent(@NotNull BlobFloatingPet floatingPet, boolean isAsync) {
        super(isAsync);
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
}
