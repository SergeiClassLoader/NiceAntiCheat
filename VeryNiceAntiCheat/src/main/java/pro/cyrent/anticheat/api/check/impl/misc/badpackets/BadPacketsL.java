package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "L",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Checks for invalid sneak packets being sent",
        punishmentVL = 20,
        punishable = false,
        state = CheckState.RELEASE)
public class BadPacketsL extends Check {

    private int lastStopSneak, lastLastStopSneak, lastStartSneak = 1000;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {
                this.lastLastStopSneak = this.lastStopSneak;
                this.lastStartSneak++;
                this.lastStopSneak++;

                if (this.lastStartSneak < 2 && this.lastStopSneak > 20 && this.lastLastStopSneak > 20) {

                    if (getData().getMovementProcessor().getSkippedPackets() > 0) {
                        return;
                    }

                    if (getData().generalCancel()) {
                        this.threshold = 0;
                        return;
                    }

                    if (++this.threshold > 4) {
                        this.threshold = 4;
                        this.fail(
                                "startTick="+this.lastStartSneak,
                                "stopTick="+this.lastStopSneak,
                                "lastStopTick="+this.lastLastStopSneak);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .1);
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {

                WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event.getPacketReceiveEvent());

                if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SNEAKING) {
                    this.lastStartSneak = 0;
                }

                if (action.getAction() == WrapperPlayClientEntityAction.Action.STOP_SNEAKING) {
                    this.lastStopSneak = 0;
                }
            }
        }
    }
}
