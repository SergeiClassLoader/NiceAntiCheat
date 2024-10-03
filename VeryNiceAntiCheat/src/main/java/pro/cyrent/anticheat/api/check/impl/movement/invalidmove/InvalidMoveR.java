package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInformation(
        name = "InvalidMove",
        subName = "R",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.INVALID_MOVE,
        description = "Detects abnormal wall collision motion spoofing",
        punishable = false,
        state = CheckState.PRE_RELEASE)
public class InvalidMoveR extends Check {

    private double threshold;
    private boolean requireHeadMovement = false;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (getData().isBedrock()) {
                return;
            }

            boolean horizontalCollide = getData().getCollisionWorldProcessor().isCollidingHorizontal();
            boolean clientCollide = (getData().getMovementProcessor().getClientWallCollision().isSet()
                    && getData().getMovementProcessor().getClientWallCollision().getDelta() < 2);

            if (horizontalCollide) {
                this.requireHeadMovement = true;
            }

            double deltaX = getData().getMovementProcessor().getDeltaXAbs();
            double deltaZ = getData().getMovementProcessor().getDeltaZAbs();

            boolean invalidDistance = deltaX < 0.005 && !flying.hasRotationChanged()
                    || deltaZ < 0.005 && !flying.hasRotationChanged();

            double deltaYaw = getData().getMovementProcessor().getDeltaYaw();
            double deltaPitch = getData().getMovementProcessor().getDeltaPitch();

            double distance = Math.abs(deltaYaw - deltaPitch);

            if (this.requireHeadMovement && (deltaPitch > 0F || distance > 0F || deltaYaw > 0F)) {
                this.requireHeadMovement = false;
            }

            if (clientCollide && !horizontalCollide
                    && !this.requireHeadMovement
                    && getData().getMovementProcessor().getDeltaXZ() > .1F
                    && !exempt() && !invalidDistance) {

                if (++this.threshold > 6.0) {
                    this.threshold = 5.0;
                    this.fail(
                            "clientTick=" + getData().getMovementProcessor().getClientWallCollision().getDelta(),
                            "serverTick=" + getData().getCollisionWorldProcessor().getCollidingHorizontallyTimer().getDelta());
                }
            } else {
                this.threshold -= Math.min(this.threshold, .005);
            }
        }
    }

    private boolean exempt() {
        if (getData().generalCancel()
                || getData().getLastFireTickTimer().hasNotPassed(3)
                || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                || getData().getCollisionWorldProcessor().getSoulSandTicks() > 0
                || getData().getPotionProcessor().getPoisonTicks() > 0
                || getData().getMovementProcessor().getLastNearBorderUpdate() <= 20
                || getData().getLastBlockPlaceCancelTimer().getDelta() < 10
                || getData().getLastBlockPlaceTimer().getDelta() < 10
                || getData().getCollisionWorldProcessor().isHalfBlock()
                || getData().getCollisionWorldProcessor().getHopperTicks() > 0
                || getData().isBedrock()
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 5
                || getData().getSetBackProcessor().getSetBackTick() > 0
                || (getData().getLastFallDamageTimer().isSet() && getData().getLastFallDamageTimer().getDelta() < 10)
                || (getData().getBlockProcessor().getLastCombatWallTicks() < 20)
                || getData().getBlockProcessor().getLastWebUpdateTick() < 7
                || (getData().getPotionProcessor().getSpeedPotionAmplifier() == 0 && getData().getPotionProcessor().isSpeedPotion())
                || (getData().getPotionProcessor().getSlownessAmplifier() == 0 && getData().getPotionProcessor().isSlownessPotion())
                || getData().getLastExplosionTimer().hasNotPassed(40)
                || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(20)
                || getData().getLastSuffocationTimer().hasNotPassed(3)) {
            this.threshold -= Math.min(this.threshold, .03);
            return true;
        }

        if (getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().isSet()
                && getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() < 1) {
            this.threshold = 0;
            return true;
        }

        if (getData().getCollisionProcessor().getWebTicks() != 0 ||
                getData().getCollisionProcessor().getLiquidTicks() != 0) {
            this.threshold = 0;
            return true;
        }

        return false;
    }
}
