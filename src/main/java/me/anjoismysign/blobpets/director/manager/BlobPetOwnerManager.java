package me.anjoismysign.blobpets.director.manager;

import me.anjoismysign.blobpets.director.PetsManagerDirector;
import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import me.anjoismysign.blobpets.entity.petowner.BlobPetOwner;
import us.mytheria.bloblib.entities.BlobSerializableManager;

import java.util.List;

public class BlobPetOwnerManager extends BlobSerializableManager<BlobPetOwner> {
    public BlobPetOwnerManager(PetsManagerDirector director,
                               boolean logActivity) {
        super(director,
                x -> x,
                BlobPetOwner::GENERATE,
                "BlobPetOwner",
                logActivity,
                null,
                null);
    }

    @Override
    public void unload() {
        List<BlobFloatingPet> petsToRemove = getAll().stream()
                .flatMap(blobPetOwner -> blobPetOwner.getHeldPets().values().stream())
                .toList();
        petsToRemove.forEach(BlobFloatingPet::remove);
        super.unload();
    }
}
