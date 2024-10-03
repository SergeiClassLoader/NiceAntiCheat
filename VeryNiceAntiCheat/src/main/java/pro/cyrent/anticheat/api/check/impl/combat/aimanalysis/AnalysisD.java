package pro.cyrent.anticheat.api.check.impl.combat.aimanalysis;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "Analysis",
        subName = "D",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM_ANALYSIS,
        punishable = false,
        experimental = true,
        enabled = false,
        description = "Experimental aim pattern detection",
        state = CheckState.ALPHA)
public class AnalysisD extends Check {

    private double threshold, buffer;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 4) {

                if (this.getData().getMovementProcessor().getDeltaXZ() < .085
                        || this.getData().generalCancel()
                        || getData().getSensitivityProcessor().getSensitivityCycles() < 50) {
                    return;
                }

                if (getData().isBedrock()) return;

                // Previous check falsed so i replaced it with a non falsing check.

                if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3) {
                    long sensitivity = getData().getSensitivityProcessor().getSensitivity();

                    if (sensitivity == -1) {
                        if (++this.threshold > 50) {
                            this.threshold = 25;
                            this.fail("sensitivity-Collected="+sensitivity);
                        }
                    } else {
                        this.threshold = 0;
                    }
                }
            }
        }
    }
}