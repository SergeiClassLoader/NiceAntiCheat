package pro.cyrent.anticheat.api.check.impl.bedrock.move.fly;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "GeyserFly",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.GEYSER_FLY,
        description = "Detects if the player is moving vertically in the air improperly (bedrock only)",
        punishmentVL = 20,
        state = CheckState.ALPHA)
public class GeyserFlyA extends Check {

    private boolean zeroZeroThree, secondChance;

    private int fixTicks;

    private double threshold, waterBypassCheck, bypass;

    @Override
    public void onPacket(PacketEvent event) {

        if (!getData().isBedrock()) {
            return;
        }

        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasPositionChanged()
                    || getData().getMovementProcessor().getLastFlyingPacket() != null
                    && !getData().getMovementProcessor().getLastFlyingPacket().hasPositionChanged()
                    || getData().getMovementProcessor().getLastLastFlyingPacket() != null
                    && !getData().getMovementProcessor().getLastLastFlyingPacket().hasPositionChanged() ) {
                this.zeroZeroThree = true;
            } else {
                this.zeroZeroThree = false;
            }

            boolean clientGround = getData().getMovementProcessor().getTo().isOnGround();
            boolean lastGround = getData().getMovementProcessor().getFrom().isOnGround();

            if (!clientGround && lastGround
                    && getData().getCollisionWorldProcessor().getCollideHorizontalTicks() > 0) {
                this.fixTicks = 0;
            } else {
                if (this.fixTicks < 20) {
                    this.fixTicks++;
                }
            }

            if (!clientGround && !lastGround) {

                double deltaY = getData().getMovementProcessor().getDeltaY();
                double lastDeltaY = getData().getMovementProcessor().getLastDeltaY();

                double predicted = (lastDeltaY - 0.08) * 0.9800000190734863D;

                if (Math.abs(predicted) < 0.003) {
                    predicted = 0.0;
                }

                double offset = Math.abs(deltaY - predicted);

                double maxOffset = this.zeroZeroThree ? 0.12 : this.fixTicks > 0 ? 0.5 : 1E-3;

                if (offset >= maxOffset && !exempt()) {
                    if (++this.threshold > 12) {
                        this.fail("Invalid fly prediction",
                                "max-offset="+maxOffset,
                                "offset="+offset,
                                "deltaY="+deltaY,
                                "predicted="+predicted);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, 0.03);
                }
            }
        }
    }

    private boolean exempt() {
        if (getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(9)
                || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getMovementProcessor().getLastFlightTimer().getDelta() < 40
                || getData().getCollisionProcessor().isWater()
                || getData().getCollisionProcessor().getWebTicks() > 0
                || getData().getPistonUpdateTimer().getDelta() < 3
                || getData().getVelocityProcessor().getServerVelocityTicks() < 20
                || getData().getVelocityProcessor().getVelocityTicksConfirmed() < 20
                || getData().getCollisionWorldProcessor().getBlockAboveTimer().getDelta() < 20
                || getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                || getData().getPotionProcessor().isJumpPotion()
                && getData().getPotionProcessor().getJumpPotionTicks() < 1
                || !getData().getPotionProcessor().isJumpPotion()
                && getData().getPotionProcessor().getJumpPotionTicks() > 0
                || getData().getCollisionProcessor().getLiquidTicks() > 0
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
            this.threshold = 0;
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

        if (getData().getActionProcessor().isTeleportingV3()
                && getData().getCollisionWorldProcessor().getAnvilTicks() > 0) {
            this.threshold = 0;
            return true;
        }

        if (getData().getActionProcessor().isTeleportingV3()
                || getData().getActionProcessor().isTeleporting()
                || getData().getActionProcessor().getLastServerPositionTick() > 0
                || getData().getActionProcessor().isTeleportingReal()
                || getData().getLastTeleport().getDelta() < 20) {
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
