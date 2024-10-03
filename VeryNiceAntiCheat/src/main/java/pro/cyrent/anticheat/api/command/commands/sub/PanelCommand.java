package pro.cyrent.anticheat.api.command.commands.sub;


import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.auth.HTTPUtil;

import java.util.HashMap;
import java.util.Map;

public class PanelCommand {

    public void execute(String[] args, String s, CommandSender commandSender) {
        PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(((Player) commandSender).getUniqueId());
        if (!(user != null && user.isDev(((Player) commandSender).getPlayer()))) {
            return;
        }

        if (args.length > 1) {
            String mode = args[1];

            boolean foundMode = false;

            if (mode.equals("expireAll")) {
                foundMode = true;

                commandSender.sendMessage(ChatColor.GRAY + "Expiring all panel sessions...");

                Anticheat.INSTANCE.getExecutorService().execute(() -> {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("XOR", "81cc8734-5266-40e1-9931-b44dc4006c25");
                    headers.put("license", Anticheat.INSTANCE.getLicense());

                    HTTPUtil.getResponse("https://panel.antiskid.club/service/destroy", headers);
                    headers.clear();

                    commandSender.sendMessage(ChatColor.GREEN + "All sessions should be expired.");
                });
            }

            if (!foundMode) {
                commandSender.sendMessage(ChatColor.RED
                        + "Modes: expireAll, or just type nothing to get a panel link.");
            }

            return;
        }

        commandSender.sendMessage(ChatColor.GRAY + "Generating panel link...");

        Anticheat.INSTANCE.getExecutorService().execute(() -> {
            Map<String, String> headers = new HashMap<>();

            headers.put("xor", "498b588e-1956-45c3-b1e9-ad9a2c3f1beb");
            headers.put("license", Anticheat.INSTANCE.getLicense());
            headers.put("username", user.getUsername());

            String result = HTTPUtil.getResponse("https://panel.antiskid.club/service/create", headers);

            if (result != null && result.contains("-")) {
                TextComponent textComponent = new TextComponent(ChatColor.GREEN
                        + "You can access the panel by clicking here ");

                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                        "https://panel.antiskid.club/panel/login?uid=" + result));

                user.getPlayer().spigot().sendMessage(textComponent);
            } else {
                commandSender.sendMessage(ChatColor.RED + "Unable to contact customer panel, try again later.");
            }

            headers.clear();
        });
    }
}