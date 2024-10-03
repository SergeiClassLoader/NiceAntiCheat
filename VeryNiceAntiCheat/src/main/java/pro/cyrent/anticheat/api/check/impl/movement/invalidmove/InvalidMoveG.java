package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;


import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "InvalidMove", checkNameEnum = CheckName.INVALID_MOVE,
        subName = "G",
        checkType = CheckType.MOVEMENT,
        punishmentVL = 50,
        punishable = false,
        enabled = false,
        description = "Detects invalid vertical movements on ladders",
        state = CheckState.ALPHA)
public class InvalidMoveG extends Check {

    private double threshold;
    private boolean secondChance = false;

    private int delayedFlyingTicks, lastVelocity;
    private double bypass, waterBypassCheck;
    private boolean zeroThree;

    private boolean position, lastPosition, lastLastPosition,
            lastLastLastPosition, lastLastLastLastPosition, lastLastLastLastLastPosition;

    public void onPost(PacketEvent event) {
        if (event.isMovement()) {
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

            if (Anticheat.INSTANCE.getVersionSupport().getClientProtocol(getData()) > 47) {
                if (!this.lastPosition || !this.lastLastPosition
                        || !this.lastLastLastPosition || !this.lastLastLastLastPosition
                        || !this.lastLastLastLastLastPosition) {
                    this.zeroThree = true;
                }
            } else {
                if (!this.lastPosition
                        || !this.lastLastPosition
                        || getData().getMovementProcessor().getDeltaXZ() < 0.1) {
                    this.zeroThree = true;
                }
            }

            ++this.delayedFlyingTicks;

            this.lastLastLastLastLastPosition = this.lastLastLastLastPosition;
            this.lastLastLastLastPosition = this.lastLastLastPosition;
            this.lastLastLastPosition = this.lastLastPosition;
            this.lastLastPosition = this.lastPosition;
            this.lastPosition = this.position;
            this.position = flying.hasPositionChanged();

            if (!flying.hasPositionChanged() || !getData().getCollisionWorldProcessor().isClimbing()) return;

            boolean clientGround = getData().getMovementProcessor().getTo().isOnGround();
            boolean lastGround = getData().getMovementProcessor().getFrom().isOnGround();

            double deltaY = getData().getMovementProcessor().getDeltaY();
            double lastLastDeltaY = getData().getMovementProcessor().getLastLastDeltaY();

            double distance = Double.MAX_VALUE;
            double predicted = Double.MAX_VALUE;

            //todo: poll on horizontal engine instead of split? (for vert poll or polllast or peaklast?)

            double lastDeltaY = getData().getMovementProcessor().getLastDeltaY();


            if (!this.lastPosition) {
                lastDeltaY = (lastLastDeltaY - 0.08D) * 0.9800000190734863D;

                if (Math.abs(lastDeltaY) < (Anticheat.INSTANCE.getVersionSupport().getClientProtocol(getData()) > 47
                        ? 0.003 : 0.005D)) {
                    lastDeltaY = 0.0D;
                }
            }

            double motionY = lastDeltaY - 0.08D;

            motionY *= 0.9800000190734863D;

            if (Math.abs(motionY) < (Anticheat.INSTANCE.getVersionSupport().getClientProtocol(getData()) > 47
                    ? 0.003 : 0.005D)) {
                motionY = 0.0D;
            }

            double jumpHeight = (0.42F + (getData().getPotionProcessor().getJumpPotionAmplifier() * 0.1F));

            if (!clientGround && lastGround
                    && deltaY == jumpHeight
                    && lastDeltaY != jumpHeight) {
                motionY = jumpHeight;
            }

            if (Math.abs(deltaY - motionY) > 1E-7) {
                //Fixed motions for ladders.
                double fixedUp = (.2D - 0.08D) * 0.9800000190734863D;
                double fixedDown = -0.15D;
                double fixedNoMotion = 0.0D;
                double fixedNoMotionNext = (0.0D - 0.08D) * 0.9800000190734863D;

                if (Math.abs(deltaY - fixedDown) < 1E-7) {
                    motionY = fixedDown;
                } else if (Math.abs(deltaY - fixedNoMotion) < 1E-7) {
                    motionY = fixedNoMotion;
                } else if (Math.abs(deltaY - fixedNoMotionNext) < 1E-7) {
                    motionY = fixedNoMotionNext;
                } else if (Math.abs(deltaY - fixedUp) < 1E-7) {
                    motionY = fixedUp;
                }
            }

            //fixes 0.08 issue with it not predicting at random.
            if (Math.abs(deltaY - motionY) > 1E-7) {
                double fixed = (motionY - 0.08D) * 0.9800000190734863D;

                if (Math.abs(deltaY - fixed) < 1E-7) {
                    motionY = fixed;
                }
            }

            final double current = Math.abs(motionY - deltaY);

            if (current < distance) {
                predicted = motionY;
                distance = current;
            }

            double deltaXZ = getData().getMovementProcessor().getDeltaXZ();

            //fix this better ^^^^ & vvvv
            final double threshold =
                    this.zeroThree && Anticheat.INSTANCE.getVersionSupport().getClientProtocol(getData()) > 47 ? 0.06D
                    : this.zeroThree && Anticheat.INSTANCE.getVersionSupport().getClientProtocol(getData()) <= 47 ? 0.03D
                    : 1e-06D;

            final boolean invalid = distance != Double.MAX_VALUE && predicted != Double.MAX_VALUE
                    && distance > threshold && !clientGround && !lastGround;

            if (invalid && !exempt(deltaY)) {

                if (++this.threshold > 2.5) {

                    this.fail("offset="+distance,
                            "maxOffset="+threshold,
                            "zeroZeroThree="+this.zeroThree,
                            "deltaY="+deltaY,
                            "predicted="+predicted,
                            "deltaXZ="+deltaXZ);


                }
            } else {
                this.threshold -= Math.min(this.threshold, (this.zeroThree ? 0.03 : 0.01));
            }
        }
    }


