package pro.cyrent.anticheat.api.check.impl.combat.killaura;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "KillAura",
        subName = "I",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.KILL_AURA,
        description = "Detects KeepSprint based off predictions",
        punishable = false,
        state = CheckState.PRE_RELEASE)
public class KillAuraI extends Check {

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {

                WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

                if (!flying.hasPositionChanged()) {
                    return;
                }

                boolean isMovement = getData().getMovementProcessor().getDeltaXZ() > .15;

                if (getData().getMovementPredictionProcessor().isCanFlag() && isMovement) {
                    this.fail("movement=" + isMovement);
                }
            }
        }
    }
}
