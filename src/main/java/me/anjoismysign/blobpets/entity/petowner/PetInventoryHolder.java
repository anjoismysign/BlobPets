package me.anjoismysign.blobpets.entity.petowner;

import me.anjoismysign.blobpets.entity.BlobPet;
import me.anjoismysign.blobpets.entity.PlayerPet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PetInventoryHolder {

    /**
     * Gets the pets of this pet owner that are accessible in the pet inventory
     *
     * @return The pets
     */
    List<PlayerPet> getPets();

    /**
     * Serializes the pets of this pet owner
     *
     * @return The serialized pets
     */
    default Map<String, Object> serializePets() {
        Map<String, Object> pets = new HashMap<>();
        for (int i = 0; i < getPets().size(); i++) {
            PlayerPet pet = getPet(i);
            if (pet == null) continue;
            pets.put(String.valueOf(i), pet.serialize());
        }
        return pets;
    }

    static List<PlayerPet> deserializePets(Map<String, Object> pets) {
        List<PlayerPet> playerPets = new ArrayList<>();
        for (Map.Entry<String, Object> entry : pets.entrySet()) {
            playerPets.add(PlayerPet.deserialize((Map<String, Object>) entry.getValue()));
        }
        return playerPets;
    }

    /**
     * Adds a pet to the pet inventory
     *
     * @param pet         The pet to add
     * @param level       The level of the pet
     * @param displayName The display name of the pet
     */
    default void addPet(@NotNull BlobPet pet,
                        int level,
                        @Nullable String displayName) {
        getPets().add(new PlayerPet(pet.getKey(), level, displayName));
    }

    /**
     * Adds a pet to the pet inventory.
     * It will have pet's name as display name.
     *
     * @param pet   The pet to add
     * @param level The level of the pet
     */
    default void addPet(@NotNull BlobPet pet,
                        int level) {
        addPet(pet, level, null);
    }

    /**
     * Adds a pet to the pet inventory.
     * It will have a level of 1.
     * It will have pet's name as display name.
     *
     * @param pet The pet to add
     */
    default void addPet(@NotNull BlobPet pet) {
        addPet(pet, 1);
    }

    /**
     * Removes a pet from the pet inventory
     *
     * @param pet The pet to remove
     */
    default void removePet(@NotNull PlayerPet pet) {
        getPets().remove(pet);
    }

    /**
     * Removes a pet from the pet inventory through its index
     *
     * @param index The index of the pet to remove
     */
    default void removePet(int index) {
        getPets().remove(index);
    }

    /**
     * Gets a pet from the pet inventory through its index
     *
     * @param index The index of the pet
     * @return The pet
     */
    @Nullable
    default PlayerPet getPet(int index) {
        return getPets().get(index);
    }
}
