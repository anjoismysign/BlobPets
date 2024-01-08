package me.anjoismysign.blobpets.entity;

import org.apache.commons.io.FilenameUtils;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.displayentity.DisplayPetData;
import us.mytheria.bloblib.displayentity.DisplayPetRecord;
import us.mytheria.bloblib.entities.BlobObject;

import java.io.File;
import java.util.Objects;

public record PetData(@Nullable ItemStack getItemStack,
                      @Nullable BlockData getBlockData,
                      @Nullable Particle getParticle,
                      @Nullable String getCustomName,
                      @NotNull String getKey) implements BlobObject {

    /**
     * Creates a new {@link PetData} instance.
     *
     * @param petRecord The {@link DisplayPetRecord} to use.
     * @param key       The key of the pet.
     * @return The new {@link PetData} instance.
     */
    @NotNull
    public static PetData of(@NotNull DisplayPetRecord petRecord,
                             @NotNull String key) {
        Objects.requireNonNull(petRecord, "'petRecord' cannot be null.");
        Objects.requireNonNull(key, "'key' cannot be null.");
        return new PetData(petRecord.itemStack(),
                petRecord.blockData(),
                petRecord.particle(),
                petRecord.customName(),
                key);
    }

    public static PetData fromFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String key = FilenameUtils.removeExtension(file.getName());
        DisplayPetRecord petRecord = DisplayPetRecord.read(config);
        if (petRecord.itemStack() == null && petRecord.blockData() == null)
            throw new NullPointerException("PetData must have either an ItemStack or a BlockData");
        return of(petRecord, key);
    }

    @NotNull
    public DisplayPetRecord toDisplayPetRecord() {
        return new DisplayPetRecord(getItemStack, getBlockData, getParticle, getCustomName);
    }

    @NotNull
    public DisplayPetData toDisplayPetData() {
        return new DisplayPetData(toDisplayPetRecord());
    }

    @Override
    public File saveToFile(File directory) {
        File file = instanceFile(directory);
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        toDisplayPetRecord().serialize(yamlConfiguration);
        try {
            yamlConfiguration.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
