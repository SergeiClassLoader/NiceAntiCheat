package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import org.bukkit.Material;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.processor.combat.VelocityProcessor;
import pro.cyrent.anticheat.impl.processor.prediction.horizontal.HorizontalProcessor;

import java.util.List;

@CheckInformation(
        name = "InvalidMove",
        checkNameEnum = CheckName.INVALID_MOVE,
        checkType = CheckType.MOVEMENT,
        punishmentVL = 8,
        description = "Detects if the player jumps the improper height",
        state = CheckState.ALPHA)
public class  InvalidMoveA extends Check {

    private double waterBypassCheck;
    private int bypass;
    private boolean secondChance = false;

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            boolean ground = getData().getMovementProcessor().getTo().isOnGround();
            boolean lastGround = getData().getMovementProcessor().getFrom().isOnGround();

            double deltaY = getData().getMovementProcessor().getDeltaY();

            double maxJumpHeight = 0.42F + (getData()
                    .getPotionProcessor().getJumpPotionAmplifier() * 0.1F);

            if (getData().getMovementProcessor().getDeltaXZ() < .2
                    && !ground && lastGround
                    && getData().getMovementProcessor().getDeltaY() > .404
                    && getData().getMovementProcessor().getDeltaY() < .407) {
                maxJumpHeight = deltaY;
            }

            boolean invalidJump = deltaY != maxJumpHeight && !ground && lastGround;

            if (invalidJump && deltaY > 0.0 && !exempt()) {

                this.getData().getSetBackProcessor().setLastInvalidTick(50);

                if (++this.threshold > 1) {
                    this.fail("deltaY="+deltaY,
                            "max-jump-height="+maxJumpHeight,
                            "sc="+this.secondChance);

                }
            } else {
                this.threshold -= Math.min(this.threshold, 0.002);
            }
        }
    }

    private boolean exempt() {
        if (getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                || getData().getMovementProcessor().getLastFlightTimer().hasNotPassed(20)
                || getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getCollisionProcessor().getLastEnderDragonNearTimer().isSet()
                && getData().getCollisionProcessor().getLastEnderDragonNearTimer().hasNotPassed(20)
                || getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                || getData().getLastExplosionTimer().isSet()
                && getData().getLastExplosionTimer().hasNotPassed(40)
                || getData().getCollisionProcessor().isWeb()
                || getData().getSetBackProcessor().getSetBackTick() > 0
                || getData().getActionProcessor().getRespawnTimer().isSet()
                && getData().getActionProcessor().getRespawnTimer().getDelta() < 10
                || getData().getPotionProcessor().getJumpPotionTicks() > 0
                && !getData().getPotionProcessor().isJumpPotion()
                || getData().getPotionProcessor().getJumpPotionTicks() < 1
                && getData().getPotionProcessor().isJumpPotion()
                || getData().getPotionProcessor().getJumpPotionTicks() > 0
                || getData().getMovementProcessor().isBouncedOnBed()
                || getData().getCollisionWorldProcessor().getWallTicks() > 0
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 5
                || getData().getCollisionWorldProcessor().isDoor()
                || getData().getCollisionWorldProcessor().isClimbing()
                //   || getData().getFishingRodVelocity().getDelta() < 2
                || getData().getCollisionWorldProcessor().isCarpet()
                || getData().isBedrock()
                || getData().getHorizontalProcessor().getVelocitySimulator()
                .getLastVelocitySimulatedTimer().isSet()
                && getData().getHorizontalProcessor().getVelocitySimulator()
                .getLastVelocitySimulatedTimer().getDelta() < 6
                || getData().getCollisionWorldProcessor().getBlockAboveTicks() > 0
                || getData().getCollisionWorldProcessor().getBlockAboveTimer().isSet()
                && getData().getCollisionWorldProcessor().getBlockAboveTimer().getDelta() < 20
                || getData().getPotionProcessor().isJumpPotion()
                && getData().getPotionProcessor().getJumpPotionTicks() < 1
                || !getData().getPotionProcessor().isJumpPotion()
                || getData().getPotionProcessor().getInvalidPotionAmpliferTicks() <= 40
                || getData().getPistonUpdateTimer().isSet() && getData().getPistonUpdateTimer().getDelta() < 3
                || getData().getActionProcessor().getLastVehicleTimer().isSet()
                && getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(20)) {
            this.threshold = 0;
            return true;
        }

        if (getData().getCollisionProcessor().getLiquidFullyTicks() > 0
                || getData().getVelocityProcessor().getVelocityTicksConfirmed() < 20
                || getData().getVelocityProcessor().getServerVelocityTicks() < 20) {
            return true;
        }

        if (getData().getActionProcessor().isTeleportingV2()) {
            this.threshold -= Math.min(this.threshold, 0.03);
            return true;
        }

        if (getData().getActionProcessor().isTeleportingV2()
                && getData().getCollisionWorldProcessor().getAnvilTicks() > 0) {
            this.threshold -= Math.min(this.threshold, 0.06);
            return true;
        }

        if (getData().generalCancel()) {
            return true;
        }

        if (getData().getLastBlockPlaceTimer().isSet() && getData().getLastBlockPlaceTimer().getDelta() < 7
                || getData().getLastBlockPlaceCancelTimer().isSet() && getData().getLastBlockPlaceCancelTimer().getDelta() < 7) {
            this.threshold = 0;
            return true;
        }


        if (getData().getGhostBlockProcessor().getLastGhostBlockTimer().isSet()
                && getData().getGhostBlockProcessor().getLastGhostBlockTimer().getDelta() < 4) {
            return true;
        }

        if (getData().getGhostBlockProcessor().getLiquidBypass() > 0) {

            if ((this.waterBypassCheck += .75) < 90) {
                this.threshold = 0;
                return true;
            } else {
                if (!this.secondChance) {
                    this.waterBypassCheck /= 1.25;
                    this.secondChance = true;
                }
            }
        }

        if (getData().getMovementProcessor().getLastGhostBlockAboveTick() > 0
                && getData().getCollisionWorldProcessor().getBlockAboveTicks() < 1
                && this.bypass < 20) {
            this.bypass++;
            return true;
        }

        return false;
    }
}

