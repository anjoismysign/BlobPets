package me.anjoismysign.blobpets.director;

import me.anjoismysign.blobpets.BlobPets;
import us.mytheria.bloblib.entities.GenericManager;

public class PetsManager extends GenericManager<BlobPets, PetsManagerDirector> {

    public PetsManager(PetsManagerDirector managerDirector) {
        super(managerDirector);
    }
}