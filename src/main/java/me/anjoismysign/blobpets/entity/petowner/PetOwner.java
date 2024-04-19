package me.anjoismysign.blobpets.entity.petowner;

import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface PetOwner extends PetInventoryHolder {

    /**
     * Opens a UI to manage pets
     */
    void managePets();

    /**
     * Will open the pet menu
     */
    void openPetStorage();

    /**
     * Will open the pet inventory
     */
    void openPetInventory();

    /**
     * Gets the player associated with this pet owner
     *
     * @return The player
     */
    Player getPlayer();

    /**
     * Gets the held pets.
     * <p>
     * The key is the index of the pet in the pet inventory
     * <p>
     * The value is the pet itself
     *
     * @return The held pets
     */
    @NotNull
    Map<Integer, BlobFloatingPet> getHeldPets();

    /**
     * Checks whether the pet owner is holding a pet
     *
     * @return If the pet owner is holding a pet
     */
    default boolean isHoldingAPet() {
        return !getHeldPets().isEmpty();
    }

    /**
     * Will check if the pet owner is holding a pet by the hold index
     * The hold index is the order the pet was held in, not the index of #getPets.
     *
     * @param inventoryIndex The index of the pet
     * @return If the pet owner is holding the pet
     */
    default boolean isHoldingPet(int inventoryIndex) {
        return getHeldPets().containsKey(inventoryIndex);
    }

    /**
     * Gets the pet the owner is holding by the hold index.
     * The hold index is the order the pet was held in, not the index of #getPets.
     *
     * @param inventoryIndex The index of the pet
     * @return The pet the owner is holding
     */
    @Nullable
    default BlobFloatingPet getHeldPet(int inventoryIndex) {
        return getHeldPets().get(inventoryIndex);
    }

    /**
     * Returns a pet to the pet storage by its inventory index
     *
     * @param inventoryIndex The index of the pet in the pet inventory
     * @return If the operation was successful. False if denied.
     */
    boolean returnPet(int inventoryIndex);

    /**
     * Will reload the held pets
     */
    void reloadHeldPets();
}
