package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "D",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        description = "Detects if a players rotations are rounded",
        punishmentVL = 8,
        state = CheckState.RELEASE)
public class AimD extends Check {


    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            boolean exempt = getData().getActionProcessor().isTeleportingV2();

            if (exempt || getData().generalCancel()) return;

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 4
                    || getData().getScaffoldProcessor().getScaffoldTimerJump().getDelta() < 4) {

                float deltaYaw = getData().getMovementProcessor().getDeltaYawAbs();
                float deltaPitch = getData().getMovementProcessor().getDeltaPitchAbs();

                double roundPitch = preciseRound(deltaPitch, 1);
                double roundYaw = preciseRound(deltaYaw, 1);

                boolean invalid = ((deltaPitch == roundPitch && deltaPitch > 0.001)
                        || (deltaYaw == roundYaw && deltaYaw > 0.001)) && deltaPitch % 1.5F != 0.0F
                        && deltaYaw > 1.0F;

                if (invalid && getData().getSensitivityProcessor().getSensitivity() == -1) {
                    if (++this.threshold > 12.5) {
                        this.fail("deltaPitch=" + deltaPitch,
                                "deltaYaw=" + deltaYaw,
                                "sensitivity="+getData().getSensitivityProcessor().getSensitivity());
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, 0.5);
                }

            }
        }
    }

    public static double preciseRound(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}