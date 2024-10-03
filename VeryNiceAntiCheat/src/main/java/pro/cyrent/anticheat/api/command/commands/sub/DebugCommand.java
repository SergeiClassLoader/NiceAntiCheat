package pro.cyrent.anticheat.api.command.commands.sub;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;

import java.util.concurrent.atomic.AtomicBoolean;

public class DebugCommand {

    private final String line = Anticheat.INSTANCE.getMessageValues().getLineMessage();

    public void execute(String[] args, String s, CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to execute this command.");
            return;
        }


        PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender));

        try {
            if (user == null) {
                commandSender.sendMessage("If you see this message contact moose1301");
                return;
            }

            if (args.length < 2) {
                commandSender.sendMessage(ChatColor.RED + "Usage: /" + Anticheat.INSTANCE.getConfigValues().getCommandName()
                        + " debug (player)");
                return;
            }
            String targetName = args[1];

            if (targetName.length() < 0) {
                commandSender.sendMessage("Please enter a valid username.");
                return;
            }
            PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(Bukkit.getPlayer(args[1]));

            if (target == null) {
                commandSender.sendMessage(ChatColor.RED + "User is not online");
                return;
            }

            if (args.length != 3) {
                commandSender.sendMessage("\n");
                commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                        + ChatColor.RED
                        + " Too many arguments where found, Please contact a dev if you don't know what you're doing.");
                commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                        + " Any previous debugging has now stopped!");
                commandSender.sendMessage("\n");
                user.setDebugMode(false);
                user.setDebuggedUser(null);
                return;
            }

            AtomicBoolean debug = new AtomicBoolean(false);

            user.getDebugType().forEach(type -> {

                if (type.equalsIgnoreCase(args[2])) {
                    debug.set(true);
                }
            });

            if (debug.get()) {

                if (user.isDebugMode()) {
                    user.setDebugMode(false);
                    user.setDebuggedUser(null);
                    user.setDebugSet("null");
                    commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                            + " Debugging has stopped for " + ChatColor.RED + targetName);
                    return;
                }

                user.setDebugSet(args[2]);
                user.setDebugMode(true);
                user.setDebuggedUser(target);
            } else {
                user.setDebugMode(false);
                user.setDebuggedUser(null);
                user.setDebugSet("null");
            }

            if (user.isDebugMode() && user.getDebuggedUser() != null && !user.getDebugSet()
                    .equalsIgnoreCase("null")) {
                commandSender.sendMessage("\n" + line);
                commandSender.sendMessage("\n");
                commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                        + " Player " + ChatColor.RED + target.getUsername()
                        + ChatColor.RESET + " is now being debugged for "
                        + ChatColor.GOLD + args[2].toUpperCase());
                commandSender.sendMessage("\n");
                commandSender.sendMessage(line + "\n");
            } else if (!user.isDebugMode() && user.getDebuggedUser() == null
                    && user.getDebugSet().equalsIgnoreCase("null")) {
                commandSender.sendMessage("\n");
                commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                        + " No debug method called '" + args[2] + "' was found.");
                commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                        + " Any previous debugging has now stopped!");
                commandSender.sendMessage("\n");
            }


        } catch (NullPointerException ignored) {

        }

    }
}