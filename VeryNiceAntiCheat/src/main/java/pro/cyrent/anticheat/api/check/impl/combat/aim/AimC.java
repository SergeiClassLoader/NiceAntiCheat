package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "C",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        description = "Detects if a players rotation modulo contains 0 only",
        punishmentVL = 8,
        state = CheckState.PRE_RELEASE)
public class AimC extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            boolean exempt = getData().getActionProcessor().isTeleportingV2();

            if (exempt || getData().generalCancel()) return;

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 3) return;

            float constant = getData().getSensitivityProcessor().getSensitivityValue() / 142.0F;

            float pitch = getData().getMovementProcessor().getTo().getPitch();
            float yaw = getData().getMovementProcessor().getTo().getYaw();

            float moduloPitch = Math.abs(pitch % constant);
            float moduloYaw = Math.abs(yaw % constant);

            if (moduloPitch == 0.0D && moduloYaw == 0.0D) {
                if (this.threshold++ > 30) {
                    this.fail("moduloPitch=" + moduloPitch,
                            "moduloYaw="+moduloYaw,
                            "sonstant="+constant);
                }
            } else {
                this.threshold -= Math.min(this.threshold, 0.05);
            }
        }
    }
}
