package pro.cyrent.anticheat.api.command;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.command.commands.MainCommand;
import pro.cyrent.anticheat.api.command.commands.sub.shorten.ShortAlertCommand;
import pro.cyrent.anticheat.api.command.commands.sub.shorten.ShortLogsCommand;
import pro.cyrent.anticheat.util.config.ConfigValues;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class CommandManager {
    private final List<Command> commandList = new CopyOnWriteArrayList<>();

    public CommandManager() {

        try {

            ConfigValues configValues = Anticheat.INSTANCE.getConfigValues();

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName(),
                    null,
                    "Main command.", true));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " alerts",
                    "/" + configValues.getCommandName() + " alerts",
                    "Toggle on, and off alerts.", true));


            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " dev",
                    "/" + configValues.getCommandName() + " dev",
                    "Toggle on, and off dev alerts.", true));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " forceban",
                    "/" + configValues.getCommandName() + " forceban (player)",
                    "Forceban a player with the anticheat.", true));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " ping",
                    "/" + configValues.getCommandName() + " ping (player)",
                    "Gets the ping of the target player.", true));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " version",
                    "/" + configValues.getCommandName() + " version (player)",
                    "Gets the version of the target player.", true));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " gui",
                    "/" + configValues.getCommandName() + " gui",
                    "GUI for the anticheat.", true));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " stats",
                    "/" + configValues.getCommandName() + " stats",
                    "Stats GUI for the anticheat.", true));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " logs",
                    "/" + configValues.getCommandName() + " logs (player) (global)",
                    "Displays a players current logs.", true));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " clearlogs",
                    "/" + configValues.getCommandName() + " clearlogs",
                    "Deletes all player logs.", true));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " reload",
                    "/" + configValues.getCommandName() + " reload",
                    "Reloads anticheat configs", true));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " crash",
                    "/" + configValues.getCommandName() + " crash (player)",
                    "Player Crasher", true));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " debug",
                    "/" + configValues.getCommandName() + " debug (player) (type)",
                    "Development Purposes for debugging.", true));

//            addCommand(new Command(new MainCommand(configValues.getCommandName()),
//                    configValues.getCommandName() + " banwave",
//                    "/" + configValues.getCommandName()
//                            + " banwave (enable/disable/start/stop/add/remove/clear/timely/list) (player)",
//                    "Uses banwave system.", false));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " testkb",
                    "/" + configValues.getCommandName() + " testkb (player) (silent)",
                    "Tests a players velocity", true));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " forcebot",
                    "/" + configValues.getCommandName() + " forcebot (player)",
                    "Hypixel Watchdog style bot", true));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " info",
                    "/" + configValues.getCommandName() + " info (player)",
                    "See a clients information", true));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " packetlog",
                    "/" + configValues.getCommandName() + " packetlog (player)",
                    "Used for development purposes", true));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " top",
                    "/" + configValues.getCommandName() + " top",
                    "Show the top users flagging", true));

            addCommand(new Command(new MainCommand(configValues.getCommandName()),
                    configValues.getCommandName() + " cps",
                    "/" + configValues.getCommandName() + " cps (player)",
                    "Shows click information about the player in chat and or hot-bar", false));

            addCommand(new Command(new ShortLogsCommand("logs"),
                    "logs",
                    "/logs (player)",
                    "Displays a players current logs.", true));

            addCommand(new Command(new ShortAlertCommand("alerts"),
                    "alerts",
                    "/alerts",
                    "Toggle on, and off alerts.", true));

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addCommand(Command... commands) {
        for (Command command : commands) {
            commandList.add(command);
            if (command.isEnabled()) {
                CommandUtils.registerCommand(command);
            }
        }
    }

    public void removeCommand() {
        commandList.forEach(CommandUtils::unRegisterBukkitCommand);
    }
}

