package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "U",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if a players sends spectate packets without being in spectator",
        punishmentVL = 3,
        state = CheckState.RELEASE)
public class BadPacketsU extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.SPECTATE) {
                if (getData().generalCancel()) {
                    this.threshold = 0;
                    return;
                }

                if (++this.threshold > 2) {
                    this.fail("threshold=" + threshold);
                }
            }
        }
    }
}
