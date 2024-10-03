package pro.cyrent.anticheat.api.command.commands.sub;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.impl.combat.entity.EntityA;
import pro.cyrent.anticheat.api.user.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestKillAuraCommand {

    public void execute(String[] args, String s, CommandSender commandSender) {

        if (commandSender instanceof Player) {
            PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender));

            try {
                if (user != null) {

                    if (args.length >= 2) {
                        String targetName = args[1];

                        if (targetName.length() > 0) {
                            PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(Bukkit.getPlayer(args[1]));
                            if (target != null) {
                                EntityA entityBot = (EntityA) target
                                        .getCheckManager().forClass(EntityA.class);

                                if (entityBot != null && entityBot.isEnabled()) {

                                    if (!entityBot.created) {

                                        if (target.getPlayer().getLocation() != null) {
                                            double x = target.getPlayer().getLocation().getX(),
                                                    y = target.getPlayer().getLocation().getY() + 3,
                                                    z = target.getPlayer().getLocation().getZ();

                                            target.setDidBotCommand(true);

                                            boolean flicker = false;

                                            if (args.length == 3) {
                                                if (args[2] != null
                                                        && args[2].equalsIgnoreCase("flicker")) {
                                                    entityBot.createEntity(target, user, y, x, z,
                                                            0, 0, 0, true);
                                                    flicker = true;
                                                } else {
                                                    entityBot.createEntity(target, user, y, x, z,
                                                            0, 0, 0, false);
                                                }
                                            } else {
                                                entityBot.createEntity(target, user, y, x, z,
                                                        0, 0, 0, false);
                                            }

                                            commandSender.sendMessage(ChatColor.GREEN
                                                    + "Now spawning the KillAura Bot for the player: " + targetName
                                                    + " (using flicker: " + flicker + ")");

                                        } else {
                                            commandSender.sendMessage(ChatColor.GREEN
                                                    + "[!] There was an issue spawning the bot at the players location!");
                                        }
                                    } else {
                                        commandSender.sendMessage(ChatColor.RED
                                                + "[!] The bot has already spawned, please wait!");
                                    }
                                } else {
                                    commandSender.sendMessage(ChatColor.RED
                                            + "[!] Could not spawn bot, make sure the EntityA check is enabled!");
                                }
                            } else {
                                commandSender.sendMessage(ChatColor.RED
                                        + "[!] The player your trying to find is either offline or invalid, " +
                                        "please try another name.");
                            }
                        } else {
                            commandSender.sendMessage("Please enter a valid username.");
                        }
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Usage: /" +
                                Anticheat.INSTANCE.getConfigValues().getCommandName()
                                + " forcebot (player) (flicker)");
                    }
                } else {
                    commandSender
                            .sendMessage("If you see this contact moose1301");
                }
            } catch (NullPointerException ignored) {

            }
        } else {
            if (args.length >= 2) {
                String targetName = args[1];

                if (targetName.length() > 0) {
                    PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(Bukkit.getPlayer(args[1]));
                    if (target != null) {
                        EntityA entityBot = (EntityA) target
                                .getCheckManager().forClass(EntityA.class);

                        if (entityBot != null && entityBot.isEnabled()) {

                            if (!entityBot.created) {
                                commandSender.sendMessage(ChatColor.GREEN
                                        + "Now spawning the KillAura Bot for the player: " + targetName);

                                target.setDidBotCommand(true);
                            } else {
                                commandSender.sendMessage(ChatColor.RED
                                        + "[!] The bot has already spawned, please wait!");
                            }
                        } else {
                            commandSender.sendMessage(ChatColor.RED
                                    + "[!] Could not spawn bot, make sure the EntityA check is enabled!");
                        }
                    } else {
                        commandSender.sendMessage(ChatColor.RED
                                + "[!] The player your trying to find is either offline or invalid, " +
                                "please try another name.");
                    }
                } else {
                    commandSender.sendMessage("Please enter a valid username.");
                }
            } else {
                commandSender.sendMessage(ChatColor.RED + "Usage: /" +
                        Anticheat.INSTANCE.getConfigValues().getCommandName()
                        + " forcebot (player)");
            }
        }
    }
}
