package me.anjoismysign.blobpets.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncBlobPetsExpansionLoadEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final boolean isFirstLoad;

    public AsyncBlobPetsExpansionLoadEvent(boolean isFirstLoad) {
        super(true);
        this.isFirstLoad = isFirstLoad;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public boolean isFirstLoad() {
        return isFirstLoad;
    }
}
