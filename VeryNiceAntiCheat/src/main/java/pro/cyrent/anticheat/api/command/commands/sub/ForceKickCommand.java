package pro.cyrent.anticheat.api.command.commands.sub;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;

public class ForceKickCommand {

    public void execute(String[] args, String s, CommandSender commandSender) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return;
        }
        Player player = (Player) commandSender;
        PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(player);

        try {
            if (user == null) {
                commandSender.sendMessage("If you see this message contact support.");
                return;
            }

            if (args.length < 2) {
                commandSender.sendMessage(ChatColor.RED
                        + "Usage: /" + Anticheat.INSTANCE.getConfigValues().getCommandName() + " forcekick (player)");
                return;
            }
            String targetName = args[1];

            if (targetName.length() == 0) {
                commandSender.sendMessage("User not working");
                return;
            }
            Player targetPlayer = Bukkit.getPlayer(targetName);
            if (targetPlayer == null) {
                commandSender.sendMessage(ChatColor.RED + "User not online");
                return;
            }
            PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(targetPlayer);
            if (target == null) {
                commandSender.sendMessage(ChatColor.RED + "User not online");
                return;
            }
            commandSender.sendMessage(ChatColor.GREEN
                    + "Kicking player: " + targetName);
            targetPlayer.kickPlayer(ChatColor.RED + "Error Loading Data, Please try again.");


        } catch (NullPointerException nullP) {
            nullP.printStackTrace();
        }
    }
}
