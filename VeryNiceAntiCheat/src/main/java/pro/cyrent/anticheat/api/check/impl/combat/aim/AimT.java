package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

import java.util.Deque;
import java.util.LinkedList;

@CheckInformation(
        name = "Aim",
        subName = "T",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishable = false,
        experimental = true,
        description = "Finds invalid distance ranges using the players sampled pitch",
        state = CheckState.BETA)
public class AimT extends Check {

    private double threshold;
    private double lastDeltaPitch;


    private final Deque<Double> pitchCounts = new LinkedList<>();
    private final Deque<Double> pitchCompressed = new LinkedList<>();

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasRotationChanged()) {
                return;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 4) {

                if (this.getData().getMovementProcessor().getDeltaXZ() < .085
                        || getData().getCinematicProcessor().isCinematic()
                        || getData().getSensitivityProcessor().sensitivityCycles < 50
                        || this.getData().generalCancel()) {
                    return;
                }

                if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 2) {

                    double pitchAccel = getData().getMovementProcessor().getPitchAccel();
                    double compressedAccel = Math.abs(pitchAccel - this.lastDeltaPitch);

                    float pitch = getData().getMovementProcessor().getTo().getPitch();

                    this.pitchCounts.add((double) pitch);
                    this.pitchCompressed.add(compressedAccel);

                    if (this.pitchCounts.size() + this.pitchCompressed.size() >= 80) {

                        double maxPitch = Math.abs(StreamUtil.getMaximumDouble(this.pitchCounts));
                        double minPitch = Math.abs(StreamUtil.getMinimumDouble(this.pitchCounts));

                        double range = Math.abs(maxPitch - minPitch);

                        if (range < 1.0D && maxPitch < 3.0 && minPitch < 3.0) {
                            if (++this.threshold > 4.75) {
                                this.fail("range="+range,
                                        "min="+minPitch,
                                        "max="+maxPitch);
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, .125);
                        }

                        this.pitchCounts.clear();
                        this.pitchCompressed.clear();
                    }

                    this.lastDeltaPitch = pitchAccel;
                }
            }
        }
    }
}