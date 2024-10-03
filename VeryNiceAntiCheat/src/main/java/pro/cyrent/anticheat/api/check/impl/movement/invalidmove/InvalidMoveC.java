package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.impl.processor.combat.VelocityProcessor;


@CheckInformation(
        name = "InvalidMove",
        checkNameEnum = CheckName.INVALID_MOVE,
        subName = "C",
        checkType = CheckType.MOVEMENT,
        punishmentVL = 12,
        description = "Detects if the player moves upwards at a high rate",
        state = CheckState.BETA)
public class InvalidMoveC extends Check {

    private int bypass;
    private int fallTicks;
    private double threshold;
    private int ticksBypass = 0;

    private double slimeHeight = 0;

    private double lastGroundY = Double.MAX_VALUE;

    public void runPacket(PacketEvent event) {
        if (event.isMovement()) {

            boolean ground = getData().getMovementProcessor().getTo().isOnGround()
                    || getData().getCollisionWorldProcessor().isGround();

            if (getData().getMovementProcessor().getTo().getPosY()
                    < getData().getMovementProcessor().getFrom().getPosY()) {
                if (this.fallTicks < 20) {
                    this.fallTicks++;
                }
            } else {
                this.fallTicks -= Math.min(this.fallTicks, 2);
            }

            double deltaY = getData().getMovementProcessor().getDeltaY();
            double lastDeltaY = getData().getMovementProcessor().getLastDeltaY();
            double currentPositionY = getData().getMovementProcessor().getTo().getPosY();

            double max = this.fallTicks > 0 ? .59261F : 0.42F
                    + (getData().getPotionProcessor().getJumpPotionAmplifier() * 0.1F);

            if (getData().getPotionProcessor().getJumpPotionTicks() > 0) {
                max += 0.03;
            }

            boolean velocity = false;

            if (getData().getVelocityProcessor().getServerVelocityTicks() < 20
                    || getData().getVelocityProcessor().getVelocityATicks() < 20) {
                max += Math.abs(getData().getVelocityProcessor().getVelocityY());
                velocity = true;
            }

            if(getData().getCollisionWorldProcessor().isSlime() && ground && deltaY < 0) {
                this.slimeHeight = -lastDeltaY;
            } else if (getData().getCollisionWorldProcessor().getSlimeTicks() == 0
                    && !getData().getMovementProcessor().isBouncedOnSlime()) {
                this.slimeHeight = 0;
            }

            if (this.slimeHeight != 0 && this.slimeHeight >= max) {
                max = this.slimeHeight;
            }

                if (getData().getMovementProcessor().isLastHeldItemExempt()) {
                    this.ticksBypass = 20;
                    return;
                }

            if (this.ticksBypass > 0) {
                this.ticksBypass--;
            }

            boolean dynamic = false;
            double groundMax = 0;
            double distance = 0;

            if (this.lastGroundY != Double.MAX_VALUE) {

                double distanceFromLastGround = currentPositionY - this.lastGroundY;
                double minGround = this.lastGroundY + max + 3.5;

                if (getData().getVelocityProcessor().getVelocityATicks() < 20
                        || getData().getVelocityProcessor().getServerVelocityTicks() < 20) {
                    minGround += 3.0;
                }

                if (getData().getActionProcessor().getExplosionTimer().getPositionDelta() < 100
                        || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                        || getData().generalCancel()
                        || exempt()
                        || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                        || getData().getMovementProcessor().isBouncedOnSlime()
                        || getData().getMovementProcessor().isBouncedOnBed() && getData().getProtocolVersion() > 47
                        || getData().generalCancel()) {
                    this.lastGroundY = Double.MAX_VALUE;
                }

                if (currentPositionY >= minGround && lastGroundY != Double.MAX_VALUE
                        && distanceFromLastGround > (8.0 + max)) {
                    dynamic = true;
                    groundMax = minGround;
                    distance = distanceFromLastGround;
                }
            }

            if (ground) {
                this.lastGroundY = currentPositionY;
            }

            if (deltaY > max && this.ticksBypass <= 0 && !exempt() || dynamic && !exempt()) {

                getData().getSetBackProcessor().setLastInvalidTick(50);

                if (++this.threshold > 1.0) {

                    if (dynamic) {
                        this.fail("(Dynamic)",
                                "deltaY=" + deltaY,
                                "threshold=" + this.threshold,
                                "took-velocity=" + velocity,
                                "height-at="+currentPositionY,
                                "height-limit="+groundMax,
                                "distanceFromGround="+distance,
                                "fallTicks=" + this.fallTicks);
                    } else {
                        this.fail("(Limit)",
                                "deltaY=" + deltaY,
                                "threshold=" + this.threshold,
                                "max=" + max,
                                "took-velocity=" + velocity,
                                "fallTicks=" + this.fallTicks);
                    }
                }
            } else {
                this.threshold -= Math.min(this.threshold, 0.003);
            }
        }
    }


