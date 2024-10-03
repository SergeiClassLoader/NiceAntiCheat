package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.api.check.data.ReachData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "B",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishable = false,
        experimental = true,
        enabled = false,
        description = "Detects if a players head is snapping while attacking",
        state = CheckState.RELEASE)
public class AimB extends Check {

    private double threshold;

    @Override
    public void onReach(ReachData event) {

        if (event.isValidHitbox() && event.isAttack()) {

            if (getData().getMovementProcessor().getDeltaXZ() < 0.08) {
                return;
            }

            float deltaYaw = getData().getMovementProcessor().getDeltaYawAbs();

            boolean exempt = getData().getActionProcessor().isTeleportingV3()
                    || getData().getLastTeleport().getDelta() < 20
                    || getData().generalCancel()
                    || getData().getHorizontalProcessor().getVelocitySimulator().getLastVelocitySimulatedTimer().getDelta() < 20;

            if (exempt) return;

            float pitch = getData().getMovementProcessor().getDeltaPitchAbs();
            float lastDeltaPitch = getData().getMovementProcessor().getLastDeltaPitchAbs();

            if (event.getDistance() >= 2.25) {

                if (deltaYaw > 30.0F && pitch < 1.0F && lastDeltaPitch < 1.0F) {
                    if (++this.threshold > 7.5) {
                    this.fail(
                            "yaw="+Math.abs(deltaYaw),
                            "lastYaw="+Math.abs(getData().getMovementProcessor().getLastDeltaYaw()),
                            "sensitivity="+getData().getSensitivityProcessor().getSensitivity());
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .005);
                }
            } else {
                this.threshold -= Math.min(this.threshold, .00725);
            }
        }
    }
}