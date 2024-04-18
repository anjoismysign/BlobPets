package me.anjoismysign.blobpets;

import me.anjoismysign.blobpets.director.PetsManagerDirector;
import me.anjoismysign.blobpets.entity.*;
import me.anjoismysign.blobpets.entity.petowner.PetOwner;
import me.anjoismysign.blobpets.settings.PetPacking;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class BlobPetsAPI {
    private static BlobPetsAPI instance;
    private final PetsManagerDirector director;

    private BlobPetsAPI(PetsManagerDirector director) {
        this.director = director;
    }

    protected static BlobPetsAPI getInstance(PetsManagerDirector director) {
        if (instance == null)
            instance = new BlobPetsAPI(director);
        return instance;
    }

    public static BlobPetsAPI getInstance() {
        return instance;
    }

    @Nullable
    public PetOwner getPetOwner(Player player) {
        Objects.requireNonNull(player, "'player' cannot be null.");
        return director.getBlobPetOwnerManager().isBlobSerializable(player)
                .orElse(null);
    }

    @Nullable
    public PetMeasurements getPetMeasurements(@NotNull String key) {
        Objects.requireNonNull(key, "'key' cannot be null.");
        return director.getPetMeasurementsDirector().getObjectManager().getObject(key);
    }

    @Nullable
    public PetAnimations getPetAnimation(@NotNull String key) {
        Objects.requireNonNull(key, "'key' cannot be null.");
        return director.getPetAnimationsDirector().getObjectManager().getObject(key);
    }

    @Nullable
    public PetData getPetData(@NotNull String key) {
        Objects.requireNonNull(key, "'key' cannot be null.");
        return director.getPetDataDirector().getObjectManager().getObject(key);
    }

    @Nullable
    public BlobPet getBlobPet(@NotNull String key) {
        Objects.requireNonNull(key, "'key' cannot be null.");
        return director.getBlobPetDirector().getObjectManager().getObject(key);
    }

    /**
     * Gets an AttributePet by its key
     *
     * @param key the AttributePet key
     * @return the AttributePet, or null if not found
     */
    @Nullable
    public AttributePet getAttributePet(@NotNull String key) {
        Objects.requireNonNull(key, "'key' cannot be null.");
        return director.getAttributePetDirector().getObjectManager().getObject(key);
    }

    /**
     * Checks if a BlobPet key is linked to an AttributePet
     *
     * @param key the BlobPet key
     * @return the AttributePet, or null if not linked
     */
    @Nullable
    public AttributePet isLinkedToAttributePet(@NotNull String key) {
        Objects.requireNonNull(key, "'key' cannot be null.");
        return director.getAttributePetDirector().isLinked(key);
    }

    @NotNull
    public PetPacking getPetPacking() {
        return director.getConfigManager().getPetPacking();
    }

    public boolean useSimpleStorage() {
        return director.getConfigManager().useSimpleStorage();
    }

    /**
     * Whether to display levels
     *
     * @return Whether to display levels
     */
    public boolean displayLevels() {
        return director.getConfigManager().getDisplayLevel().register();
    }

    /**
     * Gets the apply delay
     *
     * @return the apply delay
     */
    public int getApplyDelay() {
        return director.getConfigManager().getApplyDelay();
    }
}
