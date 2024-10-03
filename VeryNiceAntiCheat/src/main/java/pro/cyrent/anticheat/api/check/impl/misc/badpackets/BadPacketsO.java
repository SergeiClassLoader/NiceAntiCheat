package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "O",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Player is sending block packets while sending switch slot packets",
        punishmentVL = 20,
        punishable = false,
        state = CheckState.BETA)
public class BadPacketsO extends Check {

    private boolean placing;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {

            if (event.isMovement()) {
                this.placing = false;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {

                if (getData().generalCancel()
                        || getData().isBedrock()
                        || Anticheat.INSTANCE.getVersionSupport().getClientProtocol(getData()) != 47) {
                    this.threshold = 0;
                    return;
                }

                if (this.placing) {
                    if (++this.threshold > 3) {
                        this.fail();
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, 1);
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
                this.placing = true;
            }
        }
    }
}
