package me.anjoismysign.blobpets.entity;

import me.anjoismysign.blobpets.BlobPetsAPI;
import me.anjoismysign.blobpets.director.PetsManagerDirector;
import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import me.anjoismysign.blobpets.entity.floatingpet.PackBlockPet;
import me.anjoismysign.blobpets.entity.floatingpet.PackItemPet;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.displayentity.DisplayFloatingPetSettings;
import us.mytheria.bloblib.displayentity.PackMaster;
import us.mytheria.bloblib.entities.BlobObject;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.bloblib.exception.ConfigurationFieldException;
import us.mytheria.bloblib.utilities.TextColor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record BlobPet(@NotNull PetData getPetData,
                      @NotNull PetAnimations getPetAnimations,
                      @NotNull PetMeasurements getPetMeasurements,
                      @NotNull TranslatableItem getDisplay,
                      @NotNull String getKey) implements BlobObject {

    @Nullable
    public static BlobPet by(@NotNull String key) {
        return BlobPetsAPI.getInstance().getBlobPet(key);
    }

    public static BlobPet fromFile(File file, PetsManagerDirector director) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String key = file.getName().replace(".yml", "");
        if (!config.isString("PetData"))
            throw new ConfigurationFieldException("'PetData' is not valid or set");
        if (!config.isString("PetAnimations"))
            throw new ConfigurationFieldException("'PetAnimations' is not valid or set");
        if (!config.isString("PetMeasurements"))
            throw new ConfigurationFieldException("'PetMeasurements' is not valid or set");
        if (!config.isString("TranslatableItem"))
            throw new ConfigurationFieldException("'TranslatableItem' is not valid or set");
        String petDataKey = config.getString("PetData");
        PetData petData = director.getPetDataDirector().getObjectManager().getObject(petDataKey);
        if (petData == null)
            throw new NullPointerException("'PetData' doesn't point to a valid PetData");
        String petAnimationsKey = config.getString("PetAnimations");
        PetAnimations petAnimations = director.getPetAnimationsDirector().getObjectManager().getObject(petAnimationsKey);
        if (petAnimations == null)
            throw new NullPointerException("'PetAnimations' doesn't point to a valid PetAnimation");
        String petMeasurementsKey = config.getString("PetMeasurements");
        PetMeasurements petMeasurements = director.getPetMeasurementsDirector().getObjectManager().getObject(petMeasurementsKey);
        if (petMeasurements == null)
            throw new NullPointerException("'PetMeasurements' doesn't point to a valid PetMeasurements");
        String translatableItemKey = config.getString("TranslatableItem");
        TranslatableItem translatableItem = TranslatableItem.by(translatableItemKey);
        if (translatableItem == null)
            throw new NullPointerException("'TranslatableItem' doesn't point to a valid TranslatableItem");
        return new BlobPet(petData, petAnimations, petMeasurements, translatableItem, key);
    }

    @Override
    public File saveToFile(File directory) {
        File file = instanceFile(directory);
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        yamlConfiguration.set("PetData", getPetData);
        yamlConfiguration.set("PetAnimation", getPetAnimations);
        yamlConfiguration.set("PetMeasurements", getPetMeasurements);
        yamlConfiguration.set("TranslatableItem", getDisplay.identifier());
        try {
            yamlConfiguration.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    @NotNull
    public DisplayFloatingPetSettings toDisplayFloatingPetSettings() {
        return new DisplayFloatingPetSettings(getPetAnimations
                .toEntityAnimationsCarrier(), getPetMeasurements.toDisplayMeasurements());
    }

    /**
     * Will create a new instance of ItemDisplayFloatingPet
     *
     * @param owner      the owner of the pet
     * @param packMaster the pack master related to the owner
     * @param index      the inventory index
     * @return a new instance of ItemDisplayFloatingPet
     */
    public BlobFloatingPet asItemDisplay(@NotNull Player owner,
                                         @NotNull PackMaster<BlobFloatingPet> packMaster,
                                         int index) {
        Objects.requireNonNull(owner, "'owner' cannot be null");
        ItemStack itemStack = getPetData().getItemStack();
        Particle particle = getPetData().getParticle();
        String customName = getPetData().getCustomName();
        DisplayFloatingPetSettings settings = toDisplayFloatingPetSettings();
        if (itemStack == null || itemStack.getType().isAir())
            throw new IllegalStateException("ItemStack cannot be null nor be air");
        return new PackItemPet(owner, itemStack, particle, customName,
                settings, getKey, packMaster, index);
    }

    /**
     * Will create a new instance of BlockDisplayFloatingPet
     *
     * @param owner      the owner of the pet
     * @param packMaster the pack master related to the owner
     * @param index      the inventory index
     * @return a new instance of BlockDisplayFloatingPet
     */
    public BlobFloatingPet asBlockDisplay(@NotNull Player owner,
                                          @NotNull PackMaster<BlobFloatingPet> packMaster,
                                          int index) {
        Objects.requireNonNull(owner, "'owner' cannot be null");
        BlockData blockData = getPetData().getBlockData();
        Particle particle = getPetData().getParticle();
        String customName = getPetData().getCustomName();
        DisplayFloatingPetSettings settings = toDisplayFloatingPetSettings();
        if (blockData == null || blockData.getMaterial().isAir())
            throw new IllegalStateException("ItemStack cannot be null nor be air");
        return new PackBlockPet(owner, blockData, particle, customName,
                settings, getKey, packMaster, index);
    }

    /**
     * Will display the pet as an item
     *
     * @param player the player to display the pet to
     * @return the ItemStack displaying the pet
     */
    @NotNull
    public ItemStack display(@NotNull Player player) {
        Objects.requireNonNull(player, "'player' cannot be null");
        ItemStack itemStack = getPetData().getItemStack();
        if (itemStack == null)
            itemStack = new ItemStack(getPetData().getBlockData().getPlacementMaterial());
        itemStack = new ItemStack(itemStack);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null)
            return itemStack;
        ItemStack localized = getDisplay.localize(player.getLocale()).getClone();
        ItemMeta localizedMeta = localized.getItemMeta();
        if (localizedMeta == null)
            return itemStack;
        String displayName = localizedMeta.hasDisplayName() ?
                localizedMeta.getDisplayName() :
                TextColor.PARSE("&7[Lvl %level%] &fPet");
        meta.setDisplayName(displayName);
        List<String> lore = localizedMeta.hasLore() ?
                localizedMeta.getLore() :
                new ArrayList<>();
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Will return true if the pet is an item pet
     *
     * @return true if the pet is an item pet
     */
    public boolean isBlobItemPet() {
        return getPetData().getItemStack() != null;
    }

    /**
     * Will return true if the pet is a block pet
     *
     * @return true if the pet is a block pet
     */
    public boolean isBlobBlockPet() {
        return getPetData().getBlockData() != null;
    }
}
