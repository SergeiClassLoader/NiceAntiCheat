package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "InvalidMove", checkNameEnum = CheckName.INVALID_MOVE,
        subName = "E",
        checkType = CheckType.MOVEMENT,
        punishmentVL = 10,
        description = "Detects if the player moves consistently at the same speed",
        state = CheckState.RELEASE)
public class InvalidMoveE extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (flying.hasPositionChanged() && flying.hasRotationChanged()) {

                double xz = getData().getMovementProcessor().getDeltaXZ();
                double lastXZ = getData().getMovementProcessor().getLastDeltaXZ();

                double modulo = 0.1 % (xz % 0.1);

                boolean invalid = modulo < 1e-8 && xz > .11 && lastXZ > .11;

                if (invalid && !exempt()) {

                    this.getData().getSetBackProcessor().setLastInvalidTick(50);

                    if (++this.threshold > 3) {
                        this.fail("motion="+modulo,
                                "deltaXZ="+xz,
                                "lastDeltaXZ="+lastXZ);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, 0.05);
                }
            }
        }
    }

    private boolean exempt() {
        if (getData().generalCancel()
                || getData().getMovementProcessor().getPositionTicks() < 10
                || getData().getMovementProcessor().getRotatingTicks() < 3
                || getData().getCollisionWorldProcessor().isHalfBlock()
                || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                || getData().getCollisionProcessor().getWebTicks() > 0
                || getData().getLastExplosionTimer().hasNotPassed(40)
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 10
                || getData().getCollisionProcessor()
                .getLastEnderDragonNearTimer().hasNotPassed(20)
                || getData().getMovementProcessor().getLastFlightTimer().hasNotPassed(20)
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().getCollisionWorldProcessor().getWallTicks() > 0
                || getData().getCollisionProcessor().getLiquidTicks() > 0
                || getData().getHorizontalProcessor().getVelocitySimulator()
                .getLastVelocitySimulatedTimer().getDelta() < 6
                || getData().getCollisionWorldProcessor().isDoor()
                || getData().getMovementProcessor().isBouncedOnBed()
                || getData().getCollisionWorldProcessor().getSoulSandTicks() > 0
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().isBedrock()
                || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                || getData().getMovementProcessor().isBouncedOnSlime()
                || getData().getPistonUpdateTimer().getDelta() < 3
                || getData().getCollisionWorldProcessor().getIceTicks() > 0
                || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(20)
                || getData().getCollisionProcessor().getLiquidTicks() > 0) {
            this.threshold = 0;
            return true;
        }


        if (getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 6
                || getData().getVelocityProcessor().getServerVelocityTicks() <= 6
                || getData().getActionProcessor().isTeleportingV2()) {
            this.threshold -= Math.min(this.threshold, 0.02);
            return true;
        }

        return false;
    }
}
