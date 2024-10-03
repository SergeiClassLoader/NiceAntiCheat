package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "C",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if the player places blocks with impossible data in the packet",
        punishmentVL = 3,
        state = CheckState.RELEASE)
public class BadPacketsC extends Check {

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            PacketTypeCommon packetType = event.getPacketReceiveEvent().getPacketType();

            if (packetType == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
                WrapperPlayClientPlayerBlockPlacement placement =
                        new WrapperPlayClientPlayerBlockPlacement(event.getPacketReceiveEvent());

                if (placement.getCursorPosition() != null && !getData().isBedrock()) {

                    boolean invalid = placement.getCursorPosition().x > 1.0
                            || placement.getCursorPosition().y > 1.0
                            || placement.getCursorPosition().z > 1.0
                            || placement.getCursorPosition().x < 0.0
                            || placement.getCursorPosition().y < 0.0
                            || placement.getCursorPosition().z < 0.0;

                    if (invalid) {
                        this.fail("posX="+placement.getCursorPosition().x,
                                "posY="+placement.getCursorPosition().y,
                                "posZ="+placement.getCursorPosition().z);
                    }
                }
            }
        }
    }
}
