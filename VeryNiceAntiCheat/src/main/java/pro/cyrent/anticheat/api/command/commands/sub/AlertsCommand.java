package pro.cyrent.anticheat.api.command.commands.sub;


import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;


public class AlertsCommand {

    public void execute(String[] args, String s, CommandSender commandSender) {
        PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender));
        if (user == null) {
            commandSender.sendMessage("If you see this message contact moose1301");
            return;
        }
        if (user.isAlerts()) {
            user.setAlerts(false);
            commandSender.sendMessage(ChatColor.RED + "Alerts have been toggled off!");
        } else {
            user.setAlerts(true);
            commandSender.sendMessage(ChatColor.GREEN + "Alerts have been toggled on!");
        }

    }
}
