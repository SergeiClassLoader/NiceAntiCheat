package pro.cyrent.anticheat.api.check.impl.combat.aimanalysis;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.api.check.data.ReachData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.evicting.EvictingList;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInformation(
        name = "Analysis",
        subName = "J",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM_ANALYSIS,
        description = "Detects aim patterns without enough vertical modifications",
        experimental = true,
        state = CheckState.ALPHA)
public class AnalysisJ extends Check {

    private double threshold, distance = 0;
    private int invalid;

    private final EvictingList<Integer> invalidPitchDataSet = new EvictingList<>(20);


    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasRotationChanged()
                    || getData().generalCancel()
                    || getData().getCinematicProcessor().isCinematic()
                    || getData().getMovementProcessor().getDeltaXZ() < 0.085
                    || getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 3) {
                this.threshold -= Math.min(this.threshold, .01);
                return;
            }

            FlyingLocation to = getData().getMovementProcessor().getTo();
            FlyingLocation from = getData().getMovementProcessor().getFrom();

            float pitch = to.getPitch();
            float previousPitch = from.getPitch();

            float deltaYaw = getData().getMovementProcessor().getDeltaYawAbs();
            float lastDeltaYaw = getData().getMovementProcessor().getLastDeltaYawAbs();

            float deltaPitch = getData().getMovementProcessor().getDeltaPitchAbs();
            float lastDeltaPitch = getData().getMovementProcessor().getLastDeltaPitchAbs();

            boolean pitchMoved = deltaPitch != 0;

            boolean equalDelta = deltaPitch == lastDeltaPitch;
            boolean equalPitch = pitch == previousPitch;
            boolean invalid = equalPitch && equalDelta && !pitchMoved;

            // This check is designed to get aim assists when players don't move their pitch, but they do their yaw.
            if (invalid && deltaYaw > 0.1D && lastDeltaYaw > 0.1D) {
                this.invalid++;
            } else {
                this.invalid = 0;
            }

            if (this.distance > 2.0) {
                this.invalidPitchDataSet.add(this.invalid);
            }

            if (this.invalidPitchDataSet.isFull()) {
                double avg = StreamUtil.getAverage(this.invalidPitchDataSet);
                int max = StreamUtil.getMaximumInt(this.invalidPitchDataSet);

                if (avg > 2 && max > 6) {
                    if (++this.threshold > 7.5) {
                        this.fail(
                                "avg="+avg,
                                "max="+max,
                                "invalid="+this.invalid,
                                "deltaYaw="+deltaYaw,
                                "deltaPitch="+deltaPitch,
                                "lastDeltaPitch="+lastDeltaPitch,
                                "pitch="+pitch,
                                "lastPitch="+previousPitch);

                        this.threshold *= .5;
                        this.invalidPitchDataSet.clear();
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .5);
                }
            }
        }
    }

    @Override
    public void onReach(ReachData reachData) {
        if (reachData != null && reachData.isAttack() && reachData.isValidHitbox()) {
            this.distance = reachData.getDistance();
        }
    }
}