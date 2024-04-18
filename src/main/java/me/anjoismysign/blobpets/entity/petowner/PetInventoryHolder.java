package me.anjoismysign.blobpets.entity.petowner;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import me.anjoismysign.anjo.entities.Tuple2;
import me.anjoismysign.blobpets.entity.BlobPet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public interface PetInventoryHolder extends PetStorage {

    @NotNull
    @SuppressWarnings("unchecked")
    static Tuple2<Map<Integer, String>, BiMap<String, Integer>> deserializePets(Map<String, Object> pets) {
        Map<Integer, String> inventory = pets.containsKey("Inventory") ?
                (Map<Integer, String>) pets.get("Inventory") :
                new HashMap<>();
        BiMap<String, Integer> storage = pets.containsKey("Storage") ?
                (BiMap<String, Integer>) pets.get("Storage") :
                HashBiMap.create();
        return new Tuple2<>(inventory, storage);
    }

    @NotNull
    default BlobPet getBlobPet(@NotNull String key) {
        Objects.requireNonNull(key, "'key' cannot be null");
        return Objects.requireNonNull(BlobPet.by(key), "BlobPet with key '" + key + "' does not exist");
    }

    @Nullable
    default BlobPet findBlobPet(@NotNull String key) {
        Objects.requireNonNull(key, "'key' cannot be null");
        return BlobPet.by(key);
    }

    Map<Integer, String> getInventory();

    @Nullable
    default BlobPet isEquipped(int index) {
        String key = getInventory().get(index);
        if (key == null)
            return null;
        return getBlobPet(key);
    }

    /**
     * Equips a pet to the pet inventory
     *
     * @param key   The key of the pet
     * @param index The index to equip the pet to
     * @return Whether the pet was equipped
     */
    default boolean equip(@NotNull String key,
                          int index) {
        Objects.requireNonNull(key, "'key' cannot be null");
        if (getInventory().containsKey(index))
            return false;
        getInventory().put(index, key);
        return true;
    }

    /**
     * Equips a pet to the pet inventory
     *
     * @param pet The pet
     * @return Whether the pet was equipped
     */
    default boolean equip(@NotNull BlobPet pet,
                          int index) {
        Objects.requireNonNull(pet, "'pet' cannot be null");
        return equip(pet.getKey(), index);
    }

    /**
     * Unequips a pet from the pet inventory
     *
     * @param index The index to unequip the pet from
     * @return Whether the pet was unequipped
     */
    default boolean unequip(int index) {
        return getInventory().remove(index) != null;
    }

    /**
     * Serializes the pets of this pet owner
     *
     * @return The serialized pets
     */
    default Map<String, Object> serializePets() {
        Map<String, Object> pets = new HashMap<>();
        pets.put("Inventory", getInventory());
        pets.put("Storage", getStorage());
        return pets;
    }

    /**
     * Gets a pet from the pet inventory through its index
     *
     * @param index The index of the pet in the inventory
     * @return The pet
     */
    @Nullable
    default BlobPet getPet(int index) {
        String key = getInventory().get(index);
        if (key == null)
            return null;
        return getBlobPet(key);
    }
}
