package pro.cyrent.anticheat.api.check.impl.combat.aim;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "Aim",
        subName = "X",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishable = false,
        experimental = true,
        description = "Checks if the players sensitivity is invalid constantly",
        state = CheckState.PRE_BETA)
public class AimX extends Check {

    private double threshold;
    private double lastMCPSensitivity;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 4) {

                if (this.getData().getMovementProcessor().getDeltaXZ() < .085
                        || this.getData().generalCancel()
                        || getData().getSensitivityProcessor().getSensitivityCycles() < 25) {
                    return;
                }

                if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 2) {

                    double deltaYaw = getData().getMovementProcessor().getDeltaYawAbs();
                    double pitchDelta = getData().getMovementProcessor().getDeltaPitchAbs();

                    if ((deltaYaw > 0.0 || pitchDelta > 0.0) && deltaYaw < 50.0 && pitchDelta < 50.0) {

                        boolean set = getData().getSensitivityProcessor().isSetSensitivity();

                        double sensitivityMCP = getData().getSensitivityProcessor().getSensitivityValue();

                        if (!getData().getCinematicProcessor().isCinematic()
                                || this.lastMCPSensitivity != sensitivityMCP || set) {
                            this.threshold = 0;
                        }

                        if (set && getData().getCinematicProcessor().isCinematic()) {
                            this.threshold /= Math.min(this.threshold, 2);
                        }

                        if (!set && getData().getSensitivityProcessor().getSensitivity() == -1) {

                            if (this.lastMCPSensitivity == sensitivityMCP
                                    && getData().getCinematicProcessor().isCinematic()) {

                                if (++this.threshold > 70) {
                                    this.fail("sensitivityMCP="+sensitivityMCP,
                                            "lastMCP="+this.lastMCPSensitivity,
                                            "sensitivity="+getData().getSensitivityProcessor().getSensitivity());
                                }
                            } else {
                                this.threshold -= Math.min(this.threshold, 0.005);
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, .125);
                        }

                        this.lastMCPSensitivity = sensitivityMCP;
                    }
                }
            }
        }
    }
}
