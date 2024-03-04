package me.anjoismysign.blobpets.entity.floatingpet;

import me.anjoismysign.blobpets.BlobPetsAPI;
import me.anjoismysign.blobpets.entity.BlobPet;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlobFloatingPet {

    /**
     * Gets the key of the pet
     *
     * @return the key
     */
    String getKey();

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
