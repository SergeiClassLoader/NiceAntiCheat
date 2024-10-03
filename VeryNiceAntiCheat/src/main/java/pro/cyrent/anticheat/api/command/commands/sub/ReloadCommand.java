package pro.cyrent.anticheat.api.command.commands.sub;


import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class ReloadCommand {

    public void execute(String[] args, String s, CommandSender commandSender) {

        if (commandSender instanceof Player) {
            PlayerData commandS = Anticheat.INSTANCE.getUserManager().getUser((Player) commandSender);

            if (commandS == null) return;


            Anticheat.INSTANCE.getUserManager().getUserMap().values().forEach(user -> {

                    if (user == null) {
                        commandSender.sendMessage("If you see this message contact moose1301");
                        return;
                    }

                if (user.getPlayer().isOp() ||
                        user.getPlayer().hasPermission(Anticheat.INSTANCE.getPermissionValues().getReloadCommand())) {
                    user.getPlayer().sendMessage("\n" + Anticheat.INSTANCE.getConfigValues().getPrefix()
                           + " " + ChatColor.RED + "Reloading may cause errors, or issues!\n");

                    user.getPlayer().sendMessage("\n" + Anticheat.INSTANCE.getConfigValues().getPrefix()
                            + ChatColor.GOLD + " Reloading... \n");

                    Anticheat.INSTANCE.getConfigLoader().load();
                    Anticheat.INSTANCE.getMessageLoader().load();
                    Anticheat.INSTANCE.getPermissionLoader().load();
                    Anticheat.INSTANCE.getConnectionLoader().load();
                    Anticheat.INSTANCE.getChecksLoader().load();

                    Anticheat.INSTANCE.getExecutorService().execute(() -> Anticheat.INSTANCE.getUserManager().getUserMap()
                            .values().forEach(data -> data.getCheckManager().reloadAnticheat()));

                    user.getPlayer().sendMessage("\n" + Anticheat.INSTANCE.getConfigValues().getPrefix()
                            + ChatColor.GREEN + " Reloaded! \n\n");

                    user.getPlayer().sendMessage("\n");
                }
            });
        } else {
            Anticheat.INSTANCE.getUserManager().getUserMap().values().forEach(user -> {

                if (user == null) {
                    commandSender.sendMessage("If you see this message contact moose1301");
                    return;
                }

                commandSender.sendMessage("\n" + Anticheat.INSTANCE.getConfigValues().getPrefix()
                        + " " + ChatColor.RED + "Reloading may cause errors, or issues!\n");

                commandSender.sendMessage("\n" + Anticheat.INSTANCE.getConfigValues().getPrefix()
                        + ChatColor.GOLD + " Reloading... \n");

                Anticheat.INSTANCE.getConfigLoader().load();
                Anticheat.INSTANCE.getMessageLoader().load();
                Anticheat.INSTANCE.getPermissionLoader().load();

                Anticheat.INSTANCE.getExecutorService().execute(() -> Anticheat.INSTANCE.getUserManager().getUserMap()
                        .values().forEach(data -> data.getCheckManager().reloadAnticheat()));

                commandSender.sendMessage("\n" + Anticheat.INSTANCE.getConfigValues().getPrefix()
                        + ChatColor.GREEN + " Reloaded! \n\n");

                commandSender.sendMessage("\n");
            });
        }
    }
}
