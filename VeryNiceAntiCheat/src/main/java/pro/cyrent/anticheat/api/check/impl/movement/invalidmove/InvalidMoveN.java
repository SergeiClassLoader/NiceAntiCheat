package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "InvalidMove", checkNameEnum = CheckName.INVALID_MOVE,
        subName = "N",
        checkType = CheckType.MOVEMENT,
        punishmentVL = 8,
        punishable = false,
        description = "Detects invalid motions when jumping out of water",
        state = CheckState.ALPHA)
public class InvalidMoveN extends Check {

    private double threshold;
    private int checkTicks;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (getData().getActionProcessor().isTeleportingV2()
                    || getData().isBedrock()
                    || !flying.hasPositionChanged()
                    || getData().generalCancel()
                    || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                    || getData().getLastExplosionTimer().getDelta() < 100
                    || getData().getPistonUpdateTimer().getDelta() < 20
                    || getData().getPotionProcessor().getJumpPotionTicks() > 0
                    || getData().getHorizontalProcessor().getVelocitySimulator()
                    .getLastVelocitySimulatedTimer().getDelta() < 6
                    || getData().getVelocityProcessor().getServerVelocityTicks() < 20
                    || getData().getVelocityProcessor().getVelocityTicksConfirmed() < 20
                    || getData().getPotionProcessor().getSpeedPotionAmplifier() > 7
                    || getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0
                    || getData().getActionProcessor().isTeleportingReal()) {
                this.threshold -= Math.min(this.threshold, 0.025);
                return;
            }

            //in web
            if (!getData().getCollisionProcessor().isWaterFully()
                    && !getData().getCollisionProcessor().isLavaFully() && this.checkTicks == 0) {

                if (getData().getCollisionProcessor().getLiquidFullyTicks() > 0) {
                    this.checkTicks = 1;
                }

                if (this.checkTicks-- > 0) {

                    double deltaY = getData().getMovementProcessor().getDeltaY();
                    double max = .42F;

                    if (deltaY > max) {
                        if (++this.threshold > 1.5) {
                            this.fail("deltaY=" + deltaY,
                                    "max=" + max);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .035);
                    }
                }
            } else {
                this.checkTicks = 0;
            }
        }
    }
}
