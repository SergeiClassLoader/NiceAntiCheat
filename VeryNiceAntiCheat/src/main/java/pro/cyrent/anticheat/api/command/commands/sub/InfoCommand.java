package pro.cyrent.anticheat.api.command.commands.sub;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.command.commands.util.PlayerInformationGUI;
import pro.cyrent.anticheat.api.user.PlayerData;

public class InfoCommand {

    public void execute(String[] args, String s, CommandSender commandSender) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Not allowed in console!");
            return;
        }
        PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender));


        try {
            if (user == null) {
                commandSender.sendMessage("If you see this message contact moose1301");
                return;
            }
            if (args.length < 2) {
                commandSender.sendMessage(ChatColor.RED + "Usage: /" + Anticheat.INSTANCE.getConfigValues().getCommandName()
                        + " info (player)");
                return;

            }
            String targetName = args[1];

            if (targetName.length() < 0) {
                commandSender.sendMessage("Please enter a valid username.");
                return;
            }
            PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(Bukkit.getPlayer(args[1]));
            if (target == null) {
                commandSender.sendMessage(ChatColor.RED + "User not found,  " +
                        "please try another name.");
                return;
            }
            new Thread(() -> new PlayerInformationGUI()
                    .openGUI((Player) commandSender, target)).start();


        } catch (NullPointerException ignored) {
        }

    }
}
