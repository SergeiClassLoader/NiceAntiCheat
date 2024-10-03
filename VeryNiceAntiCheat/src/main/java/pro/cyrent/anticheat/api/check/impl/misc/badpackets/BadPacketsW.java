package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "W",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if a player places a block, but their hand doesn't animate",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.BETA)
public class BadPacketsW extends Check {

    private int lastSwing;
    private int lastPlacement;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {

            if (event.isMovement()) {
                this.lastPlacement++;
                this.lastSwing++;

                if (getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() < 3) {
                    this.lastPlacement = 0;
                }

                if (this.lastPlacement < 3 && this.lastSwing > 20) {
                    if (++this.threshold > 10.0) {
                        this.fail("threshold=" + threshold);
                    }
                } else {
                    this.threshold = 0;
                }
            }


            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ANIMATION) {
                this.lastSwing = 0;
            }
        }
    }
}