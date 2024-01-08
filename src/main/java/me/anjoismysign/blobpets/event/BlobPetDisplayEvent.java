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

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    private ItemStack display;
    private final int level;

    /**
     * Called when a BlobPet is displayed
     *
     * @param pet     The pet
     * @param display The display
     */
    public BlobPetDisplayEvent(@NotNull BlobPet pet,
                               @NotNull ItemStack display,
                               @NotNull int level) {
        super(pet, false);
        Objects.requireNonNull(display, "'display' cannot be null!");
        this.display = display;
        this.level = level;
    }

    /**
     * Gets the level of the pet
     *
     * @return the level of the pet
     */
    public int getLevel() {
        return level;
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
