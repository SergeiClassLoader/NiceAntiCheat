package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "InvalidMove",
        subName = "O",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.INVALID_MOVE,
        description = "Detects invalid vertical movements based off predictions in webs",
        punishmentVL = 10.0,
        punishable = false,
        state = CheckState.ALPHA)
public class InvalidMoveO extends Check {

    private double threshold;
    private boolean secondChance = false;
    private double bypass, waterBypassCheck;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (flying.hasPositionChanged()) {


                if (getData().getCollisionProcessor().isWebInside()
                        && !getData().getMovementProcessor().getTo().isOnGround()
                        && !getData().getMovementProcessor().getFrom().isOnGround()
                        && getData().getCollisionProcessor().getWebTicks() > 3) {

                    double deltaY = getData().getMovementProcessor().getDeltaY();
                    double lastDeltaY = getData().getMovementProcessor().getLastDeltaY();
                    double deltaXZ = getData().getMovementProcessor().getDeltaXZ();

                    double prediction = (lastDeltaY - 0.08D) * 0.9800000190734863D;

                    prediction *= 0.05000000074505806D;

                    double offset = Math.abs(deltaY - prediction);

                    if (deltaY >= 0 && deltaXZ > .01) {
                        if (offset > 0.001 && offset < 0.009 && !exempt()) {
                            if (++this.threshold > 12) {
                                this.fail("offset="+offset,
                                        "deltaY="+deltaY,
                                        "lastDeltaY="+lastDeltaY,
                                        "deltaXZ="+deltaXZ);
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, 0.025);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, 0.05);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, 0.075);
                }
            }
        }
    }


    private boolean exempt() {
        if (getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(9)
                || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getMovementProcessor().getLastFlightTimer().getDelta() < 40
                || getData().getPistonUpdateTimer().getDelta() < 3
                || getData().isBedrock()
                || getData().getVelocityProcessor().getVelocityATicks() < 20
                || getData().getVelocityProcessor().getServerVelocityTicks() < 20
                || getData().getLastWorldChange().getDelta() < 20
                || getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                || getData().getPotionProcessor().isJumpPotion()
                && getData().getPotionProcessor().getJumpPotionTicks() < 1
                || !getData().getPotionProcessor().isJumpPotion()
                && getData().getPotionProcessor().getJumpPotionTicks() > 0
                || getData().getCollisionProcessor().getLiquidFullyTicks() > 0
                || getData().getCollisionProcessor()
                .getLastEnderDragonNearTimer().hasNotPassed(20)
                || getData().getMovementProcessor().getTick() < 7
                || getData().getMovementProcessor().getTo() == null
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().getPotionProcessor().getJumpPotionAmplifier() > 90) {
            this.threshold -= Math.min(this.threshold, 0.045);
            return true;
        }

        if (getData().generalCancel()) {
            this.threshold -= Math.min(this.threshold, 0.045);
            return true;
        }

        if ((getData().getCollisionWorldProcessor().isHalfBlock()
                || getData().getCollisionWorldProcessor().getWallTicks() > 0)
                && getData().getMovementProcessor().isPositionGround()) {
            this.threshold -= Math.min(this.threshold, 0.045);
            return true;
        }

        if (getData().getGhostBlockProcessor().getLastInvalidTick() < 3) {
            this.threshold = 0;
            return true;
        }

        if (getData().getGhostBlockProcessor().getLastGhostBlockTimer().getDelta() < 3) {
            return true;
        }

        if (getData().getActionProcessor().isTeleportingV3()
                && getData().getCollisionWorldProcessor().getAnvilTicks() > 0) {
            this.threshold = 0;
            return true;
        }

        if (getData().getActionProcessor().isTeleportingV2()
                || getData().getLastTeleport().getDelta() < 40) {
            return true;
        }

        if (getData().getMovementProcessor().getLastGhostBlockAboveTick() >= 19
                && ++this.bypass < 20) {
            this.threshold = 0;
            return true;
        }

        if (getData().getGhostBlockProcessor().getLiquidBypass() > 0) {

            if ((this.waterBypassCheck += 1) < 10) {
                this.threshold = 0;
                return true;
            } else {
                if (!this.secondChance) {
                    this.waterBypassCheck /= 1.25;
                    this.secondChance = true;
                }
            }
        }

        return false;
    }
}
