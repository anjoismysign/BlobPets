package me.anjoismysign.blobpets.event;

import me.anjoismysign.blobpets.entity.BlobPet;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Called when a BlobPet is displayed
 */
public class BlobPetDisplayEvent extends BlobPetEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private ItemStack display;

    /**
     * Called when a BlobPet is displayed
     *
     * @param pet     The pet
     * @param display The display
     */
    public BlobPetDisplayEvent(@NotNull BlobPet pet,
                               @NotNull ItemStack display) {
        super(pet, false);
        Objects.requireNonNull(display, "'display' cannot be null!");
        this.display = display;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    /**
     * Gets the display of the pet
     *
     * @return the display of the pet
     */
    public ItemStack getDisplay() {
        return display;
    }

    /**
     * Sets the display of the pet
     *
     * @param display the display of the pet
     */
    public void setDisplay(ItemStack display) {
        this.display = display;
    }
}
