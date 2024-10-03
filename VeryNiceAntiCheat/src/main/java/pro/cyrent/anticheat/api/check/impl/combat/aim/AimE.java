package pro.cyrent.anticheat.api.check.impl.combat.aim;


import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.util.stream.StreamUtil;

import java.util.Deque;
import java.util.LinkedList;

@CheckInformation(
        name = "Aim",
        subName = "E",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishable = false,
        description = "Checks aggressive quick/highly randomized rotations",
        state = CheckState.BETA)
public class AimE extends Check {

    private double threshold;
    private final Deque<Float> pitchChanges = new LinkedList<>();
    private final Deque<Float> yawChanges = new LinkedList<>();

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasRotationChanged()) {
                return;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 200 || this.getData().getMovementProcessor().getDeltaXZ() < .085) {
                this.pitchChanges.clear();
                return;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3) {

                float pitchDelta = getData().getMovementProcessor().getDeltaPitchAbs();
                float deltaYaw = getData().getMovementProcessor().getDeltaYawAbs();

                if (this.yawChanges.size() + this.pitchChanges.size() >= 150) {

                    double yawDev = StreamUtil.getStandardDeviation(this.yawChanges);
                    double pitchDev = StreamUtil.getStandardDeviation(this.pitchChanges);

                    double pitchAvg = StreamUtil.getStandardDeviation(this.pitchChanges);
                    double yawAvg = StreamUtil.getAverage(this.yawChanges);

                    if (yawDev > 50.0 || pitchDev > 50.0 || yawAvg > 50.0 || pitchAvg > 50.0) {
                        if (++this.threshold > 3) {
                            this.fail("yawDev="+yawDev,
                                    "yawAvg="+yawAvg,
                                    "pitchDev="+pitchDev,
                                    "pitchAvg="+pitchAvg);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .5);
                    }

                    this.yawChanges.clear();
                    this.pitchChanges.clear();
                }

                if (deltaYaw > 0) {
                    this.yawChanges.add(deltaYaw);
                }

                if (pitchDelta > 0) {
                    this.pitchChanges.add(pitchDelta);
                }
            }
        }
    }
}