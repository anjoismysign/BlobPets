package me.anjoismysign.blobpets.command;

import me.anjoismysign.blobpets.BlobPetsAPI;
import me.anjoismysign.blobpets.director.PetsManagerDirector;
import me.anjoismysign.blobpets.entity.BlobPet;
import me.anjoismysign.blobpets.entity.petowner.BlobPetOwner;
import me.anjoismysign.blobpets.entity.petowner.PetOwner;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.CommandDirector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlobPetsCmd {
    private BlobPetsCmd(PetsManagerDirector managerDirector) {
        CommandDirector director = new CommandDirector(managerDirector
                .getPlugin(), "blobpets");
        /*
         * /bp give <blobpet> <player>
         * /bp managepets <player>
         * /bp managepets
         */
        director.addNonAdminChildTabCompleter(data -> {
            String[] args = data.args();
            if (args.length != 1)
                return null;
            return List.of("managepets");
        });
        director.addNonAdminChildCommand(data -> {
            String[] args = data.args();
            CommandSender sender = data.sender();
            if (args.length < 1)
                return false;
            String subCommand = args[0];
            if (!subCommand.equalsIgnoreCase("managepets"))
                return false;
            if (!(sender instanceof Player player)) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("System.Console-Not-Allowed-Command", sender)
                        .toCommandSender(sender);
                return true;
            }
            PetOwner petOwner = BlobPetsAPI.getInstance()
                    .getPetOwner(player);
            if (petOwner == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .toCommandSender(player);
                return true;
            }
            petOwner.managePets();
            return true;
        });
        director.addAdminChildTabCompleter(data -> {
            String[] args = data.args();
            switch (args.length) {
                case 1 -> {
                    List<String> completions = new ArrayList<>();
                    completions.add("managepets");
                    return completions;
                }
                case 2 -> {
                    String subCommand = args[0];
                    if (!subCommand.equalsIgnoreCase("managepets"))
                        return null;
                    List<String> completions = new ArrayList<>();
                    completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .toList());
                    return completions;
                }
                default -> {
                    return null;
                }
            }
        });
        director.addAdminChildCommand(data -> {
            String[] args = data.args();
            CommandSender sender = data.sender();
            if (args.length < 2)
                return false;
            String subCommand = args[0];
            if (!subCommand.equalsIgnoreCase("managepets"))
                return false;
            // subCommand = managepets
            String inputPlayer = args[2];
            Player target = Bukkit.getPlayer(inputPlayer);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return true;
            }
            PetOwner petOwner = BlobPetsAPI.getInstance()
                    .getPetOwner(target);
            if (petOwner == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", sender)
                        .toCommandSender(sender);
                return true;
            }
            petOwner.managePets();
            return true;
        });
        director.addAdminChildTabCompleter(data -> {
            String[] args = data.args();
            switch (args.length) {
                case 1 -> {
                    List<String> completions = new ArrayList<>();
                    completions.add("give");
                    return completions;
                }
                case 2 -> {
                    String subCommand = args[0];
                    if (!subCommand.equalsIgnoreCase("give"))
                        return null;
                    return managerDirector.getBlobPetDirector()
                            .getObjectManager()
                            .keys()
                            .stream()
                            .toList();
                }
                case 3 -> {
                    String subCommand = args[0];
                    if (!subCommand.equalsIgnoreCase("give"))
                        return null;
                    List<String> completions = new ArrayList<>();
                    completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .toList());
                    return completions;
                }
                default -> {
                    return null;
                }
            }
        });
        director.addAdminChildCommand(data -> {
            String[] args = data.args();
            CommandSender sender = data.sender();
            if (args.length < 3)
                return false;
            String subCommand = args[0];
            if (!subCommand.equalsIgnoreCase("give"))
                return false;
            // subCommand = give
            String key = args[1];
            BlobPet pet = managerDirector.getBlobPetDirector()
                    .getObjectManager()
                    .getObject(key);
            if (pet == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Object.Not-Found", sender)
                        .toCommandSender(sender);
                return true;
            }
            String inputPlayer = args[2];
            Player target = Bukkit.getPlayer(inputPlayer);
            if (target == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return true;
            }
            BlobPetOwner petOwner = (BlobPetOwner) BlobPetsAPI.getInstance()
                    .getPetOwner(target);
            if (petOwner == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return true;
            }
            int amount = 1;
            if (args.length > 3) {
                try {
                    amount = Integer.parseInt(args[3]);
                    if (amount < 1)
                        amount = 1;
                } catch (NumberFormatException e) {
                    BlobLibMessageAPI.getInstance()
                            .getMessage("Builder.Number-Exception", sender)
                            .toCommandSender(sender);
                    return true;
                }
            }
            petOwner.addPet(pet, amount);
            return true;
        });
    }

    public static BlobPetsCmd of(@NotNull PetsManagerDirector director) {
        Objects.requireNonNull(director);
        return new BlobPetsCmd(director);
    }
}
