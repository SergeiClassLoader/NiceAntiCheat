package pro.cyrent.anticheat.impl.processor.prediction.movement;

import org.bukkit.World;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.processor.prediction.horizontal.HorizontalProcessor;
import pro.cyrent.anticheat.impl.processor.prediction.horizontal.MoveFlyingResult;
import pro.cyrent.anticheat.util.magic.MagicValues;
import pro.cyrent.anticheat.util.math.Motion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pro.cyrent.anticheat.util.minecraftmaths.MinecraftMath;

import java.util.List;
import java.util.function.Consumer;


@Getter
@Setter
public class MovementPredictionProcessor extends Event {

    private final PlayerData data;

    private int ticksSinceItemBlock;
    private int tickSinceSlotChange;
    private int ticksToIgnore;

    private boolean sneak, hitslow, sprint;

    private double buffer, threshold;

    private boolean canFlag = false, canFlagSneak = false;

    public MovementPredictionProcessor(PlayerData user) {
        this.data = user;
    }

    public void onPrePredictionProcess(PlayerData user, boolean sprint, boolean sneak, boolean hitslow,
                                       List<HorizontalProcessor.TransactionVelocityEntry> velocityEntries,
                                       WrapperPlayClientPlayerFlying flyingPacket, int delay,
                                       boolean zeroThree, boolean fastMather, boolean timerPacket) {

        double max = getData().getHorizontalProcessor().getClientCollideTicks()
                < 10 || getData().getCollisionWorldProcessor().getCollideHorizontalTicks() > 0
                ? 0.002 : getData().getProtocolVersion() > 47 ? 1E-4 :
                fastMather ? 7E-5 : 1E-7;

        if (zeroThree && !timerPacket) {
            max = getData().getProtocolVersion() > 47
                    ? 0.06D * delay
                    : 0.03D * delay;
        }

        double finalMax = max;

        this.handlePrediction(user, flyingPacket, sprint, sneak, hitslow, velocityEntries, predicted -> {

            double predictionValue = predicted.getSmallest();

            this.canFlag = false;

            if (predictionValue != Double.MAX_VALUE) {

                if (predictionValue > finalMax) {
                    if (++this.buffer > 7.0) {
                        this.buffer = 7;
                        this.canFlag = true;
                    }
                } else {
                    this.buffer -= Math.min(this.buffer, 0.0125);
                }
            }
        }, () -> {
            this.buffer -= Math.min(this.buffer, 0.0125);
            this.canFlag = false;
        });

        this.handlePredictionSneak(getData(), flyingPacket, true, velocityEntries, predicted -> {
            double predictionValue = predicted.getSmallest();

            this.canFlagSneak = false;

            // handle 0.003 on the max prediction value
            //TODO: add 0.03 accounting

            double deltaXZ = getData().getMovementProcessor().getDeltaXZ();

            int amplifer = getData().getPotionProcessor().getSpeedPotionAmplifier();
            double maxSpeed = 0.1 + (amplifer * 0.037);

            if (predictionValue != Double.MAX_VALUE) {
                double maxValue = deltaXZ < maxSpeed ? 0.001 : 1E-6;

                if (predictionValue >= maxValue) {

                    if (++this.threshold > 25.5) {
                        this.canFlagSneak = true;
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, 0.06);
                    this.canFlagSneak = false;
                }
            }
        }, () -> {
            this.threshold -= Math.min(this.threshold, 0.03);
            this.canFlagSneak = false;
        });
    }

