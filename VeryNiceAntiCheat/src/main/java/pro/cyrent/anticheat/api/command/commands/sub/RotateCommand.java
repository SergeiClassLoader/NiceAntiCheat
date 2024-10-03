package pro.cyrent.anticheat.api.command.commands.sub;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;

/**
 * @author Moose1301
 * @date 7/10/2024
 */
public class RotateCommand {
    public void execute(String[] args, String s, CommandSender commandSender) {
        PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender).getUniqueId());
        if (user == null) {
            return;
        }
        if (args.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "Invalid arugments, please supply a player.");
            return;
        }
        if (Bukkit.getServer().getPlayer(args[1]) == null) {
            commandSender.sendMessage(ChatColor.RED + "Could not find the player " + ChatColor.YELLOW
                    + args[1]);
            return;
        }

        PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(Bukkit.getServer()
                .getPlayer(args[1]).getUniqueId());

        if (target == null) {
            commandSender.sendMessage(ChatColor.RED + "Could not find the player " + ChatColor.YELLOW
                    + args[1]);
            return;
        }
        commandSender.sendMessage(ChatColor.GREEN + "Rotating " + ChatColor.YELLOW
                + target.getUsername());
        Location location = target.getPlayer().getLocation().clone();
        location.setYaw(location.getYaw() + 180);
        target.getPlayer().teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);


    }

}
