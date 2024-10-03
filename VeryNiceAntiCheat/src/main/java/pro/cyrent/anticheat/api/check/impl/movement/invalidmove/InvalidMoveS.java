package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "InvalidMove",
        subName = "S",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.INVALID_MOVE,
        description = "Detects abnormal wall collision motion spoofing",
        punishable = false,
        state = CheckState.PRE_ALPHA)
public class InvalidMoveS extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasPositionChanged()) {
                return;
            }

            if (getData().isBedrock()) return;

            double deltaY = getData().getMovementProcessor().getDeltaY();

            boolean positionGround = getData().getMovementProcessor().getTo().getPosY() % 0.015625 == 0;

            boolean serverGround = getData().getCollisionWorldProcessor().isGround()
                    || getData().getCollisionProcessor().isServerGround();

            boolean clientGround = getData().getMovementProcessor().getTo().isOnGround();


            if ((positionGround || deltaY % 0.015625 == 0)
                    && getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0) {
                return;
            }

            if (Math.abs(deltaY - 0.5926045976350611) < 1E-9 && getData().getMovementProcessor().getLastDeltaY() < 0) {
                return;
            }

            if ((deltaY < -0.0784D && !serverGround || deltaY > 0.0) && clientGround && !exempt()) {
                if (++this.threshold > 3.0) {
                    this.fail(
                            "deltaY="+deltaY);
                }
            } else {
                this.threshold -= Math.min(this.threshold, .005);
            }
        }
    }

    private boolean exempt() {
        if (getData().generalCancel()
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().isBedrock()
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 5
                || getData().getSetBackProcessor().getSetBackTick() > 0
                || (getData().getGhostBlockProcessor().getLastGhostBlockTimer().isSet() &&
                getData().getGhostBlockProcessor().getLastGhostBlockTimer().getDelta() < 3)
                || getData().getBlockProcessor().getLastCombatWallTicks() < 20
                || getData().getBlockProcessor().getLastWebUpdateTick() < 7
                || getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                || (getData().getPotionProcessor().getSpeedPotionAmplifier() == 0 && getData().getPotionProcessor().isSpeedPotion())
                || (getData().getPotionProcessor().getSlownessAmplifier() == 0 && getData().getPotionProcessor().isSlownessPotion())
                || getData().getLastExplosionTimer().hasNotPassed(40)
                || getData().getActionProcessor().getExplosionTimer().getDelta() < 5
                || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(20)
                || getData().getLastSuffocationTimer().hasNotPassed(3)
                || getData().getActionProcessor().isTeleportingV2()
                || (getData().getLastFallDamageTimer().isSet()
                || getData().getLastCactusDamageTimer().isSet() && getData().getLastCactusDamageTimer().getDelta() < 20
                || getData().getPistonUpdateTimer().getDelta() < 3
                || getData().getCollisionWorldProcessor().getSoulSandTicks() > 0
                && getData().getLastFallDamageTimer().getDelta() < 3)) {
            this.threshold -= Math.min(this.threshold, .005);
            return true;
        }


        if (getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().hasNotPassed(40)
                || getData().getLastBlockPlaceTimer().getDelta() < 40
                || getData().getLastBlockPlaceCancelTimer().getDelta() < 40) {
            this.threshold -= Math.min(this.threshold, .005);
            return true;
        }

        if (getData().getCollisionProcessor().getWebTicks() != 0) {
            this.threshold -= Math.min(this.threshold, .005);
            return true;
        }

        return false;
    }

}
