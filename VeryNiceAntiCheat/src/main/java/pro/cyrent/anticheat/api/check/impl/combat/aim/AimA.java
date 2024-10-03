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
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        description = "Detects if a player aim movements are consistent (pitch)",
        state = CheckState.RELEASE)
public class AimA extends Check {

    private double threshold;
    private final Deque<Float> pitchChanges = new LinkedList<>();

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasRotationChanged()) {
                return;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 200) {
                this.pitchChanges.clear();
                return;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3) {

                float pitchDelta = getData().getMovementProcessor().getDeltaPitchAbs();

                float deltaYaw = getData().getMovementProcessor().getDeltaYawAbs();

                if (deltaYaw > 1.0F && !getData().getCinematicProcessor().isCinematic()) {
                    this.pitchChanges.add(pitchDelta);

                    if (this.pitchChanges.size() >= 80) {
                        this.pitchChanges.removeFirst();

                        double min = pitchChanges.stream()
                                .mapToDouble(Float::doubleValue)
                                .min()
                                .orElse(0);

                        double max = pitchChanges.stream()
                                .mapToDouble(Float::doubleValue)
                                .max()
                                .orElse(0);

                        double std = StreamUtil.getStandardDeviation(this.pitchChanges);

                        double deltaMinMax = Math.abs(min - max);

                        if (std < .03 && deltaMinMax < .1) {
                            if (++this.threshold > 3) {
                                this.fail(
                                        "std="+std,
                                        "delta="+deltaMinMax,
                                        "sensitivity="+getData().getSensitivityProcessor().getSensitivity());
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, 0.5);
                        }
                    }
                }
            }
        }
    }
}