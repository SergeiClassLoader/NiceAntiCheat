package pro.cyrent.anticheat.api.command.commands.sub;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.command.commands.util.GUIUtils;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.auth.HTTPUtil;
import pro.cyrent.anticheat.util.json.JSONArray;
import pro.cyrent.anticheat.util.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TopViolationsCommand {

    public void execute(String[] args, String s, CommandSender commandSender) {

        if (!(commandSender instanceof Player)) return;

        PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender));
        if (user != null) {
            if (args.length == 2 && args[1].equalsIgnoreCase("session")) {
                this.contactDatabaseTopSession(user);
                return;
            }
            this.contactDatabaseTopTotal(user);
        }
    }

    public static void openGUI(Player player, List<LogsCommand.WebResultTop> results, boolean onlineOnly) {

        PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(player);

        if (user == null) {
            return;
        }
        results.sort((o1, o2) -> Integer.compare(o2.getSize(), o1.getSize()));

        user.results = results;

        if (results.isEmpty()) {
            player.sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                    + " No suspected cheaters found.");

            return;
        }


        int totalPages = (int) Math.ceil((double) results.size() / user.pageSize);

        if (totalPages == 0) {
            player.sendMessage(ChatColor.RED + "No suspected cheaters found.");
            return;
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, 54,
                ChatColor.RED + "Top Player Violations");


        ItemStack back = new ItemStack(Material.LEVER);
        ItemMeta backItemMeta = back.getItemMeta();
        backItemMeta.setDisplayName(ChatColor.RED + "Back");
        back.setItemMeta(backItemMeta);

        ItemStack grayDye = new ItemStack(onlineOnly ? Material.EMERALD : Material.REDSTONE);
        ItemMeta grayDyeMeta = grayDye.getItemMeta();
        grayDyeMeta.setDisplayName(onlineOnly ? ChatColor.GREEN + "Show all players"
                : ChatColor.GREEN + "Show online players only");
        grayDye.setItemMeta(grayDyeMeta);


        ItemStack redstone = new ItemStack(Material.REDSTONE_TORCH_ON);
        ItemMeta redstoneMeta = redstone.getItemMeta();
        redstoneMeta.setDisplayName(ChatColor.GREEN + "Next Page");
        redstone.setItemMeta(redstoneMeta);

        inventory.setItem(45, back);
        inventory.setItem(49, grayDye);
        inventory.setItem(53, redstone);

        int startIndex = user.currentPage * user.pageSize;
        int endIndex = Math.min((user.currentPage + 1) * user.pageSize, results.size());

        List<LogsCommand.WebResultTop> onlinePlayers = new ArrayList<>();

        if (user.isOnlineOnly()) {
            for (LogsCommand.WebResultTop result : results) {
                Player player2 = Bukkit.getPlayer(result.getPlayerName());

                if (player2 != null && player2.isOnline()) {
                    onlinePlayers.add(result);
                }
            }

            // Populate the inventory with online players for the current page
            int inventoryIndex = 0; // Track the index in the inventory
            for (int i = startIndex; i < Math.min(startIndex + user.pageSize, onlinePlayers.size()); i++) {
                LogsCommand.WebResultTop result = onlinePlayers.get(i);
                inventory.setItem(inventoryIndex, GUIUtils.createPlayerSkullOnline(result.getPlayerName(),
                        result.getUuid(), result.getSize()));
                inventoryIndex++; // Move to the next slot
            }
        } else {
            // Populate the inventory with all players for the current page
            for (int i = startIndex; i < endIndex; i++) {
                LogsCommand.WebResultTop result = results.get(i);
                inventory.setItem(i - startIndex, GUIUtils.createPlayerSkull(result.getPlayerName(),
                        result.getUuid(), result.getSize()));
            }
        }

        player.openInventory(inventory);

    }

    private void contactDatabaseTopTotal(PlayerData player) {
        Anticheat.INSTANCE.getExecutorService().execute(() -> {
            List<LogsCommand.WebResultTop> webResults = new ArrayList<>();
            Map<String, String> headers = new HashMap<>();
            headers.put("LumosKey", Anticheat.INSTANCE.getLicense());


            String result = HTTPUtil.getResponse("https://backend.antiskid.club/top", headers);
            if (result == null) {
                player.getPlayer().sendMessage(ChatColor.RED + "There was an error contacting the logs database.");
                return;
            }
            JSONArray array = new JSONArray(result);
            for (Object element : array) {
                JSONObject object = (JSONObject) element;

                webResults.add(new LogsCommand.WebResultTop(
                        null, null, 0, false,
                        object.getString("uuid"), object.getString("playerName"),
                        object.getInt("count")
                ));
            }

            openGUI(player.getPlayer(), webResults, false);
        });

    }

    private void contactDatabaseTopSession(PlayerData player) {
        Anticheat.INSTANCE.getExecutorService().execute(() -> {
            List<LogsCommand.WebResultTop> webResults = new ArrayList<>();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                PlayerData data = Anticheat.INSTANCE.getUserManager().getUser(onlinePlayer);
                if (data == null ||  data.getSessionLogs().isEmpty()) continue;
                webResults.add(new LogsCommand.WebResultTop(
                        null, null, 0, false,
                        data.getUuid().toString(), data.getUsername(),
                        data.getSessionLogs().size()
                ));
            }

            openGUI(player.getPlayer(), webResults, false);
        });

    }
}
