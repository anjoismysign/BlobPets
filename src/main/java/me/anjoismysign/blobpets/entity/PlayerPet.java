package me.anjoismysign.blobpets.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public record PlayerPet(@NotNull String key,
                        int level,
                        @Nullable String displayName) {

    public PlayerPet level(int level) {
        return new PlayerPet(key, level, displayName);
    }

    public PlayerPet displayName(@Nullable String displayName) {
        Objects.requireNonNull(displayName, "'displayName' cannot be null");
        return new PlayerPet(key, level, displayName);
    }

    public PlayerPet key(@NotNull String key) {
        Objects.requireNonNull(key, "'key' cannot be null");
        return new PlayerPet(key, level, displayName);
    }

    @NotNull
    public BlobPet getBlobPet() {
        return Objects.requireNonNull(BlobPet.by(key), "BlobPet with key '" + key + "' does not exist");
    }

    public Map<String, Object> serialize() {
        return Map.of(
                "Key", key,
                "Level", level,
                "DisplayName", displayName
        );
    }

    @NotNull
    public static PlayerPet deserialize(Map<String, Object> map) {
        return new PlayerPet(
                (String) map.get("Key"),
                (int) map.get("Level"),
                (String) map.get("DisplayName")
        );
    }
}
