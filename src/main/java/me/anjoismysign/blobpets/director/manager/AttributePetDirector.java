package me.anjoismysign.blobpets.director.manager;

import me.anjoismysign.blobpets.director.PetsManagerDirector;
import me.anjoismysign.blobpets.entity.AttributePet;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.ObjectDirector;
import us.mytheria.bloblib.entities.ObjectDirectorData;
import us.mytheria.bloblib.exception.KeySharingException;

import java.util.HashMap;
import java.util.Map;

public class AttributePetDirector extends ObjectDirector<AttributePet> {
    private static final Map<String, AttributePet> linked = new HashMap<>();

    public AttributePetDirector(PetsManagerDirector managerDirector) {
        super(managerDirector, ObjectDirectorData.simple(managerDirector
                .getRealFileManager(), "AttributePet"), file -> {
            AttributePet attributePet = AttributePet.fromFile(file);
            String key = attributePet.getBlobPetKey();
            if (linked.containsKey(key))
                throw KeySharingException.DEFAULT(key);
            linked.put(key, attributePet);
            return attributePet;
        }, false);
    }

    @Override
    public void reload() {
        linked.clear();
        super.reload();
    }

    /**
     * Checks if a BlobPet is linked to an AttributePet
     *
     * @param key the BlobPet key
     * @return the AttributePet if it is linked, null otherwise
     */
    @Nullable
    public AttributePet isLinked(String key) {
        return linked.get(key);
    }
}
