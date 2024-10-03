package pro.cyrent.anticheat.api.check.impl.combat.velocity;

import org.bukkit.enchantments.Enchantment;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.math.MathUtil;

@CheckInformation(
        name = "Velocity",
        subName = "E",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.VELOCITY,
        description = "Detects next movement horizontal abuse (0.03) velocity",
        punishable = false,
        state = CheckState.BETA)
public class VelocityE extends Check {

    // This is an old "prediction" system which is used to stop first tick velocity since it can be abused
    // By zero-zero three, and no motion set velocities/velocity modules that abuse brute forcing.
    // These velocities also go by the names "Hypixel, or No Motion Set"
    // Patches out Slinky client's no motion velocity which would otherwise get past Velocity C/C1
    // Thanks to FlyCode for finding this dumb issue and making me have to put this old ass check in
    // Hopefully this fixes the problem.

    //TODO: add 1.12+ support to this even tho its a shit check

    private int exemptTicks;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            if (preExempt()) {
                this.threshold -= Math.min(this.threshold, .015);
                return;
            }

            if (getData().getFishingRodTimer().getDelta() < 40
                    && getData().getVelocityProcessor().getVelocityATicks() < 20) {
                return;
            }

            // old hardcoded move flying method.
            double moveFlyingAmount = MathUtil.movingFlyingV3(getData());

            // first tick & make sure there is move flying data it can get.
            if (getData().getVelocityProcessor().getVelocityATicks() == 1 && moveFlyingAmount > 0) {

                double deltaXZ = getData().getMovementProcessor().getDeltaXZ();

                // The velocity X & Z hypot but multiplied by 0.6F so we account for attacking
                double velocityXZ = getData().getVelocityProcessor().getVelocityXZ();

                // sub-track from the pre transaction (x & z) velocity combined.
                double prediction = (velocityXZ - moveFlyingAmount) - 0.2;

                // Fixes jump resetting without exemptiong
                if (getData().getMovementProcessor().getDeltaY() == 0.42F) {
                    prediction -= 0.6;
                }

                // Get the last tick since position sent
                int positionTicks = getData().getMovementProcessor().getTicksSincePosition();

                // Max movement before player should be detected based on 0.03 calculations.
                double maxMovement = 0.1 + (positionTicks != 0 ? positionTicks * 0.03D : 0.03D);

                // make sure it's greater than 0
                if (velocityXZ > 0.2 && prediction != 0 && deltaXZ >= maxMovement) {
                    // Get the ratio of the velocity
                    double ratio = deltaXZ / prediction;

                    // if their ratio is below 0.85 on average and prediction is greater than the movement speed
                    // their cheating.
                    if (ratio < 0.90D
                            && ratio >= 0 && prediction > deltaXZ && !exempting()) {

                        if (++this.threshold > 2.5) {
                            this.fail("ratio="+ratio,
                                    "deltaXZ="+deltaXZ,
                                    "prediction="+prediction);

                            if (Anticheat.INSTANCE.getConfigValues().isSimulateVelocity()) {
                                getData().getHorizontalProcessor().getVelocitySimulator().triggerSimulator(true);
                            }
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, 0.06);
                    }
                }
            }
        }
    }

    public boolean preExempt() {
        if (this.exemptTicks > 0) {
            this.exemptTicks--;
            this.threshold = 0;
            return true;
        }

        if (getData().getPlayer().getItemInHand() != null
                && getData().getPlayer().getItemInHand().containsEnchantment(Enchantment.KNOCKBACK)) {
            this.exemptTicks = 20;
            this.threshold = 0;
            return true;
        }

        return false;
    }


    private boolean exempting() {
        // exempt for certain situations.
        if (getData().generalCancel()
                || getData().getProtocolVersion() >= 393
                || (getData().getCollisionWorldProcessor().getBlockAboveTicks() > 0)
                || getData().getCollisionWorldProcessor().getSoulSandTicks() > 0
                || (getData().getPotionProcessor().getSpeedPotionAmplifier() > 5)
                || getData().getCollisionWorldProcessor().getHopperTicks() > 0
                || getData().getActionProcessor().getRespawnTimer().getDelta() < 20
                || getData().getSetBackProcessor().getLastDead() > 0
                || (getData().getLastTeleport().isSet() && getData().getLastTeleport().getDelta() < 10)
                || (getData().getLastEnderPearl().isSet() && getData().getLastEnderPearl().getDelta() < 10)
                || (getData().getPotionProcessor().getJumpPotionAmplifier() > 3)
                || getData().getActionProcessor().isTeleportingV3()
                || (getData().getMovementProcessor().getClientWallCollision().isSet()
                && getData().getMovementProcessor().getClientWallCollision().getDelta() < 20)
                || getData().getCollisionWorldProcessor().isCollidingHorizontal()) {
            this.threshold -= Math.min(this.threshold, 0.025);
            return true;
        }

        return false;
    }
}