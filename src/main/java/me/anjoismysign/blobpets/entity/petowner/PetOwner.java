package me.anjoismysign.blobpets.entity.petowner;

import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface PetOwner extends PetInventoryHolder {

    /**
     * Will open the pet menu
     */
    void openPetMenu();

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
     * @param holdIndex The index of the pet
     * @return If the pet owner is holding the pet
     */
    default boolean isHoldingPet(int holdIndex) {
        return getHeldPets().containsKey(holdIndex);
    }

    /**
     * Gets the pet the owner is holding by the hold index.
     * The hold index is the order the pet was held in, not the index of #getPets.
     *
     * @param holdIndex The index of the pet
     * @return The pet the owner is holding
     */
    @Nullable
    default BlobFloatingPet getHeldPet(int holdIndex) {
        return getHeldPets().get(holdIndex);
    }

    /**
     * Returns a held pet to storage by its storage index.
     * The storage index is the index of the pet in #getPets
     *
     * @param storageIndex The realIndex of the pet
     * @return If the pet was returned. False if no pet was held by that real index.
     */
    boolean returnHeldPet(int storageIndex);

    /**
     * Returns a held pet to storage
     *
     * @param pet The pet to return
     * @return If the pet was returned. False if pet is not held.
     */
    default boolean returnHeldPet(BlobFloatingPet pet) {
        for (Map.Entry<Integer, BlobFloatingPet> entry : getHeldPets().entrySet()) {
            if (!entry.getValue().equals(pet))
                continue;
            return returnHeldPet(entry.getKey());
        }
        return false;
    }

    /**
     * Holds a pet by its storage index.
     * The storage index is the index of the pet in #getPets
     *
     * @param storageIndex The index of the pet
     * @return If the operation was successful. False if denied.
     */
    boolean holdPet(int storageIndex);

    /**
     * Will reload the held pets
     */
    void reloadHeldPets();
}
