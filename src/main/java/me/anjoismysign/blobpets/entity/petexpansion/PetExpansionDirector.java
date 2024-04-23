package me.anjoismysign.blobpets.entity.petexpansion;

import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import me.anjoismysign.blobpets.event.AsyncBlobPetsExpansionLoadEvent;
import me.anjoismysign.blobpets.event.BlobFloatingPetDestroyEvent;
import me.anjoismysign.blobpets.event.BlobFloatingPetSpawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.ObjectDirector;
import us.mytheria.bloblib.entities.ObjectDirectorData;
import us.mytheria.bloblib.exception.KeySharingException;
import us.mytheria.bloblib.managers.ManagerDirector;
import us.mytheria.bloblib.utilities.HandyDirectory;

import java.io.File;
import java.util.*;
import java.util.function.Function;

public class PetExpansionDirector<T extends PetExpansion> extends ObjectDirector<T> {
    private final Map<String, T> linked;
    private final Set<File> expansions;
    private File expansionDirectory;

    private PetExpansionDirector(@NotNull ManagerDirector managerDirector,
                                 @NotNull String objectName,
                                 @NotNull Function<File, T> readFunction,
                                 @NotNull Map<String, T> linked) {
        super(managerDirector, ObjectDirectorData.simple(managerDirector
                .getRealFileManager(), objectName), readFunction, false);
        this.linked = linked;
        this.expansions = new HashSet<>();
    }

    public static <T extends PetExpansion> PetExpansionDirector<T> of(@NotNull ManagerDirector managerDirector,
                                                                      @NotNull String objectName,
                                                                      @NotNull Function<File, T> readFunction) {
        Objects.requireNonNull(managerDirector, "'managerDirector' cannot be null");
        Objects.requireNonNull(objectName, "'objectName' cannot be null");
        Objects.requireNonNull(readFunction, "'readFunction' cannot be null");
        Map<String, T> linked = new HashMap<>();
        Function<File, T> function = file -> {
            T attributePet = readFunction.apply(file);
            String key = attributePet.getBlobPetKey();
            if (linked.containsKey(key))
                throw KeySharingException.DEFAULT(key);
            linked.put(key, attributePet);
            return attributePet;
        };
        return new PetExpansionDirector<>(managerDirector, objectName, function, linked);
    }

    @EventHandler
    public void onReload(AsyncBlobPetsExpansionLoadEvent event) {
        expansions.forEach(file -> {
            getObjectManager().loadFile(file, throwable -> {
            });
        });
        expansions.clear();
        if (expansionDirectory != null)
            HandyDirectory.of(expansionDirectory).deleteRecursively();
    }

    @Override
    public void reload() {
        linked.clear();
        expansions.clear();
        super.reload();
    }

    @EventHandler
    public void onSpawn(BlobFloatingPetSpawnEvent event) {
        BlobFloatingPet floatingPet = event.getFloatingPet();
        int holdIndex = event.getIndex();
        String key = floatingPet.getKey();
        T pet = isLinked(key);
        if (pet == null)
            return;
        Player owner = floatingPet.getPetOwner();
        pet.apply(owner, holdIndex);
    }

    @EventHandler
    public void onDestroy(BlobFloatingPetDestroyEvent event) {
        BlobFloatingPet floatingPet = event.getFloatingPet();
        int holdIndex = event.getIndex();
        String key = floatingPet.getKey();
        T pet = isLinked(key);
        if (pet == null)
            return;
        Player owner = floatingPet.getPetOwner();
        pet.unapply(owner, holdIndex);
    }

    /**
     * Checks if a BlobPet is linked to an AttributePet
     *
     * @param key the BlobPet key
     * @return the AttributePet if it is linked, null otherwise
     */
    @Nullable
    public T isLinked(String key) {
        return linked.get(key);
    }

    /**
     * Adds an expansion file to be loaded
     *
     * @param file the file to be loaded
     */
    public void addExpansion(@NotNull File file) {
        Objects.requireNonNull(file, "'file' cannot be null");
        expansions.add(file);
    }

    public void setExpansionDirectory(@NotNull File expansionDirectory) {
        Objects.requireNonNull(expansionDirectory, "'expansionDirectory' cannot be null");
        this.expansionDirectory = expansionDirectory;
    }
}
