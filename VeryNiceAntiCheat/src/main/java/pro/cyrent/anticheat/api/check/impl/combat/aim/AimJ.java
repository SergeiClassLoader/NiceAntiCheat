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
        subName = "J",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        experimental = true,
        punishable = false,
        description = "Detects if a player aim movements are consistent (yaw)",
        state = CheckState.PRE_RELEASE)
public class AimJ extends Check {

    private double threshold;
    private final Deque<Float> yawChanges = new LinkedList<>();

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasRotationChanged()) {
                return;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 40) {
                this.yawChanges.clear();
                return;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3) {

                float yawAbs = getData().getMovementProcessor().getDeltaYawAbs();
                float lastYaw = getData().getMovementProcessor().getLastDeltaYawAbs();

                if (yawAbs > 1.0F && lastYaw > 1.0F) {
                    this.yawChanges.add(yawAbs);

                    if (this.yawChanges.size() >= 80) {
                        this.yawChanges.removeFirst();

                        double min = yawChanges.stream()
                                .mapToDouble(Float::doubleValue)
                                .min()
                                .orElse(0);

                        double max = yawChanges.stream()
                                .mapToDouble(Float::doubleValue)
                                .max()
                                .orElse(0);

                        double std = StreamUtil.getStandardDeviation(this.yawChanges);

                        double deltaMinMax = Math.abs(min - max);

                        if (std < .12 && deltaMinMax < .12) {
                            if (++this.threshold > 6) {
                                this.fail(
                                        "std="+std,
                                        "delta="+deltaMinMax);
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, 0.1);
                        }
                    }
                }
            }
        }
    }
}