    //KEEP SPRINT
    private void handlePrediction(PlayerData user, WrapperPlayClientPlayerFlying currentFlying, boolean useSprint,
                                  boolean useSneak, boolean useHitSlow,
                                  List<HorizontalProcessor.TransactionVelocityEntry> velocityEntries,
                                  Consumer<Predicted> consumer, Runnable exemptRunnable) {

        // pre-checks
        if (user.generalCancel()
                || (user.getPotionProcessor().getSpeedPotionTicks() > 0 && !user.getPotionProcessor().isSpeedPotion())
                || (user.getPotionProcessor().getSlownessTicks() > 0 && !user.getPotionProcessor().isSlownessPotion())
                || user.getMovementProcessor().getPositionTicks() < 7
                || user.getMovementProcessor().getClientWallCollision().hasNotPassed(8)
                || this.exempt(getData())
                || getData().getActionProcessor().getLastWalkSpeedTimer().getDelta() < 20
                || !getData().getCombatProcessor().isAttack()
                || getData().getPotionProcessor().getSpeedPotionAmplifier() == 0 && getData().getPotionProcessor().isSpeedPotion()
                || getData().getPotionProcessor().getSlownessAmplifier() == 0 && getData().getPotionProcessor().isSlownessPotion()
                || useHitSlow && getData().getActionProcessor().getLastSprintTick() < 7
                || user.getCollisionWorldProcessor().getCollidingHorizontallyTimer().hasNotPassed(8)
                || user.getActionProcessor().isTeleportingV2()) {
            exemptRunnable.run();
            return;
        }

        WrapperPlayClientPlayerFlying wrappedInFlyingPacket = user.getMovementProcessor().getFlyingPacket();
        WrapperPlayClientPlayerFlying lastFlying = user.getMovementProcessor().getLastFlyingPacket();
        WrapperPlayClientPlayerFlying lastLastLast = user.getMovementProcessor().getLastLastFlyingPacket();

        if (wrappedInFlyingPacket == null || lastFlying == null || lastLastLast == null) return;

        // zero zero three
        if ((currentFlying.hasPositionChanged() && lastFlying.hasPositionChanged() && lastLastLast.hasPositionChanged())) {
            double smallest = Double.MAX_VALUE;
            double motionX = 0;
            double motionZ = 0;

            boolean hitslowdown = false, sprinting = false;

            World world = getData().getPlayer().getWorld();

            if (world == null) return;

            float blockFriction = Anticheat.INSTANCE.getInstanceManager().getInstance().getSlipperiness(getData(),
                    getData().getMovementProcessor().getTo()
                            .toLocation(world));

            float blockFrictionFrom = Anticheat.INSTANCE.getInstanceManager().getInstance().getSlipperiness(getData(),
                    getData().getMovementProcessor().getFrom()
                            .toLocation(world));

            float blockFrictionLast = Anticheat.INSTANCE.getInstanceManager().getInstance().getSlipperiness(getData(),
                    getData().getMovementProcessor().getFromFrom()
                            .toLocation(world));

            if (blockFrictionFrom != blockFriction
                    || blockFrictionFrom != blockFrictionLast
                    || blockFriction != blockFrictionLast) {
                this.ticksToIgnore = 20;
                return;
            }

            if (this.ticksToIgnore-- > 0) {
                return;
            }


            // prediction block
            prediction:
            {
                final Motion realMotion = new Motion(
                        user.getHorizontalProcessor().getDeltaX(),
                        0, user.getHorizontalProcessor().getDeltaZ()
                );

                final Motion motion = new Motion(
                        user.getHorizontalProcessor().getLastDeltaX(),
                        0,
                        user.getHorizontalProcessor().getLastDeltaZ()
                );

                // this is where we get the specific data needed
                boolean[] sprintIteration = this.getSprintingIterations(useSprint);

                // copy of the horizontal movement runner, but because the processor version isn't designed to work
                // with hit slow down and keep sprint, this one will handle it
                // the processor version is designed to deal with invalid movement and velocity
                for (HorizontalProcessor.TransactionVelocityEntry velocityEntry : velocityEntries) {
                    for (int forwardBrutes = -1; forwardBrutes < 2; ++forwardBrutes) {
                        for (int strafeBrutes = -1; strafeBrutes < 2; ++strafeBrutes) {
                            for (boolean sneaking : MagicValues.TRUE_FALSE) {
                                for (boolean jump : MagicValues.TRUE_FALSE) {
                                    for (boolean sprint : sprintIteration) {
                                        for (boolean using : MagicValues.TRUE_FALSE) {
                                            for (boolean hitSlowdown : this.getSlowDownIterations(sprint, useHitSlow)) {

                                                motion.set(user.getHorizontalProcessor().getLastDeltaX(),
                                                        0, user.getHorizontalProcessor().getLastDeltaZ()
                                                );

                                                boolean ground = lastFlying.isOnGround();
                                                float forward = forwardBrutes;
                                                float strafe = strafeBrutes;

                                                // ignore sprint and no forward movement
                                                if (sprint && forward <= 0) {
                                                    continue;
                                                }

                                                // walkSpeed / 2 is the same as the nms move speed
                                                double attributeSpeed =
                                                        this.getData().getActionProcessor().getWalkSpeed();

                                                // apply the clients sprint speed value
                                                if (sprint) {
                                                    attributeSpeed += (attributeSpeed * .30000001192092896);
                                                }

                                                // apply the current speed potion multiplier to the move speed
                                                if (user.getPotionProcessor().isSpeedPotion()) {
                                                    attributeSpeed += user.
                                                            getPotionProcessor().getSpeedPotionAmplifier()
                                                            * 0.20000000298023224D * attributeSpeed;
                                                }

                                                // apply the current slowness potion multiplier to the move speed
                                                if (user.getPotionProcessor().isSlownessPotion()) {
                                                    attributeSpeed += user.
                                                            getPotionProcessor().getSlownessAmplifier() *
                                                            -.15000000596046448D * attributeSpeed;
                                                }

                                                // sneaking logic
                                                if (sneaking) {
                                                    forward *= (float) 0.3D;
                                                    strafe *= (float) 0.3D;
                                                }

                                                // using logic
                                                if (using) {
                                                    forward *= 0.2F;
                                                    strafe *= 0.2F;
                                                }

                                                // forward & strafe logic
                                                forward *= 0.98F;
                                                strafe *= 0.98F;

                                                // check if they were last on ground 3 ticks ago
                                                if (lastLastLast.isOnGround()) {
                                                    motion.getMotionX().multiply(blockFrictionLast * 0.91F);
                                                    motion.getMotionZ().multiply(blockFrictionLast * 0.91F);
                                                } else {
                                                    motion.getMotionX().multiply(0.91F);
                                                    motion.getMotionZ().multiply(0.91F);
                                                }

                                                // apply the velocity from the last velocity tick
                                                if (velocityEntry != null) {
                                                    motion.getMotionX().set(velocityEntry.getX());

                                                    motion.getMotionZ().set(velocityEntry.getZ());
                                                }

                                                // add hit slowdown to the current prediction
                                                if (getData().getCombatProcessor().isAttack()
                                                        && getData().getActionProcessor().isLastSprinting()) {
                                                    motion.getMotionX().multiply(0.6D);
                                                    motion.getMotionZ().multiply(0.6D);
                                                }

                                                // round the prediction
                                                motion.round();

                                                float slipperiness = 0.91F;

                                                // apply the blocks below friction
                                                if (ground) slipperiness = blockFriction * 0.91F;

                                                float moveSpeed = (float) attributeSpeed;
                                                final float moveFlyingFriction;

                                                // apply the move speed multiplier if on ground
                                                if (ground) {
                                                    float moveSpeedMultiplier = 0.16277136F /
                                                            (slipperiness * slipperiness * slipperiness);

                                                    // 1.13+ only
                                                    if (Anticheat.INSTANCE.getVersionSupport()
                                                            .getClientProtocol(getData()) > 404) {
                                                        moveSpeedMultiplier =
                                                                0.21600002F / (blockFriction
                                                                        * blockFriction * blockFriction);
                                                    }

                                                    moveFlyingFriction = moveSpeed * moveSpeedMultiplier;

                                                    // append the acceleration from sprint jumping
                                                    if (jump && sprint) {
                                                        final float radians = user.getHorizontalProcessor()
                                                                .getYaw() * 0.017453292F;

                                                        motion.getMotionX().subtract(MinecraftMath.sin(0, radians) * 0.2F);

                                                        motion.getMotionZ().add(MinecraftMath.cos(0, radians) * 0.2F);
                                                    }
                                                } else {
                                                    // apply the air movement acceleration
                                                    moveFlyingFriction = (float)
                                                            (sprint ? ((double) 0.02F +
                                                                    (double) 0.02F * 0.3D) : 0.02F);
                                                }

                                                MoveFlyingResult moveFlyingResult = MagicValues.moveFlyingResult(
                                                        getData(),
                                                        forward, strafe,
                                                        user.getHorizontalProcessor().getYaw(),
                                                        moveFlyingFriction,
                                                        0,
                                                        getData().getProtocolVersion()
                                                );

                                                // apply the move flying we got above
                                                motion.apply(moveFlyingResult);

                                                // set the motionY to 0 since we don't need it
                                                motion.getMotionY().set(0.0);

                                                // calculate the delta X & Y to generate the square root
                                                double delta = realMotion.distanceSquared(motion);

                                                if (delta < smallest) {
                                                    smallest = delta;
                                                    motionX = motion.getMotionX().get();
                                                    motionZ = motion.getMotionZ().get();

                                                    hitslowdown = hitSlowdown;
                                                    sprinting = sprint;


                                                    // we don't need good accuracy for this check, this will do
                                                    if (delta < 1E-7) {
                                                        break prediction;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // accept consumer with predicted data
            if (smallest != Double.MAX_VALUE && (getData().getActionProcessor().isSprinting()
                    || getData().getActionProcessor().isLastSprinting())) {
                consumer.accept(new Predicted(smallest, motionX, motionZ, sprinting, hitslowdown));
            }
        }
    }


    //SNEAK PREDICTTIONS
    private void handlePredictionSneak(PlayerData user, WrapperPlayClientPlayerFlying currentFlying,
                                       boolean useSneak,
                                       List<HorizontalProcessor.TransactionVelocityEntry> velocityEntries,
                                       Consumer<Predicted> consumer, Runnable exemptRunnable) {

        // pre-checks
        if (user.generalCancel()
                || (user.getPotionProcessor().getSpeedPotionTicks() > 0 && !user.getPotionProcessor().isSpeedPotion())
                || (user.getPotionProcessor().getSlownessTicks() > 0 && !user.getPotionProcessor().isSlownessPotion())
                || user.getMovementProcessor().getPositionTicks() < 7
                || user.getMovementProcessor().getClientWallCollision().hasNotPassed(8)
                || this.exempt(getData())
                || !useSneak
                || !getData().getActionProcessor().isSneaking()
                || getData().getActionProcessor().getLastSneakTick() < 20
                || user.getCollisionWorldProcessor().getCollidingHorizontallyTimer().hasNotPassed(8)
                || getData().getVelocityProcessor().getServerVelocityTicks() < 20
                || getData().getVelocityProcessor().getVelocityTicksConfirmed() < 20
                || user.getActionProcessor().isTeleportingV2()) {
            exemptRunnable.run();
            return;
        }

        WrapperPlayClientPlayerFlying wrappedInFlyingPacket = user.getMovementProcessor().getFlyingPacket();
        WrapperPlayClientPlayerFlying lastFlying = user.getMovementProcessor().getLastFlyingPacket();
        WrapperPlayClientPlayerFlying lastLastLast = user.getMovementProcessor().getLastLastFlyingPacket();

        if (wrappedInFlyingPacket == null || lastFlying == null || lastLastLast == null) return;

        // zero zero three
        if ((currentFlying.hasPositionChanged() && lastFlying.hasPositionChanged() && lastLastLast.hasPositionChanged())) {
            double smallest = Double.MAX_VALUE;
            double motionX = 0;
            double motionZ = 0;

            boolean hitslowdown = false, sprinting = false;

            World world = getData().getPlayer().getWorld();

            if (world == null) return;

            float blockFriction1 = Anticheat.INSTANCE.getInstanceManager().getInstance().getSlipperiness(getData(),
                    getData().getMovementProcessor().getTo()
                            .toLocation(world));

            float blockFrictionFrom = Anticheat.INSTANCE.getInstanceManager().getInstance().getSlipperiness(getData(),
                    getData().getMovementProcessor().getFrom()
                            .toLocation(world));

            float blockFrictionLast = Anticheat.INSTANCE.getInstanceManager().getInstance().getSlipperiness(getData(),
                    getData().getMovementProcessor().getFromFrom()
                            .toLocation(world));

            if (blockFrictionFrom != blockFriction1
                    || blockFrictionFrom != blockFrictionLast
                    || blockFriction1 != blockFrictionLast) {
                this.ticksToIgnore = 20;
                return;
            }

            if (this.ticksToIgnore-- > 0) {
                return;
            }

            // prediction block
            prediction:
            {
                final Motion realMotion = new Motion(
                        user.getHorizontalProcessor().getDeltaX(),
                        0, user.getHorizontalProcessor().getDeltaZ()
                );

                final Motion motion = new Motion(
                        user.getHorizontalProcessor().getLastDeltaX(),
                        0,
                        user.getHorizontalProcessor().getLastDeltaZ()
                );

                // this is where we get the specific data needed
                boolean[] sneakIteration = this.getSneakingIterations(useSneak);

                // copy of the horizontal movement runner, but because the processor version isn't designed to work
                // with hit slow down and keep sprint, this one will handle it
                // the processor version is designed to deal with invalid movement and velocity
                for (HorizontalProcessor.TransactionVelocityEntry velocityEntry : velocityEntries) {
                    for (int forwardBrutes = -1; forwardBrutes < 2; ++forwardBrutes) {
                        for (int strafeBrutes = -1; strafeBrutes < 2; ++strafeBrutes) {
                            for (boolean sneaking : sneakIteration) {
                                for (boolean jump : MagicValues.TRUE_FALSE) {
                                    for (boolean sprint : MagicValues.TRUE_FALSE) {
                                        for (boolean using : MagicValues.TRUE_FALSE) {
                                            for (boolean hitSlowdown : MagicValues.TRUE_FALSE) {

                                                motion.set(user.getHorizontalProcessor().getLastDeltaX(),
                                                        0, user.getHorizontalProcessor().getLastDeltaZ()
                                                );

                                                boolean ground = lastFlying.isOnGround();
                                                float forward = forwardBrutes;
                                                float strafe = strafeBrutes;

                                                // ignore sprint and no forward movement
                                                if (sprint && forward <= 0) {
                                                    continue;
                                                }

                                                // walkSpeed / 2 is the same as the nms move speed
                                                double attributeSpeed =
                                                        this.getData().getActionProcessor().getWalkSpeed();

                                                // apply the clients sprint speed value
                                                if (sprint) {
                                                    attributeSpeed += (attributeSpeed * .30000001192092896);
                                                }

                                                // apply the current speed potion multiplier to the move speed
                                                if (user.getPotionProcessor().isSpeedPotion()) {
                                                    attributeSpeed += user.
                                                            getPotionProcessor().getSpeedPotionAmplifier()
                                                            * 0.20000000298023224D * attributeSpeed;
                                                }

                                                // apply the current slowness potion multiplier to the move speed
                                                if (user.getPotionProcessor().isSlownessPotion()) {
                                                    attributeSpeed += user.
                                                            getPotionProcessor().getSlownessAmplifier() *
                                                            -.15000000596046448D * attributeSpeed;
                                                }

                                                // sneaking logic
                                                if (sneaking) {
                                                    forward *= (float) 0.3D;
                                                    strafe *= (float) 0.3D;
                                                }

                                                // using logic
                                                if (using) {
                                                    forward *= 0.2F;
                                                    strafe *= 0.2F;
                                                }

                                                // forward & strafe logic
                                                forward *= 0.98F;
                                                strafe *= 0.98F;

                                                // check if they were last on ground 3 ticks ago
                                                if (lastLastLast.isOnGround()) {
                                                    motion.getMotionX().multiply(blockFrictionLast * 0.91F);
                                                    motion.getMotionZ().multiply(blockFrictionLast * 0.91F);
                                                } else {
                                                    motion.getMotionX().multiply(0.91F);
                                                    motion.getMotionZ().multiply(0.91F);
                                                }

                                                // apply the velocity from the last velocity tick
                                                if (velocityEntry != null) {
                                                    motion.getMotionX().set(velocityEntry.getX());

                                                    motion.getMotionZ().set(velocityEntry.getZ());
                                                }

                                                // add hit slowdown to the current prediction
                                                if (hitSlowdown) {
                                                    motion.getMotionX().multiply(0.6D);
                                                    motion.getMotionZ().multiply(0.6D);
                                                }

                                                // round the prediction
                                                motion.round();

                                                float slipperiness = 0.91F;

                                                // apply the blocks below friction
                                                if (ground) slipperiness = blockFrictionLast * 0.91F;

                                                float moveSpeed = (float) attributeSpeed;
                                                final float moveFlyingFriction;

                                                // apply the move speed multiplier if on ground
                                                if (ground) {
                                                    float moveSpeedMultiplier = 0.16277136F /
                                                            (slipperiness * slipperiness * slipperiness);

                                                    // 1.13+ only
                                                    if (Anticheat.INSTANCE.getVersionSupport()
                                                            .getClientProtocol(getData()) > 404) {
                                                        moveSpeedMultiplier =
                                                                0.21600002F / (blockFrictionLast
                                                                        * blockFrictionLast * blockFrictionLast);
                                                    }

                                                    moveFlyingFriction = moveSpeed * moveSpeedMultiplier;

                                                    // append the acceleration from sprint jumping
                                                    if (jump && sprint) {
                                                        final float radians = user.getHorizontalProcessor()
                                                                .getYaw() * 0.017453292F;

                                                        motion.getMotionX().subtract(MinecraftMath.sin(0, radians) * 0.2F);

                                                        motion.getMotionZ().add(MinecraftMath.cos(0, radians) * 0.2F);
                                                    }
                                                } else {
                                                    // apply the air movement acceleration
                                                    moveFlyingFriction = (float)
                                                            (sprint ? ((double) 0.02F +
                                                                    (double) 0.02F * 0.3D) : 0.02F);
                                                }

                                                MoveFlyingResult moveFlyingResult = MagicValues.moveFlyingResult(
                                                        getData(),
                                                        forward, strafe,
                                                        user.getHorizontalProcessor().getYaw(),
                                                        moveFlyingFriction,
                                                        0,
                                                        getData().getProtocolVersion());

                                                // apply the move flying we got above
                                                motion.apply(moveFlyingResult);

                                                // set the motionY to 0 since we don't need it
                                                motion.getMotionY().set(0.0);

                                                // calculate the delta X & Y to generate the square root
                                                double delta = realMotion.distanceSquared(motion);

                                                if (delta < smallest) {
                                                    smallest = delta;
                                                    motionX = motion.getMotionX().get();
                                                    motionZ = motion.getMotionZ().get();

                                                    hitslowdown = hitSlowdown;
                                                    sprinting = sprint;


                                                    // we don't need good accuracy for this check, this will do
                                                    if (delta < 1E-7) {
                                                        break prediction;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // accept consumer with predicted data
            if (smallest != Double.MAX_VALUE) {
                consumer.accept(new Predicted(smallest, motionX, motionZ, sprinting, hitslowdown));
            }
        }
    }

    private boolean[] getSneakingIterations(boolean useIteration) {
        return getData().getActionProcessor().isSneaking()
                && getData().getActionProcessor().getLastSneakTick() > 20 && useIteration
                ? MagicValues.TRUE : MagicValues.TRUE_FALSE;
    }

    private boolean[] getSprintingIterations(boolean useIteration) {
        return getData().getActionProcessor().isSprinting()
                && getData().getActionProcessor().getLastSprintTick() > 3 && useIteration
                ? MagicValues.TRUE : MagicValues.TRUE_FALSE;
    }

    private boolean[] getSlowDownIterations(boolean sprinting, boolean useIteration) {
        return MagicValues.TRUE_FALSE;
    }

    private boolean exempt(PlayerData user) {
        return (user.getCollisionWorldProcessor().isHalfBlock()
                || user.getCollisionWorldProcessor().getCollidingHorizontallyTimer().hasNotPassed()
                || user.getCollisionWorldProcessor().isCollidingHorizontal()
                || user.getCollisionWorldProcessor().getLiquidTicks() > 0
                || user.getCollisionWorldProcessor().isDoor()
                || user.getCollisionWorldProcessor().isBlockAbove()
                || user.getActionProcessor().isTeleportingV2()
                || user.getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                || user.getCollisionWorldProcessor().getAnvilTicks() > 0
                || user.getCollisionWorldProcessor().getWallTicks() > 0
                || user.getActionProcessor().getLastVehicleTimer().getDelta() < 20
                || user.getLastExplosionTimer().getDelta() < 20
                || user.getCollisionProcessor().getWebTicks() > 0
                || user.getCollisionWorldProcessor().getClimbableTicks() > 0
                || user.getCollisionWorldProcessor().getPistionTicks() > 0
                || user.getPotionProcessor().getSlownessTicks() > 0
                || user.getCollisionWorldProcessor().getSoulSandTicks() > 0);
    }

    @Getter
    @AllArgsConstructor
    private final static class Predicted {
        private final double smallest;
        private final double motionX, motionZ;
        private boolean sprinting, hitslowdown;
    }
}