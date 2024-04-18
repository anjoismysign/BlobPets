package me.anjoismysign.blobpets.entity;

public enum AssetType {
    PET_MEASUREMENTS("petMeasurements"),
    PET_ANIMATIONS("petAnimations"),
    PET_DATA("petData"),
    BLOB_PET("blobPet"),
    ATTRIBUTE_PET("attributePet");

    private final String directoryName;

    AssetType(String directoryName) {
        this.directoryName = directoryName;
    }

    public String getDirectoryName() {
        return directoryName;
    }
}
