package me.anjoismysign.blobpets.entity;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record IndexedKey(int getNumber,
                         @NotNull String getKey) {

    public static IndexedKey of(int index,
                                @NotNull String key) {
        Objects.requireNonNull(key, "'key' cannot be null");
        return new IndexedKey(index, key);
    }
}
