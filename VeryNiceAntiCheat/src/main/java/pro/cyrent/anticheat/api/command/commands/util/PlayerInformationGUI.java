package pro.cyrent.anticheat.api.command.commands.util;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class PlayerInformationGUI {
    private final int maxSlots = 9 * 3;
    private final Map<UUID, SkullMeta> skullMetaMap = new WeakHashMap<>();

    private final List<Integer> firstColorSlots = Arrays.asList(
            17,
            26,
            25,
            24,
            23,
            22,
            21,
            20,
            19,
            18
    );

    private final List<Integer> secondColorSlots = Arrays.asList(
            9,
            0,
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8
    );

    public void openGUI(Player viewer, PlayerData target) {
        String playerName = target.getPlayer().getName();
        String guiName = "Lumos | Client » " + (playerName.length() <= 16 ? playerName : "");

        Inventory inventory = Bukkit.getServer().createInventory(null,
                maxSlots, ChatColor.RED + guiName);

        for (int i = 0; i < maxSlots; i++) {
            inventory.setItem(i, GUIUtils.createSpacer((byte) 7));
        }

        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

        //Using caching, so we don't bother to mojang API everytime for the same player
        SkullMeta skullMeta = (this.skullMetaMap.containsKey(target.getPlayer().getUniqueId()) ?
                this.skullMetaMap.get(target.getPlayer().getUniqueId())
                : (SkullMeta) skull.getItemMeta());

        if (!this.skullMetaMap.containsKey(target.getPlayer().getUniqueId())) {

            skullMeta.setOwner(target.getPlayer().getName());

            skullMeta.setDisplayName(ChatColor.AQUA + target.getPlayer().getName());
            this.skullMetaMap.put(target.getPlayer().getUniqueId(), skullMeta);
        }

        skull.setItemMeta(skullMeta);
        inventory.setItem(13, skull);

        int connectionDrop = (int) target.getTransactionProcessor().getTransactionPingDrop();
        int transactionPing = (int) target.getTransactionProcessor().getTransactionPing();

        inventory.setItem(11, GUIUtils.generateItem(new ItemStack(Material.ENCHANTED_BOOK, 1),
                ChatColor.AQUA + "User Info",

                Arrays.asList(ChatColor.GRAY + "User Version: " + ChatColor.GREEN
                                + Anticheat.INSTANCE.getVersionSupport().getClientVersion(target).name()
                                + " - " + target.getProtocolVersion(),
                        "",

                        ChatColor.GRAY + "User Sensitivity: " + ChatColor.GREEN + target
                                .getSensitivityProcessor().getSensitivity() + "%",

                        "",

                        ChatColor.GRAY + "Client Channel: " + ChatColor.GREEN + target
                                .getCustomPayloadProcessor().getChannel(),

                        ChatColor.GRAY + "Client Brand: " + ChatColor.GREEN + target
                                .getCustomPayloadProcessor().getBrand(),

                        ChatColor.GRAY + "Client Brand Type: " + ChatColor.GREEN + target
                                .getCustomPayloadProcessor().getCurrentBrandType().name(),

                        "",

                        ChatColor.GRAY + "Login Date & Time: "
                                + ChatColor.GREEN + target.getLoginTime(),

                        ChatColor.GRAY + "Connected for: "
                                + ChatColor.GREEN + Anticheat.INSTANCE.timePlayer(target.getLoginMilis()))

        ));

        inventory.setItem(15, GUIUtils.generateItem(new ItemStack(Material.REDSTONE_TORCH_ON, 1),
                ChatColor.AQUA + "Connection Information",
                Arrays.asList(ChatColor.GRAY + "KeepAlive Ping: " + ChatColor.GREEN
                                + target.getTransactionProcessor().getKeepAlivePing()
                                + ChatColor.GRAY + "ms",

                        ChatColor.GRAY + "Transaction Ping: " + ChatColor.GREEN + transactionPing
                                + ChatColor.GRAY + "ms",

                        ChatColor.GRAY + "Ping Difference: "
                                + ChatColor.GREEN + Math.abs(target
                                .getTransactionProcessor().getKeepAlivePing()
                                - target.getTransactionProcessor().getTransactionPing()),

                        ChatColor.GRAY + "Queued Pre Packets: " + ChatColor.GREEN
                                + target.getTransactionProcessor().getTransactionQueue().size(),

                        ChatColor.GRAY + "Queued Keep-Alive Packets: " + ChatColor.GREEN +
                                target.getTransactionProcessor().getKeepAliveQueue().size(),

                        ChatColor.GRAY + "Packet Drop: " + this.getColorToPing(connectionDrop) + connectionDrop,

                        ChatColor.GRAY + "Transaction Ping Ticks: "
                                + ChatColor.LIGHT_PURPLE +
                                (target.getTransactionProcessor().getPingTicks()))));

        this.fillColors(inventory);
        viewer.openInventory(inventory);
    }

    private ChatColor getColorToPing(int connectionDrop) {
        return (connectionDrop > 30 ? (connectionDrop > 60 ? ChatColor.RED : ChatColor.YELLOW) : ChatColor.GREEN);
    }

    private void fillColors(Inventory inventory) {


        ItemStack firstColor = GUIUtils.createSpacer((byte) 3);
        ItemStack secondColor = GUIUtils.createSpacer((byte) 4);

        this.firstColorSlots.forEach(integer -> inventory.setItem(integer, firstColor));
        this.secondColorSlots.forEach(integer -> inventory.setItem(integer, secondColor));

     /*   this.firstColorSlots.forEach(integer -> {

            int randomNumber = generateRandomNumberExcluding(0, 15, new int[]{7, 8, 15});

            ItemStack firstColor = GUIUtils.createSpacer((byte) randomNumber);

            inventory.setItem(integer, firstColor);
        });*/
    }

    public static int generateRandomNumberExcluding(int min, int max, int[] exclusions) {
        Random random = new Random();

        int randomNumber;
        do {
            randomNumber = random.nextInt(max - min + 1) + min;
        } while (contains(exclusions, randomNumber));

        return randomNumber;
    }

    public static boolean contains(int[] array, int value) {
        for (int item : array) {
            if (item == value) {
                return true;
            }
        }
        return false;
    }
}
