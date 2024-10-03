package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckInformation(
        name = "BadPackets",
        subName = "Y",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects invalid digging packets",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.BETA)
public class BadPacketsY extends Check {

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
                final WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event.getPacketReceiveEvent());

                final boolean isUsedForDigging = packet.getAction() == DiggingAction.START_DIGGING
                        || packet.getAction() == DiggingAction.FINISHED_DIGGING
                        || packet.getAction() == DiggingAction.CANCELLED_DIGGING;
                if (isUsedForDigging) {
                    return;
                }

                // 1.8 and above clients always send digging packets that aren't used for digging at 0, 0, 0, facing DOWN
                // 1.7 and below clients do the same, except use SOUTH for RELEASE_USE_ITEM
                final BlockFace expectedFace = getData().getProtocolVersion() < 47
                        && packet.getAction() == DiggingAction.RELEASE_USE_ITEM
                        ? BlockFace.SOUTH : BlockFace.DOWN;

                if (packet.getBlockFace() != expectedFace
                        || packet.getBlockPosition().getX() != 0
                        || packet.getBlockPosition().getY() != 0
                        || packet.getBlockPosition().getZ() != 0
                ) {
                    this.fail("packetFace="+packet.getBlockFace().name(),
                            "face="+expectedFace,
                            "posX=" + packet.getBlockPosition().getX(),
                            "posY=" + packet.getBlockPosition().getY(),
                            "posZ=" + packet.getBlockPosition().getZ(),
                            "action="+packet.getAction(),
                            "version="+getData().getProtocolVersion());
                }
            }
        }
    }
}
