package me.anjoismysign.blobpets.event;

import me.anjoismysign.blobpets.entity.BlobPet;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class AsyncBlobPetsLoadEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final boolean isFirstLoad;
    private final Collection<BlobPet> pets;

    public AsyncBlobPetsLoadEvent(boolean isFirstLoad,
                                  @NotNull Collection<BlobPet> pets) {
        super(true);
        this.isFirstLoad = isFirstLoad;
        this.pets = pets;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    /**
     * Get whether this is the first load of the plugin
     *
     * @return whether this is the first load of the plugin
     */
    public boolean isFirstLoad() {
        return isFirstLoad;
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
