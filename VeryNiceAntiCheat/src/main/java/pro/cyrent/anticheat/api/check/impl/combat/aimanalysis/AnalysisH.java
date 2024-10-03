package pro.cyrent.anticheat.api.check.impl.combat.aimanalysis;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInformation(
        name = "Analysis",
        subName = "H",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM_ANALYSIS,
        description = "Finds consistent pitch ranges when entering combat",
        experimental = true,
        state = CheckState.PRE_BETA)
public class AnalysisH extends Check {

    private double threshold;
    private int timeSinceLastStable;

    private double stablePitchValue;

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

            if (this.timeSinceLastStable-- < 1) {

                if (deltaYaw > 0.2D && deltaPitch == 0.0D && Math.abs(pitch) == Math.abs(previousPitch)) {

                    if (pitch == this.stablePitchValue) {
                        if (++this.threshold > 4.0) {
                            this.fail(
                                    "deltaYaw="+deltaYaw,
                                    "deltaPitch="+deltaPitch,
                                    "pitch="+pitch,
                                    "stable-pitch="+this.stablePitchValue);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .01);
                    }

                    this.timeSinceLastStable = 20;
                    this.stablePitchValue = pitch;
                }
            }
        }
    }
}