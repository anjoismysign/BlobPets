package me.anjoismysign.blobpets.entity;

import org.jetbrains.annotations.NotNull;

public record IndexedPlayerPet(@NotNull PlayerPet getPlayerPet,
                               int getIndex) {
}
