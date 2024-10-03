package pro.cyrent.anticheat.api.check.impl.combat.aimanalysis;


import org.bukkit.util.Vector;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.api.check.data.ReachData;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import org.joml.Vector3d;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

@CheckInformation(
        name = "Analysis",
        checkNameEnum = CheckName.AIM_ANALYSIS,
        checkType = CheckType.COMBAT,
        punishable = false,
        description = "Detects invalid aim pattern based on the attack location over time",
        state = CheckState.BETA)
public class AnalysisA extends Check {

    private double threshold, lastStd, lastDev, lastAvg;
    private BodyPartEnum bodyPartEnum;
    private final Deque<Float> pitchChanges = new LinkedList<>();

    @Override
    public void onReach(ReachData data) {
        if (data.isValidHitbox() && data.isAttack()) {

            FlyingLocation from = getData().getMovementProcessor().getFrom();

            // Fixed issue with reach vector not being used with the proper reach system.
            Vector vector = getData().getReachProcessor().getHitVector();

            if (vector == null || getData().getCombatProcessor().getCancelTicks() > 0) {
                this.threshold -= Math.min(this.threshold, .005);
                return;
            }

            final double distanceYt = vector.getY() - (from.getPosY() + 1.62F);

            final float generatedPitchTest = clamp_pitch((float) distanceYt);
            final float deltaYaw = getData().getMovementProcessor().getDeltaYawAbs();

            if (generatedPitchTest > -2 && generatedPitchTest < -.2) {
                this.bodyPartEnum = BodyPartEnum.LEGS_FEET;
            } else if (generatedPitchTest >= -.2 && generatedPitchTest <= .2) {
                this.bodyPartEnum = BodyPartEnum.CHEST;
            } else {
                this.bodyPartEnum = BodyPartEnum.HEAD;
            }

            if (deltaYaw > 1.0F) {
                this.pitchChanges.add(generatedPitchTest);
            }

            if (this.pitchChanges.size() >= 60) {
                double std = StreamUtil.getStandardDeviation(this.pitchChanges);
                double avg = StreamUtil.getAverage(this.pitchChanges);
                double dev = StreamUtil.getDeviation(this.pitchChanges);

                double stdDelta = Math.abs(std - this.lastStd);
                double devDelta = Math.abs(dev - this.lastDev);
                double avgDelta = Math.abs(avg - this.lastAvg);

                boolean invalid = std < 0.06 && Math.abs(avg) < .3 && dev < .25
                        || stdDelta < 0.01 && devDelta < 0.01 && avgDelta < 0.03;

                if (invalid) {

                    // upped threshold as ik some ppl are bots and can false this check somehow
                    if (++this.threshold > 7.0) {
                        this.fail("std="+std,
                                "avg="+avg,
                                "dev="+dev,
                                "stdDelta="+stdDelta,
                                "avgDelta="+avgDelta,
                                "devDelta="+devDelta,
                                "bodyPart="+this.bodyPartEnum.name());
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .0725);
                }

                this.lastAvg = avg;
                this.lastStd = std;
                this.lastDev = dev;
                this.pitchChanges.clear();
            }
        }
    }

    public enum BodyPartEnum {
        HEAD,
        CHEST,
        LEGS_FEET,
    }


    public static float clamp_pitch(float var0) {

        if (var0 >= 90.0F) {
            var0 = 90.0F;
        }

        if (var0 <= -90.0F) {
            var0 = -90.0F;
        }

        return var0;
    }
}