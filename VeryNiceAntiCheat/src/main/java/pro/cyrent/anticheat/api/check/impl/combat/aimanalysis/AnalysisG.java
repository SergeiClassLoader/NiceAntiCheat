package pro.cyrent.anticheat.api.check.impl.combat.aimanalysis;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInformation(
        name = "Analysis",
        subName = "G",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM_ANALYSIS,
        description = "Finds consistent pitch movements based on previous stored aim data.",
        experimental = true,
        state = CheckState.PRE_BETA)
public class AnalysisG extends Check {

    private double threshold;
    private int invalid;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasRotationChanged()
                    || getData().generalCancel()
                    || getData().getMovementProcessor().getDeltaXZ() < 0.085
                    || getData().getCinematicProcessor().isCinematic()
                    || getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 3) {
                this.threshold -= Math.min(this.threshold, .01);
                return;
            }

            FlyingLocation to = getData().getMovementProcessor().getTo();
            FlyingLocation from = getData().getMovementProcessor().getFrom();

            float pitch = to.getPitch();
            float previousPitch = from.getPitch();

            float deltaYaw = getData().getMovementProcessor().getDeltaYawAbs();
            float deltaPitch = getData().getMovementProcessor().getDeltaPitchAbs();

            boolean invalidPreviousPitch = (previousPitch < -1.45 && previousPitch > -1.47 || previousPitch > 1.45
                    && previousPitch < 1.47);

            if (invalidPreviousPitch) {
                if (deltaPitch > .01F && deltaYaw > 0.01F) {
                    this.invalid++;

                    if (this.invalid > 300) {
                        this.threshold = 0;
                    }
                } else {
                    this.invalid -= Math.min(this.invalid, 1);
                }
            } else {
                if (this.invalid >= 10 && deltaYaw > 1.0F & deltaPitch == 0.0F && pitch == previousPitch) {
                    if (++this.threshold > 3.0) {
                        this.fail(
                                "deltaYaw="+deltaYaw,
                                "deltaPitch="+deltaPitch,
                                "pitch="+pitch,
                                "previousPitch="+previousPitch,
                                "invalid="+this.invalid,
                                "threshold="+this.threshold);
                    }

                    this.invalid = 0;
                } else {
                    this.threshold -= Math.min(this.threshold, .01);
                }
            }
        }
    }
}