package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "R",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishmentVL = 2,
        description = "Detects snap/smooth like rotations when the player first attacks",
        state = CheckState.PRE_RELEASE)
public class AimR extends Check {

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

                    float yawDelta = Math.abs(getData().getMovementProcessor().getDeltaYaw());
                    float pitchDelta = Math.abs(getData().getMovementProcessor().getDeltaPitch());

                    if (pitchDelta < 0.5 || yawDelta < 0.5) {
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


                    if (offsetX > .5 && offsetY > .5) {
                        if (++this.threshold > .95) {
                            this.fail(
                                    "offsetX="+offsetX,
                                    "offsetY="+offsetY,
                                    "sensitivity="+sensitivity,
                                    "sensitivityValue="+sensitivityValue,
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