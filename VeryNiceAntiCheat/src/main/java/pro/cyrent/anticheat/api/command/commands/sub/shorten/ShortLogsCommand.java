package pro.cyrent.anticheat.api.command.commands.sub.shorten;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.command.commands.sub.LogsCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.util.ArrayList;

public class ShortLogsCommand extends BukkitCommand {

    private final LogsCommand logsCommand = new LogsCommand();

    public ShortLogsCommand(String name) {
        super(name);
        this.description = "Shortened logs command";
        this.usageMessage = "/" + name;
        this.setAliases(new ArrayList<>());
    }

    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] args) {

        if (!commandSender.hasPermission(Anticheat.INSTANCE.getPermissionValues().getLogsCommand())) {
            commandSender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (commandLabel.equalsIgnoreCase("logs")) {

            if (args.length > 0) {

                String[] modifiedArgs = new String[args.length + 2];
                System.arraycopy(args, 0, modifiedArgs, 1, args.length);
                modifiedArgs[modifiedArgs.length - 1] = "total";

                this.logsCommand.execute(modifiedArgs, commandLabel, commandSender);
            } else {
                commandSender.sendMessage("Usage: /logs (player)");
            }
        }
        return false;
    }
}
