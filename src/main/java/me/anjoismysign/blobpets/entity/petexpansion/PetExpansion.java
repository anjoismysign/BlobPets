package me.anjoismysign.blobpets.entity.petexpansion;

import me.anjoismysign.blobpets.BlobPetsAPI;
import me.anjoismysign.blobpets.entity.BlobPet;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.entities.BlobObject;

import java.util.Objects;

public interface PetExpansion extends BlobObject {

    /**
     * Gets the key of the BlobPet linked to this PetExpansion.
     *
     * @return The key of the BlobPet linked to this PetExpansion.
     */
    @NotNull
    String getBlobPetKey();

    /**
     * Gets the BlobPet linked to this PetExpansion.
     *
     * @return The BlobPet linked to this PetExpansion.
     */
    @NotNull
    default BlobPet getBlobPet() {
        return Objects.requireNonNull(BlobPetsAPI.getInstance()
                .getBlobPet(getBlobPetKey()), "No BlobPet is linked. " +
                "Maybe using an old reference?");
    }

    /**
     * Applies the pet expansion to the player.
     *
     * @param player    The player to apply the pet expansion to.
     * @param holdIndex The index of the pet expansion in the pack.
     */
    void apply(@NotNull Player player, int holdIndex);

    /**
     * Unapplies the pet expansion from the player.
     *
     * @param player The player to unapply the pet expansion from.
     */
    void unapply(@NotNull Player player, int holdIndex);
}
