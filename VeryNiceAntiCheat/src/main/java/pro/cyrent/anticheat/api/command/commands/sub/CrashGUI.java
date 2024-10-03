package pro.cyrent.anticheat.api.command.commands.sub;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.command.commands.util.GUIUtils;
import pro.cyrent.anticheat.api.user.PlayerData;

import java.util.Collections;


public class CrashGUI {

    public void execute(String[] args, String s, CommandSender commandSender) {

        if (!(commandSender instanceof Player)) return;

        PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender));

        if (user == null) {
            return;
        }


        if (args.length <= 1) {
            commandSender.sendMessage(ChatColor.RED + "Invalid arguments, please supply a player.");
            return;
        }
        if (Bukkit.getServer().getPlayer(args[1]) == null) {
            commandSender.sendMessage(ChatColor.RED + "Could not find the player " + ChatColor.GRAY
                    + args[1]);
            return;
        }
        PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(Bukkit.getServer()
                .getPlayer(args[1]));

        if (target == null) {
            commandSender.sendMessage(ChatColor.RED + "Could not find the player " + ChatColor.GRAY
                    + args[1]);
            return;
        }
        if (target.isDev(Bukkit.getServer().getPlayer(args[1]))) {
            commandSender.sendMessage(ChatColor.RED + "Cannot Crash Client!");
            return;
        }

        user.setTargetCrashPlayer(target);
        this.crashGUI(user.getPlayer());
    }

    private void crashGUI(Player player) {
        Inventory inventory = Bukkit.getServer().createInventory(null, 9,
                ChatColor.RED + "Select a crash method.");

        inventory.setItem(0, GUIUtils.generateItem(new ItemStack(Material.TNT, 1),
                ChatColor.GREEN + "Freeze",
                Collections.singletonList(ChatColor.YELLOW + "Freezes the game forever.")));

        inventory.setItem(1, GUIUtils.generateItem(new ItemStack(Material.PACKED_ICE, 1),
                ChatColor.GREEN + "Lag",
                Collections.singletonList(ChatColor.YELLOW + "Kills the FPS")));

        inventory.setItem(2, GUIUtils.generateItem(new ItemStack(Material.BOOK, 1),
                ChatColor.GREEN + "Inventory Crash",
                Collections.singletonList(ChatColor.YELLOW + "Crashes the game when the main inventory is opened.")));

        inventory.setItem(3, GUIUtils.generateItem(new ItemStack(Material.PUMPKIN, 1),
                ChatColor.GREEN + "Demo Spam",
                Collections.singletonList(ChatColor.YELLOW + "Spam's the demo menu")));

        inventory.setItem(4, GUIUtils.generateItem(new ItemStack(Material.SHEARS, 1),
                ChatColor.GREEN + "Cut Connection",
                Collections.singletonList(ChatColor.YELLOW + "Cut's the players connection")));

        inventory.setItem(5, GUIUtils.generateItem(new ItemStack(Material.STICK, 1),
                ChatColor.GREEN + "Magic",
                Collections.singletonList(ChatColor.YELLOW + "Does some magic with packets to crash the client.")));

        inventory.setItem(6, GUIUtils.generateItem(new ItemStack(Material.BARRIER, 1),
                ChatColor.GREEN + "Combo",
                Collections.singletonList(ChatColor.YELLOW + "Combines multiple crash modes into one with an extra one")));

        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = inventory.getItem(i);

            if (itemStack == null) {
                inventory.setItem(i, GUIUtils.createSpacer());
            }
        }

        player.openInventory(inventory);
    }
}
