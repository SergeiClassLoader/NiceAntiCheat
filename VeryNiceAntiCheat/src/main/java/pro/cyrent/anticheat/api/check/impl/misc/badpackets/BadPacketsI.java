package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "I",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Patches Old Stitch Client (makes old stitch flag every check)",
        punishmentVL = 1,
        state = CheckState.RELEASE)
public class BadPacketsI extends Check {

    private int coolDown;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {

                if (this.coolDown-- == 1) {
                    getData().getTransactionProcessor().sendPacket(new WrapperPlayServerKeepAlive(-723743),
                            getData().getPlayer());

                    getData().getBadPacketTimer().reset();
                } else {
                    WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

                    if (!flying.isOnGround() && getData().getBadPacketTimer().hasNotPassed(5)) {
                        this.coolDown = 5;
                    }
                }
            }
        }
    }
}
