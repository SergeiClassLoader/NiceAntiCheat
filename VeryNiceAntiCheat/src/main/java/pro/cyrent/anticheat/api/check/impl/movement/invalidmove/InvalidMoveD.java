package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import org.bukkit.Material;


@CheckInformation(
        name = "InvalidMove", checkNameEnum = CheckName.INVALID_MOVE,
        subName = "D",
        checkType = CheckType.MOVEMENT,
        punishmentVL = 8,
        description = "Detects if a player is jumping but not moving up or down.",
        state = CheckState.BETA)
public class InvalidMoveD extends Check {
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            if (getData().getPotionProcessor().getJumpPotionAmplifier() > 90
                    || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                    || getData().getCollisionProcessor().getNearBoatTicks() > 0
                    || getData().getMovementProcessor().getLastFlightTimer().hasNotPassed(20)
                    || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(20)
                    || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                    || getData().generalCancel()
                    || getData().getHorizontalProcessor().getVelocitySimulator()
                    .getLastVelocitySimulatedTimer().getDelta() < 6
                    || getData().getCollisionWorldProcessor().getCarpetTicks() > 0
                    || getData().getCollisionWorldProcessor().isBed()
                    || getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                    || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                    || getData().getCollisionWorldProcessor().getWallTicks() > 0
                    || getData().getCollisionProcessor().getLiquidFullyTicks() > 0
                    || getData().getBlockProcessor().getLastCombatWallTicks() < 20
                    || getData().getPotionProcessor().isJumpPotion()
                    && getData().getPotionProcessor().getJumpPotionTicks() < 1
                    || !getData().getPotionProcessor().isJumpPotion()
                    || getData().getPotionProcessor().getInvalidPotionAmpliferTicks() <= 40
                    || getData().getCollisionWorldProcessor().isDoor()
                    || getData().getLastExplosionTimer().hasNotPassed(40)
                    || getData().getCollisionWorldProcessor().getAnvilTicks() > 0
                    || getData().getCollisionWorldProcessor().getCauldronTicks() > 0
                    || getData().getCollisionWorldProcessor().getHopperTicks() > 0
                    || getData().getCollisionWorldProcessor().isSkull()
                    || getData().getCollisionWorldProcessor().getSoulSandTicks() > 0
                    || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                    || getData().getCollisionWorldProcessor().getEnderPortalTicks() > 0
                    || getData().isBedrock()
                    || getData().getPistonUpdateTimer().getDelta() < 3
                    || getData().getCollisionProcessor().getWebTicks() > 0
                    || getData().getCollisionWorldProcessor().getLillyPadTicks() > 0
                    || getData().getCollisionProcessor()
                    .getLastEnderDragonNearTimer().hasNotPassed(20)
                    || getData().getCollisionWorldProcessor().getBlockAboveTimer().hasNotPassed(9)
                    || getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0) {
                this.threshold = 0;
                return;
            }

            if (getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 20
                    || getData().getVelocityProcessor().getServerVelocityTicks() <= 20) {
                this.threshold = 0;
                return;
            }

            if (getData().getActionProcessor().isTeleportingV2() || getData().getActionProcessor().isTeleportingV3()) {
                return;
            }

            if (getData().getMovementProcessor().getLastGhostBlockAboveTick() > 0) {
                return;
            }

            if (getData().getGhostBlockProcessor().getLastGhostBlockTimer().hasNotPassed(5)) {
                return;
            }

            boolean ground = getData().getMovementProcessor().getTo().isOnGround();
            boolean lastGround = getData().getMovementProcessor().getFrom().isOnGround();

            double deltaY = getData().getMovementProcessor().getDeltaY();

            if (!ground && lastGround) {

                if (deltaY == 0.0) {

                    if (getData().getPlayer().getItemInHand() != null
                            && (getData().getLastBlockPlaceCancelTimer().getDelta() < 20
                            || getData().getBlockProcessor()
                            .getLastPlacementPacket().getDelta() < 10
                            || getData().getBlockProcessor()
                            .getLastConfirmedCancelPlaceTimer().getDelta() < 20)
                            && (getData().getPlayer().getItemInHand().getType() == Material.LADDER
                            || getData().getPlayer().getItemInHand().getType() == Material.VINE
                            || getData().getPlayer().getItemInHand().getType() == Material.WEB
                            || getData().getPlayer().getItemInHand().getType() == Material.WATER
                            || getData().getPlayer().getItemInHand().getType() == Material.WATER_BUCKET
                            || getData().getPlayer().getItemInHand().getType() == Material.LAVA
                            || getData().getPlayer().getItemInHand().getType() == Material.LAVA_BUCKET
                            || getData().getPlayer().getItemInHand().getType() == Material.SLIME_BLOCK)) {

                        if (!Anticheat.INSTANCE.getConfigValues().isGhostBlockSupport()) {

                            getData().consoleLogLagBack("InvalidMove D, deltaY=" + deltaY);

                            getData().setBack();
                        }
                        return;
                    }

                    if (++this.threshold > 2.0) {
                        this.fail("deltaY=" + deltaY, "threshold=" + threshold);

                        if (Anticheat.INSTANCE.getConfigValues().isLagbacks()) {
                            getData().setBack();
                        }

                    }
                } else {
                    this.threshold -= Math.min(this.threshold, 0.03);
                }
            }
        }
    }
}