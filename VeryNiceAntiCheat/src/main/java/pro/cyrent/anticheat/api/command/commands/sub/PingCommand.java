package pro.cyrent.anticheat.api.command.commands.sub;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PingCommand {

    private final String line = Anticheat.INSTANCE.getMessageValues().getLineMessage();

    public void execute(String[] args, String s, CommandSender commandSender) {

        if (commandSender instanceof Player) {
            PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender));

            try {
                if (user != null) {

                    if (args.length >= 2) {
                        String targetName = args[1];

                        if (targetName.length() > 0) {
                            PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(Bukkit.getPlayer(args[1]));
                            if (target != null) {

                                long transPing = target
                                        .getTransactionProcessor().getTransactionPing();

                                long postTransPing = target
                                        .getTransactionProcessor().getPostTransactionPing();

                                long keepPing = target
                                        .getTransactionProcessor().getKeepAlivePing();


                                boolean lagging = target
                                        .getMovementProcessor().getLastFlyingPauseTimer().hasNotPassed(20);

                                boolean tranLag = target
                                        .getTransactionProcessor().getTransactionPingDrop() > 500
                                        || target
                                        .getTransactionProcessor().getTransactionPing() >= 900
                                        || target
                                        .getTransactionProcessor().getTransactionPing() <= 0
                                        || target
                                        .getTransactionProcessor().getPostTransactionPing() <= 0
                                        || target
                                        .getTransactionProcessor().getTransactionQueue().size() > 200;

                                boolean keepLag = target
                                        .getTransactionProcessor().getKeepAliveQueue().size() > 75
                                        || target
                                        .getTransactionProcessor().getKeepAlivePing() > 1000;

                                commandSender.sendMessage("\n" + line);
                                commandSender.sendMessage("Player: " + ChatColor.RED + target.getPlayer().getName());
                                commandSender.sendMessage("\n");
                                commandSender.sendMessage("Transaction Ping: " + ChatColor.GREEN + transPing);
                                commandSender.sendMessage("Post Transaction Ping: " + ChatColor.GREEN + postTransPing);
                                commandSender.sendMessage("Keep-Alive Ping: " + ChatColor.GREEN + keepPing);
                                commandSender.sendMessage("\n");
                                commandSender.sendMessage("Position Lag: " + ChatColor.GREEN + lagging);
                                commandSender.sendMessage("Transaction Lag: " + ChatColor.GREEN + tranLag);
                                commandSender.sendMessage("Keep-Alive Lag: " + ChatColor.GREEN + keepLag);

                                commandSender.sendMessage(line + "\n");
                            } else {
                                commandSender.sendMessage(ChatColor.RED + "User is not online");
                            }
                        } else {
                            commandSender.sendMessage("Please enter a valid username.");
                        }
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Usage: /" + Anticheat.INSTANCE.getConfigValues().getCommandName()
                                + " ping (player)");
                    }
                } else {
                    commandSender.sendMessage("If you see this message contact moose1301");
                }
            } catch (NullPointerException ignored) {

            }
        } else {
            if (args.length >= 2) {
                String targetName = args[1];

                if (targetName.length() > 0) {
                    PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(Bukkit.getPlayer(args[1]));
                    if (target != null) {

                        long transPing = target
                                .getTransactionProcessor().getTransactionPing();

                        long keepPing = target
                                .getTransactionProcessor().getKeepAlivePing();

                        boolean lagging = target
                                .getMovementProcessor().getLastFlyingPauseTimer().hasNotPassed(20);


                        long postTransPing = target
                                .getTransactionProcessor().getPostTransactionPing();

                        boolean tranLag = target
                                .getTransactionProcessor().getTransactionPingDrop() > 500
                                || target
                                .getTransactionProcessor().getTransactionPing() >= 900
                                || target
                                .getTransactionProcessor().getTransactionPing() <= 0
                                || target
                                .getTransactionProcessor().getPostTransactionPing() <= 0
                                || target
                                .getTransactionProcessor().getTransactionQueue().size() > 200;

                        boolean keepLag = target
                                .getTransactionProcessor().getKeepAliveQueue().size() > 75
                                || target
                                .getTransactionProcessor().getKeepAlivePing() > 15000;

                        commandSender.sendMessage("\n" + line);
                        commandSender.sendMessage("Player: " + ChatColor.RED + target.getPlayer().getName());
                        commandSender.sendMessage("\n");
                        commandSender.sendMessage("Transaction Ping: " + ChatColor.GREEN + transPing);
                        commandSender.sendMessage("Post Transaction Ping: " + ChatColor.GREEN + postTransPing);
                        commandSender.sendMessage("Keep-Alive Ping: " + ChatColor.GREEN + keepPing);
                        commandSender.sendMessage("\n");
                        commandSender.sendMessage("Position Lag: " + ChatColor.GREEN + lagging);
                        commandSender.sendMessage("Transaction Lag: " + ChatColor.GREEN + tranLag);
                        commandSender.sendMessage("Keep-Alive Lag: " + ChatColor.GREEN + keepLag);

                        commandSender.sendMessage(line + "\n");
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "User is not online");
                    }
                } else {
                    commandSender.sendMessage("Please enter a valid username.");
                }
            } else {
                commandSender.sendMessage(ChatColor.RED + "Usage: /" + Anticheat.INSTANCE.getConfigValues().getCommandName()
                        + " ping (player)");
            }
        }
    }
}