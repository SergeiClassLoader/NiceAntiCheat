package pro.cyrent.anticheat.api.check.impl.combat.aim;


import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "Q",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishmentVL = 8,
        description = "Detects very small changes in the players pitch",
        state = CheckState.PRE_BETA)
public class AimQ extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasPositionChanged() || getData().getActionProcessor().getTeleportTicks() <= 10) {
                this.threshold -= Math.min(this.threshold, 0.05);
                return;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 4) {

                if (this.getData().getMovementProcessor().getDeltaXZ() < .085
                        || this.getData().generalCancel()) {
                    this.threshold -= Math.min(this.threshold, .05);
                    return;
                }

                float yawDelta = Math.abs(getData().getMovementProcessor().getDeltaYaw());
                float lastDeltaYaw = Math.abs(getData().getMovementProcessor().getLastDeltaYaw());
                float pitchDelta = Math.abs(getData().getMovementProcessor().getDeltaPitch());
                float lastDeltaPitch = Math.abs(getData().getMovementProcessor().getLastDeltaPitch());

                boolean invalidPitch = pitchDelta < 0.001 && pitchDelta > 0
                        && lastDeltaPitch < 0.001 && lastDeltaPitch > 0
                        || pitchDelta == lastDeltaPitch && pitchDelta > 0.0;

                if (yawDelta > 2.5F && lastDeltaYaw > 2.5 && invalidPitch) {
                    if (++this.threshold > 10.0) {
                        this.fail(
                                "pitchDelta="+pitchDelta,
                                "yawDelta="+yawDelta);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, 1);
                }
            }
        }
    }
}