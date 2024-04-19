package me.anjoismysign.blobpets.entity;

import me.anjoismysign.blobpets.BlobPetsAPI;
import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import me.anjoismysign.blobpets.entity.petowner.BlobPetOwner;
import me.anjoismysign.blobpets.settings.PetPacking;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.displayentity.PackMaster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a PetOwnerPack.
 *
 * @param getOwner      - the owner
 * @param getHeldPets   - the held pets. They are added when a pet is added to the pack,
 *                      for example when instantiating a DisplayPackFloatingPet
 * @param getMaxSize    - the max size of the pack
 * @param getPackMaster - the pack master
 */
public record PetOwnerPack(
        @NotNull BlobPetOwner getOwner,
        @NotNull Map<Integer, BlobFloatingPet> getHeldPets,
        int getMaxSize,
        @NotNull PackMaster<BlobFloatingPet> getPackMaster) {

    public static PetOwnerPack deserialize(@NotNull Map<String, Object> map,
                                           @NotNull BlobPetOwner owner) {
        Map<Integer, BlobFloatingPet> packMap = new HashMap<>();
        int maxSize = map.containsKey("MaxSize") ? (int) map.get("MaxSize") : BlobPetsAPI.getInstance()
                .getPetPacking().getDefaultSize();
        BlobPetsAPI api = BlobPetsAPI.getInstance();
        PetPacking petPacking = api.getPetPacking();
        PackMaster<BlobFloatingPet> packMaster = PackMaster
                .of(petPacking.getRowSize(),
                        petPacking.getDistance(),
                        packMap,
                        petPacking.getPivot());
        return new PetOwnerPack(owner, packMap, maxSize, packMaster);
    }

    public void apply(@NotNull PetPacking petPacking) {
        Objects.requireNonNull(petPacking, "'petPacking' cannot be null");
        getPackMaster.setPivot(petPacking.getPivot());
        getPackMaster.setMaxPerRow(petPacking.getRowSize());
        getPackMaster.setComponentLength(petPacking.getDistance());
    }

    public void removeHeldPets() {
        List<BlobFloatingPet> held = getHeldPets.values().stream().toList();
        held.forEach(BlobFloatingPet::remove);
    }

    public boolean isOwnerOnline() {
        return getOwner.getPlayer() != null && getOwner.getPlayer().isOnline();
    }

    private boolean canAdd() {
        return getOwner.getInventory().size() < getMaxSize();
    }

    @Nullable
    private BlobPet getPet(int storageIndex) {
        return getOwner.getPet(storageIndex);
    }

    public boolean add(@NotNull String key,
                       boolean equipInInventory) {
        Objects.requireNonNull(key, "'key' cannot be null");
        Player player = getOwner.getPlayer();
        if (player == null)
            return false;
        if (!canAdd() && equipInInventory) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobPets.Pack-Size-Exceeded", player)
                    .handle(player);
            player.closeInventory();
            return false;
        }
        BlobPet blobPet = getOwner.getBlobPet(key);
        Map<Integer, String> inventory = getOwner.getInventory();
        int index = inventory.keySet().stream()
                .filter(i -> inventory.get(i) == null)
                .findFirst().orElse(inventory.size());
        if (blobPet.isBlobBlockPet())
            blobPet.asBlockDisplay(player, getPackMaster, index);
        else
            blobPet.asItemDisplay(player, getPackMaster, index);
        if (equipInInventory)
            getOwner.equip(blobPet, index);
        return true;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("MaxSize", getMaxSize);
        return map;
    }
}
