package pro.cyrent.anticheat.api.command.commands.sub;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.auth.HTTPUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Moose1301
 * @date 8/21/2024
 */
public class LookupCommand {
    public void execute(String[] args, String s, CommandSender commandSender) {

        if (!(commandSender instanceof Player)) {
            return;
        }

        if (args.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "/" + Anticheat.INSTANCE.getConfigValues().getCommandName() +
                    " lookup (player)");
            return;
        }

        String playerName = args[1];

        if (playerName == null) return;

        if (playerName.length() < 1) {
            commandSender.sendMessage(ChatColor.RED + "Please specify a players name!");
            return;
        }

        PlayerData playerData = Anticheat.INSTANCE.getUserManager().getUser((Player) commandSender);

        if (playerData == null) return;

        if (Anticheat.INSTANCE.getConfigValues().isOwnMongo()) {
            commandSender.sendMessage(ChatColor.RED + "Not supported");
        } else {
            getPlayerBan((Player) commandSender, playerName);
        }


    }

    private void getPlayerBan(Player player, String playerName) {
        Bukkit.getScheduler().runTaskAsynchronously(Anticheat.INSTANCE.getPlugin(), () -> {
            Map<String, String> headers = new HashMap<>();
            headers.put("LumosKey", Anticheat.INSTANCE.getLicense());
            headers.put("mode", "PLAYER");
            headers.put("name", playerName);

            String information = HTTPUtil.getResponse("https://backend.antiskid.club/service/ban", headers);
            if (information == null || information.equalsIgnoreCase("NULL")) {
                player.sendMessage(ChatColor.RED + playerName + " has no logged bans.");
                return;
            }
            player.sendMessage(ChatColor.GREEN + "Found punishment info: " + ChatColor.GOLD + information);
        });
    }
}