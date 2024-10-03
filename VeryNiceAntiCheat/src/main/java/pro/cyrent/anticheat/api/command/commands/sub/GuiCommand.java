package pro.cyrent.anticheat.api.command.commands.sub;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.Check;
import pro.cyrent.anticheat.api.check.CheckState;
import pro.cyrent.anticheat.api.command.commands.util.GUIUtils;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.tps.TpsMonitor;

import java.util.Arrays;
import java.util.Collections;


public class GuiCommand {

    public void execute(String[] args, String s, CommandSender commandSender) {
        PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender));
        if (user == null) {
            commandSender.sendMessage("If you see this message contact moose1301");
            return;
        }


        Inventory inventory = Bukkit.getServer().createInventory(null, 27,
                ChatColor.RED + Anticheat.INSTANCE.getAnticheatName());

        int totalChecks = user.getCheckManager().sortChecksAlphabetically().size();

        int enabledChecks = (int) user.getCheckManager().sortChecksAlphabetically().stream()
                .filter(Check::isEnabled).count();

        int disabledChecks = (int) user.getCheckManager().sortChecksAlphabetically().stream()
                .filter(check -> !check.isEnabled()).count();

        int noPunishChecks = (int) user.getCheckManager().sortChecksAlphabetically().stream()
                .filter(check -> !check.isPunishable()).count();

        int punishableChecks = (int) user.getCheckManager().sortChecksAlphabetically().stream()
                .filter(Check::isPunishable).count();

        int experimental = (int) user.getCheckManager().sortChecksAlphabetically().stream()
                .filter(Check::isExperimental).count();

        int dev = (int) user.getCheckManager().sortChecksAlphabetically().stream()
                .filter(check -> check.getState() == CheckState.DEV).count();

        inventory.setItem(11, GUIUtils.generateItem(new ItemStack(Material.BOOK, 1),
                ChatColor.AQUA + "Checks",
                Collections.singletonList(ChatColor.GRAY + "Enable/Disable checks ingame")));

        inventory.setItem(13, GUIUtils.generateItem(new ItemStack(Material.DIAMOND, 1),
                ChatColor.AQUA + String.format("%s Anticheat", Anticheat.INSTANCE.getAnticheatName()),
                Arrays.asList(ChatColor.GRAY + "Version: " +
                                ChatColor.GREEN + Anticheat.INSTANCE.getVersion(),
                        ChatColor.GRAY + "Checks: "
                                + ChatColor.WHITE + totalChecks + ChatColor.GRAY + "/"
                                + ChatColor.GREEN + enabledChecks + ChatColor.GRAY + "/"
                                + ChatColor.RED + disabledChecks + ChatColor.GRAY + "/"
                                + ChatColor.GOLD + punishableChecks + ChatColor.GRAY + "/"
                                + ChatColor.YELLOW + noPunishChecks + ChatColor.GRAY + "/"
                                + ChatColor.LIGHT_PURPLE + experimental + ChatColor.GRAY + "/"
                                + ChatColor.DARK_PURPLE + dev,
                        ChatColor.WHITE + "* Total Checks",
                        ChatColor.GREEN + "* Checks Enabled",
                        ChatColor.RED + "* Checks Disabled",
                        ChatColor.GOLD + "* Punishment Checks",
                        ChatColor.YELLOW + "* No Punishment Checks",
                        ChatColor.LIGHT_PURPLE + "* Experimental Checks",
                        ChatColor.DARK_PURPLE + "* In-Development Checks"
                )
        ));

//            inventory.setItem(15, GUIUtils.generateItem(new ItemStack(Material.SPONGE, 1),
//                    ChatColor.AQUA + "Statistics",
//                    Arrays.asList(
//                            ChatColor.GRAY + "Player Threads: "
//                                    + ChatColor.RED + Anticheat.INSTANCE
//                                    .getThreadManager().getUserThreads().size() + ChatColor.DARK_GRAY + "/" +
//                                    ChatColor.RED + Anticheat.INSTANCE.getThreadManager().getMaxThreads(),
//
//                            ChatColor.GRAY + "Available Cores: "
//                                    + ChatColor.RED + Anticheat.INSTANCE
//                                    .getThreadManager().getThreads(),
//
//                            ChatColor.GRAY + "User(s) Loaded: "
//                                    + ChatColor.RED + Anticheat.INSTANCE
//                                    .getUserManager().getUserMap().size(),
//
//                            ChatColor.GRAY + "Server Lag: " + (Anticheat.INSTANCE.isServerLagging()
//                                    || Anticheat.INSTANCE.getLastServerLagTick() > 0
//                                    ? ChatColor.RED + "Yes" : ChatColor.GREEN + "No"),
//
//                            ChatColor.GRAY + "TPS: (1s, 5s, 10s, 1m, 5m, 15m)",
//                            ChatColor.RESET + this.getTPS().toString()
//                    )
//            ));


        for (int slots = 0; slots < 27; slots++) {
            if (inventory.getItem(slots) == null) inventory.setItem(slots, GUIUtils.createSpacer((byte) 7));
        }

        ((Player) commandSender).openInventory(inventory);

    }

    private StringBuilder getTPS() {
        StringBuilder tpsBuilder = new StringBuilder();
        TpsMonitor tpsMonitor = Anticheat.INSTANCE.getTpsMonitor();

        double tps1Sec = tpsMonitor.tps1Sec();
        double tps5Sec = tpsMonitor.tps5Sec();
        double tps10Sec = tpsMonitor.tps10Sec();
        double tps1Min = tpsMonitor.tps1Min();
        double tps5Min = tpsMonitor.tps5Min();
        double tps15Min = tpsMonitor.tps15Min();

        tpsBuilder.append(tpsMonitor.formatTps(tps1Sec)).append(
                tpsMonitor.formatTps(tps1Sec)).append(ChatColor.GRAY);

        tpsBuilder.append(tpsMonitor.formatTps(tps5Sec)).append(
                tpsMonitor.format(tps5Sec)).append(ChatColor.GRAY).append(", ");

        tpsBuilder.append(tpsMonitor.formatTps(tps10Sec)).append(
                tpsMonitor.format(tps10Sec)).append(ChatColor.GRAY).append(", ");

        tpsBuilder.append(tpsMonitor.formatTps(tps1Min)).append(
                tpsMonitor.format(tps1Min)).append(ChatColor.GRAY).append(", ");

        tpsBuilder.append(tpsMonitor.formatTps(tps5Min)).append(
                tpsMonitor.format(tps5Min)).append(ChatColor.GRAY).append(", ");

        tpsBuilder.append(tpsMonitor.formatTps(tps15Min)).append(
                tpsMonitor.format(tps15Min));

        return tpsBuilder;
    }
}
