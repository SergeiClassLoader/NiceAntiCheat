package pro.cyrent.anticheat.api.command.commands.sub;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;


public class CpsCommand {

    public void execute(String[] args, String s, CommandSender commandSender) {
        if (args.length <= 1) {
            commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix() + " " +
                    ChatColor.RED + "Invalid arguments, please supply a valid online players name.");
            return;
        }
        if (Bukkit.getServer().getPlayer(args[1]) == null) {
            commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix() + " " +
                    ChatColor.RED + "Could not find the player " + ChatColor.GRAY
                    + args[1]);
            return;
        }
        PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(Bukkit.getServer()
                .getPlayer(args[1]).getUniqueId());

        if (target == null) {
            commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix() + " "
                    + ChatColor.RED + "Could not find the player " + ChatColor.GRAY
                    + args[1]);
            return;
        }
        PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender).getUniqueId());

        if (user.getCpsMonitorRunnable() != null) {
            commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix() + " "
                    + ChatColor.RED + "No longer monitoring " + target.getUsername() + "'s CPS!");

            user.getCpsMonitorRunnable().cancel();
            user.setCpsMonitorRunnable(null);

        } else {
            commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix() + " "
                    + ChatColor.GREEN + "CPS Monitor for " + ChatColor.GRAY
                    + target.getUsername() + ChatColor.GREEN + " has now started!");

            user.setTargetMonitorUser(target);
            user.startCPSMonitor();
        }

    }
}
