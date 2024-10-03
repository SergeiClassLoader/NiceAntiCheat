package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.math.Verbose;

@CheckInformation(
        name = "Aim",
        subName = "W",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        description = "Detects if a players rotations has odd rounding",
        punishmentVL = 8,
        state = CheckState.PRE_BETA)
public class AimW extends Check {

    private final Verbose threshold = new Verbose();

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            boolean exempt = getData().getActionProcessor().isTeleportingV2();

            if (exempt || getData().generalCancel()) return;

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 4
                    || getData().getScaffoldProcessor().getScaffoldTimerJump().getDelta() < 4) {

                float deltaYaw = getData().getMovementProcessor().getDeltaYawAbs();
                float deltaPitch = getData().getMovementProcessor().getDeltaPitchAbs();


                double roundPitch = preciseRound(deltaPitch, 2);
                double roundYaw = preciseRound(deltaYaw, 2);

                boolean invalidRound = Math.abs(roundPitch - deltaPitch) < 1E-5
                        || Math.abs(roundYaw - deltaYaw) < 1E-5;

                boolean invalid = deltaPitch % 1.5F != 0.0F
                        && deltaYaw > 1.0F && invalidRound;

                if (getData().getSensitivityProcessor().getSensitivityValue() % 0.5F == 0.0
                        && getData().getSensitivityProcessor().getSensitivityValue() != -1
                        || getData().getSensitivityProcessor().getSensitivityValue() % 0.5F == 0.0
                        && getData().getSensitivityProcessor().getSensitivity() != -1) {
                    this.threshold.setVerbose(0);
                    return;
                }

                if (invalid) {

                    if (this.threshold.flag(25, 750L)) {
                        this.fail("deltaPitch=" + deltaPitch,
                                "deltaYaw=" + deltaYaw,
                                "sensitivity=" + getData().getSensitivityProcessor().getSensitivity());

                    }
                }
            }
        }
    }

    public static double preciseRound(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}