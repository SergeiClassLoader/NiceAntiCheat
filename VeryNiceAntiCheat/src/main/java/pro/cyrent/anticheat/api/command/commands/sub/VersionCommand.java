package pro.cyrent.anticheat.api.command.commands.sub;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;

public class VersionCommand {

    private final String line = ChatColor.GRAY + "§m------------------------------------------";

    public void execute(String[] args, String s, CommandSender commandSender) {

        if (commandSender instanceof Player) {
            PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender));
            if (user == null) {
                commandSender.sendMessage("If you see this message contact moose1301");
                return;
            }
            try {


                if (args.length < 2) {

                    commandSender.sendMessage(ChatColor.RED + "Usage: /"
                            + Anticheat.INSTANCE.getConfigValues().getCommandName() + " version (player)");
                    return;
                }
                String targetName = args[1];

                if (targetName.isEmpty()) {
                    commandSender.sendMessage("Please enter a valid username.");
                    return;
                }
                PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(Bukkit.getPlayer(args[1]));
                if (target == null) {
                    commandSender.sendMessage(ChatColor.RED + "User is not online)");
                    return;
                }
                commandSender.sendMessage("\n" + line);
                commandSender.sendMessage("Player: " + ChatColor.RED + target.getPlayer().getName());
                commandSender.sendMessage("\n");
                commandSender.sendMessage("Game Protocol ID: "
                        + ChatColor.GREEN + target.getProtocolVersion());
                commandSender.sendMessage("Version: "
                        + ChatColor.GREEN + Anticheat.INSTANCE.getVersionSupport().getClientVersion(target));

                commandSender.sendMessage(line + "\n");


            } catch (NullPointerException ignored) {

            }
        } else {
            if (args.length < 2) {
                commandSender.sendMessage(ChatColor.RED + "Usage: /" + Anticheat.INSTANCE.getConfigValues().getCommandName() + " version (player)");
                return;
            }
            String targetName = args[1];

            if (targetName.isEmpty()) {
                commandSender.sendMessage("Please enter a valid username.");
                return;
            }
            PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(Bukkit.getPlayer(args[1]));
            if (target == null) {
                commandSender.sendMessage(ChatColor.RED + "User is not online");
                return;
            }

            commandSender.sendMessage("\n" + line);
            commandSender.sendMessage("Player: " + ChatColor.RED + target.getPlayer().getName());
            commandSender.sendMessage("\n");
            commandSender.sendMessage("Game Protocol ID: "
                    + ChatColor.GREEN + target.getProtocolVersion());
            commandSender.sendMessage("Version: "
                    + ChatColor.GREEN + Anticheat.INSTANCE.getVersionSupport().getClientVersion(target));

            commandSender.sendMessage(line + "\n");

        }
    }
}