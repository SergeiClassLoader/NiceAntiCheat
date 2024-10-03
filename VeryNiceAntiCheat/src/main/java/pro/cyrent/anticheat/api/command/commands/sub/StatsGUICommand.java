package pro.cyrent.anticheat.api.command.commands.sub;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.command.commands.util.GUIUtils;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.auth.HTTPUtil;
import pro.cyrent.anticheat.util.stats.StatsUtil;
import pro.cyrent.anticheat.util.tps.TpsMonitor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class StatsGUICommand {

    public void execute(String[] args, String s, CommandSender commandSender) {

        if (!(commandSender instanceof Player)) return;

        PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender));
        Bukkit.getScheduler().runTaskAsynchronously(Anticheat.INSTANCE.getPlugin(), () -> {
            Map<String, String> headers = new HashMap<>();
            headers.put("LumosKey", Anticheat.INSTANCE.getLicense());
            headers.put("mode", "COUNT");

            int count = Integer.parseInt(HTTPUtil.getResponse("https://backend.antiskid.club/service/ban", headers));
            Bukkit.getScheduler().runTask(Anticheat.INSTANCE.getPlugin(), () -> {
                if (user != null) {
                    this.openGUI(user.getPlayer(), count);
                }
            });
        });

    }

    private void openGUI(Player player, int count) {
        Inventory inventory = Bukkit.getServer().createInventory(null, 9,
                ChatColor.RED + "Lumos Statistics");

        inventory.setItem(1, GUIUtils.generateItem(new ItemStack(Material.SKULL_ITEM, 1),
                ChatColor.AQUA + "Player Statistics",
                Arrays.asList(
                        ChatColor.GRAY + "Users Loaded: " + ChatColor.LIGHT_PURPLE + Anticheat.INSTANCE
                                .getUserManager().getUserMap().size(),
                        ChatColor.GRAY + "Overall Players Loaded: " + ChatColor.GREEN + StatsUtil.overallPlayersJoined,
                        "",
                        ChatColor.GRAY + "Amount Of Players Kicked: " + ChatColor.RED + StatsUtil.kickAmount,
                        ChatColor.GRAY + "Amount Of Players Banned: " + ChatColor.DARK_RED + StatsUtil.banAmount,
                        ChatColor.GRAY + "Amount Of Total Players Banned: " + ChatColor.DARK_RED + count,
                        ChatColor.GRAY + "Amount Of Player Sent Flags: " + ChatColor.AQUA + StatsUtil.flagAmount,
                        "",
                        ChatColor.GRAY + "Most Banned For Check: " +
                                (!StatsUtil.checkFriendlyNameBans.isEmpty() ? ChatColor.GREEN + StatsUtil
                                        .getMostFrequentStringName(StatsUtil.checkFriendlyNameBans) : ChatColor.RED + "None"),
                        ChatColor.GRAY + "Most Kicked For Check: " +
                                (!StatsUtil.checkFriendlyNameKicks.isEmpty() ? ChatColor.GREEN + StatsUtil
                                        .getMostFrequentStringName(StatsUtil.checkFriendlyNameKicks) : ChatColor.RED + "None")
                )));

        inventory.setItem(3, GUIUtils.generateItem(new ItemStack(Material.COMPASS, 1),
                ChatColor.AQUA + "Server Statistics",
                Arrays.asList(ChatColor.GRAY + "Server Lag: " + (Anticheat.INSTANCE.isServerLagging()
                                || Anticheat.INSTANCE.getLastServerLagTick() > 0
                                ? ChatColor.RED + "Yes" : ChatColor.GREEN + "No"),
                        ChatColor.GRAY + "TPS: (1s, 5s, 10s, 1m, 5m, 15m)",
                        ChatColor.RESET + this.getTPS().toString(),
                        ChatColor.GRAY + "Last Server Lag Tick: "
                                + ChatColor.RED + Anticheat.INSTANCE.getLastServerTick(),
                        ChatColor.GRAY + "Current Date: " + ChatColor.AQUA + Anticheat.INSTANCE.currentDate)));

        inventory.setItem(5, GUIUtils.generateItem(new ItemStack(Material.REDSTONE, 1),
                ChatColor.AQUA + "Thread Statistics",
                Arrays.asList(
                        ChatColor.GRAY + "Player Thread(s): "
                                + ChatColor.GREEN + Anticheat.INSTANCE
                                .getThreadManager().getUserThreads().size() + ChatColor.DARK_GRAY + "/" +
                                ChatColor.RED + Anticheat.INSTANCE.getThreadManager().getMaxThreads(),
                        ChatColor.GRAY + "Available Server Core(s): "
                                + ChatColor.AQUA + Anticheat.INSTANCE
                                .getThreadManager().getThreads())));

        inventory.setItem(7, GUIUtils.generateItem(new ItemStack(Material.IRON_SWORD, 1),
                ChatColor.AQUA + "Anticheat Information",
                Arrays.asList(
                        ChatColor.GRAY + "Main anti-cheat command: " + ChatColor.AQUA
                                + "/" + Anticheat.INSTANCE.getConfigValues().getCommandName(),
                        ChatColor.GRAY + "Prefix: " + ChatColor.AQUA + Anticheat.INSTANCE.getConfigValues().getPrefix(),
                        ChatColor.GRAY + "Version: " + ChatColor.AQUA + Anticheat.INSTANCE.getVersion())));

        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = inventory.getItem(i);

            if (itemStack == null) {
                inventory.setItem(i, GUIUtils.createSpacer());
            }
        }

        player.openInventory(inventory);
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
