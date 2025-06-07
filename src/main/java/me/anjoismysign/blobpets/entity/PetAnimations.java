package me.anjoismysign.blobpets.entity;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.displayentity.EntityAnimationsCarrier;
import us.mytheria.bloblib.entities.BlobObject;

import java.io.File;
import java.util.Objects;

public record PetAnimations(double getFollowSpeed,
                            double getWalkAwaySpeed,
                            double getHoverSpeed,
                            double getHoverHeightCeiling,
                            double getHoverHeightFloor,
                            double getYOffset,
                            double getParticlesOffset,
                            double getTeleportDistanceThreshold,
                            double getApproachDistanceThreshold,
                            double getMinimumDistance,
                            @NotNull String getKey) implements BlobObject {

    /**
     * Creates a new {@link PetAnimations} instance.
     *
     * @param entityAnimationsCarrier The {@link EntityAnimationsCarrier} to use.
     * @param key                     The key of the pet.
     * @return The new {@link PetAnimations} instance.
     */
    @NotNull
    public static PetAnimations of(@NotNull EntityAnimationsCarrier entityAnimationsCarrier,
                                   @NotNull String key) {
        Objects.requireNonNull(entityAnimationsCarrier, "'entityAnimationsCarrier' cannot be null.");
        Objects.requireNonNull(key, "'key' cannot be null.");
        return new PetAnimations(entityAnimationsCarrier.followSpeed(),
                entityAnimationsCarrier.walkAwaySpeed(), entityAnimationsCarrier.hoverSpeed(),
                entityAnimationsCarrier.hoverHeightCeiling(), entityAnimationsCarrier.hoverHeightFloor(),
                entityAnimationsCarrier.yOffset(), entityAnimationsCarrier.particlesOffset(),
                entityAnimationsCarrier.teleportDistanceThreshold(),
                entityAnimationsCarrier.approachDistanceThreshold(),
                entityAnimationsCarrier.minimumDistance(), key);
    }

    public static PetAnimations fromFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String key = file.getName().replace(".yml", "");
        EntityAnimationsCarrier animationsCarrier = EntityAnimationsCarrier.READ_OR_FAIL_FAST(config);
        return of(animationsCarrier, key);
    }

    @NotNull
    public EntityAnimationsCarrier toEntityAnimationsCarrier() {
        return new EntityAnimationsCarrier(getFollowSpeed, getWalkAwaySpeed, getHoverSpeed, getHoverHeightCeiling,
                getHoverHeightFloor, getYOffset, getParticlesOffset, getTeleportDistanceThreshold,
                getApproachDistanceThreshold, getMinimumDistance);
    }

    @Override
    public File saveToFile(File directory) {
        File file = instanceFile(directory);
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        toEntityAnimationsCarrier().serialize(yamlConfiguration);
        try {
            yamlConfiguration.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
