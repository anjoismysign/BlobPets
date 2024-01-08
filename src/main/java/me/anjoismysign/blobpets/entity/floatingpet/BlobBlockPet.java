package me.anjoismysign.blobpets.entity.floatingpet;

import me.anjoismysign.blobpets.BlobPetsAPI;
import me.anjoismysign.blobpets.entity.AttributePet;
import me.anjoismysign.blobpets.event.BlobFloatingPetDestroyEvent;
import me.anjoismysign.blobpets.event.BlobFloatingPetSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.api.BlobLibPetAPI;
import us.mytheria.bloblib.displayentity.BlockDisplayFloatingPet;
import us.mytheria.bloblib.displayentity.DisplayFloatingPetSettings;

public class BlobBlockPet extends BlockDisplayFloatingPet implements BlobFloatingPet {
    private final String key;

    public BlobBlockPet(@NotNull Player owner,
                        @NotNull BlockData display,
                        @Nullable Particle particle,
                        @Nullable String customName,
                        @NotNull DisplayFloatingPetSettings settings,
                        @NotNull String key) {
        super(owner, display, particle, customName, settings);
        this.key = key;
        spawn();
    }

    @Override
    public void spawn() {
        super.spawn();
        BlobLibPetAPI.getInstance()
                .setPetType(entity, key);
        Player player = findOwnerOrFail();
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("BlobPets"),
                () -> {
                    if (!player.isValid() || !player.isOnline())
                        return;
                    BlobFloatingPetSpawnEvent event = new BlobFloatingPetSpawnEvent(this);
                    Bukkit.getPluginManager().callEvent(event);
                }, BlobPetsAPI.getInstance().getApplyDelay());
    }

    @Override
    public void destroy() {
        if (!Bukkit.getPluginManager().getPlugin("BlobPets")
                .isEnabled()) {
            AttributePet.unapply(this);
        } else {
            BlobFloatingPetDestroyEvent event = new BlobFloatingPetDestroyEvent(this);
            Bukkit.getPluginManager().callEvent(event);
        }
        super.destroy();
    }

    public String getKey() {
        return key;
    }

    public EntityType getEntityType() {
        return EntityType.BLOCK_DISPLAY;
    }

    public void remove() {
        destroy();
    }

    @NotNull
    public Player getPetOwner() {
        return findOwnerOrFail();
    }
}
