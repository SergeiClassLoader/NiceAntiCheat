package pro.cyrent.anticheat.api.check.impl.combat.aim;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "Z",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishable = false,
        experimental = true,
        description = "Checks if the players sensitivity is invalid constantly",
        state = CheckState.BETA)
public class AimZ extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 2) {
                long sensitivity = getData().getSensitivityProcessor().getSensitivity();

                double deltaXZ = getData().getMovementProcessor().getDeltaXZ();

                if (deltaXZ < .12) {
                    return;
                }

                float yawAccel = Math.abs(getData().getMovementProcessor().getYawAccel());
                float deltaYaw = Math.abs(getData().getMovementProcessor().getDeltaYaw());

                if (deltaYaw > 400.0 && yawAccel > 10) {
                    if (++this.threshold > 10.0) {
                        this.fail(
                                "yaw="+deltaYaw,
                                "yawAccel="+yawAccel,
                                "sensitivity="+sensitivity);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .1);
                }
            }
        }
    }
}