package me.anjoismysign.blobpets.event;

import me.anjoismysign.blobpets.entity.petowner.PetOwner;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PetOwnerLoadEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final PetOwner petOwner;

    public PetOwnerLoadEvent(PetOwner petOwner) {
        super(false);
        this.petOwner = petOwner;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    /**
     * Get the PetOwner that was loaded
     *
     * @return the PetOwner that was loaded
     */
    public PetOwner getPetOwner() {
        return petOwner;
    }
}