    private boolean exempt(double deltaY) {
        if (getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(9)
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getMovementProcessor().getLastFlightTimer().getDelta() < 40
                || getData().getCollisionWorldProcessor().isWater()
                || getData().getCollisionProcessor().getWebTicks() > 0
                || getData().getPistonUpdateTimer().getDelta() < 3
                || getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                || getData().getPotionProcessor().isJumpPotion()
                && getData().getPotionProcessor().getJumpPotionTicks() < 1
                || !getData().getPotionProcessor().isJumpPotion()
                && getData().getPotionProcessor().getJumpPotionTicks() > 0
                || getData().getCollisionWorldProcessor().getLiquidTicks() > 0
                || getData().isBedrock()
                || getData().getCollisionProcessor()
                .getLastEnderDragonNearTimer().hasNotPassed(20)
         //       || getData().getFishingRodVelocity().getDelta() < 2
          //      || getData().getLastExplosionTimer().hasNotPassed(40)
                || getData().getMovementProcessor().getTick() < 7
                || getData().getMovementProcessor().getTo() == null
                || getData().getMovementProcessor().getTo().getPosY() <= 0.0
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().getPotionProcessor().getJumpPotionAmplifier() > 90) {
            return true;
        }


        if (getData().generalCancel()) {
            return true;
        }

        if ((getData().getCollisionWorldProcessor().isHalfBlock()
                || getData().getCollisionWorldProcessor().getWallTicks() > 0)
                && getData().getMovementProcessor().isPositionGround()) {
            return true;
        }

        if (getData().getGhostBlockProcessor().getLastInvalidTick() < 3) {
            this.threshold = 0;
            return true;
        }

        if (getData().getGhostBlockProcessor().getLastGhostBlockTimer().getDelta() < 3) {
            return true;
        }

        double toY = getData().getMovementProcessor().getTo().getPosY();
        double fromY = getData().getMovementProcessor().getFrom().getPosY();

        double deltaXZ = this.getData().getMovementProcessor().getDeltaXZ();

        if (fromY > toY && deltaXZ < 0.125
                && getData().getPotionProcessor().getJumpPotionTicks() > 0) {
            return true;
        }

        if (getData().getActionProcessor().isTeleportingV2()
                && getData().getCollisionWorldProcessor().getAnvilTicks() > 0) {
            this.threshold = 0;
            return true;
        }

        if (getData().getActionProcessor().isTeleportingV2() || getData().getLastTeleport().getDelta() < 20) {
            return true;
        }

        if (getData().getMovementProcessor().getLastGhostBlockAboveTick() >= 19
                && ++this.bypass < 20) {
            this.threshold = 0;
            return true;
        }

        if (getData().getGhostBlockProcessor().getLiquidBypass() > 0) {

            if ((this.waterBypassCheck += 1) < 10) {
                this.threshold = 0;
                return true;
            } else {
                if (!this.secondChance) {
                    this.waterBypassCheck /= 1.25;
                    this.secondChance = true;
                }
            }
        }

        return false;
    }
}
