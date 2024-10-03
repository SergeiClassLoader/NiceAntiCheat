package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import org.bukkit.GameMode;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "V",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if a player sends settings/window clicks with other packets",
        punishmentVL = 3,
        experimental = true,
        state = CheckState.BETA)
public class BadPacketsV extends Check {

    private double threshold;
    private boolean sent;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {

            if (event.isMovement()) {
                this.sent = false;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLIENT_SETTINGS
                    || event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
                this.sent = true;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ANIMATION
                    || event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_DIGGING
                    || event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT
                    || event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.TAB_COMPLETE
                    || event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {

                if (getData().getProtocolVersion() > 47 && getData().getPlayer().getGameMode() == GameMode.SPECTATOR) {
                    return;
                }

                if (this.sent) {
                    if (++this.threshold > 2.5) {
                        this.fail("packet=" + event.getPacketReceiveEvent().getPacketType().getName());
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .075);
                }
            }
        }
    }
}
