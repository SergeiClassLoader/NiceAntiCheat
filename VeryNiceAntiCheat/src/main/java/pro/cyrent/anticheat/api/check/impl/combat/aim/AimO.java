package pro.cyrent.anticheat.api.check.impl.combat.aim;


import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.google.common.collect.Lists;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.util.stream.StreamUtil;

import java.util.List;

@CheckInformation(
        name = "Aim",
        subName = "O",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishmentVL = 8,
        description = "Detects too accurate mouse deltas",
        punishable = false,
        experimental = true,
        state = CheckState.DEV)
public class AimO extends Check {

    private double threshold;
    private float lastDeltaXRound = -420;

    private int lastChange;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasRotationChanged()) return;

            if (this.getData().getMovementProcessor().getDeltaXZ() < .085
                    || this.getData().generalCancel()
                    || getData().getSensitivityProcessor().getSensitivityCycles() < 20) {
                return;
            }

            long sensitivity = getData().getSensitivityProcessor().getSensitivity();

            if (sensitivity == -1) return;

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3) {

                float deltaYawAbs = getData().getMovementProcessor().getDeltaYawAbs();
                float deltaYaw = getData().getMovementProcessor().getDeltaYaw();
                float lastDeltaYaw = getData().getMovementProcessor().getLastDeltaYaw();

                if (deltaYaw > 0.0 && lastDeltaYaw < 0.0
                        || deltaYaw == 0 && lastDeltaYaw > 0
                        || lastDeltaYaw == 0 && deltaYaw > 0
                        || deltaYaw == 0 && lastDeltaYaw < 0
                        || lastDeltaYaw == 0 && deltaYaw < 0
                        || deltaYaw < 0.0 && lastDeltaYaw > 0.0) {
                    this.lastChange = 2;
                }

                if (deltaYawAbs > 1.0F && Math.abs(lastDeltaYaw) > 1.0F && this.lastChange-- > 0) {

                    float f = sensitivity * .6F + .2F;
                    float f1 = f * f * f * 8.0F;

                    float deltaX = deltaYawAbs / 0.15F / f1;
                    float delta = Math.abs(deltaX - this.lastDeltaXRound);

                    if (delta == 0 && this.lastDeltaXRound != -420) {
                        if (++this.threshold > 7.75) {
                            this.fail(
                                    "delta="+delta,
                                    "deltaX="+deltaX);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .02);
                    }

                    this.lastDeltaXRound = deltaX;
                }
            }
        }
    }
}