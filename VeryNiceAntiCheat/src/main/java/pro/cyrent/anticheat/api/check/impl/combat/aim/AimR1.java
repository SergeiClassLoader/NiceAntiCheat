package pro.cyrent.anticheat.api.check.impl.combat.aim;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "R1",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishable = false,
        experimental = true,
        description = "Checks if the players sensitivity is invalid constantly",
        state = CheckState.DEV)
public class AimR1 extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 4) {

                if (this.getData().getMovementProcessor().getDeltaXZ() < .085
                        || this.getData().generalCancel()
                        || getData().getSensitivityProcessor().getSensitivityCycles() < 150) {
                    this.threshold = 0;
                    return;
                }

                if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3) {

                    float sensitivityValue = getData().getSensitivityProcessor().getSensitivityValue();
                    long sensitivity = getData().getSensitivityProcessor().getSensitivity();

                    if (sensitivityValue == -1L || sensitivity == -1L) {
                        this.threshold = 0;
                        return;
                    }

                    if (sensitivity > 190) {
                        return;
                    }

                    float yawDelta = Math.abs(getData().getMovementProcessor().getDeltaYaw());
                    float pitchDelta = Math.abs(getData().getMovementProcessor().getDeltaPitch());

                    if (pitchDelta > 3.0 || yawDelta > 3.0) {
                        return;
                    }

                    if (pitchDelta < 0.2 || yawDelta < 0.2) {
                        return;
                    }

                    float f = sensitivityValue * .6F + .2F;
                    float f1 = f * f * f * 1.2F;


                    float deltaYaw = getData().getMovementProcessor().getDeltaYaw();
                    float deltaPitch = getData().getMovementProcessor().getDeltaPitch();

                    float deltaX = deltaYaw / f1;
                    float deltaY = deltaPitch / f1;

                    float offsetX = Math.abs(Math.round(deltaX) - deltaX);
                    float offsetY = Math.abs(Math.round(deltaY) - deltaY);

                    double maxMovement = Math.max(yawDelta, pitchDelta);

                    double dpiCalculation = (maxMovement / sensitivity) * 1000;


                    if (offsetX > .2 && offsetY > .2 && dpiCalculation < 100) {

                        if (++this.threshold > 2) {
                            this.fail(
                                    "offsetX="+offsetX,
                                    "offsetY="+offsetY,
                                    "sensitivity="+sensitivity,
                                    "sensitivityValue="+sensitivityValue,
                                    "dpiCalc="+dpiCalculation,
                                    "deltaYaw="+yawDelta,
                                    "deltaPitch="+pitchDelta,
                                    "threshold="+this.threshold);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .025);
                    }
                }
            }
        }
    }
}
