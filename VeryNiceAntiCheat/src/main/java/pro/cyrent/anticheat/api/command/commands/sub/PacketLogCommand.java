package pro.cyrent.anticheat.api.command.commands.sub;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.util.paste.PasteUtil;


public class PacketLogCommand {

    public void execute(String[] args, String s, CommandSender commandSender) {
        try {
            if (args.length == 0) {
                commandSender.sendMessage(ChatColor.RED
                        + "Usage: /" + Anticheat.INSTANCE.getConfigValues().getCommandName() + " packetlog (name)");
                return;
            }

            if (Anticheat.INSTANCE.getPacketLogPlayer() != null) {
                Anticheat.INSTANCE.setPacketLogPlayer(null);

                commandSender.sendMessage(ChatColor.GREEN + "Packet logger has been stopped, uploading log...");


                // You need your own paste system for this to work!


                Anticheat.INSTANCE.getExecutorService().execute(() -> {
                    String paste = PasteUtil.createCustom(Anticheat.INSTANCE.getPacketLogList().toString());
                    Anticheat.INSTANCE.getPacketLogList().clear();

                    if (paste != null) {
                        commandSender.sendMessage(ChatColor.GREEN + "Movement Log:" + ChatColor.GRAY + paste);
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Unable to paste? [1]");
                    }
                });


                Anticheat.INSTANCE.getExecutorService().execute(() -> {
                    String paste = PasteUtil.createCustom(Anticheat.INSTANCE.getConnectionPacketLogList().toString());
                    Anticheat.INSTANCE.getConnectionPacketLogList().clear();

                    if (paste != null) {
                        commandSender.sendMessage(ChatColor.GREEN + "Connection Log:" + ChatColor.GRAY + paste);
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Unable to paste? [2]");
                    }
                });


                Anticheat.INSTANCE.getExecutorService().execute(() -> {
                    String paste = PasteUtil.createCustom(Anticheat.INSTANCE.getCheckPacketLog().toString());
                    Anticheat.INSTANCE.getCheckPacketLog().clear();

                    if (paste != null) {
                        commandSender.sendMessage(ChatColor.GREEN + "Check Exemption Log:" + ChatColor.GRAY + paste);
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Unable to paste? [2]");
                    }
                });

                Anticheat.INSTANCE.getExecutorService().execute(() -> {
                    String paste = PasteUtil.createCustom(Anticheat.INSTANCE.getPlayerLogsList().toString());

                    if (paste != null) {
                        commandSender.sendMessage(ChatColor.GREEN + "Player Log:" + ChatColor.GRAY + paste);
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Unable to paste? [3]");
                    }

                    Anticheat.INSTANCE.getCheckPacketLog().clear();
                });

                return;
            }

            Player player = Bukkit.getPlayer(args[1]);

            if (player == null || !player.isOnline()) {
                commandSender.sendMessage(ChatColor.RED + "Could not find player.");
                return;
            }

            Anticheat.INSTANCE.getPacketLogList().clear();
            Anticheat.INSTANCE.setPacketLogPlayer(player);

            commandSender.sendMessage(ChatColor.GREEN + "Now PacketLogging " + ChatColor.GRAY
                    + player.getName());


        } catch (ArrayIndexOutOfBoundsException e) {
            commandSender.sendMessage(ChatColor.RED
                    + "Usage: /" + Anticheat.INSTANCE.getConfigValues().getCommandName() + " packetlog (name)");
        }
    }
}

