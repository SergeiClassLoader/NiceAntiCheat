package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "InvalidMove", checkNameEnum = CheckName.INVALID_MOVE,
        subName = "M",
        checkType = CheckType.MOVEMENT,
        punishmentVL = 8,
        punishable = false,
        description = "Detects invalid vertical movements in liquids",
        state = CheckState.ALPHA)
public class InvalidMoveM extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasPositionChanged()) return;

            if (getData().generalCancel()) {
                return;
            }

            if (getData().getActionProcessor().isTeleportingV2()
                    || getData().isBedrock()
                    || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                    || getData().getPistonUpdateTimer().getDelta() < 20
                    || getData().getVelocityProcessor().getServerVelocityTicks() < 20
                    || getData().getVelocityProcessor().getVelocityTicksConfirmed() < 20
                    || getData().getPotionProcessor().getSpeedPotionAmplifier() > 7
                    || getData().getCollisionWorldProcessor().getLiquidTicks() > 0
                    || getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0
                    || getData().getActionProcessor().isTeleportingReal()) {
                this.threshold -= Math.min(this.threshold, 0.025);
                return;
            }

            //in web
            if (getData().getCollisionProcessor().isWaterFully()
                    || getData().getCollisionProcessor().isLavaFully()) {

                double deltaY = getData().getMovementProcessor().getDeltaY();
                double max = .42F;

                if (deltaY > max) {
                    if (++this.threshold > 2.5) {
                        this.fail("deltaY="+deltaY,
                                "max="+max);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .035);
                }
            }
        }
    }
}
