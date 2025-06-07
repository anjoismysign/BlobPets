package me.anjoismysign.blobpets.entity;

import me.anjoismysign.blobpets.BlobPetsAPI;
import me.anjoismysign.blobpets.entity.floatingpet.BlobFloatingPet;
import me.anjoismysign.blobpets.entity.petexpansion.PetExpansion;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.exception.ConfigurationFieldException;

import java.io.File;
import java.util.*;

public record AttributePet(@NotNull Map<Attribute, List<AttributeModifier>> getAttributeModifiers,
                           @NotNull String getBlobPetKey,
                           @NotNull String getKey) implements PetExpansion {

    public static AttributePet fromFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String key = file.getName().replace(".yml", "");
        if (!config.isString("BlobPet"))
            throw new ConfigurationFieldException("'BlobPet' is not valid or set");
        String blobPetKey = config.getString("BlobPet");
        if (!config.isConfigurationSection("Attributes"))
            throw new ConfigurationFieldException("'Attributes' is not valid or set");
        Map<Attribute, List<AttributeModifier>> attributeModifiers = new HashMap<>();
        ConfigurationSection attributesSection = config.getConfigurationSection("Attributes");
        attributesSection.getKeys(false).forEach(attributeName -> {
            if (!attributesSection.isConfigurationSection(attributeName))
                throw new ConfigurationFieldException("Attribute '" + attributeName + "' is not valid");
            ConfigurationSection attributeSection = attributesSection.getConfigurationSection(attributeName);
            try {
                Attribute attribute = Attribute.valueOf(attributeName);
                if (!attributeSection.isDouble("Amount"))
                    throw new ConfigurationFieldException("Attribute '" + attributeName + "' has an invalid amount (DECIMAL NUMBER)");
                double amount = attributeSection.getDouble("Amount");
                if (!attributeSection.isString("Operation"))
                    throw new ConfigurationFieldException("Attribute '" + attributeName + "' is missing 'Operation' field");
                AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(attributeSection.getString("Operation"));
                attributeModifiers.computeIfAbsent(attribute, k -> new ArrayList<>())
                        .add(new AttributeModifier("", amount, operation));
            } catch (IllegalArgumentException e) {
                throw new ConfigurationFieldException("Attribute '" + attributeName + "' has an invalid Operation");
            }
        });
        return new AttributePet(attributeModifiers, blobPetKey, key);
    }

    /**
     * Will unapply the AttributePet from the owner
     *
     * @param floatingPet the pet
     */
    public static void unapply(@NotNull BlobFloatingPet floatingPet, int holdIndex) {
        Objects.requireNonNull(floatingPet, "'floatingPet' cannot be null");
        String key = floatingPet.getKey();
        AttributePet pet = BlobPetsAPI.getInstance().isLinkedToAttributePet(key);
        if (pet == null)
            return;
        Player owner = floatingPet.getPetOwner();
        pet.unapply(owner, holdIndex);
    }

    @Override
    public File saveToFile(File directory) {
        File file = instanceFile(directory);
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        yamlConfiguration.set("BlobPet", getBlobPetKey);
        try {
            yamlConfiguration.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public void apply(@NotNull Player player, int holdIndex) {
        Objects.requireNonNull(player, "'player' cannot be null");
        getAttributeModifiers.forEach((attribute, list) -> {
            list.forEach(attributeModifier -> {
                AttributeInstance attributeInstance = player.getAttribute(attribute);
                if (attributeInstance == null) {
                    Bukkit.getPluginManager().getPlugin("BlobPets")
                            .getLogger().severe("Attribute " + attribute.name() + " is not supported, inside " +
                                    "AttributePet " + getKey() + ". Skipping.");
                    return;
                }
                AttributeModifier old = attributeInstance.getModifiers()
                        .stream()
                        .filter(modifier -> modifier.getName().equals(attribute.name() + "." + holdIndex))
                        .findFirst().orElse(null);
                if (old != null)
                    attributeInstance.removeModifier(old);
                attributeModifier = new AttributeModifier(UUID.randomUUID(),
                        attribute.name() + "." + holdIndex,
                        attributeModifier.getAmount(),
                        attributeModifier.getOperation());
                attributeInstance.addModifier(attributeModifier);
            });
        });
    }

    public void unapply(@NotNull Player player, int holdIndex) {
        Objects.requireNonNull(player, "'player' cannot be null");
        getAttributeModifiers.forEach((attribute, list) -> {
            list.forEach(attributeModifier -> {
                AttributeInstance attributeInstance = player.getAttribute(attribute);
                if (attributeInstance == null) {
                    Bukkit.getPluginManager().getPlugin("BlobPets")
                            .getLogger().severe("Attribute " + attribute.name() + " is not supported, inside " +
                                    "AttributePet " + getKey() + ". Skipping.");
                    return;
                }
                attributeInstance.getModifiers()
                        .stream()
                        .filter(modifier -> modifier.getName().equals(attribute.name() + "." + holdIndex))
                        .toList().stream().findFirst().ifPresent(attributeInstance::removeModifier);
            });
        });
    }
}
