package pro.cyrent.anticheat.util.version.support;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.version.IVersion;
import com.viaversion.viaversion.api.Via;
import org.bukkit.ChatColor;

public class ViaVersionSupport implements IVersion {

    @Override
    public int getClientVersion(PlayerData user) {
        return Via.getAPI().getPlayerVersion(user.getUuid());
    }

    @Override
    public void onLoad() {
        Anticheat.INSTANCE.getPlugin().getServer().getConsoleSender().sendMessage(
                Anticheat.INSTANCE.anticheatNameColor + ChatColor.GREEN + "Hooking into ViaVersion!");
    }
}
