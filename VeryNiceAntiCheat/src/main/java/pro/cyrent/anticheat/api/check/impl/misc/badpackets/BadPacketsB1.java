package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "BadPackets",
        subName = "B1",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if the player sends a digging packet & placement packet",
        punishmentVL = 2,
        state = CheckState.RELEASE)
public class BadPacketsB1 extends Check {

    private boolean sentPlacement;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
                WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event.getPacketReceiveEvent());

                boolean exempt = getData().generalCancel() || getData().getProtocolVersion() > 47;

                if (digging.getAction() == DiggingAction.RELEASE_USE_ITEM && this.sentPlacement && !exempt) {
                    if (++this.threshold > 10) {
                        this.fail("Sending a block placement & digging packet simultaneously (NoSlow?)");
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, 1);
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
                this.sentPlacement = true;
            }

            if (event.isMovement()) {
                this.sentPlacement = false;
            }
        }
    }
}
