package pro.cyrent.anticheat.api.command.commands.sub;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;

public class ClearLogsCommand {
    public void execute(String[] args, String s, CommandSender commandSender) {
        if (args.length == 0) {
            return;
        }

        if (!(commandSender instanceof Player)) {
            return;
        }

        Player player = (Player) commandSender;

        PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(player);

        if (user == null) return;

        boolean isDev = user.isDev(player);

        if (!isDev) {
            commandSender.sendMessage(ChatColor.RED + "This is a developer only command!");
            return;
        }
        if (Anticheat.INSTANCE.getConfigValues().isOwnMongo()) {
            commandSender.sendMessage("");
            commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix() + " " + ChatColor.GOLD + "Contacting the database...");
            commandSender.sendMessage("");

            try {
                Anticheat.INSTANCE.getDatabaseManager().reset();
                commandSender.sendMessage(ChatColor.GREEN + "Successfully cleared the logs!");
            } catch (Exception e) {
                commandSender.sendMessage(ChatColor.GREEN + "Error occurred while clearing logs!");
                e.printStackTrace();
            }
        } else {
            // ur own code.
        }
    }
}