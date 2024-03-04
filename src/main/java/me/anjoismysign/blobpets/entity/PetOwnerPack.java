package me.anjoismysign.blobpets.entity;

import me.anjoismysign.anjo.entities.Tuple2;
import me.anjoismysign.blobpets.BlobPetsAPI;
import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import me.anjoismysign.blobpets.entity.petowner.BlobPetOwner;
import me.anjoismysign.blobpets.settings.PetPacking;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.displayentity.PackMaster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record PetOwnerPack(
        @NotNull BlobPetOwner getOwner,
        @NotNull Map<Integer, BlobFloatingPet> packMap,
        @NotNull int getMaxSize,
        @NotNull PackMaster<BlobFloatingPet> packMaster) {

    public static PetOwnerPack of(@NotNull BlobPetOwner owner,
                                  @NotNull Integer maxSize) {
        Map<Integer, BlobFloatingPet> map = new HashMap<>();
        BlobPetsAPI api = BlobPetsAPI.getInstance();
        PetPacking petPacking = api.getPetPacking();
        PackMaster<BlobFloatingPet> packMaster = PackMaster.of(petPacking.getRowSize(),
                petPacking.getDistance(),
                map,
                petPacking.getPivot());
        return new PetOwnerPack(owner, map, maxSize, packMaster);
    }

    public static Tuple2<PetOwnerPack, Map<Integer, Integer>> deserialize(@NotNull Map<String, Object> map,
                                                                          @NotNull BlobPetOwner owner) {
        Map<Integer, BlobFloatingPet> packMap = new HashMap<>();
        Map<Integer, Integer> indexes = map.containsKey("Indexes") ? (Map<Integer, Integer>) map.get("Indexes") : new HashMap<>();
        int maxSize = map.containsKey("MaxSize") ? (int) map.get("MaxSize") : BlobPetsAPI.getInstance()
                .getPetPacking().getDefaultSize();
        BlobPetsAPI api = BlobPetsAPI.getInstance();
        PetPacking petPacking = api.getPetPacking();
        PackMaster<BlobFloatingPet> packMaster = PackMaster.of(petPacking.getRowSize(),
                petPacking.getDistance(),
                packMap,
                petPacking.getPivot());
        return new Tuple2<>(new PetOwnerPack(owner,
                packMap, maxSize, packMaster), indexes);
    }

    public void apply(@NotNull PetPacking petPacking) {
        Objects.requireNonNull(petPacking, "'petPacking' cannot be null");
        packMaster.setPivot(petPacking.getPivot());
        packMaster.setMaxPerRow(petPacking.getRowSize());
        packMaster.setComponentLength(petPacking.getDistance());
    }

    public void removeHeldPets() {
        List<BlobFloatingPet> held = packMap.values().stream().toList();
        held.forEach(BlobFloatingPet::remove);
    }

    public void returnHeldPets() {
        removeHeldPets();
        packMap.clear();
    }

    public void reload() {
        if (packMap.isEmpty())
            return;
        List<BlobFloatingPet> held = packMap.values().stream().toList();
        held.forEach(BlobFloatingPet::remove);
        Player player = getOwner.getPlayer();
        if (player == null)
            return;
        packMap.keySet().forEach(storageIndex -> {
            PlayerPet pet = getPet(storageIndex);
            if (pet == null)
                return;
            BlobFloatingPet heldPet;
            BlobPet blobPet = pet.getBlobPet();
            if (blobPet.isBlobBlockPet())
                heldPet = blobPet.asBlockDisplay(player, packMaster, storageIndex);
            else
                heldPet = blobPet.asItemDisplay(player, packMaster, storageIndex);
            packMap.put(storageIndex, heldPet);
        });
    }

    public boolean isOwnerOnline() {
        return getOwner.getPlayer() != null && getOwner.getPlayer().isOnline();
    }

    private boolean canAdd() {
        return packMap.size() < getMaxSize();
    }

    @Nullable
    private PlayerPet getPet(int storageIndex) {
        return getOwner.getPets().get(storageIndex);
    }

    public boolean add(int storageIndex) {
        Player player = getOwner.getPlayer();
        if (player == null)
            return false;
        if (!canAdd())
            return false;
        PlayerPet pet = getPet(storageIndex);
        if (pet == null)
            return false;
        BlobPet blobPet = pet.getBlobPet();
        if (blobPet.isBlobBlockPet())
            blobPet.asBlockDisplay(player, packMaster, storageIndex);
        else
            blobPet.asItemDisplay(player, packMaster, storageIndex);
        return true;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("Indexes", packMaster.getIndexes());
        map.put("MaxSize", getMaxSize);
        return map;
    }
}
