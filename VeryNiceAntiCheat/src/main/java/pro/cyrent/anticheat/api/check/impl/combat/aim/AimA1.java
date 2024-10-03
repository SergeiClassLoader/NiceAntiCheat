package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.math.MathUtil;
import pro.cyrent.anticheat.util.math.Verbose;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInformation(
        name = "Aim",
        subName = "A1",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishmentVL = 8,
        description = "Detects invalid differences in the players mouse delta",
        punishable = false,
        state = CheckState.DEV)
public class AimA1 extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasRotationChanged() || getData().generalCancel()
                    || getData().getActionProcessor().isTeleportingV2()) return;

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3
                    || getData().getScaffoldProcessor().getScaffoldTimerJump().getDelta() < 3) {

                float deltaPitch = this.getData().getMovementProcessor().getDeltaPitch();
                float lastDeltaPitch = this.getData().getMovementProcessor().getLastDeltaPitch();
                float deltaYaw = this.getData().getMovementProcessor().getDeltaYaw();

                long expandedPitch = (long)(getData().getSensitivityProcessor().getGcdOffset() * deltaPitch);
                long expandedLastPitch = (long)(getData().getSensitivityProcessor().getGcdOffset() * lastDeltaPitch);

                boolean cinematic = getData().getSensitivityProcessor().isCinematic();
                long gcd = MathUtil.gcd(expandedPitch, expandedLastPitch);

                boolean tooLowSensitivity = getData().getSensitivityProcessor().getSensitivity() < 50
                        && getData().getSensitivityProcessor().getSensitivity() >= 0;

                boolean validAngles = deltaYaw > 0.25f && deltaPitch > 0.25f && deltaPitch < 20.0f && deltaYaw < 20.0f;
                boolean invalid = !cinematic && gcd < 131072L;

                if (invalid && validAngles && !tooLowSensitivity) {
                    if (++this.threshold > 7.0) {
                        this.fail("gcd=" + gcd,
                                "deltaPitch=" + deltaPitch,
                                "deltaYaw=" + deltaYaw,
                                "sensitivity="+getData().getSensitivityProcessor().getSensitivity(),
                                "threshold="+this.threshold);
                    }
                }
                else {
                    this.threshold -= Math.min(this.threshold, .05);
                }
            }
        }
    }
}
