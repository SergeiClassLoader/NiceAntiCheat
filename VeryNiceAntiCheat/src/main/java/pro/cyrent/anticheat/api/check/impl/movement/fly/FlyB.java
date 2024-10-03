package pro.cyrent.anticheat.api.check.impl.movement.fly;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInformation(
        name = "Fly",
        subName = "B",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.FLY,
        description = "Detects long term flight/being in the air constantly",
        punishmentVL = 10.0,
        experimental = true,
        punishable = false,
        enabled = false,
        state = CheckState.PRE_BETA)
public class FlyB extends Check {

    private double threshold;

    private double maxFallMotion;

    private int serverAirTicks;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasPositionChanged()) {
                return;
            }

            boolean clientGround = getData().getMovementProcessor().getTo().isOnGround();
            boolean serverGround = getData().getCollisionWorldProcessor().isGround();

            if (serverGround) {
                this.serverAirTicks = 0;
            } else {
                this.serverAirTicks++;
            }

            if ((getData().getLastBlockPlaceCancelTimer().getDelta() < 40 && getData().getLastBlockPlaceCancelTimer().isSet()
                    || getData().getBlockProcessor().getLastConfirmedCancelPlaceTimer().getDelta() < 10 &&
                    getData().getBlockProcessor().getLastConfirmedCancelPlaceTimer().isSet())
                    && clientGround) {
                this.serverAirTicks = 0;
                return;
            }

            if (getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() < 20) {
                this.serverAirTicks = 0;
                return;
            }

            double maxDelta = clientGround ? -.75 : -1.25;

            if (this.serverAirTicks > 150) {
                maxDelta = -1.75;
            }

            double deltaY = getData().getMovementProcessor().getDeltaY();

            int max = getData().getPotionProcessor().getJumpPotionTicks() > 0 ? 200 : 170;

            boolean valid = serverAirTicks >= max
                    && deltaY > maxDelta;

            boolean bullshit = getData().getTransactionProcessor().getTransactionPing() > 300
                    && clientGround && !serverGround;

            if (valid) {

                if (exempt()) {
                    this.serverAirTicks = 0;
                    this.threshold = 0;
                    return;
                }

                if ((this.threshold += (bullshit ? .5 : 1.0)) > 12.5) {
                    this.fail("tick="+this.serverAirTicks,
                            "deltaY="+deltaY,
                            "max="+maxDelta,
                            "clientGround="+clientGround);
                }
            } else {
                this.threshold -= Math.min(this.threshold, .05);
            }
        }
    }

    private boolean exempt() {
        return getData().generalCancel()
                || getData().getActionProcessor().isTeleportingV3()
                || getData().getMovementProcessor().isBouncedOnSlime()
                || getData().getMovementProcessor().isBouncedOnBed()
                || getData().getLastEnderPearl().getDelta() < 7
                || getData().getHorizontalProcessor().getVelocitySimulator()
                .getLastVelocitySimulatedTimer().getDelta() < 3
                || getData().getLastRotate() > 0
                || getData().getCollisionWorldProcessor().getLiquidTicks() > 0
                || getData().getCollisionProcessor().isWebFullCheck()
                || getData().getLastWorldChange().getDelta() < 20
                || getData().getLastTeleport().getDelta() < 10
                || getData().getVelocityProcessor().getExtraVelocityTicks() <= 60
                || getData().getCollisionProcessor().getWebFullTicks() > 0
                || getData().getCollisionProcessor().getWebTicks() > 0
                || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                || getData().getCollisionWorldProcessor().isCarpet()
                || getData().getVelocityProcessor().getServerVelocityTicks() <= 20
                || getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 20;
    }
}