package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;


import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.Material;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "InvalidMove",
        subName = "J",
        checkNameEnum = CheckName.INVALID_MOVE,
        checkType = CheckType.MOVEMENT,
        punishmentVL = 8,
        experimental = true,
        description = "Detects upwards invalid vertical movements",
        state = CheckState.PRE_BETA)
public class InvalidMoveJ extends Check {

    private double lastGroundY = -1;
    private int lastFall;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasPositionChanged()) return;

            ++this.lastFall;

            if (flying.isOnGround()) {
                double groundY = flying.getLocation().getY();

                if (this.lastGroundY > groundY && Math.abs(this.lastGroundY - groundY) > .9) {
                    this.lastFall = 0;
                }

                this.lastGroundY = groundY;
            }

            double deltaY = getData().getMovementProcessor().getDeltaY();
            boolean halfBlock = getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0;

            double maxDeltaYMovement = 0.42F + (getData().getPotionProcessor().getJumpPotionAmplifier() * 0.1F);

            if (getData().getPotionProcessor().getJumpPotionTicks() > 0) {
                maxDeltaYMovement += 0.0001;
            }

            if (this.lastFall < 3) {
                maxDeltaYMovement = 0.592605;
            }

            if (halfBlock && deltaY < 1.0) {
                maxDeltaYMovement = 0.6;
            }

            // velocity basic asf
            if (getData().getVelocityProcessor().getServerVelocityTicks() <= 2
                    || getData().getVelocityProcessor().getVelocityATicks() <= 2) {
                maxDeltaYMovement += Math.abs(getData().getVelocityProcessor().getVelocityY());
            }

            // explosions are ass
            if (getData().getActionProcessor().getExplosionTimer().getDelta() < 100
                    && getData().getActionProcessor().getExplosionTimer().isSet()) {
            }

            if (deltaY > Math.abs(maxDeltaYMovement) && deltaY > 0.42F && !exempt()) {

                this.getData().getSetBackProcessor().setLastInvalidTick(50);

                if (getData().getMovementProcessor().getLastHeldInvalidItemTick() > 0) {

                    this.fail(
                            "deltaY=" + deltaY,
                            "max=" + maxDeltaYMovement);
                }
            }
        }
    }

    private boolean exempt() {
        if (getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                || getData().getMovementProcessor().isBouncedOnSlime()
                || getData().getMovementProcessor().isBouncedOnBed()
                || getData().getMovementProcessor().getLastFlightTimer().hasNotPassed(20)
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getCollisionProcessor().getLastEnderDragonNearTimer().isSet()
                && getData().getCollisionProcessor().getLastEnderDragonNearTimer().hasNotPassed(20)
                || getData().getSetBackProcessor().getSetBackTick() > 0
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 5
                || getData().getLastTeleport().getDelta() < 10
                || getData().getActionProcessor().getRespawnTimer().isSet()
                && getData().getActionProcessor().getRespawnTimer().getDelta() < 20
                || getData().getMovementProcessor().isBouncedOnBed()
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 5
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getCollisionWorldProcessor().isDoor()
                || getData().getPotionProcessor().isJumpPotion()
                && getData().getPotionProcessor().getJumpPotionTicks() < 1
                || !getData().getPotionProcessor().isJumpPotion()
                && getData().getPotionProcessor().getJumpPotionTicks() > 0
                || getData().isBedrock()
                || getData().getHorizontalProcessor().getVelocitySimulator()
                .getLastVelocitySimulatedTimer().isSet()
                && getData().getHorizontalProcessor().getVelocitySimulator()
                .getLastVelocitySimulatedTimer().getDelta() < 6
                || getData().getPistonUpdateTimer().isSet() && getData().getPistonUpdateTimer().getDelta() < 20
                || getData().getActionProcessor().getLastVehicleTimer().isSet()
                && getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(20)) {
            return true;
        }

        if (getData().getCollisionProcessor().getLiquidFullyTicks() > 0) {
            return true;
        }

        if (getData().getActionProcessor().isTeleportingV2() || getData().getActionProcessor().getTeleportTicks() <= 2) {
            return true;
        }

        if (getData().getActionProcessor().isTeleportingV2()
                && getData().getCollisionWorldProcessor().getAnvilTicks() > 0) {
            return true;
        }

        if (getData().generalCancel()) {
            return true;
        }

        if (getData().getLastBlockPlaceTimer().isSet() && getData().getLastBlockPlaceTimer().getDelta() < 7
                || getData().getLastBlockPlaceCancelTimer().isSet() && getData().getLastBlockPlaceCancelTimer().getDelta() < 7) {
            return true;
        }

        if (getData().getGhostBlockProcessor().getLastGhostBlockTimer().isSet()
                && getData().getGhostBlockProcessor().getLastGhostBlockTimer().getDelta() < 4) {
            return true;
        }

        return false;
    }
}