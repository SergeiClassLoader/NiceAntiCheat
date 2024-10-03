package pro.cyrent.anticheat.api.check.impl.movement.fly;


import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.impl.processor.combat.VelocityProcessor;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.Material;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.processor.prediction.horizontal.HorizontalProcessor;
import pro.cyrent.anticheat.util.magic.MagicValues;

import java.util.Arrays;
import java.util.List;

@CheckInformation(
        name = "Fly",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.FLY,
        description = "Detects invalid vertical movements based off predictions",
        punishmentVL = 10.0,
        state = CheckState.PRE_RELEASE)
public class FlyA extends Check {

    private double verbose;
    private boolean secondChance = false;

    private int delayedFlyingTicks;
    private double bypass, waterBypassCheck;
    private boolean zeroThree;

    private boolean returnNextTick = false;

    private boolean position, lastPosition, lastLastPosition,
            lastLastLastPosition, lastLastLastLastPosition, lastLastLastLastLastPosition;

    public void onPost(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (flying.hasPositionChanged()) {
                this.delayedFlyingTicks = 0;
            }

            this.zeroThree = false;
        }
    }

    public void onFlyCheck(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (getData().getProtocolVersion() > 47) {
                if (!this.lastPosition || !this.lastLastPosition
                        || !this.lastLastLastPosition || !this.lastLastLastLastPosition
                        || !this.lastLastLastLastLastPosition
                        || getData().getMovementProcessor().getDeltaXZ() < 0.12) {
                    this.zeroThree = true;
                }
            } else {
                if (!this.lastPosition
                        || !this.lastLastPosition
                        || getData().getMovementProcessor().getDeltaXZ() < 0.12) {
                    this.zeroThree = true;
                }
            }

            if (this.zeroThree
                    && getData().getMovementProcessor().getDeltaXZ() > .07
                    && flying.hasPositionChanged()) {
                this.zeroThree = false;
            }

            ++this.delayedFlyingTicks;

            this.lastLastLastLastLastPosition = this.lastLastLastLastPosition;
            this.lastLastLastLastPosition = this.lastLastLastPosition;
            this.lastLastLastPosition = this.lastLastPosition;
            this.lastLastPosition = this.lastPosition;
            this.lastPosition = this.position;
            this.position = flying.hasPositionChanged();

            if (!flying.hasPositionChanged()) return;

            boolean clientGround = getData().getMovementProcessor().getTo().isOnGround();
            boolean lastGround = getData().getMovementProcessor().getFrom().isOnGround();

            double deltaY = getData().getMovementProcessor().getDeltaY();
            double lastLastDeltaY = getData().getMovementProcessor().getLastLastDeltaY();

            double distance = Double.MAX_VALUE;
            double predicted = Double.MAX_VALUE;

            double velocityAmountY = Double.MAX_VALUE;
            boolean tookVelocity = false;

            //todo: poll on horizontal engine instead of split? (for vert poll or polllast or peaklast?)

            for (boolean slime : Arrays.asList(true, false)) {
                for (boolean potentialFalse : Arrays.asList(true, false)) {

                    double lastDeltaY = getData().getMovementProcessor().getLastDeltaY();

                    if (!this.lastPosition) {
                        lastDeltaY = (lastLastDeltaY - MagicValues.VERTICAL_SUBTRACTED)
                                * MagicValues.VERTICAL_MULTIPLIER;

                        // old vel code here


                        if (Math.abs(lastDeltaY) < (getData().getProtocolVersion() > 47
                                ? MagicValues.MIN_VERTICAL_1_9 : MagicValues.MIN_VERTICAL_1_8)) {
                            lastDeltaY = 0.0D;
                        }
                    }

                    double motionY = lastDeltaY - MagicValues.VERTICAL_SUBTRACTED;

                    motionY *= MagicValues.VERTICAL_MULTIPLIER;

                    if (Math.abs(motionY) < (getData().getProtocolVersion() > 47
                            ? MagicValues.MIN_VERTICAL_1_9 : MagicValues.MIN_VERTICAL_1_8)) {
                        motionY = 0.0D;
                    }

                    double jumpHeight = (0.42F + (getData().getPotionProcessor().getJumpPotionAmplifier() * 0.1F));

                    if (!clientGround && lastGround
                            && deltaY == jumpHeight
                            && lastDeltaY != jumpHeight) {
                        motionY = jumpHeight;
                    }

                    boolean canGo = (getData().getCollisionWorldProcessor().isSlime()
                            || getData().getCollisionWorldProcessor().isBed()
                            && getData().getProtocolVersion() >= 335)
                            && getData().getMovementProcessor().getTo().isOnGround();

                    if (slime && canGo) {
                        if (getData().getPlayer().isSneaking()) {
                            motionY = 0;
                        } else if (motionY < 0.0D) {
                            double fixedMotion = -motionY;

                            if (Math.abs(deltaY - fixedMotion) < 1E-7) {
                                motionY = fixedMotion;
                            }
                        }
                    }

                    if (!getData().getVelocityProcessor().getVelocityFlyQueues().isEmpty()) {
                        VelocityProcessor.VelocityData velocityEntry =
                                getData().getVelocityProcessor().getVelocityFlyQueues().pollLast();

                        if (velocityEntry != null) {
                            motionY = velocityEntry.getY();
                            tookVelocity = true;
                            velocityAmountY = velocityEntry.getY();
                        }
                    }

                    if (potentialFalse && getData().getCollisionWorldProcessor()
                            .getBlockAboveTimer().getDelta() < 4
                            && getData().getCollisionWorldProcessor().getBlockAboveTimer().isSet()
                            && deltaY != lastDeltaY) {
                        motionY = (-MagicValues.VERTICAL_SUBTRACTED * MagicValues.VERTICAL_MULTIPLIER);
                    }

                    //fixes 0.08 issue with it not predicting at random.
                  /*      if (Math.abs(deltaY - motionY) > 1E-7) {
                            double fixed = (motionY - MagicValues.VERTICAL_SUBTRACTED) * MagicValues.VERTICAL_MULTIPLIER;

                            if (Math.abs(fixed) < (getData().getProtocolVersion() > 47
                                    ? MagicValues.MIN_VERTICAL_1_9 : MagicValues.MIN_VERTICAL_1_8)) {
                                fixed = 0.0D;
                            }

                            if (Math.abs(deltaY - fixed) < 1E-7) {
                                motionY = fixed;
                            }
                        }*/

                    final double current = Math.abs(motionY - deltaY);

                    if (current < distance) {
                        predicted = motionY;
                        distance = current;
                    }
                }
            }

            double deltaXZ = getData().getMovementProcessor().getDeltaXZ();

            boolean bouncyFix = (getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                    || getData().getCollisionWorldProcessor().isBed()) &&
                    (this.zeroThree || this.delayedFlyingTicks > 0 || deltaXZ < .2);

            boolean fixSkullAbove = getData().getCollisionWorldProcessor().isBlockAbove()
                    && getData().getCollisionWorldProcessor().isSkull();

            boolean fixGay = (getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                    || (getData().getCollisionWorldProcessor().getHalfBlockTimer().getDelta() < 12
                    && getData().getCollisionWorldProcessor().getHalfBlockTimer().isSet()))
                    && (getData().getCollisionWorldProcessor().getBlockAboveTimer().getDelta() < 7
                    && getData().getCollisionWorldProcessor().getBlockAboveTimer().isSet());

            //slow falling changes 0.08D to 0.01D if motion <= 0.0

            //levitation code:
            // var28 += (0.05D * (double)(this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - var27.y) * 0.2D;

            // fixes high ping problem falsely banning people?


            int max = 20 + getData().getTransactionProcessor().getPingTicks();

            boolean noMoveLag = getData().getMovementProcessor().getDeltaXZ() == 0
                    && this.zeroThree && deltaY == 0.0 && (getData().getActionProcessor().getTeleportTicks() < max
                    || getData().getLastTeleport().getDelta() < max || getData().getLastWorldChange().getDelta() < max
                    || getData().getTransactionProcessor().getTransactionQueue().size() > 20
                    || getData().getTransactionProcessor().getTransactionPing() > 200
                    || getData().getTransactionProcessor().getTransactionPing() < 20);

            boolean tp = (getData().getActionProcessor().getTeleportTicks() < 3
                    || getData().getActionProcessor().isTeleportingReal())
                    && getData().getCollisionWorldProcessor().isGround()
                    && (getData().getMovementProcessor().getDeltaXZ() == 0
                    || getData().getMovementProcessor().getLastDeltaXZ() == 0);

            boolean negativeVelocityAutism = tookVelocity &&
                    (velocityAmountY < -0.06
                            && velocityAmountY > -0.08);


            // block above and on snow & fix skull above
            final double threshold = fixGay ? .2 : bouncyFix || fixSkullAbove ? 0.120
                    // 1.9 random false shit dumb thing
                    : (this.zeroThree && getData().getProtocolVersion() > 47 && deltaY < -0.07 && deltaY > -0.08
                    && predicted > 0.003 && predicted < 0.003017) || tp ? 0.08
                    // no move lag
                    : noMoveLag ? 0.08
                    // block above
                    : getData().getCollisionWorldProcessor().getBlockAboveTimer().getDelta() < 3 ? 0.06D
                    // 1.9+
                    : this.zeroThree && getData().getProtocolVersion() > 47 ? 0.06D
                    * (this.delayedFlyingTicks == 0 ? 1 : this.delayedFlyingTicks)
                    // half ass zero three fix, mainly blazingpack shit fix
                    : this.zeroThree && (getData().getCollisionProcessor().isServerGround()
                    || getData().getCollisionProcessor().isLastServerGround())
                    && deltaY < 0 && deltaY > -.155 && predicted < 0
                    && deltaXZ <= 0.03 ? 0.155
                    //-0.0784
                    : this.zeroThree ? 0.074D
                    : 1e-06D;


            boolean invalid = distance != Double.MAX_VALUE && predicted != Double.MAX_VALUE
                    && distance > threshold && !clientGround && !lastGround
                    && !getData().getCollisionWorldProcessor().isGround();

            // fix for next tick movement since shit happens
            if (this.returnNextTick) {
                this.returnNextTick = false;

                if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                        .getUniqueId().toString().equals(getData().getUuid().toString())) {
                    Anticheat.INSTANCE.getCheckPacketLog().add(
                            "Fly A Return Type 14 (Return Next Tick)\n");
                }
                return;
            }

            if (!exempt(deltaY, negativeVelocityAutism) && invalid) {

                this.getData().getSetBackProcessor().setLastInvalidTick(50);

                if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                        .getUniqueId().toString().equals(getData().getUuid().toString())) {
                    Anticheat.INSTANCE.getCheckPacketLog().add(
                            "Fly A is currently failing, last threshold: " + this.verbose + "\n");
                }

                // old 3.5
                if (++this.verbose > 2.75) {

                    if (getData().getMovementProcessor().isLastHeldItemExempt()) {

                        if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                                .getUniqueId().toString().equals(getData().getUuid().toString())) {
                            Anticheat.INSTANCE.getCheckPacketLog().add(
                                    "Fly A Return Type 15 (SetBack for cancel place)\n");
                        }

                        this.fail("Invalid vertical prediction",
                                "offset=" + distance,
                                "max-offset=" + threshold,
                                "zeroZeroThree=" + this.zeroThree,
                                "deltaY=" + deltaY,
                                "predicted=" + predicted,
                                "deltaXZ=" + deltaXZ,
                                "lastOnSlimeTime=" + getData().getCollisionWorldProcessor().getSlimeTicks());

                    }
                } else {
                    // make threshold go down less
                    // old values 0.03 : 0.01
                    this.verbose -= Math.min(this.verbose, (this.zeroThree ? 0.03 : 0.01));
                }
            }
        }
    }

    private boolean exempt(double deltaY, boolean negativeVelocity) {

        double motion = (-.1D * 0.9800000190734863D);

        if (Math.abs(deltaY - motion) < 1E-7) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 0 | Invalid Chunk Motion");
            }
            return true;
        }

        if (negativeVelocity) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 0.5 | Negative Velocity");
            }
        }

        if (getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(3)
                && getData().getActionProcessor().getLastVehicleTimer().isSet()
                || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 5
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getMovementProcessor().getLastFlightTimer().getDelta() < 40
                && getData().getMovementProcessor().getLastFlightTimer().isSet()
                || getData().getCollisionProcessor().getWebTicks() > 0
                || getData().getPistonUpdateTimer().getDelta() < 3
                || getData().isBedrock()
                //    || getData().getMovementProcessor().getVerticalClientCollision().getDelta() <5
                || getData().getLastWorldChange().getDelta() < 20
                // || getData().getWorldProcessor().getLastUpdateNearPlayer() < 4
                || getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                || getData().getPotionProcessor().isJumpPotion()
                && getData().getPotionProcessor().getJumpPotionTicks() < 1
                || !getData().getPotionProcessor().isJumpPotion()
                && getData().getPotionProcessor().getJumpPotionTicks() > 0
                || getData().getCollisionProcessor().getLiquidFullyTicks() > 0
                || getData().getCollisionProcessor()
                .getLastEnderDragonNearTimer().hasNotPassed(20)
                || getData().getVelocityProcessor().getExtraVelocityTicks() < 7
                || getData().getMovementProcessor().getTo() == null
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().getPotionProcessor().getJumpPotionAmplifier() > 90) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 1\n" + "vehicle="
                                + getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(9)
                                + "\nClimbing="+(getData().getCollisionWorldProcessor().getClimbableTicks() > 0)
                                + "\nPiston="+(getData().getCollisionWorldProcessor().getPistionTicks() > 0)
                                +"\nFlight="+(getData().getMovementProcessor().getLastFlightTimer().getDelta() < 40)
                                +"\nWeb="+(getData().getCollisionProcessor().getWebTicks() > 0)
                                +"\nPiston="+(getData().getPistonUpdateTimer().getDelta() < 3)
                                +"\nBedrock-User="+getData().isBedrock()
                                +"\nWorldChange="+(getData().getLastWorldChange().getDelta() < 20)
                                +"\nSnow="+getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                                +"\nJumpFix-A="+(getData().getPotionProcessor().isJumpPotion()
                                && getData().getPotionProcessor().getJumpPotionTicks() < 1)
                                +"\nJumpFix-B="+(!getData().getPotionProcessor().isJumpPotion()
                                && getData().getPotionProcessor().getJumpPotionTicks() > 0)
                                +"\nLiquid="+(getData().getCollisionProcessor().getLiquidFullyTicks() > 0)
                                +"\nDragon="+getData().getCollisionProcessor()
                                .getLastEnderDragonNearTimer().hasNotPassed(20)
                                +"\nJumpAmp="+getData().getPotionProcessor().getJumpPotionAmplifier()
                                +"\nNearBoat="+(getData().getCollisionProcessor().getNearBoatTicks() > 0)
                                +"\nNull Location="+(getData().getMovementProcessor().getTo() == null) + "\n");
            }
            return true;
        }

        if (getData().generalCancel()) {
            this.verbose = 0;
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 2 (General Cancel)\n");
            }
            return true;
        }


        if ((getData().getCollisionWorldProcessor().isHalfBlock()
                || getData().getCollisionWorldProcessor().getWallTicks() > 0)
                && getData().getMovementProcessor().isPositionGround()) {

            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 3 (Half Block Position Ground)\n");
            }
            return true;
        }


        if (getData().getMovementProcessor().getComboModeTicks() > 0) {
            this.verbose -= Math.min(this.verbose, .075);
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 3.5 (Combo Mode Exempt)\n");
            }
            return true;
        }

        if (getData().getGhostBlockProcessor().getLastInvalidTick() < 5) {
            this.verbose = 0;

            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 4 (Invalid Ghost Block Tick)\n");
            }
            return true;
        }

        if (getData().getGhostBlockProcessor().getLastGhostBlockTimer().getDelta() < 3
                && getData().getGhostBlockProcessor().getLastGhostBlockTimer().isSet()) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 5 (Ghost Block)\n");
            }
            return true;
        }

        double toY = getData().getMovementProcessor().getTo().getPosY();
        double fromY = getData().getMovementProcessor().getFrom().getPosY();

        double deltaXZ = this.getData().getMovementProcessor().getDeltaXZ();

        if (fromY > toY && deltaXZ < 0.125
                && getData().getPotionProcessor().getJumpPotionTicks() > 0) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 6 (Low DeltaXZ & Jump Potion While Moving Down)\n");
            }
            return true;
        }

        if (getData().getActionProcessor().isTeleportingV3()
                && getData().getCollisionWorldProcessor().getAnvilTicks() > 0) {
            this.verbose = 0;
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 7 (Anvil Teleport)\n");
            }
            return true;
        }


        if (getData().getCollisionProcessor().isWebFullCheck()
                || getData().getActionProcessor().getRespawnTimer().getDelta() < 10 && getData().getActionProcessor()
                .getRespawnTimer().isSet()
                || getData().getTeleportProcessor().isPossiblyTeleporting()
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 10
                && getData().getHorizontalProcessor().getLastSetBackTimer().isSet()
                || getData().getLastMovementCancelTimer().getDelta() < 5 && getData().getLastMovementCancelTimer().isSet()
                || getData().getCollisionProcessor().isWebInside()) {
            this.returnNextTick = true;
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 8 (Web/teleport/movement cancel): " +
                                ", setback="+(getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 10)
                                + ", teleporting="+getData().getTeleportProcessor().isPossiblyTeleporting()
                                + ", exploding="+(getData().getActionProcessor().getExplosionTimer().getDelta() < 10)
                                + ", cancel="+(getData().getLastMovementCancelTimer().getDelta() < 5)
                                + ", webInside=" + getData().getCollisionProcessor().isWebInside()
                                + ", webFullInside=" + getData().getCollisionProcessor().isWebFullCheck()
                                +"\n");
            }
            return true;
        }

        if (getData().getMovementProcessor().getLastGhostBlockAboveTick() >= 19
                && ++this.bypass < 20) {
            this.verbose = 0;

            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 9 (Ghost Above Block Bypass)\n");
            }
            return true;
        }

        if (getData().getLastRotate() > 0) {
            getData().setLastRotate(getData().getLastRotate() - 1);
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 21 (Rotate Simulator)\n");
            }
            return true;
        }

        if (getData().getHorizontalProcessor().getVelocitySimulator()
                .getLastVelocitySimulatedTimer().getDelta() < 3
                && getData().getHorizontalProcessor().getVelocitySimulator().getLastVelocitySimulatedTimer().isSet()) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 20 (Velocity Simulator)\n");
            }
            return true;
        }

        if (getData().getGhostBlockProcessor().getLiquidBypass() > 0) {

            if ((this.waterBypassCheck += 1) < 10) {
                this.verbose = 0;
                if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                        .getUniqueId().toString().equals(getData().getUuid().toString())) {
                    Anticheat.INSTANCE.getCheckPacketLog().add(
                            "Fly A Return Type 10 (Liquid Bypass)\n");
                }
                return true;
            } else {
                if (!this.secondChance) {
                    this.waterBypassCheck /= 1.25;
                    this.secondChance = true;
                }
            }
        }

        boolean thresholdFix = deltaY < 0 && deltaXZ < 0.2
                && (getData().getVelocityProcessor().getServerVelocityTicks() <= 20
                || getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 20);


        boolean collisionVelFix = ((getData().getVelocityProcessor().getServerVelocityTicks() <= 10
                || getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 10)
                && (getData().getCollisionWorldProcessor().getCollidingHorizontallyTimer().getDelta() < 3
                && getData().getCollisionWorldProcessor().getCollidingHorizontallyTimer().isSet()
                || getData().getMovementProcessor().getClientWallCollision().getDelta() < 3
                && getData().getMovementProcessor().getClientWallCollision().isSet()));


        if (collisionVelFix) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 11 (Collision Velocity Fix)\n");
            }
            return true;
        }

        if (thresholdFix && getData().getCollisionWorldProcessor().getBlockAboveTimer().getDelta() < 10
                && getData().getCollisionWorldProcessor().getBlockAboveTimer().isSet()) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 12 (Block Above Threshold Fix)\n");
            }
            return true;
        }

        if (getData().getCollisionWorldProcessor().getBlockAboveTicks() > 0
                && getData().getPotionProcessor().getJumpPotionTicks() > 0) {
            this.verbose -= Math.min(this.verbose, (this.zeroThree ? 0.03 : 0.01));

            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 13 (Block Above & Jump Potion)\n");
            }
            return true;
        }

        if (getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0
                && (getData().getCollisionWorldProcessor().isGround()
                || getData().getCollisionProcessor().isServerGround())
                && (getData().getCollisionWorldProcessor().getHalfBlockTimer().getDelta() < 10
                && getData().getCollisionWorldProcessor().getHalfBlockTimer().isSet()
                || getData().getCollisionWorldProcessor().getBlockAboveTimer().getDelta() < 20
                && getData().getCollisionWorldProcessor().getBlockAboveTimer().isSet())) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Fly A Return Type 14 (Block Above & Half Block)\n");
            }
            return true;
        }

        return false;
    }
}

