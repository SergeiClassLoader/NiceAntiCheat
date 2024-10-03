package pro.cyrent.anticheat.api.command.commands.util;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.Check;
import pro.cyrent.anticheat.api.user.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class GUIUtils {

    public static final Map<String, SkullMeta> cache = new HashMap<>();

    public static ItemStack generateItem(ItemStack itemStack, String itemName, List<String> meta) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(meta);
        itemMeta.setDisplayName(itemName);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }


    public static ItemStack createSpacer() {
        ItemStack i = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 15);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(" ");
        i.setItemMeta(im);
        return i;
    }

    public static ItemStack createPlayerSkull(String playerName, String uuid, int size) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

        boolean actuallyOn = false;

        SkullMeta skullMeta = getCachedSkullMeta(playerName);

        if (skullMeta == null) {
            skullMeta = (SkullMeta) Bukkit.getServer().getItemFactory().getItemMeta(Material.SKULL_ITEM);
            skullMeta.setOwner(playerName);
            cacheSkullMeta(playerName, skullMeta);
        }

        skullMeta.setDisplayName(ChatColor.AQUA + playerName);

        String online = ChatColor.GRAY + "Connection Status: ";

        Player player = Bukkit.getPlayer(playerName);

        WeakHashMap<Check, Double> checks = new WeakHashMap<>();

        if (player != null) {
            PlayerData playerData = Anticheat.INSTANCE.getUserManager().getUser(player);

            if (playerData != null) {
                online += ChatColor.GREEN + "Online " + ChatColor.GRAY + "for " + ChatColor.GREEN + Anticheat.INSTANCE.timePlayer(playerData.getLoginMilis());

                actuallyOn = true;
            } else {
                online += ChatColor.RED + "Offline";
            }
        } else {
            online += ChatColor.RED + "Offline";
        }

        List<String> currentLore = new ArrayList<>(Arrays.asList(
                ChatColor.GRAY + "Total Violations: " + ChatColor.RED + size,
                online,
                "",
                ChatColor.GRAY + ChatColor.ITALIC.toString() + "Left click " + ChatColor.GRAY + "to dump logs paste",
                ChatColor.GRAY + ChatColor.ITALIC.toString() + "Middle click " + ChatColor.GRAY + "to remove the player from list"));


        currentLore.add("");
        currentLore.add(ChatColor.DARK_GRAY + uuid);

        //set the lore.
        skullMeta.setLore(currentLore);

        skull.setItemMeta(skullMeta);

        return skull;
    }

    public static ItemStack createPlayerSkullOnline(String playerName, String uuid, int size) {

        boolean actuallyOn = false;

        SkullMeta skullMeta = getCachedSkullMeta(playerName);

        if (skullMeta == null) {
            skullMeta = (SkullMeta) Bukkit.getServer().getItemFactory().getItemMeta(Material.SKULL_ITEM);
            skullMeta.setOwner(playerName);
            cacheSkullMeta(playerName, skullMeta);
        }

        skullMeta.setDisplayName(ChatColor.AQUA + playerName);

        String online = ChatColor.GRAY + "Connection Status: ";

        Player player = Bukkit.getPlayer(playerName);

        if (player != null) {
            PlayerData playerData = Anticheat.INSTANCE.getUserManager().getUser(player);

            if (playerData != null) {
                online += ChatColor.GREEN + "Online " + ChatColor.GRAY + "for " + ChatColor.GREEN + Anticheat.INSTANCE.timePlayer(playerData.getLoginMilis());

                actuallyOn = true;
            } else {
                online += ChatColor.RED + "Offline";
            }
        } else {
            online += ChatColor.RED + "Offline";
        }

        if (actuallyOn) {
            List<String> currentLore = new ArrayList<>(Arrays.asList(
                    ChatColor.GRAY + "Total Violations: " + ChatColor.RED + size,
                    online,
                    "",
                    ChatColor.GRAY + ChatColor.ITALIC.toString() + "Left click " + ChatColor.GRAY + "to dump logs paste",
                    ChatColor.GRAY + ChatColor.ITALIC.toString() + "Right click " + ChatColor.GRAY + "to teleport",
                    ChatColor.GRAY + ChatColor.ITALIC.toString() + "Middle click " + ChatColor.GRAY + "to remove the player from list"));



            currentLore.add("");
            currentLore.add(ChatColor.DARK_GRAY + uuid);

            //set the lore.
            skullMeta.setLore(currentLore);

            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

            skull.setItemMeta(skullMeta);

            return skull;
        }


        return new ItemStack(Material.AIR);
    }

    public static ItemStack createSpacer(byte color) {
        ItemStack i = new ItemStack(Material.STAINED_GLASS_PANE, 1, color);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(" ");
        i.setItemMeta(im);
        return i;
    }

    public static SkullMeta getCachedSkullMeta(String playerName) {
        return cache.get(playerName);
    }

    public static void cacheSkullMeta(String playerName, SkullMeta skullMeta) {
        cache.put(playerName, skullMeta);
    }
}
