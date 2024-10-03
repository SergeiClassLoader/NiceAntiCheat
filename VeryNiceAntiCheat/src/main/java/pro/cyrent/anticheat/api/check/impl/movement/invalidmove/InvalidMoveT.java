package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "InvalidMove",
        subName = "T",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.INVALID_MOVE,
        description = "Detects if a player moves quickly down repetitively",
        punishable = false,
        state = CheckState.PRE_ALPHA)
public class InvalidMoveT extends Check {

    private double threshold;

    private boolean set = false;
    private double lastHighestPosY;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasPositionChanged()) {
                return;
            }

            if (getData().isBedrock()) return;

            double deltaY = getData().getMovementProcessor().getDeltaY();
            double lastDeltaY = getData().getMovementProcessor().getLastDeltaY();

            double toY = getData().getMovementProcessor().getTo().getPosY();
            double fromY = getData().getMovementProcessor().getFrom().getPosY();

            if (fromY > toY) {
                this.lastHighestPosY = fromY;
                this.set = true;
            }

            double distance = Math.abs(toY - this.lastHighestPosY);
            double fullDelta = Math.abs(deltaY - lastDeltaY);

            if (deltaY < -1.0D && this.set && (getData().getMovementProcessor().getTo().isOnGround()
                    || getData().getMovementProcessor().getAirTicks() < 12)
                    && distance < 2.8 && fullDelta > 0.2D && !exempt()) {
                if (++this.threshold > 3.0) {
                    this.fail("Moving down rapidly a lot",
                            "deltaY="+deltaY,
                            "lastDeltaY="+lastDeltaY,
                            "distance="+distance,
                            "threshold="+this.threshold);
                }
            } else {
                // old 0.005
                this.threshold -= Math.min(this.threshold, .0115);
            }
        }
    }

    private boolean exempt() {
        if (getData().generalCancel()
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().isBedrock()
                || getData().getLastTeleport().getDelta() < 10
                || getData().getActionProcessor().getRespawnTimer().isSet()
                && getData().getActionProcessor().getRespawnTimer().getDelta() < 20
                || getData().getCollisionWorldProcessor().getLillyPadTicks() > 0
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 5
                || getData().getSetBackProcessor().getSetBackTick() > 0
                || (getData().getGhostBlockProcessor().getLastGhostBlockTimer().isSet() &&
                getData().getGhostBlockProcessor().getLastGhostBlockTimer().getDelta() < 3)
                || getData().getBlockProcessor().getLastCombatWallTicks() < 20
                || getData().getBlockProcessor().getLastWebUpdateTick() < 7
                || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                || getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                || (getData().getPotionProcessor().getSpeedPotionAmplifier() == 0 && getData().getPotionProcessor().isSpeedPotion())
                || (getData().getPotionProcessor().getSlownessAmplifier() == 0 && getData().getPotionProcessor().isSlownessPotion())
                || getData().getLastExplosionTimer().hasNotPassed(40)
                || getData().getActionProcessor().getExplosionTimer().getDelta() < 5
                || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(20)
                || getData().getLastSuffocationTimer().hasNotPassed(3)
                || getData().getActionProcessor().isTeleportingV2()
                || (getData().getLastFallDamageTimer().isSet() &&
                getData().getLastFallDamageTimer().getDelta() < 3)) {
            this.threshold -= Math.min(this.threshold, .005);
            return true;
        }

        if (getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().hasNotPassed(5)) {
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