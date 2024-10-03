package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if the player sets their pitch to impossible values",
        punishmentVL = 2,
        state = CheckState.RELEASE)
public class BadPacketsA extends Check {

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (getData().getActionProcessor().isTeleportingV2()
                    || getData().getActionProcessor().isTeleporting()
                    || getData().generalCancel()
                    || getData().isBedrock()
                    || getData().getActionProcessor().isTeleportingV3()) {
                return;
            }

            if (Math.abs(flying.getLocation().getPitch()) > 90.0F) {
                this.fail("pitch="+flying.getLocation().getPitch());
            }
        }
    }
}
