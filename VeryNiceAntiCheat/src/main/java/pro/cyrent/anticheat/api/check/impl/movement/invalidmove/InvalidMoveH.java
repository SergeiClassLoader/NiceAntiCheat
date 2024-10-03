package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "InvalidMove", checkNameEnum = CheckName.INVALID_MOVE,
        subName = "H",
        checkType = CheckType.MOVEMENT,
        punishmentVL = 10,
        punishable = false,
        experimental = true,
        description = "Detects invalid falling vertical movements",
        state = CheckState.PRE_ALPHA)
public class InvalidMoveH extends Check {

    private double lastPosYGround;
    private double threshold;
    private boolean ignoreUntilGround = false;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            double deltaY = getData().getMovementProcessor().getDeltaY();
            double lastDeltaY = getData().getMovementProcessor().getLastDeltaY();

            boolean clientGround = getData().getMovementProcessor().getTo().isOnGround();

            boolean exempt = getData().getActionProcessor().getTeleportTicks() <= 10
                    || getData().getTeleportProcessor().isPossiblyTeleporting()
                    || getData().getLastTeleport().getDelta() < 20
                    || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 20
                    || getData().generalCancel()
                    || getData().getLastExplosionTimer().getPositionDelta() < 20
                    && getData().getLastExplosionTimer().isSet()
                    || getData().getActionProcessor().getExplosionTimer().getPositionDelta() < 20
                    && getData().getActionProcessor().getExplosionTimer().isSet()
                    || getData().getLastEnderPearl().getDelta() < 20
                    || getData().getPistonUpdateTimer().getDelta() < 20
                    || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                    || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                    || getData().getVelocityProcessor().getServerVelocityTicks() < 20
                    || getData().getActionProcessor().getLastVehicleTimer().getDelta() < 40
                    || getData().getVelocityProcessor().getVelocityATicks() < 20;

            double posY = getData().getMovementProcessor().getTo().getPosY();

            if (posY >= (this.lastPosYGround + 3.0)) {
                this.ignoreUntilGround = true;
            }

            if (clientGround) {
                double groundDistance = Math.abs(posY - this.lastPosYGround);
                double deltaAccel = Math.abs(deltaY - lastDeltaY);

                if ((deltaY <= -0.5 && deltaAccel >= 0.9)
                        && !this.ignoreUntilGround
                        && groundDistance >= 1.0 && groundDistance <= 15 && !exempt) {
                    if (++this.threshold > 1.0) {
                        this.fail("Possibly falling too quickly",
                                "deltaY=" + deltaY,
                                "accel=" + deltaAccel,
                                "blocks-fell=" + groundDistance);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .00001);
                }

                if (this.ignoreUntilGround) {
                    this.ignoreUntilGround = false;
                }

                this.lastPosYGround = posY;
            }
        }
    }
}