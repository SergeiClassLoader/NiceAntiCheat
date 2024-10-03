package pro.cyrent.anticheat.api.check.impl.combat.aim;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "Aim",
        subName = "P",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        description = "Detects invalid yaw & pitch gcd changes",
        state = CheckState.PRE_RELEASE)
public class AimP extends Check {

    private double threshold;

    /**
     * Yeah I stole this from vulcan fight me!
     */

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3) {

                if (this.getData().getMovementProcessor().getDeltaXZ() < .085
                        || this.getData().generalCancel()
                        || getData().getSensitivityProcessor().getSensitivityCycles() < 20) {
                    this.threshold -= Math.min(this.threshold, .0125);
                    return;
                }

                final double mcpSensitivity = this.getData().getSensitivityProcessor().getSensitivityValue();

                if (mcpSensitivity < 0.01) {
                    this.threshold -= Math.min(this.threshold, .25);
                    return;
                }

                float f = (float) mcpSensitivity * 0.6f + 0.2f;
                float gcd = f * f * f * 1.2f;
                float yaw = getData().getMovementProcessor().getTo().getYaw();
                float pitch = getData().getMovementProcessor().getTo().getPitch();
                float adjustedYaw = yaw - yaw % gcd;
                float adjustedPitch = pitch - pitch % gcd;
                float yawDifference = Math.abs(yaw - adjustedYaw);
                float pitchDifference = Math.abs(pitch - adjustedPitch);
                float deltaYaw = getData().getMovementProcessor().getDeltaYaw();
                float deltaPitch = getData().getMovementProcessor().getDeltaPitch();
                float combinedChange = deltaYaw + deltaPitch;

                final boolean invalid = (yawDifference == 0.0f || pitchDifference == 0.0f) && combinedChange > 0.75f;

                if (invalid) {

                    if (++this.threshold > 8.0 && getData().getReachDistance() > 1.0) {
                        this.fail("yaw=" + yawDifference,
                                "pitch=" + pitchDifference,
                                "change=" + combinedChange,
                                "distance="+getData().getReachDistance());
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .25);
                }
            }
        }
    }
}