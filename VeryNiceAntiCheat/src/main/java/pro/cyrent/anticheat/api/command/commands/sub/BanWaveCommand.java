package pro.cyrent.anticheat.api.command.commands.sub;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BanWaveCommand {

    public void execute(String[] args, String s, CommandSender commandSender) {
        Anticheat.INSTANCE.getBanWaveManager().getExecutorService().execute(new Runnable() {

            @Override
            public void run() {
                executeAsync(args, s, commandSender);
            }
        });
    }
    public void executeAsync(String[] args, String s, CommandSender commandSender) {

        if (commandSender instanceof Player) {
            if (args.length >= 2) {
                PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender));

                if (user != null) {


                    Player playerSender = ((Player) commandSender).getPlayer();

                    if (args[1].equalsIgnoreCase("enable")) {
                        Anticheat.INSTANCE.getConfigValues().setBanWave(true);
                        commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                + " Enabled Banwave!");
                    }

                    if (args[1].equalsIgnoreCase("disable")) {
                        Anticheat.INSTANCE.getConfigValues().setBanWave(false);
                        commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                + " Disabled Banwave!");
                    }


                    List<String> players = Anticheat.INSTANCE.getBanWaveManager().getWaveList();

                    if (args[1].equalsIgnoreCase("add")) {
                        String targetName = args[2];

                        if (targetName.length() > 0) {
                            Player target = Bukkit.getPlayer(args[2]);

                            if (target != null) {
                                Anticheat.INSTANCE.getBanWaveManager().addPlayer(target.getPlayer().getName());
                                commandSender.sendMessage(
                                        Anticheat.INSTANCE.getConfigValues().getPrefix()
                                                + " You have added player: "
                                                + targetName
                                                + " to the banwave!");
                            } else {
                                Anticheat.INSTANCE.getBanWaveManager().addPlayer(targetName);
                                commandSender.sendMessage(
                                        Anticheat.INSTANCE.getConfigValues().getPrefix()
                                                + " You have added player: "
                                                + targetName
                                                + " to the banwave! (Offline Player)");
                            }
                        }
                    }

                    if (args[1].equalsIgnoreCase("timely")) {
                        if (Anticheat.INSTANCE.getConfigValues().isBanWave()) {
                            if (!Anticheat.INSTANCE.getConfigValues().isBanWaveTimely()) {
                                Anticheat.INSTANCE.getConfigValues().setBanWaveTimely(true);
                                Anticheat.INSTANCE.getBanWaveManager().doCheckUp();
                                commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                        + " Enabled timely banwaves! Banwaves will attempt to start every: "
                                        + Anticheat.INSTANCE.getConfigValues().getBanWaveCheckUpTime() + " seconds.");
                            } else {
                                Anticheat.INSTANCE.getConfigValues().setBanWaveTimely(false);
                                Anticheat.INSTANCE.getBanWaveManager().endCheckUp();
                                commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                        + " Disabled timely banwaves!");
                            }
                        } else {
                            commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                    + " Banwave is NOT enabled! Please enable it to continue");
                        }
                    }

                    if (args[1].equalsIgnoreCase("remove")) {
                        String targetName = args[2];

                        if (targetName.length() > 0) {
                            Player target = Bukkit.getPlayer(args[2]);
                            if (target != null) {
                                Anticheat.INSTANCE.getBanWaveManager().removePlayer(target.getPlayer().getName());
                                commandSender.sendMessage(
                                        Anticheat.INSTANCE.getConfigValues().getPrefix()
                                                + " You have removed player: "
                                                + target.getPlayer().getName()
                                                + " to the banwave!");
                            } else {

                                for (String player : players) {
                                    if (targetName.equalsIgnoreCase(player)) {

                                        Anticheat.INSTANCE.getBanWaveManager().removePlayer(player);
                                        commandSender.sendMessage(
                                                Anticheat.INSTANCE.getConfigValues().getPrefix()
                                                        + " You have removed player: "
                                                        + player
                                                        + " to the banwave! (Offline Player)");
                                    }
                                }
                            }
                        }
                    }

                    if (args[1].equalsIgnoreCase("clear")) {
                        Anticheat.INSTANCE.getBanWaveManager().clearPlayers();
                        commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                + " All players have been removed from the banwave!");
                    }

                    if (args[1].equalsIgnoreCase("start")) {
                        if (Anticheat.INSTANCE.getConfigValues().isBanWave()) {
                            Anticheat.INSTANCE.getBanWaveManager().commenceBanWave(commandSender);
                        } else {
                            commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                    + " Banwave is NOT enabled! Please enable it to continue");
                        }
                    }

                    if (args[1].equalsIgnoreCase("stop")) {
                        if (Anticheat.INSTANCE.getConfigValues().isBanWave()) {
                            Anticheat.INSTANCE.getBanWaveManager().stopBanWave(playerSender);
                        } else {
                            commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                    + " Banwave is NOT enabled! Please enable it to continue");
                        }
                    }

                    if (args[1].equalsIgnoreCase("list")) {
                        commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                + " Amount of players ready to be banwaved: " + players.size());
                        commandSender.sendMessage(String.join(", ", players));
                    }
                }
            }
        } else {
            if (args.length >= 2) {
                CommandSender user = commandSender;

                if (user != null) {

                    if (args[1].equalsIgnoreCase("enable")) {
                        Anticheat.INSTANCE.getConfigValues().setBanWave(true);
                        commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                + " Enabled Banwave!");
                    }

                    if (args[1].equalsIgnoreCase("disable")) {
                        Anticheat.INSTANCE.getConfigValues().setBanWave(false);
                        commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                + " Disabled Banwave!");
                    }


                    List<String> players = Anticheat.INSTANCE.getBanWaveManager().getWaveList();

                    if (args[1].equalsIgnoreCase("add")) {
                        String targetName = args[2];

                        if (targetName.length() > 0) {
                            Player target = Bukkit.getPlayer(args[2]);

                            if (target != null) {
                                Anticheat.INSTANCE.getBanWaveManager().addPlayer(target.getPlayer().getName());
                                commandSender.sendMessage(
                                        Anticheat.INSTANCE.getConfigValues().getPrefix()
                                                + " You have added player: "
                                                + targetName
                                                + " to the banwave!");
                            } else {
                                Anticheat.INSTANCE.getBanWaveManager().addPlayer(targetName);
                                commandSender.sendMessage(
                                        Anticheat.INSTANCE.getConfigValues().getPrefix()
                                                + " You have added player: "
                                                + targetName
                                                + " to the banwave! (Offline Player)");
                            }
                        }
                    }

                    if (args[1].equalsIgnoreCase("timely")) {
                        if (Anticheat.INSTANCE.getConfigValues().isBanWave()) {
                            if (!Anticheat.INSTANCE.getConfigValues().isBanWaveTimely()) {
                                Anticheat.INSTANCE.getConfigValues().setBanWaveTimely(true);
                                Anticheat.INSTANCE.getBanWaveManager().doCheckUp();
                                commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                        + " Enabled timely banwaves! Banwaves will attempt to start every: "
                                        + Anticheat.INSTANCE.getConfigValues().getBanWaveCheckUpTime() + " seconds.");
                            } else {
                                Anticheat.INSTANCE.getConfigValues().setBanWaveTimely(false);
                                Anticheat.INSTANCE.getBanWaveManager().endCheckUp();
                                commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                        + " Disabled timely banwaves!");
                            }
                        } else {
                            commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                    + " Banwave is NOT enabled! Please enable it to continue");
                        }
                    }

                    if (args[1].equalsIgnoreCase("remove")) {
                        String targetName = args[2];

                        if (targetName.length() > 0) {
                            Player target = Bukkit.getPlayer(args[2]);
                            if (target != null) {
                                Anticheat.INSTANCE.getBanWaveManager().removePlayer(target.getPlayer().getName());
                                commandSender.sendMessage(
                                        Anticheat.INSTANCE.getConfigValues().getPrefix()
                                                + " You have removed player: "
                                                + target.getPlayer().getName()
                                                + " to the banwave!");
                            } else {

                                for (String player : players) {
                                    if (targetName.equalsIgnoreCase(player)) {

                                        Anticheat.INSTANCE.getBanWaveManager().removePlayer(player);
                                        commandSender.sendMessage(
                                                Anticheat.INSTANCE.getConfigValues().getPrefix()
                                                        + " You have removed player: "
                                                        + player
                                                        + " to the banwave! (Offline Player)");
                                    }
                                }
                            }
                        }
                    }

                    if (args[1].equalsIgnoreCase("clear")) {
                        Anticheat.INSTANCE.getBanWaveManager().clearPlayers();
                        commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                + " All players have been removed from the banwave!");
                    }

                    if (args[1].equalsIgnoreCase("start")) {
                        if (Anticheat.INSTANCE.getConfigValues().isBanWave()) {
                            Anticheat.INSTANCE.getBanWaveManager().commenceBanWave(commandSender);
                        } else {
                            commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                    + " Banwave is NOT enabled! Please enable it to continue");
                        }
                    }

                    if (args[1].equalsIgnoreCase("stop")) {
                        if (Anticheat.INSTANCE.getConfigValues().isBanWave()) {
                            Anticheat.INSTANCE.getBanWaveManager().stopBanWave(commandSender);
                        } else {
                            commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                    + " Banwave is NOT enabled! Please enable it to continue");
                        }
                    }

                    if (args[1].equalsIgnoreCase("list")) {
                        commandSender.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                                + " Amount of players ready to be banwaved: " +
                                 players.size());
                        commandSender.sendMessage(String.join(", ", players));
                    }
                }
            }
        }
    }
}