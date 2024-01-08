package me.anjoismysign.blobpets.entity.petowner;

public interface PetHolder {


    /**
     * Gets the index of the pet being held by the pet owner
     *
     * @return The held pet
     */
    int getHeldPetIndex();

    /**
     * Checks if the pet owner is holding a pet
     *
     * @return If the pet owner is holding a pet
     */
    boolean isHoldingPet();

    /**
     * Will store the held pet, as of despawning it, so it's no longer being held.
     */
    void storeHeldPet();
}
