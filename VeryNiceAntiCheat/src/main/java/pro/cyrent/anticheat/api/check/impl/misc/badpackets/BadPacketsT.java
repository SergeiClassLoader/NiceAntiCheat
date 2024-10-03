package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "T",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if a players sending perfect jumps with a horse instantly",
        punishmentVL = 3,
        state = CheckState.RELEASE)
public class BadPacketsT extends Check {

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
                WrapperPlayClientEntityAction entityAction = new WrapperPlayClientEntityAction(event.getPacketReceiveEvent());

                if (entityAction.getAction() == WrapperPlayClientEntityAction.Action.START_JUMPING_WITH_HORSE) {
                    if (entityAction.getJumpBoost() < 0 || entityAction.getJumpBoost() > 100) {
                        this.fail("boost="+entityAction.getJumpBoost());
                    }
                }
            }
        }
    }
}
