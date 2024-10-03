package pro.cyrent.anticheat.api.check.impl.combat.aimanalysis;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.impl.processor.connection.tracker.NewTrackedEntity;
import pro.cyrent.anticheat.util.evicting.EvictingList;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.math.MathUtil;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

import java.util.ArrayList;
import java.util.List;

@CheckInformation(
        name = "Analysis",
        subName = "I",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM_ANALYSIS,
        description = "Detects aim patterns that follow specific hit-points",
        state = CheckState.PRE_BETA)
public class AnalysisI extends Check {

    private double threshold;
    private long lastFail = System.currentTimeMillis();

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasRotationChanged()
                    || getData().generalCancel()
                    || getData().getCinematicProcessor().isCinematic()
                    || getData().getMovementProcessor().getDeltaXZ() < 0.085
                    || getData().getReachProcessor().getTimeSincePositionSet() > 3
                    || getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 1) {
                this.threshold -= Math.min(this.threshold, .01);
                return;
            }

            NewTrackedEntity.PossiblePosition possiblePosition = getData().getReachProcessor()
                    .getPossiblePosition();

            FlyingLocation currentLocation = getData().getMovementProcessor().getTo().clone();

            if (possiblePosition != null) {

                long now = System.currentTimeMillis();

                if (Math.abs(now - this.lastFail) > 20000L) {
                    this.threshold = 0;
                }

                double d = currentLocation.getPosX() - possiblePosition.posX;
                double d2 = currentLocation.getPosZ() - possiblePosition.posZ;
                double d3 = currentLocation.getPosY() + 0.81 - possiblePosition.posY - 1.2;

                float f = (float) Math.sqrt(d * d + d2 * d2);
                float predYaw = (float) (Math.atan2(d2, d) * 180.0 / Math.PI) - 90.0f;
                float predPitch = (float) (-(Math.atan2(d3, f) * 180.0 / Math.PI));

                float currentYaw = currentLocation.getYaw();
                float currentPitch = currentLocation.getPitch();

                double offsetX = this.getDistanceBetweenAngles(currentYaw, predYaw);
                double offsetY = this.getDistanceBetweenAngles(currentPitch, predPitch);

                float yawDelta = getData().getMovementProcessor().getDeltaYaw();
                float pitchDelta = getData().getMovementProcessor().getDeltaPitch();

                if (yawDelta > 0 && pitchDelta > 0) {

                    offsetX -= yawDelta;
                    offsetY -= pitchDelta;

                    offsetX %= 360;
                    offsetY %= 90;

                    float compX = (float) Math.abs(offsetX - currentYaw);
                    float compY = (float) Math.abs(offsetY - currentPitch);

                    if (offsetY < .7 && compY < 7 && compX > 0) {
                        compX = Math.abs(MathUtil.wrapAngleTo180_float(predYaw) -
                                MathUtil.wrapAngleTo180_float(currentYaw));
                        compY = Math.abs(predPitch - currentPitch);

                        if (compX > 50 && compY < 5 && yawDelta > .5 && pitchDelta > .2) {
                            int yawCount = this.countSameNumbers(yawDelta);
                            int pitchCount = this.countSameNumbers(pitchDelta);

                            if ((yawCount >= 3 || pitchCount >= 3)) {
                                if (++this.threshold > 5) {
                                    this.fail(
                                            "yaw-count="+yawCount,
                                            "pitch-count="+pitchCount,
                                            "compX="+compX,
                                            "compY="+compY,
                                            "offsetX="+offsetX,
                                            "offsetY="+offsetY,
                                            "yawDelta="+yawDelta,
                                            "pitchDelta="+pitchDelta);
                                }

                                this.lastFail = now;
                            }
                        }
                    }
                }
            }
        }
    }

    private int countSameNumbers(float value) {
        String toString = Float.toString(value);
        List<Integer> values = new ArrayList<>();

        if (toString.contains(".")) {
            int decimalIndex = toString.indexOf('.');

            for (int i = decimalIndex + 1; i < toString.length(); i++) {
                char currentChar = toString.charAt(i);

                if (Character.isDigit(currentChar)) {
                    values.add(Character.getNumericValue(currentChar));
                }
            }
        }

        return StreamUtil.getDuplicates(values);
    }


    private double getDistanceBetweenAngles(float f, float f2) {
        float f3 = Math.abs(f - f2) % 360.0f;

        if (f3 > 180.0f) {
            f3 = 360.0f - f3;
        }

        return f3;
    }
}