package pro.cyrent.anticheat.api.check.impl.combat.velocity;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.impl.processor.combat.VelocityProcessor;
import pro.cyrent.anticheat.impl.processor.world.CollisionProcessor;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Velocity",
        subName = "B",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.VELOCITY,
        description = "Detects jump reset velocities",
        punishmentVL = 10,
        state = CheckState.RELEASE)
public class VelocityB extends Check {

    private double threshold;

    private double amountResets, amountOfVelocity;

    private double lastDeltaY;

    public void onVelocity(PacketEvent event) {
        if (event.isMovement()) {
            this.runCheck();
        }
    }

    private void runCheck() {
        if (getData().getHorizontalProcessor().getVelocityTicks() == 0) {
            double deltaY = getData().getMovementProcessor().getDeltaY();

            double ratio = this.amountResets > 0 && this.amountOfVelocity > 0 ?
                    this.amountResets / this.amountOfVelocity : 0;

            if (deltaY == 0.42F && !exempt()) {
                this.amountResets++;

                if (++this.threshold > 12.0) {
                    this.fail(
                            "motionY="+deltaY,
                            "jumpRatio=" + ratio,
                            "lastMotionY="+lastDeltaY);
                }
            } else {
                this.amountOfVelocity++;
                this.threshold -= Math.min(this.threshold, .3);
            }


            if (this.amountOfVelocity > 100 || this.amountResets > 100) {
                this.amountResets = this.amountOfVelocity = 0;
            }

            this.lastDeltaY = deltaY;
        }
    }

    private boolean exempt() {
        if (getData().generalCancel()
                || getData().getLastFireTickTimer().hasNotPassed(3)
                || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                || getData().getCollisionWorldProcessor().getSoulSandTicks() > 0
                || getData().getPotionProcessor().getPoisonTicks() > 0
                || getData().getCollisionWorldProcessor().isHalfBlock()
                || getData().isBedrock()
                || getData().getCollisionWorldProcessor().getHopperTicks() > 0
                || getData().getBlockProcessor().getLastWebUpdateTick() < 7
                || getData().getLastExplosionTimer().hasNotPassed(40)
                || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(20)
                || getData().getLastSuffocationTimer().hasNotPassed(3)
                || getData().getCollisionWorldProcessor().getCollidingHorizontallyTimer().getDelta() < 5
                || getData().getActionProcessor().isTeleportingV2()
                || getData().getLastFallDamageTimer().getDelta() < 10) {
            return true;
        }


        if (getData().getCollisionWorldProcessor().getBlockAboveTimer().hasNotPassed(19) ||
                getData().getCollisionProcessor().getWebTicks() != 0 ||
                getData().getCollisionWorldProcessor().getLiquidTicks() != 0) {
            this.threshold = 0;
            return true;
        }

        return false;
    }
}