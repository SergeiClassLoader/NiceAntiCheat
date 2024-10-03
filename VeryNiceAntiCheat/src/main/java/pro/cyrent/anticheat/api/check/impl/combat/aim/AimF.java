package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "F",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        description = "Detects if the players aim doesn't follow proper GCD",
        punishmentVL = 12,
        state = CheckState.RELEASE)
public class AimF extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            boolean exempt = getData().getActionProcessor().isTeleportingV2();

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 20
                    || getData().getScaffoldProcessor().getScaffoldTimerJump().getDelta() < 20) {


                if (getData().getCinematicProcessor().isCinematic()) {
                    this.threshold -= Math.min(this.threshold, .085);
                }

                if (getData().getCinematicProcessor().getTicks() > 0) {
                    this.threshold -= Math.min(this.threshold, 1.5);
                    return;
                }

                if (exempt || getData().generalCancel() || !flying.hasRotationChanged()) return;

                double deltaYaw = getData().getMovementProcessor().getDeltaYawAbs();
                double deltaPitch = getData().getMovementProcessor().getDeltaPitchAbs();

                double pitchGCD = Math.abs(getData().getSensitivityProcessor().getPitchGCD());

                double maxSpeed = 10.0;

                if (getData().getSensitivityProcessor().getSensitivity() < 50
                        && getData().getSensitivityProcessor().getSensitivity() >= 0) {
                    maxSpeed = 1.35;
                }

                if (Math.abs(deltaPitch) > 0.03f && Math.abs(deltaYaw) > 1.0f
                        && Math.abs(deltaPitch) <= maxSpeed
                        && (getData().getSensitivityProcessor().getSensitivity() < 0
                        || getData().getSensitivityProcessor().getSensitivity() > 200)) {

                    if (pitchGCD <= 131072L && pitchGCD > 0L) {

                        if (getData().getCinematicProcessor().isSpecialGCD()) {
                            this.threshold -= Math.min(this.threshold, 1);
                            return;
                        }

                        if (++this.threshold > 20) {
                            this.fail("pitchGCD=" + pitchGCD,
                                    "Sensitivity=" + getData().getSensitivityProcessor().getSensitivity(),
                                    "deltaYaw=" + deltaYaw,
                                    "deltaPitch=" + deltaPitch,
                                    "cinematic=" + getData().getCinematicProcessor().isCinematic(),
                                    "specialGCD=" + getData().getCinematicProcessor().isSpecialGCD());
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .07);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, 0.07);
                }
            }
        }
    }
}
