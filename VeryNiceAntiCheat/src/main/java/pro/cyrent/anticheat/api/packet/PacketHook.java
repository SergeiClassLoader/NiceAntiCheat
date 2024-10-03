package pro.cyrent.anticheat.api.packet;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import org.bukkit.entity.Player;

public class PacketHook extends PacketListenerAbstract {
    public PacketHook() {
        super(PacketListenerPriority.LOWEST);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(player);

            if (user != null) {
                user.handlePacketEvent(PacketEvent.Direction.SERVER, event.getPacketType(),
                        null, event, event, false);

            }
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {

        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(player);

            if (user != null) {
                user.handlePacketEvent(PacketEvent.Direction.CLIENT, event.getPacketType(), event,
                        null, event, false);
            }
        }
    }
}
