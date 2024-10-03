package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "BadPackets",
        subName = "D1",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if a player attempts to attack while sending digging packets",
        punishable = false,
        state = CheckState.PRE_BETA)
public class BadPacketsD1 extends Check {

    private double threshold;
    private boolean sent;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
                WrapperPlayClientPlayerDigging digging =
                        new WrapperPlayClientPlayerDigging(event.getPacketReceiveEvent());

                if (digging.getAction() == DiggingAction.START_DIGGING
                        || digging.getAction() == DiggingAction.FINISHED_DIGGING
                        || digging.getAction() == DiggingAction.CANCELLED_DIGGING) {
                    this.sent = true;
                }
            } else if (event.isMovement()) {
                this.sent = false;
            } else if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                if (this.sent && !getData().generalCancel()) {
                    if (++this.threshold > 10) {
                        this.fail("Mining a block while attacking?");
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .005);
                }
            }
        }
    }
}
