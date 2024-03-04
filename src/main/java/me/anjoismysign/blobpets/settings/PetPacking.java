package me.anjoismysign.blobpets.settings;

import org.bukkit.util.Vector;

public record PetPacking(double getDistance,
                         int getRowSize,
                         int getDefaultSize,
                         Vector getPivot) {

    public static PetPacking of(double distance,
                                int rowSize,
                                int defaultSize,
                                Vector pivot) {
        return new PetPacking(distance,
                rowSize,
                defaultSize,
                pivot);
    }
}
