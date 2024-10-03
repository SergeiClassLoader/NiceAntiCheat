package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "InvalidMove", checkNameEnum = CheckName.INVALID_MOVE,
        subName = "F",
        checkType = CheckType.MOVEMENT,
        punishmentVL = 7,
        description = "Detects invalid motions while on ground (towers/steps)",
        state = CheckState.RELEASE)
public class InvalidMoveF extends Check {
    
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (flying.hasPositionChanged()) {

                if (getData().getActionProcessor().isTeleporting()
                        || getData().getPotionProcessor().getJumpPotionAmplifier() > 90
                        || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                        || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                        || getData().getMovementProcessor().getLastFlightTimer().hasNotPassed(20)
                        || getData().getCollisionProcessor().getNearBoatTicks() > 0
                        || getData().getCollisionProcessor()
                        .getLastEnderDragonNearTimer().hasNotPassed(20)
                        || getData().getLastExplosionTimer().hasNotPassed(40)
                        || getData().getPotionProcessor().isJumpPotion()
                        && getData().getPotionProcessor().getJumpPotionTicks() < 1
                        || !getData().getPotionProcessor().isJumpPotion()
                        || getData().getMovementProcessor().isBouncedOnBed()
                        && getData().getPotionProcessor().getJumpPotionTicks() > 0
                        || getData().getMovementProcessor().isBouncedOnSlime()
                        || getData().getCollisionWorldProcessor().getWallTicks() > 0
                        || getData().getCollisionProcessor().getLiquidFullyTicks() > 0
                       // || getData().getFishingRodVelocity().getDelta() < 2
                        || getData().getCollisionWorldProcessor().getHopperTicks() > 0
                        || getData().getCollisionWorldProcessor().isDoor()
                        || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                        || getData().getPistonUpdateTimer().getDelta() < 3
                        || getData().isBedrock()
                        || getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                        || getData().getCollisionWorldProcessor().isCarpet()
                        || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(20)
                        || getData().getCollisionWorldProcessor().isHalfBlock()) {
                    this.threshold = 0;
                    return;
                }

                if (getData().getVelocityProcessor().getServerVelocityTicks() <= 6
                        || getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 6
                        || getData().getActionProcessor().isTeleporting()) {
                    return;
                }

                if (getData().generalCancel()) {
                    return;
                }

                if (getData().getGhostBlockProcessor().getLastGhostBlockTimer().hasNotPassed(5)) {
                    return;
                }

                double deltaY = getData().getMovementProcessor().getDeltaY();

                boolean serverGround = getData().getCollisionProcessor().isServerGround();

                boolean ground = getData().getMovementProcessor().getTo().isOnGround();

                if (deltaY > 0.18 && ground && !serverGround) {

                    if (++this.threshold > 7) {
                        this.fail("deltaY="+deltaY);

                        if (Anticheat.INSTANCE.getConfigValues().isLagbacks()) {
                            getData().setBack();
                        }

                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .125);
                }
            }
        }
    }
}
