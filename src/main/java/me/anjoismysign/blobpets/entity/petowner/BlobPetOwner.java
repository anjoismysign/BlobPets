package me.anjoismysign.blobpets.entity.petowner;

import me.anjoismysign.anjo.entities.Tuple2;
import me.anjoismysign.anjo.entities.Uber;
import me.anjoismysign.blobpets.BlobPetsAPI;
import me.anjoismysign.blobpets.entity.BlobPet;
import me.anjoismysign.blobpets.entity.IndexedPlayerPet;
import me.anjoismysign.blobpets.entity.PetOwnerPack;
import me.anjoismysign.blobpets.entity.PlayerPet;
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
import us.mytheria.bloblib.api.BlobLibTranslatableAPI;
import us.mytheria.bloblib.displayentity.PackMaster;
import us.mytheria.bloblib.entities.BlobCrudable;
import us.mytheria.bloblib.entities.BlobSerializable;
import us.mytheria.bloblib.itemstack.ItemStackModder;

import java.util.*;

public record BlobPetOwner(@NotNull BlobCrudable blobCrudable,
                           @NotNull List<PlayerPet> getPets,
                           @NotNull Uber<Integer> maxSizeUber,
                           @NotNull Uber<PetOwnerPack> petOwnerPackUber) implements BlobSerializable,
        PetOwner {

    public static BlobPetOwner GENERATE(@NotNull BlobCrudable crudable) {
        Objects.requireNonNull(crudable, "'crudable' cannot be null!");
        Document document = crudable.getDocument();
        Map<String, Object> serializedPets = document.containsKey("Pets") ?
                (Map<String, Object>) document.get("Pets") :
                new HashMap<>();

        List<PlayerPet> pets = PetInventoryHolder.deserializePets(serializedPets);
        BlobPetOwner owner = new BlobPetOwner(crudable,
                pets,
                Uber.drive(BlobPetsAPI.getInstance().getPetPacking().getDefaultSize()),
                Uber.fly());
        Map<String, Object> serializedPetOwnerPack = document.containsKey("PetOwnerPack") ?
                (Map<String, Object>) document.get("PetOwnerPack") :
                new HashMap<>();
        Tuple2<PetOwnerPack, Map<Integer, Integer>> tuple = PetOwnerPack.deserialize(serializedPetOwnerPack, owner);
        PetOwnerPack pack = tuple.first();
        owner.petOwnerPackUber.talk(pack);
        Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("BlobPets"),
                () -> {
                    Player player = owner.getPlayer();
                    if (player == null || !player.isValid() || !player.isOnline())
                        return;
                    Map<Integer, Integer> indexes = tuple.second();
                    List<Integer> holdIndexes = indexes.keySet().stream()
                            .sorted(Comparator.comparingInt(indexes::get))
                            .toList();
                    holdIndexes.forEach(index -> {
                        pack.add(indexes.get(index));
                    });
                    PetOwnerLoadEvent loadEvent = new PetOwnerLoadEvent(owner);
                    Bukkit.getPluginManager().callEvent(loadEvent);
                });
        return owner;
    }

    @Override
    public void openPetMenu() {
        Player player = getPlayer();
        BlobLibInventoryAPI.getInstance().customSelector(
                "View-Pets",
                player,
                "Pets",
                "Pets",
                //The pets supplier
                () -> {
                    List<IndexedPlayerPet> pets = new ArrayList<>();
                    for (int i = 0; i < getPets().size(); i++) {
                        PlayerPet pet = getPet(i);
                        if (pet == null) continue;
                        IndexedPlayerPet indexedPet = new IndexedPlayerPet(pet, i);
                        pets.add(indexedPet);
                    }
                    return pets;
                },
                //Once clicked an expansion, will proceed to load
                indexedPlayerPet -> {
                    if (!player.isOnline() || !player.isValid())
                        return;
                    int storageIndex = indexedPlayerPet.getIndex();
                    if (BlobPetOwner.this.isHoldingPet(storageIndex)) {
                        if (!returnHeldPet(storageIndex))
                            return;
                        BlobLibMessageAPI.getInstance()
                                .getMessage("BlobPets.Pet-Despawned",
                                        player)
                                .handle(player);
                        player.closeInventory();
                        return;
                    }
                    if (!holdPet(storageIndex)) {
                        BlobLibMessageAPI.getInstance()
                                .getMessage("BlobPets.Pack-Size-Exceeded", player)
                                .handle(player);
                        player.closeInventory();
                        return;
                    }
                    BlobLibMessageAPI.getInstance()
                            .getMessage("BlobPets.Pet-Spawned",
                                    player)
                            .handle(player);
                    player.closeInventory();

                },
                //displays the indexedPlayerPet
                indexedPlayerPet -> {
                    int storageIndex = indexedPlayerPet.getIndex();
                    PlayerPet playerPet = indexedPlayerPet.getPlayerPet();
                    BlobPet pet = playerPet.getBlobPet();
                    ItemStack display = pet.display(player);
                    ItemMeta itemMeta = display.getItemMeta();
                    if (itemMeta != null) {
                        boolean isHeld = isHoldingPet(storageIndex);
                        String translatableBlockKey = isHeld ? "BlobPets.Despawn-Pet" : "BlobPets.Spawn-Pet";
                        List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
                        lore.addAll(BlobLibTranslatableAPI.getInstance()
                                .getTranslatableBlock(translatableBlockKey, player)
                                .get());
                        itemMeta.setLore(lore);
                        display.setItemMeta(itemMeta);
                    }
                    if (BlobPetsAPI.getInstance().displayLevels()) {
                        ItemStackModder modder = ItemStackModder.mod(display);
                        modder.replace("%level%", playerPet.level() + "");
                    }
                    BlobPetDisplayEvent event = new BlobPetDisplayEvent(pet, display, indexedPlayerPet.getIndex());
                    Bukkit.getPluginManager().callEvent(event);
                    return event.getDisplay();
                });
    }

    public @Nullable Player getPlayer() {
        return BlobSerializable.super.getPlayer();
    }

    public @NotNull Map<Integer, BlobFloatingPet> getHeldPets() {
        return petOwnerPackUber.thanks().packMap();
    }

    public boolean returnHeldPet(int storageIndex) {
        PetOwnerPack pack = petOwnerPackUber.thanks();
        PackMaster<BlobFloatingPet> packMaster = pack.packMaster();
        int holdIndex = packMaster.getIndex(storageIndex);
        Tuple2<Integer, BlobFloatingPet> tuple = packMaster.getComponent(holdIndex);
        if (tuple == null)
            return false;
        BlobFloatingPet pet = tuple.second();
        if (pet == null)
            return false;
        pet.remove();
        pack.packMaster().removeComponent(holdIndex);
        return true;
    }

    public boolean holdPet(int storageIndex) {
        return petOwnerPackUber.thanks().add(storageIndex);
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
