package me.anjoismysign.blobpets.entity.petowner;

import com.google.common.collect.BiMap;
import me.anjoismysign.anjo.entities.Tuple2;
import me.anjoismysign.anjo.entities.Uber;
import me.anjoismysign.blobpets.BlobPetsAPI;
import me.anjoismysign.blobpets.entity.BlobPet;
import me.anjoismysign.blobpets.entity.PetOwnerPack;
import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import me.anjoismysign.blobpets.event.BlobPetDisplayEvent;
import me.anjoismysign.blobpets.event.PetOwnerLoadEvent;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.api.BlobLibInventoryAPI;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.displayentity.PackMaster;
import us.mytheria.bloblib.entities.BlobCrudable;
import us.mytheria.bloblib.entities.BlobSerializable;
import us.mytheria.bloblib.itemstack.ItemStackModder;
import us.mytheria.bloblib.utilities.TextColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a BlobPetOwner.
 *
 * @param blobCrudable     - the BlobCrudable
 * @param getStorage       - the storage. Key: BlobPet key, Value: the amount of pets
 * @param getInventory     - the inventory. Key: the index, Value: BlobPet key
 * @param maxSizeUber      - the max size
 * @param petOwnerPackUber - the PetOwnerPack
 */
public record BlobPetOwner(@NotNull BlobCrudable blobCrudable,
                           @NotNull BiMap<String, Integer> getStorage,
                           @NotNull Map<Integer, String> getInventory,
                           @NotNull Uber<Integer> maxSizeUber,
                           @NotNull Uber<PetOwnerPack> petOwnerPackUber) implements BlobSerializable,
        PetOwner {

    /**
     * Gets the BlobPetOwner by the player.
     * If player is not inside cache, will send a message to the player.
     *
     * @param player - the player
     * @return The BlobPetOwner
     */
    @Nullable
    public static BlobPetOwner by(@NotNull Player player) {
        PetOwner petOwner = BlobPetsAPI.getInstance()
                .getPetOwner(player);
        if (petOwner == null) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("Player.Not-Found", player)
                    .handle(player);
            return null;
        }
        return (BlobPetOwner) petOwner;
    }

    @SuppressWarnings("unchecked")
    public static BlobPetOwner GENERATE(@NotNull BlobCrudable crudable) {
        Objects.requireNonNull(crudable, "'crudable' cannot be null!");
        Document document = crudable.getDocument();
        Map<String, Object> serializedPets = document.containsKey("Pets") ?
                (Map<String, Object>) document.get("Pets") :
                new HashMap<>();
        Tuple2<Map<Integer, String>, BiMap<String, Integer>> petInventoryTuple = PetInventoryHolder.deserializePets(serializedPets);
        BlobPetOwner owner = new BlobPetOwner(crudable,
                petInventoryTuple.second(),
                petInventoryTuple.first(),
                Uber.drive(BlobPetsAPI.getInstance().getPetPacking().getDefaultSize()),
                Uber.fly());
        Map<String, Object> serializedPetOwnerPack = document.containsKey("PetOwnerPack") ?
                (Map<String, Object>) document.get("PetOwnerPack") :
                new HashMap<>();
        PetOwnerPack pack = PetOwnerPack.deserialize(serializedPetOwnerPack, owner);
        owner.petOwnerPackUber.talk(pack);
        Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("BlobPets"),
                () -> {
                    Player player = owner.getPlayer();
                    if (player == null || !player.isValid() || !player.isOnline())
                        return;
                    owner.getInventory.forEach((index, key) -> {
                        BlobPet pet = owner.findBlobPet(key);
                        if (pet == null)
                            throw new RuntimeException("BlobPet with key '" + key + "' no longer exists!");
                        owner.holdPet(pet.getKey(), false);
                    });
                    PetOwnerLoadEvent loadEvent = new PetOwnerLoadEvent(owner);
                    Bukkit.getPluginManager().callEvent(loadEvent);
                });
        return owner;
    }

    @Override
    public void managePets() {
        Player player = getPlayer();
        BlobLibInventoryAPI.getInstance()
                .trackInventory(player, "Manage-Pets")
                .getInventory().open(player);
    }

    @Override
    public void openPetInventory() {
        Player player = getPlayer();
        BlobLibInventoryAPI.getInstance().customSelector(
                "View-Pet-Inventory",
                player,
                "Pets",
                "Pets",
                //The pets supplier
                () -> getInventory.entrySet().stream()
                        .toList(),
                //Once clicked a pet, will do logic
                entry -> {
                    if (!player.isOnline() || !player.isValid())
                        return;
                    returnHeldPet(entry.getKey());
                    addPet(entry.getValue(), 1);
                    BlobLibMessageAPI.getInstance()
                            .getMessage("BlobPets.Pet-Despawned",
                                    player)
                            .handle(player);
                    openPetInventory();
                },
                //displays the pet
                entry -> {
                    BlobPet blobPet = getBlobPet(entry.getValue());
                    ItemStack display = blobPet.display(player);
                    BlobPetDisplayEvent event = new BlobPetDisplayEvent(blobPet,
                            display);
                    Bukkit.getPluginManager().callEvent(event);
                    return event.getDisplay();
                },
                owner -> managePets());
    }

    @Override
    public void openPetStorage() {
        Player player = getPlayer();
        BlobLibInventoryAPI.getInstance().customSelector(
                "View-Pet-Storage",
                player,
                "Pets",
                "Pets",
                //The pets supplier
                () -> getStorage.entrySet().stream()
                        .toList(),
                //Once clicked a pet, will do logic
                entry -> {
                    if (!player.isOnline() || !player.isValid())
                        return;
                    String key = entry.getKey();
                    if (!holdPet(key))
                        return;
                    subtractPet(key, 1);
                    BlobLibMessageAPI.getInstance()
                            .getMessage("BlobPets.Pet-Spawned",
                                    player)
                            .handle(player);
                    openPetStorage();
                },
                //displays the pet
                entry -> {
                    BlobPet blobPet = getBlobPet(entry.getKey());
                    ItemStack display = blobPet.display(player);
                    ItemMeta meta = display.getItemMeta();
                    String displayName = meta.hasDisplayName() ?
                            meta.getDisplayName() :
                            blobPet.getPetData().getCustomName();
                    ItemStackModder modder = ItemStackModder.mod(display);
                    modder.displayName(displayName + " " + TextColor.PARSE("&8(x" + entry.getValue() + ")"));
                    BlobPetDisplayEvent event = new BlobPetDisplayEvent(blobPet,
                            display);
                    Bukkit.getPluginManager().callEvent(event);
                    return event.getDisplay();
                },
                owner -> managePets());
    }

    public @Nullable Player getPlayer() {
        return BlobSerializable.super.getPlayer();
    }

    public @NotNull Map<Integer, BlobFloatingPet> getHeldPets() {
        return petOwnerPackUber.thanks().getHeldPets();
    }

    public boolean returnHeldPet(int index) {
        PetOwnerPack pack = petOwnerPackUber.thanks();
        PackMaster<BlobFloatingPet> packMaster = pack.getPackMaster();
        Tuple2<Integer, BlobFloatingPet> tuple = packMaster.getComponent(index);
        if (tuple == null)
            return false;
        BlobFloatingPet pet = tuple.second();
        if (pet == null)
            return false;
        getInventory.remove(index);
        pet.remove();
        pack.getPackMaster().removeComponent(index);
        return true;
    }

    public boolean returnHeldPetByStorage(int storageIndex) {
        PetOwnerPack pack = petOwnerPackUber.thanks();
        PackMaster<BlobFloatingPet> packMaster = pack.getPackMaster();
        int holdIndex = packMaster.getIndex(storageIndex);
        return returnHeldPet(holdIndex);
    }

    public boolean holdPet(@NotNull String key,
                           boolean equipInInventory) {
        return petOwnerPackUber.thanks().add(key, equipInInventory);
    }

    public boolean holdPet(@NotNull String key) {
        return holdPet(key, true);
    }

    public boolean holdPet(int index) {
        String key = getInventory.get(index);
        if (key == null)
            return false;
        return holdPet(key);
    }

    public void reloadHeldPets() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getPluginManager().getPlugin("BlobPets")
                    .getLogger().severe("Attempted to reload pet on non-primary thread!");
            return;
        }
        petOwnerPackUber.thanks().reload();
    }

    public void removeHeldPets() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getPluginManager().getPlugin("BlobPets")
                    .getLogger().severe("Attempted to remove pet on non-primary thread!");
            return;
        }
        petOwnerPackUber.thanks().removeHeldPets();
    }

    public BlobCrudable serializeAllAttributes() {
        BlobCrudable crudable = blobCrudable();
        Document document = crudable.getDocument();
        document.put("Pets", serializePets());
        document.put("PetOwnerPack", petOwnerPackUber.thanks().serialize());
        return crudable;
    }
}
