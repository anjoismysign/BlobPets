package me.anjoismysign.blobpets.entity.petowner;

import me.anjoismysign.anjo.entities.Uber;
import me.anjoismysign.blobpets.BlobPetsAPI;
import me.anjoismysign.blobpets.entity.BlobPet;
import me.anjoismysign.blobpets.entity.IndexedPlayerPet;
import me.anjoismysign.blobpets.entity.PlayerPet;
import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import me.anjoismysign.blobpets.event.BlobPetDisplayEvent;
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
import us.mytheria.bloblib.entities.BlobCrudable;
import us.mytheria.bloblib.entities.BlobSerializable;
import us.mytheria.bloblib.itemstack.ItemStackModder;

import java.util.*;

public record BlobPetOwner(@NotNull BlobCrudable blobCrudable,
                           @NotNull List<PlayerPet> getPets,
                           @NotNull Uber<Integer> heldPetIndexUber,
                           @NotNull Uber<Boolean> holdingPetUber,
                           @NotNull Uber<BlobFloatingPet> heldPetUber) implements BlobSerializable,
        PetOwner {

    public static BlobPetOwner GENERATE(@NotNull BlobCrudable crudable) {
        Objects.requireNonNull(crudable, "'crudable' cannot be null!");
        Document document = crudable.getDocument();
        Map<String, Object> serializedPets = document.containsKey("Pets") ?
                (Map<String, Object>) document.get("Pets") :
                new HashMap<>();
        List<PlayerPet> pets = PetInventoryHolder.deserializePets(serializedPets);
        int heldPetIndex = crudable.hasInteger("HeldPetIndex").orElse(0);
        boolean holdingPet = crudable.hasBoolean("IsHoldingPet").orElse(false);
        BlobPetOwner owner = new BlobPetOwner(crudable,
                pets,
                new Uber<>(heldPetIndex),
                new Uber<>(holdingPet),
                Uber.fly());
        Player player = owner.getPlayer();
        Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("BlobPets"),
                () -> {
                    if (!player.isValid() || !player.isOnline())
                        return;
                    BlobFloatingPet heldPet;
                    if (holdingPet) {
                        PlayerPet pet = pets.get(heldPetIndex);
                        BlobPet blobPet = pet.getBlobPet();
                        if (blobPet.isBlobBlockPet())
                            heldPet = blobPet.asBlockDisplay(player);
                        else
                            heldPet = blobPet.asItemDisplay(player);
                        owner.heldPetUber.talk(heldPet);
                    } else {
                        heldPet = null;
                    }
                });
        return owner;
    }

    @Override
    public void openPetSelector() {
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
                    int index = indexedPlayerPet.getIndex();
                    if (BlobPetOwner.this.getHeldPetIndex() == index) {
                        if (isHoldingPet()) {
                            getHeldPet().remove();
                            heldPetUber.talk(null);
                            holdingPetUber.talk(false);
                            BlobLibMessageAPI.getInstance()
                                    .getMessage("BlobPets.Pet-Despawned",
                                            player)
                                    .handle(player);
                            player.closeInventory();
                            return;
                        }
                    }
                    if (isHoldingPet()) {
                        getHeldPet().remove();
                    }
                    PlayerPet playerPet = indexedPlayerPet.getPlayerPet();
                    BlobPet blobPet = playerPet.getBlobPet();
                    BlobFloatingPet pet;
                    if (blobPet.isBlobBlockPet())
                        pet = blobPet.asBlockDisplay(player);
                    else
                        pet = blobPet.asItemDisplay(player);
                    heldPetUber.talk(pet);
                    heldPetIndexUber.talk(index);
                    holdingPetUber.talk(true);
                    BlobLibMessageAPI.getInstance()
                            .getMessage("BlobPets.Pet-Spawned",
                                    player)
                            .handle(player);
                    player.closeInventory();

                },
                //displays the indexedPlayerPet
                indexedPlayerPet -> {
                    int index = indexedPlayerPet.getIndex();
                    PlayerPet playerPet = indexedPlayerPet.getPlayerPet();
                    BlobPet pet = playerPet.getBlobPet();
                    ItemStack display = pet.display(player);
                    ItemMeta itemMeta = display.getItemMeta();
                    if (itemMeta != null) {
                        boolean isHeld = BlobPetOwner.this.getHeldPetIndex() == index
                                && isHoldingPet();
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

    @Override
    public @Nullable Player getPlayer() {
        return BlobSerializable.super.getPlayer();
    }

    @Override
    public BlobFloatingPet getHeldPet() {
        return heldPetUber.thanks();
    }

    @Override
    public void reloadHeldPet() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getPluginManager().getPlugin("BlobPets")
                    .getLogger().severe("Attempted to reload pet on non-primary thread!");
            return;
        }
        if (!isHoldingPet())
            return;
        BlobFloatingPet pet = getHeldPet();
        pet.remove();
        BlobPet blobPet = pet.getBlobPet();
        if (blobPet.isBlobBlockPet())
            pet = blobPet.asBlockDisplay(getPlayer());
        else
            pet = blobPet.asItemDisplay(getPlayer());
        heldPetUber.talk(pet);
    }

    @Override
    public int getHeldPetIndex() {
        return heldPetIndexUber.thanks();
    }

    @Override
    public boolean isHoldingPet() {
        return holdingPetUber.thanks() && getHeldPet() != null;
    }

    @Override
    public void storeHeldPet() {
        if (!isHoldingPet())
            return;
        PlayerPet held = getPet(getHeldPetIndex());
        if (held == null) return;

    }

    @Override
    public BlobCrudable serializeAllAttributes() {
        BlobCrudable crudable = blobCrudable();
        Document document = crudable.getDocument();
        document.put("Pets", serializePets());
        document.put("HeldPetIndex", getHeldPetIndex());
        document.put("IsHoldingPet", isHoldingPet());
        return crudable;
    }
}
