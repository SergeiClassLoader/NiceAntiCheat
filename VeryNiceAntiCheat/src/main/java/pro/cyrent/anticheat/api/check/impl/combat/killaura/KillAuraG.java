package pro.cyrent.anticheat.api.check.impl.combat.killaura;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.impl.processor.prediction.horizontal.HorizontalProcessor;
import pro.cyrent.anticheat.util.block.collide.CollisionUtil;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "KillAura",
        subName = "G",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.KILL_AURA,
        punishmentVL = 10,
        description = "Detects LiquidBounces KillAura (on any setting)",
        state = CheckState.ALPHA)
public class KillAuraG extends Check {
    
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {

                WrapperPlayClientPlayerFlying flyingPacket = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

                if (getData().generalCancel()
                        || getData().getActionProcessor().isTeleporting()
                        || getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 10
                        || getData().getVelocityProcessor().getServerVelocityTicks() <= 10
                        || getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                        || getData().getCollisionWorldProcessor().isHalfBlock()
                        || getData().getCollisionProcessor().getLiquidTicks() > 0
                        || getData().getCollisionWorldProcessor().isCarpet()
                        || getData().getActionProcessor().getTeleportTicks() < 4
                        || getData().getCollisionWorldProcessor().getBlockAboveTicks() > 0
                        || getData().getCollisionProcessor().getMountTicks() > 0
                        || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                        || getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() < 2
                        || getData().getCollisionWorldProcessor().getAnvilTicks() > 0
                        || getData().getMovementProcessor().getClientWallCollision().getDelta() < 10
                        || getData().getCollisionProcessor().isWeb()
                        || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                        || getData().getCollisionWorldProcessor().getIceTicks() > 0
                        || getData().getCollisionWorldProcessor().isBed()
                        || getData().getPotionProcessor().isSlownessPotion()
                        || getData().getCollisionWorldProcessor().getSoulSandTicks() > 0
                        || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                        || getData().getCollisionWorldProcessor().getCollidingHorizontallyTimer().hasNotPassed(8)
                        || !getData().getPotionProcessor().isSpeedPotion()
                        && getData().getPotionProcessor().getSpeedPotionTicks() > 0
                        || getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 2) {
                    return;
                }

                final HorizontalProcessor velocityCheckProcessor =
                        getData().getHorizontalProcessor();

                if (velocityCheckProcessor == null) return;

                WrapperPlayClientPlayerFlying lastFlying = getData().getMovementProcessor().getLastFlyingPacket();
                WrapperPlayClientPlayerFlying lastLastLast = getData().getMovementProcessor().getLastLastFlyingPacket();

                if (lastFlying == null || lastLastLast == null) return;

                // zero zero three
                if ((flyingPacket.hasPositionChanged() && lastFlying.hasPositionChanged() && lastLastLast.hasPositionChanged())) {

                    if (CollisionUtil.isNearWall(this.getData().getMovementProcessor().getTo())
                            || CollisionUtil.isNearWall(this.getData().getMovementProcessor().getFrom())) {
                        return;
                    }

                    if (velocityCheckProcessor.getLiquidBounceDetector() > 7) {

                        if (++this.threshold > 2.5) {
                            this.fail("Old-Liquid-Threshold="+velocityCheckProcessor.getLiquidBounceDetector());
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .0125);
                    }
                }
            }
        }
    }
}
