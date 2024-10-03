package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "InvalidMove", checkNameEnum = CheckName.INVALID_MOVE,
        subName = "B",
        checkType = CheckType.MOVEMENT,
        punishmentVL = 5,
        description = "Detects if the player moves up and down rapidly",
        state = CheckState.RELEASE)
public class InvalidMoveB extends Check {

    private double waterBypassCheck;
    private int bypass;
    private boolean secondChance = false;

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            double deltaY = getData().getMovementProcessor().getDeltaY();
            double lastDeltaY = getData().getMovementProcessor().getLastDeltaY();

            if (deltaY > 0.0 && lastDeltaY <= 0.0 && !exempt()) {
                if (++this.threshold > 3.5) {

                    this.getData().getSetBackProcessor().setLastInvalidTick(50);
                    this.fail("dy=" + deltaY, "ldy=" + lastDeltaY);
                    this.threshold = 3.5;
                }
            } else {
                this.threshold -= Math.min(this.threshold, .125);
            }
        }
    }

    private boolean exempt() {
        if (getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                || getData().getActionProcessor().getLastVehicleTimer().isSet()
                && getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(19)
                || getData().getCollisionProcessor().getLastEnderDragonNearTimer().isSet()
                && getData().getCollisionProcessor().getLastEnderDragonNearTimer().hasNotPassed(19)
                || getData().getCollisionWorldProcessor().isBed()
                || getData().getSetBackProcessor().getSetBackTick() > 0
                || getData().getCollisionWorldProcessor().getSoulSandTicks() > 0
                || getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                || getData().getCollisionWorldProcessor().isSkull()
                || getData().getCollisionWorldProcessor().getHopperTicks() > 0
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 5
                || getData().getCollisionWorldProcessor().getAnvilTicks() > 0
                || getData().getLastExplosionTimer().isSet() && getData().getLastExplosionTimer().hasNotPassed(39)
                || getData().getCollisionWorldProcessor().getCarpetTicks() > 0
                || getData().getHorizontalProcessor().getVelocitySimulator().getLastVelocitySimulatedTimer().isSet()
                && getData().getHorizontalProcessor().getVelocitySimulator().getLastVelocitySimulatedTimer().getDelta() < 6
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().getCollisionProcessor().isWeb()
                || getData().getHorizontalProcessor().getVelocitySimulator().getLastVelocitySimulatedTimer().isSet()
                && getData().getHorizontalProcessor().getVelocitySimulator().getLastVelocitySimulatedTimer().getDelta() < 6
                || getData().getCollisionWorldProcessor().isClimbing()
                || getData().getPotionProcessor().isJumpPotion() && getData().getPotionProcessor().getJumpPotionTicks() < 1
                || !getData().getPotionProcessor().isJumpPotion() && getData().getPotionProcessor().getJumpPotionTicks() > 0
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getActionProcessor().getTeleportTicks() < 3
                || getData().getCollisionWorldProcessor().getWallTicks() > 0
                || getData().getCollisionWorldProcessor().getLillyPadTicks() > 0
                || getData().getLastCactusDamageTimer().isSet() && getData().getLastCactusDamageTimer().getDelta() < 20
                || getData().getCollisionProcessor().getLiquidFullyTicks() > 0
                || getData().isBedrock()
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 10
                || getData().getPistonUpdateTimer().isSet() && getData().getPistonUpdateTimer().getDelta() < 3
                || getData().getCollisionWorldProcessor().isDoor()
                || getData().getMovementProcessor().getLastFlightTimer().isSet()
                && getData().getMovementProcessor().getLastFlightTimer().hasNotPassed(19)
                || getData().getCollisionWorldProcessor().getBlockAboveTimer().isSet()
                && getData().getCollisionWorldProcessor().getBlockAboveTimer().hasNotPassed(9)
                || getData().getCollisionWorldProcessor().isHalfBlock()) {
            this.threshold -= Math.min(this.threshold, .125);
            return true;
        }

        if (getData().getLastFallDamageTimer().isSet()
                && getData().getLastFallDamageTimer().getDelta() < 10) {
            this.threshold -= Math.min(this.threshold, .25);
            return true;
        }

        if (getData().generalCancel()) {
            return true;
        }

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
                        "InvalidMove B Return Type 1 (Collision Velocity Fix)\n");
            }
            return true;
        }

        if (getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 10
                || getData().getVelocityProcessor().getServerVelocityTicks() <= 10
                || getData().getActionProcessor().isTeleporting()) {
            return true;
        }

        if (getData().getActionProcessor().isTeleporting()
                && getData().getCollisionWorldProcessor().getAnvilTicks() > 0) {
            this.threshold = 0;
            return true;
        }

        if (getData().getGhostBlockProcessor().getLastGhostBlockTimer().isSet()
                && getData().getGhostBlockProcessor().getLastGhostBlockTimer().hasNotPassed(5)) {
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