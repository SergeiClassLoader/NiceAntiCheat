package pro.cyrent.anticheat.api.check.impl.combat.aimanalysis;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "Analysis",
        subName = "C",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM_ANALYSIS,
        punishmentVL = 20,
        description = "Detects and finds divisor offsets in the players aim",
        state = CheckState.RELEASE)
public class AnalysisC extends Check {

    private double threshold;
    private float previousDivisor;
    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 4) {

                if (this.getData().getMovementProcessor().getDeltaXZ() < .085) {
                    return;
                }

                int sensitivity = (int) getData().getSensitivityProcessor().getSensitivity();

                final float divisorYaw = getData().getSensitivityProcessor().getYawGCD();

                final float f = sensitivity / 200.0f * 0.6f + 0.2f;
                final float gcdTable = (float) (f * f * f * 8.0F * .15D);
                float divisor = gcdTable / divisorYaw;
                float deltaDivisor = Math.abs(divisor - this.previousDivisor);

                if (deltaDivisor > 1E-7) {
                    ++this.threshold;

                    if (this.threshold > 150) {
                        this.threshold = 0;

                        this.fail("deltaDivisor="+deltaDivisor);
                    }
                } else {
                    if (deltaDivisor != 0) {
                        this.threshold = 0;
                    }
                }

                this.previousDivisor = divisor;
            }
        }
    }
}