package pro.cyrent.anticheat.api.check.impl.combat.velocity;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.impl.processor.combat.VelocityProcessor;
import pro.cyrent.anticheat.impl.processor.world.CollisionProcessor;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Velocity",
        subName = "D",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.VELOCITY,
        description = "Detects wall collision velocity spoofing",
        punishable = false,
        state = CheckState.PRE_RELEASE)
public class VelocityD extends Check {

    private double threshold;

    private double velocityY;

    private int ticks;

    public void onVelocity(PacketEvent event) {
        if (event.isMovement()) {
            this.runCheck();

            for (VelocityProcessor.VelocityDataB entry : getData().getVelocityProcessor().getConfirmedVelocities()) {
                if (entry != null
                        && entry.getVelocityType() == VelocityProcessor.VelocityType.POST) {
                    this.velocityY = entry.getY();
                    this.ticks = 0;
                }
            }
        }
    }

    private void runCheck() {

        if (this.velocityY > 0.0) {
            if (Math.abs(this.velocityY) < 0.005
                    || ++this.ticks > 6) {
                this.velocityY = 0;
                return;
            }

            if (getData().isBedrock()) return;

            boolean horizontalCollide = getData().getCollisionWorldProcessor().isCollidingHorizontal();
            boolean clientCollide = getData().getMovementProcessor().getClientWallCollision().getDelta() < 2 &&
                    getData().getMovementProcessor().getClientWallCollision().isSet();

            if (clientCollide && !horizontalCollide && !exempt()) {
                if (++this.threshold > 1.25) {
                    this.fail("Spoofing client wall collisions, while not colliding on the server side");
                }
            } else {
                this.threshold -= Math.min(this.threshold, .005);
            }
        }
    }

    private boolean exempt() {


        if (getData().generalCancel()
                || getData().getLastFireTickTimer().hasNotPassed(3)
                || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                || getData().getCollisionWorldProcessor().getSoulSandTicks() > 0
                || getData().getPotionProcessor().getPoisonTicks() > 0
                || getData().getMovementProcessor().getLastNearBorderUpdate() <= 20
                || getData().getCollisionWorldProcessor().isHalfBlock()
                || getData().getCollisionWorldProcessor().getHopperTicks() > 0
                || getData().isBedrock()
                || getData().getBlockProcessor().getLastCombatWallTicks() < 20
                || getData().getBlockProcessor().getLastWebUpdateTick() < 7
                || getData().getPotionProcessor().getSlownessAmplifier() == 0 && getData().getPotionProcessor().isSlownessPotion()
                || getData().getLastExplosionTimer().hasNotPassed(40)
                || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(20)
                || getData().getLastSuffocationTimer().hasNotPassed(3)
                || getData().getActionProcessor().isTeleporting()
                || getData().getLastFallDamageTimer().getDelta() < 10) {
            this.velocityY = 0;
            return true;
        }


        if (getData().getBlockProcessor()
                .getLastConfirmedBlockPlaceTimer().hasNotPassed(1)) {
            this.threshold = 0;
            this.velocityY = 0;
            return true;
        }

        if (getData().getCollisionProcessor().getWebTicks() != 0 ||
                getData().getCollisionWorldProcessor().getLiquidTicks() != 0) {
            this.threshold = 0;
            this.velocityY = 0;
            return true;
        }

        return false;
    }
}