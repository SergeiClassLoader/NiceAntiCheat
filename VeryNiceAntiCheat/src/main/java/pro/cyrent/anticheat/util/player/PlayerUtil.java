package pro.cyrent.anticheat.util.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.bukkit.entity.Player;

public class PlayerUtil {
    public static void sendPacket(PacketWrapper<?> packet, Player player) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public static void sendPacket(Object packet, Player player) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, (PacketWrapper<?>) packet);
    }

    public static void sendPacketSilently(Object packet, Player player) {
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, (PacketWrapper<?>) packet);
    }
}
