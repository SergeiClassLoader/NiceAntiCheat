package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "K",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishable = false,
        experimental = true,
        description = "Checks if the players sensitivity is invalid constantly",
        state = CheckState.PRE_BETA)
public class AimK extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 4) {

                if (this.getData().getMovementProcessor().getDeltaXZ() < .085
                        || this.getData().generalCancel()
                        || getData().getSensitivityProcessor().getSensitivityCycles() < 300) {
                    return;
                }

                if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3
                        || getData().getScaffoldProcessor().getScaffoldTimerJump().getDelta() < 3) {

                    double deltaYaw = getData().getMovementProcessor().getDeltaYawAbs();
                    double pitchDelta = getData().getMovementProcessor().getDeltaPitchAbs();

                    if (deltaYaw < 1.0 || pitchDelta < 1.0) {
                        this.threshold -= Math.min(this.threshold, 0.08);
                        return;
                    }

                    long sensitivity = getData().getSensitivityProcessor().getSensitivity();

                    if (sensitivity > 200 || sensitivity < 0) {
                        if (++this.threshold > 200) {
                            this.fail("sensitivity="+sensitivity);
                        }
                    } else {
                        this.threshold = 0;
                    }

                }
            }
        }
    }
}