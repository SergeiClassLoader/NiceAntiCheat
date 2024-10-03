package pro.cyrent.anticheat.api.command.commands.sub.shorten;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.command.commands.sub.AlertsCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.util.ArrayList;

public class ShortAlertCommand extends BukkitCommand {

    private final AlertsCommand alertsCommand = new AlertsCommand();

    public ShortAlertCommand(String name) {
        super(name);
        this.description = "Shortened alert command";
        this.usageMessage = "/" + name;
        this.setAliases(new ArrayList<>());
    }

    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("alerts")) {

            if (!commandSender.hasPermission(Anticheat.INSTANCE.getPermissionValues().getAlertCommand())) {
                commandSender.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }

            this.alertsCommand.execute(args, commandLabel, commandSender);
        }
        return false;
    }
}
