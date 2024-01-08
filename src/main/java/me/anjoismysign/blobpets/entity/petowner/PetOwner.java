package me.anjoismysign.blobpets.entity.petowner;

import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface PetOwner extends PetInventoryHolder, PetHolder {

    /**
     * Will open the pet inventory
     */
    void openPetSelector();

    /**
     * Gets the player associated with this pet owner
     *
     * @return The player
     */
    Player getPlayer();

    /**
     * Gets the pet being held by the pet owner
     *
     * @return The held pet
     */
    @Nullable
    BlobFloatingPet getHeldPet();

    /**
     * Will reload the held pet
     */
    void reloadHeldPet();
}
