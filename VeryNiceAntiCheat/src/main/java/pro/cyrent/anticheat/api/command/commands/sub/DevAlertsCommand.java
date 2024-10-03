package pro.cyrent.anticheat.api.command.commands.sub;


import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class DevAlertsCommand {

    public void execute(String[] args, String s, CommandSender commandSender) {
        PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender));
        if (user == null) {
            commandSender.sendMessage("If you see this message contact moose1301");
            return;
        }
            if (user.isDevAlerts()) {
                user.setDevAlerts(false);
                commandSender.sendMessage(ChatColor.RED + "[DEV] Alerts have been toggled off!");
            } else {
                user.setDevAlerts(true);
                commandSender.sendMessage(ChatColor.GREEN + "[DEV] Alerts have been toggled on!");
            }

    }
}
