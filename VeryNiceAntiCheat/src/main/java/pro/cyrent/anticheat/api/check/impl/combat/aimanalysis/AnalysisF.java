package pro.cyrent.anticheat.api.check.impl.combat.aimanalysis;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.api.check.data.ReachData;
import pro.cyrent.anticheat.impl.processor.connection.tracker.NewTrackedEntity;
import pro.cyrent.anticheat.util.block.box.HydroBB;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.google.common.collect.Lists;

import java.util.Deque;

@CheckInformation(
        name = "Analysis",
        subName = "F",
        checkNameEnum = CheckName.AIM_ANALYSIS,
        checkType = CheckType.COMBAT,
        punishable = false,
        description = "Detects invalid aim pattern based on the attack location over time",
        state = CheckState.PRE_BETA)
public class AnalysisF extends Check {

    private final Deque<Float> offsets = Lists.newLinkedList(),
            deltas = Lists.newLinkedList(),
            pitchOffsets = Lists.newLinkedList();

    private double threshold;

    @Override
    public void onReach(ReachData data) {
        if (data.isValidHitbox() && data.isAttack()) {

            if (getData().isBedrock() || getData().generalCancel()) return;

            FlyingLocation to = getData().getMovementProcessor().getTo();
            FlyingLocation from = getData().getMovementProcessor().getFrom();

            float yaw = g(to.getYaw());
            float pitch = to.getPitch();

            NewTrackedEntity.PossiblePosition trackedEntity = getData().getReachProcessor().getPossiblePosition();

            // Get the target's bounding box.
            HydroBB boundingBox = trackedEntity.getEntityBoundingBox().clone();

            /*
             * Since this anti-cheat base has a "perfect" entity tracker (apart from the scaling issues)
             * we can actually run a rotation generating method from an aim-bot cheat and compare that to the
             * actual rotations to get an idea if the player is using an aim-bot.
             *
             * Of course, not all aim-bots generate rotations the same way, however they are pretty similar
             * most of the time. I am only comparing yaw rotations and not pitch rotations, because they tend to vary
             * less in how those are calculated.
             *
             * We compare the wrapped rotation values since the generated one will be super offset from the client yaw
             * unless we wrap the two to 180 degrees.
             */
            double distanceX = boundingBox.posX() - from.getPosX();
            double distanceY = boundingBox.posY() - from.getPosY();
            double distanceZ = boundingBox.posZ() - from.getPosZ();

            // Generate the yaw based on the distance using simple trig math. Wrap to 180F.
            float generatedYaw = g((float) (Math.toDegrees(Math.atan2(distanceZ, distanceX)) - 90F));
            float generatedPitch = clamp_pitch((float) distanceY);

            // Add the offset from the actual yaw and generated yaw to the sample.
            this.offsets.add(Math.abs(yaw - generatedYaw));

            // Get the generated pitch offsets for sampling
            this.pitchOffsets.add(Math.abs(pitch - generatedPitch));

            // Add the delta yaw to the sample.
            this.deltas.add(Math.abs(to.getYaw() - from.getYaw()));

            /*
             * Easy way of checking if both samples are full. In this case there are two sample lists,
             * so we are checking at 30 per sample.
             *
             * We get the average and deviation of both as a measure of central tendency and as a measure of variation.
             *
             * A low average for the offsets indicates that the generated yaw and yaw are usually very similar,
             * indicating the player may be using an aim-bot.
             *
             * A low average for the deltas indicates the player did not move their mouse very much and should be
             * exempted to prevent false positives.
             *
             * A low deviation for the offsets indicates that the player has a consistent offset, meaning that while
             * the calculated offset might be high, the player is consistently offset, indicating they might be using an
             * aim-bot of a different rotation generation method.
             *
             * A low deviation for the deltas indicates that the player was not challenged enough during combat and
             * could hit the target at a consistent rotation, indicating that the target may be standing still. For
             * obvious reasons we would want to exempt this.
             */
            if (this.offsets.size() + this.pitchOffsets.size() + this.deltas.size() >= 120) {
                double averageDelta = StreamUtil.mean(this.deltas);
                double averageOffset = StreamUtil.mean(this.offsets);
                double pitchOffset = StreamUtil.mean(this.pitchOffsets);

                double deltasDeviation = StreamUtil.getDeviation(this.deltas);
                double offsetsDeviation = StreamUtil.getDeviation(this.offsets);
                double pitchDeviation = StreamUtil.getDeviation(this.pitchOffsets);


                if (averageDelta > 2.5 && (averageOffset < 3.0 || pitchOffset < 3.0)
                        && deltasDeviation > 1.0 && (offsetsDeviation < 3.0 || pitchDeviation < 3.0)) {
                    // Use a buffer since this check is based simply on improbability.
                    if (this.threshold > 3) this.fail(
                            "averageDelta="+averageDelta,
                            "averageOffset="+averageOffset,
                            "deltasDeviation="+deltasDeviation,
                            "offsetsDeviation="+offsetsDeviation);
                } else {
                    this.threshold -= Math.min(this.threshold, 0.25);
                }

                // Clear the sample.
                this.offsets.clear();
                this.deltas.clear();
            }
        }
    }


    public static float g(float var0) {
        var0 %= 360.0F;
        if (var0 >= 180.0F) {
            var0 -= 360.0F;
        }

        if (var0 < -180.0F) {
            var0 += 360.0F;
        }

        return var0;
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