    private boolean exempt() {
        if (getData().getActionProcessor().getLastVehicleTimer().isSet() && getData().getActionProcessor().getLastVehicleTimer().getDelta() < 10
                || getData().getCollisionProcessor().getLastEnderDragonNearTimer().isSet() && getData().getCollisionProcessor().getLastEnderDragonNearTimer().hasNotPassed(20)
                || getData().getCollisionWorldProcessor().isBed()
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 5
                || getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                || getData().getCollisionWorldProcessor().isSkull()
                || getData().getWitherTimer().getDelta() < 20
                || getData().getActionProcessor().getRespawnTimer().isSet() && getData().getActionProcessor().getRespawnTimer().getDelta() < 10
                || getData().getSetBackProcessor().getSetBackTick() > 0
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 10
                || getData().getCollisionWorldProcessor().getHopperTicks() > 0
                || getData().getLastExplosionTimer().isSet()
                && getData().getLastExplosionTimer().getPositionDelta() < 100
                || getData().getHorizontalProcessor().getVelocitySimulator().getLastVelocitySimulatedTimer().isSet() && getData().getHorizontalProcessor().getVelocitySimulator().getLastVelocitySimulatedTimer().getDelta() < 6
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().getVelocityProcessor().getExtraVelocityTicks() < 60
                || getData().getActionProcessor().getExplosionTimer().getPositionDelta() < 100
                || getData().getPotionProcessor().isJumpPotion() && getData().getPotionProcessor().getJumpPotionTicks() < 1
                || !getData().getPotionProcessor().isJumpPotion() && getData().getPotionProcessor().getJumpPotionTicks() > 0
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getActionProcessor().isTeleportingV3()
                || getData().getTeleportProcessor().isPossiblyTeleporting()
                || getData().getActionProcessor().getTeleportTicks() < 3
                || getData().getCollisionWorldProcessor().getWallTicks() > 0
                || getData().getCollisionProcessor().getLiquidFullyTicks() > 0
                || getData().getPistonUpdateTimer().isSet() && getData().getPistonUpdateTimer().getDelta() < 3
                || getData().getMovementProcessor().isBouncedOnBed()
                || getData().isBedrock()
                || getData().getPotionProcessor().getJumpPotionTicks() > 0
                || getData().getCollisionWorldProcessor().isDoor()
                || getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0
                || getData().getMovementProcessor().getLastFlightTimer().isSet() && getData().getMovementProcessor().getLastFlightTimer().hasNotPassed(10)) {
            this.threshold -= Math.min(this.threshold, .005);
            return true;
        }

        if (getData().getLastFallDamageTimer().isSet() && getData().getLastFallDamageTimer().getPositionDelta() < 3) {
            this.threshold -= Math.min(this.threshold, .005);
            return true;
        }

        if (getData().generalCancel()) {
            return true;
        }

        if (getData().getCollisionWorldProcessor().getAnvilTicks() > 0) {
            return true;
        }

        if (getData().getMovementProcessor().getLastGhostBlockAboveTick() >= 19
                && this.bypass < 20) {
            this.bypass++;
            this.threshold = 0;
            return true;
        }

        if (getData().getGhostBlockProcessor().getLastGhostBlockTimer().isSet() && getData().getGhostBlockProcessor().getLastGhostBlockTimer().hasNotPassed(1)) {
            return true;
        }

        return false;
    }
}