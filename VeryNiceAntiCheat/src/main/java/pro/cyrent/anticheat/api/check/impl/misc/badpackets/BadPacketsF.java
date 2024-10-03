package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerAbilities;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "F",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if the player sends flight/creative abilities in survival",
        punishmentVL = 1,
        state = CheckState.RELEASE)
public class BadPacketsF extends Check {

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            PacketTypeCommon packetType = event.getPacketReceiveEvent().getPacketType();

            if (packetType == PacketType.Play.Client.PLAYER_ABILITIES) {

                if (getData().getMovementProcessor().getLastFlightTimer().getDelta() < 40
                        + getData().getTransactionProcessor().getPingTicks()
                        && getData().getMovementProcessor().getLastFlightTimer().isSet()
                        || getData().generalCancel()) {
                    return;
                }

                this.fail();
            }
        }
    }
}
