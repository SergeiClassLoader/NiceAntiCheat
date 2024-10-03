package pro.cyrent.anticheat.api.command.commands.sub;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.Check;
import pro.cyrent.anticheat.api.user.PlayerData;

public class ForceBanCommand {

    public void execute(String[] args, String s, CommandSender commandSender) {

        if (commandSender instanceof Player) {
            PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender));


            try {
                if (user == null) {
                    commandSender.sendMessage("If you see this message contact moose1301");
                    return;
                }

                if (args.length < 2) {
                    commandSender.sendMessage(ChatColor.RED
                            + "Usage: /" + Anticheat.INSTANCE.getConfigValues().getCommandName() + " forceban (player)");
                    return;
                }

                String targetName = args[1];

                if (targetName.isEmpty()) {
                    commandSender.sendMessage("Please enter a valid username.");
                    return;
                }
                PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(Bukkit.getPlayer(args[1]));
                if (target == null) {

                    commandSender.sendMessage(ChatColor.RED + "[Error!] The player your trying to ban is offline, " +
                            "please try another name.");
                    return;
                }
                commandSender.sendMessage(ChatColor.GREEN
                        + "Commencing a forceful anticheat ban for the player: " + targetName);
                Bukkit.getScheduler().runTaskAsynchronously(Anticheat.INSTANCE.getPlugin(), () -> Check.punishPlayer(target, false, 40,
                        target.getCheckViolation(), "Force Banned",
                        commandSender.getName(), target.getCheckPunishVL(), target.getLastFlaggedCheck()));


            } catch (NullPointerException nullP) {

            }
        }
        else if (commandSender instanceof ConsoleCommandSender) {
            if (args.length < 2) {
                commandSender.sendMessage(ChatColor.RED
                        + "Usage: /" + Anticheat.INSTANCE.getConfigValues().getCommandName() + " forceban (player)");
                return;

            }
            String targetName = args[1];

            if (targetName.isEmpty()) {
                commandSender.sendMessage("Please enter a valid username.");
                return;
            }
            PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(Bukkit.getPlayer(args[1]));
            if (target == null) {
                commandSender.sendMessage(ChatColor.RED + "[Error!] The player your trying to ban is offline, " +
                        "please try another name.");
                return;
            }
            commandSender.sendMessage(ChatColor.GREEN
                    + "Commencing a forceful anticheat ban for the player: " + targetName);
            Bukkit.getScheduler().runTaskAsynchronously(Anticheat.INSTANCE.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    Check.punishPlayer(target, false, 40,
                            target.getCheckViolation(), target.getCheckName(),
                            target.getCheckType(), target.getCheckPunishVL(), target.getLastFlaggedCheck());
                }
            });

        }

    }
}
