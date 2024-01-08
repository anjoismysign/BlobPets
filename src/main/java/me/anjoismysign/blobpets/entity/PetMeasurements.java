package me.anjoismysign.blobpets.entity;

import org.apache.commons.io.FilenameUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.displayentity.DisplayMeasurements;
import us.mytheria.bloblib.entities.BlobObject;

import java.io.File;
import java.util.Objects;

public record PetMeasurements(float getScaleX,
                              float getScaleY,
                              float getScaleZ,
                              float getYOffset,
                              @NotNull String getKey) implements BlobObject {

    /**
     * Creates a new {@link PetMeasurements} instance.
     *
     * @param displayMeasurements The {@link DisplayMeasurements} to use.
     * @param key                 The key of the pet.
     * @return The new {@link PetMeasurements} instance.
     */
    @NotNull
    public static PetMeasurements of(@NotNull DisplayMeasurements displayMeasurements,
                                     @NotNull String key) {
        Objects.requireNonNull(displayMeasurements, "'displayMeasurements' cannot be null.");
        Objects.requireNonNull(key, "'key' cannot be null.");
        return new PetMeasurements(displayMeasurements.scaleX(), displayMeasurements.scaleY(),
                displayMeasurements.scaleZ(), displayMeasurements.yOffset(), key);
    }

    public static PetMeasurements fromFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String key = FilenameUtils.removeExtension(file.getName());
        DisplayMeasurements displayMeasurements = DisplayMeasurements.READ_OR_FAIL_FAST(config);
        return of(displayMeasurements, key);
    }

    @NotNull
    public DisplayMeasurements toDisplayMeasurements() {
        return new DisplayMeasurements(getScaleX, getScaleY, getScaleZ, getYOffset);
    }

    @Override
    public File saveToFile(File directory) {
        File file = instanceFile(directory);
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        toDisplayMeasurements().serialize(yamlConfiguration);
        try {
            yamlConfiguration.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
