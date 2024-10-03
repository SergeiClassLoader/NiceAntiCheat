package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "InvalidMove", checkNameEnum = CheckName.INVALID_MOVE,
        subName = "P",
        checkType = CheckType.MOVEMENT,
        punishmentVL = 8,
        punishable = false,
        description = "Detects invalid movement speed in webs",
        state = CheckState.ALPHA)
public class InvalidMoveP extends Check {

    private double threshold;
    private double zeroThreeThreshold;
    private int ticks;

    private int inOutFix = 0;
    private boolean lastInside = false;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());
            WrapperPlayClientPlayerFlying lastFlyingPacket = getData().getMovementProcessor().getLastFlyingPacket();
            WrapperPlayClientPlayerFlying lastLastFlyingPacket = getData().getMovementProcessor().getLastLastFlyingPacket();

            if (lastFlyingPacket == null || lastLastFlyingPacket == null) return;

            if (getData().generalCancel()) {
                return;
            }

            if (getData().getActionProcessor().isTeleportingV2()
                    || getData().getCollisionWorldProcessor().getIceTicks() > 0
                    || getData().isBedrock()
                    || getData().getHorizontalProcessor().getVelocitySimulator()
                    .getLastVelocitySimulatedTimer().getDelta() < 6
                    || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                    || getData().getLastBlockBreakTimer().getDelta() < 20
                    || getData().getLastExplosionTimer().getDelta() < 100
                    || getData().getPistonUpdateTimer().getDelta() < 20
                    || getData().getVelocityProcessor().getServerVelocityTicks() < 20
                    || getData().getVelocityProcessor().getVelocityTicksConfirmed() < 20
                    || getData().getPotionProcessor().getSpeedPotionAmplifier() > 10) {
                return;
            }

            boolean ground = getData().getMovementProcessor().getTo().isOnGround(),
                    lastGround = getData().getMovementProcessor().getFrom().isOnGround();

            boolean notGround = !getData().getMovementProcessor().getTo().isOnGround()
                    && !getData().getMovementProcessor().getFrom().isOnGround();

            boolean inside = getData().getCollisionProcessor().isWebInside();

            double max = 0.06F;

            if (inside != this.lastInside) {
                this.inOutFix = 20;
            }

            if (this.inOutFix-- > 0) {
                max = 0.12;
            }

            //in web
            if (getData().getCollisionProcessor().isWebInside() && this.lastInside
                    && getData().getCollisionProcessor().getWebTicks() > 12) {

                double xz = getData().getMovementProcessor().getDeltaXZ();

                if (notGround) {
                    if (getData().getProtocolVersion() <= 47) {
                        if (flying.hasPositionChanged()
                                && lastFlyingPacket.hasPositionChanged()
                                && lastLastFlyingPacket.hasPositionChanged() && !exempt()) {

                            this.getData().getSetBackProcessor().setLastInvalidTick(50);

                            if (++zeroThreeThreshold > 7.5) {
                                this.fail("deltaXZ="+xz,
                                        "threshold="+this.zeroThreeThreshold);
                            }
                        } else {
                            this.zeroThreeThreshold -= Math.min(this.zeroThreeThreshold, 0.03);
                        }
                    }
                }

                if (!ground && lastGround || ground && !lastGround) {
                    this.ticks = 7;
                }

                if (this.ticks-- > 0) {
                    max = 0.1F;
                }

                if (getData().getPotionProcessor().isSpeedPotion()) {
                    max += getData().getPotionProcessor().getSpeedPotionAmplifier() * 0.00825;
                }

                if (xz >= max && !exempt()) {

                    this.getData().getSetBackProcessor().setLastInvalidTick(50);

                    if (++this.threshold > 7.0) {
                        this.fail(
                                "speed="+xz,
                                "max="+max,
                                "threshold="+this.threshold);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .05);
                }
            }

            this.lastInside = inside;
        }
    }

    private boolean exempt() {
        if (getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(9)
                || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getMovementProcessor().getLastFlightTimer().getDelta() < 40
                || getData().getPistonUpdateTimer().getDelta() < 3
                || getData().isBedrock()
                || getData().getVelocityProcessor().getVelocityATicks() < 20
                || getData().getVelocityProcessor().getServerVelocityTicks() < 20
                || getData().getLastWorldChange().getDelta() < 20
                || getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                || getData().getPotionProcessor().isJumpPotion()
                && getData().getPotionProcessor().getJumpPotionTicks() < 1
                || !getData().getPotionProcessor().isJumpPotion()
                && getData().getPotionProcessor().getJumpPotionTicks() > 0
                || getData().getCollisionProcessor().getLiquidFullyTicks() > 0
                || getData().getCollisionProcessor()
                .getLastEnderDragonNearTimer().hasNotPassed(20)
                || getData().getMovementProcessor().getTick() < 7
                || getData().getMovementProcessor().getTo() == null
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().getPotionProcessor().getJumpPotionAmplifier() > 90) {
            this.threshold -= Math.min(this.threshold, 0.045);
            return true;
        }

        if (getData().generalCancel()) {
            this.threshold -= Math.min(this.threshold, 0.045);
            return true;
        }

        if ((getData().getCollisionWorldProcessor().isHalfBlock()
                || getData().getCollisionWorldProcessor().getWallTicks() > 0)
                && getData().getMovementProcessor().isPositionGround()) {
            this.threshold -= Math.min(this.threshold, 0.045);
            return true;
        }

        if (getData().getGhostBlockProcessor().getLastInvalidTick() < 3) {
            this.threshold = 0;
            return true;
        }

        if (getData().getGhostBlockProcessor().getLastGhostBlockTimer().getDelta() < 3) {
            return true;
        }

        if (getData().getActionProcessor().isTeleportingV3()
                && getData().getCollisionWorldProcessor().getAnvilTicks() > 0) {
            this.threshold = 0;
            return true;
        }

        if (getData().getActionProcessor().isTeleportingV2()
                || getData().getLastTeleport().getDelta() < 40) {
            return true;
        }

        return false;
    }
}
