package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "Q",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if a player is spamming block breaks (nuker)",
        punishmentVL = 20,
        punishable = false,
        state = CheckState.BETA)
public class BadPacketsQ extends Check {

    private double threshold;
    private int amount;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {

                if (getData().generalCancel()
                        || getData().getMovementProcessor().getSkippedPackets() > 0
                        || getData().getTransactionProcessor().getTransactionPing() > 700
                        || getData().getTransactionProcessor().getTransactionPingDrop() > 200
                        || getData().getMovementProcessor().getLastFlyingPauseTimer().getDelta() < 10
                        || getData().isBedrock()) {
                    this.amount = 0;
                    this.threshold = 0;
                    return;
                }

                WrapperPlayClientPlayerDigging digging =
                        new WrapperPlayClientPlayerDigging(event.getPacketReceiveEvent());

                if (digging.getBlockPosition() != null) {

                    if (digging.getBlockPosition().getX() != 0
                            && digging.getBlockPosition().getY() != 0
                            && digging.getBlockPosition().getZ() != 0) {

                        if (this.amount >= 2) {
                            if (++this.threshold > 40) {
                                this.fail("amount="+this.amount);
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, 2);
                        }

                        this.amount++;
                    }
                }
            }

            if (event.getPacketReceiveEvent().getPacketType()
                    == PacketType.Play.Client.WINDOW_CONFIRMATION || event.isMovement()) {
                this.amount = 0;
            }
        }
    }
}
