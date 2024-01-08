package me.anjoismysign.blobpets.entity.floatingpet;

import me.anjoismysign.blobpets.BlobPetsAPI;
import me.anjoismysign.blobpets.entity.BlobPet;
import me.anjoismysign.blobpets.entity.PlayerPet;
import me.anjoismysign.blobpets.entity.petowner.PetOwner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface BlobFloatingPet {

    /**
     * Gets the key of the pet
     *
     * @return the key
     */
    String getKey();

    /**
     * Gets the PlayerPet associated with this pet.
     * It's expected to work as long as the BlobFloatingPet is the held pet.
     *
     * @return the PlayerPet
     */
    @NotNull
    default PlayerPet getPlayerPet() {
        PetOwner petOwner = BlobPetsAPI.getInstance()
                .getPetOwner(getPetOwner());
        Objects.requireNonNull(petOwner, "'petOwner' is null");
        return Objects.requireNonNull(petOwner.getPet(petOwner.getHeldPetIndex()));
    }

    /**
     * Gets the owner of the pet
     *
     * @return the owner
     */
    @NotNull
    Player getPetOwner();

    /**
     * Gets the EntityType of the pet
     *
     * @return the EntityType
     */
    EntityType getEntityType();

    /**
     * Gets the BlobPet instance of this pet
     *
     * @return the BlobPet instance
     */
    @Nullable
    default BlobPet getBlobPet() {
        return BlobPetsAPI.getInstance().getBlobPet(getKey());
    }

    /**
     * Will remove the pet from the world
     */
    void remove();
}
