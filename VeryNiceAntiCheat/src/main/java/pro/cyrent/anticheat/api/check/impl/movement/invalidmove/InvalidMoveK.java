package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "InvalidMove", checkNameEnum = CheckName.INVALID_MOVE,
        subName = "K",
        checkType = CheckType.MOVEMENT,
        punishmentVL = 5,
        punishable = false,
        description = "Detects invalid teleport motions",
        state = CheckState.ALPHA)
public class InvalidMoveK extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasPositionChanged()) {
                return;
            }

            double deltaXZ = getData().getMovementProcessor().getDeltaXZ();
            double deltaY = getData().getMovementProcessor().getDeltaY();

            if (deltaXZ >= 1.0
                    && (getData().getVelocityProcessor().getVelocityTicksConfirmed() > 20
                    && getData().getVelocityProcessor().getExtraVelocityTicks() > 60
                    && getData().getVelocityProcessor().getServerVelocityTicks() > 20) && !exempt()) {

                if ((this.threshold += 0.22) > 2.0) {
                    this.getData().getSetBackProcessor().setLastInvalidTick(50);
                    this.fail(
                            "movement-speed=" + deltaXZ,
                            "motion-y=" + deltaY);
                }
            } else {
                this.threshold -= Math.min(this.threshold, .0075);
            }
        }
    }


    private boolean exempt() {
        if (getData().generalCancel()) {
            return true;
        }

        // TODO: make step on vanilla not bypass.
        if (getData().getActionProcessor().isTeleportingV2()
                || getData().getCollisionWorldProcessor().getIceTicks() > 0
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 10
                || getData().getPotionProcessor().getJumpPotionTicks() > 0
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getPistonUpdateTimer().isSet() && getData().getPistonUpdateTimer().getDelta() < 20
                || getData().getActionProcessor().getLastWalkSpeedTimer().isSet() && getData().getActionProcessor().getLastWalkSpeedTimer().getDelta() < 20
                || getData().isBedrock()
                || getData().getVelocityProcessor().getVelocityDataPre() != null &&
                getData().getVelocityProcessor().getVelocityDataPre().getY() > .7
                && getData().getVelocityProcessor().getVelocityATicks() < 20
                || getData().getMovementProcessor().getLastFlightTimer().getDelta() < 20 +
                getData().getTransactionProcessor().getPingTicks()
                && getData().getMovementProcessor().getLastFlightTimer().isSet()
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 5
                || getData().getActionProcessor().getRespawnTimer().isSet() && getData().getActionProcessor().getRespawnTimer().getDelta() < 10
                || getData().getLastTeleport().isSet() && getData().getLastTeleport().getDelta() < 5
                || getData().getGhostBlockProcessor().getLastGhostBlockTimer().isSet() && getData().getGhostBlockProcessor().getLastGhostBlockTimer().getDelta() < 10
                || getData().getGhostBlockProcessor().getLastInvalidTick() < 9
                || getData().getLastEnderPearl().isSet() && getData().getLastEnderPearl().getDelta() < 5
                || getData().getPistonUpdateTimer().isSet() && getData().getPistonUpdateTimer().getDelta() < 12
                || getData().getHorizontalProcessor().getVelocitySimulator().getLastVelocitySimulatedTimer().isSet() && getData().getHorizontalProcessor().getVelocitySimulator().getLastVelocitySimulatedTimer().getDelta() < 6
                || getData().getGhostBlockProcessor().getLastInvalidTick() < 10
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getPotionProcessor().getSpeedPotionAmplifier() > 10
                || getData().getActionProcessor().isTeleportingReal()) {
            return true;
        }

        if (getData().getPotionProcessor().getSpeedPotionAmplifier() == 0 && getData().getPotionProcessor().isSpeedPotion()
                || getData().getPotionProcessor().getSlownessAmplifier() == 0 && getData().getPotionProcessor().isSlownessPotion()
                || getData().getPotionProcessor().isSpeedPotion()
                && getData().getPotionProcessor().getSpeedPotionTicks() < 1
                || !getData().getPotionProcessor().isSpeedPotion() && getData().getPotionProcessor().getSpeedPotionTicks() > 0) {
            this.threshold = 0;
            return true;
        }

        if (getData().getMovementProcessor().getPositionTicks() < 10) {
            this.threshold = 0;
            return true;
        }

        if (getData().getActionProcessor().getWalkSpeed() != 0.1F) {
            this.threshold = 0;
            return true;
        }

        return false;
    }
}
