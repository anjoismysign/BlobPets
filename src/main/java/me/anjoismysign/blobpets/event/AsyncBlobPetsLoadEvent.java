package me.anjoismysign.blobpets.event;

import me.anjoismysign.blobpets.entity.BlobPet;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;

public class AsyncBlobPetsLoadEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    private final Collection<BlobPet> pets;

    public AsyncBlobPetsLoadEvent(Collection<BlobPet> pets) {
        super(true);
        this.pets = pets;
    }

    /**
     * Get all the BlobPets that were loaded
     *
     * @return all the BlobPets that were loaded
     */
    public Collection<BlobPet> getPets() {
        return pets;
    }
}
