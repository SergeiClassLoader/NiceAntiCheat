package pro.cyrent.anticheat.api.check.impl.bedrock.misc;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "GeyserPackets",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.GEYSER_PACKETS,
        description = "Detects if the player sends invalid packets (bedrock only)",
        punishmentVL = 2,
        state = CheckState.RELEASE)
public class GeyserPacketsA extends Check {

    @Override
    public void onPacket(PacketEvent event) {

        if (!getData().isBedrock()) {
            return;
        }

        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (getData().getActionProcessor().isTeleportingV2()
                    || getData().getActionProcessor().isTeleporting()
                    || getData().generalCancel()
                    || getData().getActionProcessor().isTeleportingV3()) {
                return;
            }

            if (Math.abs(flying.getLocation().getPitch()) > 90.0F) {
                this.fail("Impossible Pitch Location",
                        "pitch=" + flying.getLocation().getPitch());
            }
        }
    }
}
