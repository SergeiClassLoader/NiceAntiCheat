package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "S",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if a player attacks while in the settings menu",
        punishmentVL = 1,
        state = CheckState.RELEASE)
public class BadPacketsS extends Check {

    private boolean settingSent = false;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLIENT_SETTINGS) {
                this.settingSent = true;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {

                if (getData().generalCancel()) {
                    this.settingSent = false;
                    return;
                }

                if (this.settingSent) {
                    this.fail("sent=" + settingSent);
                }
            }

            if (event.isMovement()) {
                this.settingSent = false;
            }
        }
    }
}
