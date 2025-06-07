package me.anjoismysign.blobpets.entity.petowner;

import me.anjoismysign.anjo.entities.Tuple2;
import me.anjoismysign.anjo.entities.Uber;
import me.anjoismysign.blobpets.BlobPetsAPI;
import me.anjoismysign.blobpets.entity.BlobPet;
import me.anjoismysign.blobpets.entity.IndexedKey;
import me.anjoismysign.blobpets.entity.PetOwnerPack;
import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import me.anjoismysign.blobpets.event.BlobPetDisplayEvent;
import me.anjoismysign.blobpets.event.PetOwnerLoadEvent;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import us.mytheria.bloblib.itemstack.ItemStackBuilder;
import us.mytheria.bloblib.itemstack.ItemStackModder;
import us.mytheria.bloblib.utilities.TextColor;

import java.util.*;

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
                           @NotNull Map<String, Integer> getStorage,
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
        Tuple2<Map<Integer, String>, Map<String, Integer>> petInventoryTuple = PetInventoryHolder.deserializePets(serializedPets);
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
                    owner.reloadHeldPets();
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
                        .map((entry) -> IndexedKey.of(entry.getKey(), entry.getValue()))
                        .toList(),
                //Once clicked a pet, will do logic
                indexedKey -> {
                    if (!player.isOnline() || !player.isValid())
                        return;
                    if (!returnPet(indexedKey.getNumber())) {
                        player.sendMessage("Failed to return pet by index " + indexedKey.getNumber());
                        return;
                    }
                    addPet(indexedKey.getKey(), 1);
                    BlobLibMessageAPI.getInstance()
                            .getMessage("BlobPets.Pet-Despawned",
                                    player)
                            .handle(player);
                    openPetInventory();
                },
                //displays the pet
                indexedKey -> {
                    BlobPet blobPet = findBlobPet(indexedKey.getKey());
                    if (blobPet == null) {
                        Bukkit.getPluginManager().getPlugin("BlobPets")
                                .getLogger().severe("Data was removed but is in circulation: " + indexedKey.getKey());
                        return ItemStackBuilder.build(Material.BARRIER)
                                .displayName("&cNot found: " + indexedKey.getKey())
                                .build();
                    }
                    ItemStack display = blobPet.display(player);
                    BlobPetDisplayEvent event = new BlobPetDisplayEvent(blobPet,
                            display);
                    Bukkit.getPluginManager().callEvent(event);
                    return event.getDisplay();
                },
                owner -> managePets(),
                null,
                null);
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
                    if (!holdPet(key)) {
                        openPetStorage();
                        return;
                    }
                    subtractPet(key, 1);
                    BlobLibMessageAPI.getInstance()
                            .getMessage("BlobPets.Pet-Spawned",
                                    player)
                            .handle(player);
                    openPetStorage();
                },
                //displays the pet
                entry -> {
                    BlobPet blobPet = findBlobPet(entry.getKey());
                    if (blobPet == null) {
                        Bukkit.getPluginManager().getPlugin("BlobPets")
                                .getLogger().severe("Data was removed but is in circulation: " + entry.getKey());
                        return ItemStackBuilder.build(Material.BARRIER)
                                .displayName("&cNot found: " + entry.getKey())
                                .build();
                    }
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
                owner -> managePets(),
                null,
                null);
    }

    public @Nullable Player getPlayer() {
        return BlobSerializable.super.getPlayer();
    }

    public @NotNull Map<Integer, BlobFloatingPet> getHeldPets() {
        return petOwnerPackUber.thanks().getHeldPets();
    }

    @Override
    public boolean returnPet(int inventoryIndex) {
        BlobPet blobPet = getPet(inventoryIndex);
        if (blobPet == null)
            return false;
        String key = blobPet.getKey();
        //remove the first entry in which the key is the same
        List<String> pets = getInventory.values().stream()
                .filter(Objects::nonNull)
                .toList();
        int index = pets.indexOf(key);
        if (index == -1)
            throw new RuntimeException("Pet with key '" + key + "' not found in inventory!");
        pets = new ArrayList<>(pets);
        pets.remove(index);
        returnAllHeldPets();
        pets.forEach(this::holdPet);
        return true;
    }

    private boolean returnHeldPet(int index) {
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

    public void returnAllHeldPets() {
        var dupe = new HashMap<>(getHeldPets());
        dupe.forEach((index, pet) -> returnHeldPet(index));
    }

    public boolean holdPet(@NotNull String key,
                           boolean equipInInventory) {
        return petOwnerPackUber.thanks().add(key, equipInInventory);
    }

    public boolean holdPet(@NotNull String key) {
        return holdPet(key, true);
    }

    public void reloadHeldPets() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getPluginManager().getPlugin("BlobPets")
                    .getLogger().severe("Attempted to reload pet on non-primary thread!");
            return;
        }
        if (getInventory.isEmpty())
            return;
        var inventoryDupe = new HashMap<>(getInventory);
        if (!getHeldPets().isEmpty()) {
            var heldPetsDupe = new HashMap<>(getHeldPets());
            heldPetsDupe.forEach((index, floatingPet) -> {
                petOwnerPackUber.thanks()
                        .getPackMaster()
                        .removeComponent(index);
                floatingPet.remove();
            });
        }
        getInventory.clear();
        inventoryDupe.forEach((index, key) -> {
            if (key == null)
                return;
            BlobPet pet = findBlobPet(key);
            if (pet == null) {
                addPet(key, 1);
                Bukkit.getPluginManager().getPlugin("BlobPets").getLogger()
                        .severe("BlobPet with key '" + key + "' no longer exists!");
                return;
            }
            holdPet(pet.getKey(), true);
        });
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
