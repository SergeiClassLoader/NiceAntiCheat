package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "G",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if the player sends vehicle steering's with no vehicle",
        state = CheckState.RELEASE)
public class BadPacketsG extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            PacketTypeCommon packetType = event.getPacketReceiveEvent().getPacketType();

            if (packetType == PacketType.Play.Client.STEER_VEHICLE) {

                if (getData().generalCancel()) {
                    this.threshold = 0;
                    return;
                }

                if (getData().getPlayer().isInsideVehicle()
                        || getData().getCollisionProcessor().getNearBoatTicks() > 0) {
                    this.threshold = 0;
                    return;
                }

                if (++this.threshold > 40) {
                    this.fail("Sending steer vehicle packets while not in a vehicle");
                }
            }
        }
    }
}

