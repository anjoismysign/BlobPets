package me.anjoismysign.blobpets.entity.petowner;

import me.anjoismysign.blobpets.entity.BlobPet;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public interface PetStorage {
    Map<String, Integer> getStorage();

    /**
     * Gets the amount of a pet in the storage
     *
     * @param key The key of the pet
     * @return The amount of the pet
     */
    default int getAmount(@NotNull String key) {
        Objects.requireNonNull(key, "'key' cannot be null");
        return getStorage().getOrDefault(key, 0);
    }

    /**
     * Gets the amount of a pet in the storage
     *
     * @param pet The pet
     * @return The amount of the pet
     */
    default int getAmount(@NotNull BlobPet pet) {
        Objects.requireNonNull(pet, "'pet' cannot be null");
        return getAmount(pet.getKey());
    }

    /**
     * Adds a pet to the storage
     *
     * @param key    The key of the pet
     * @param amount The amount to add
     */
    default void addPet(@NotNull String key, int amount) {
        Objects.requireNonNull(key, "'key' cannot be null");
        getStorage().put(key, getAmount(key) + amount);
    }

    /**
     * Subtracts a pet to the storage
     *
     * @param key    The key of the pet
     * @param amount The amount to add
     * @return The amount left. If greater than 0, no pet was removed
     */
    default int subtractPet(@NotNull String key, int amount) {
        Objects.requireNonNull(key, "'key' cannot be null");
        int left = getAmount(key) - amount;
        if (left < 0)
            return Math.abs(left);
        if (left == 0) {
            getStorage().remove(key);
            return 0;
        }
        getStorage().put(key, getAmount(key) - amount);
        return 0;
    }

    /**
     * Adds a pet to the storage
     *
     * @param pet    The pet
     * @param amount The amount to add
     */
    default void addPet(@NotNull BlobPet pet,
                        int amount) {
        Objects.requireNonNull(pet, "'pet' cannot be null");
        addPet(pet.getKey(), amount);
    }

    /**
     * Subtracts a pet to the storage
     *
     * @param pet    The pet
     * @param amount The amount to add
     */
    default int subtractPet(@NotNull BlobPet pet,
                            int amount) {
        Objects.requireNonNull(pet, "'pet' cannot be null");
        return subtractPet(pet.getKey(), amount);
    }
}
