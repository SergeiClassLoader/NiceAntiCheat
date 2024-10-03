package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "InvalidMove", checkNameEnum = CheckName.INVALID_MOVE,
        subName = "L",
        checkType = CheckType.MOVEMENT,
        punishmentVL = 8,
        punishable = false,
        description = "Detects invalid vertical movements in webs",
        state = CheckState.ALPHA)
public class InvalidMoveL extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasPositionChanged()) return;

            //in web
            if (getData().getCollisionProcessor().isWebInside()
                    && getData().getCollisionProcessor().getWebTicks() > 3) {

                double deltaY = getData().getMovementProcessor().getDeltaY();
                double lastDeltaY = getData().getMovementProcessor().getLastDeltaY();

                if ((deltaY > 0.021F || deltaY < -0.0314) && !exempt()) {
                    if (++this.threshold > 3) {
                        this.fail(
                                "deltaY="+deltaY,
                                "lastDeltaY="+lastDeltaY);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .5);
                }
            } else {
                this.threshold = 0;
            }
        }
    }

    private boolean exempt() {
        if (getData().generalCancel()) {
            return true;
        }

        if (getData().getActionProcessor().isTeleportingV2()
                || getData().getCollisionWorldProcessor().getIceTicks() > 0
                || getData().isBedrock()
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getLastBlockBreakTimer().getDelta() < 20
                || getData().getCollisionProcessor().getLiquidTicks() > 0
                || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                || getData().getCollisionProcessor().getLiquidTicks() > 0
                || getData().getHorizontalProcessor().getVelocitySimulator()
                .getLastVelocitySimulatedTimer().getDelta() < 6
                || getData().getPistonUpdateTimer().getDelta() < 20
                || getData().getVelocityProcessor().getServerVelocityTicks() < 20
                || getData().getVelocityProcessor().getVelocityTicksConfirmed() < 20
                || getData().getPotionProcessor().getSpeedPotionAmplifier() > 10
                || getData().getActionProcessor().isTeleportingReal()) {
            return true;
        }

        return false;
    }
}