package pro.cyrent.anticheat.api.check.impl.combat.velocity;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.impl.processor.prediction.horizontal.HorizontalProcessor;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.util.magic.MagicValues;

import java.util.List;


@CheckInformation(
        name = "Velocity",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.VELOCITY,
        description = "Detects invalid vertical velocity movements based off predictions",
        punishmentVL = 8,
        punishable = false,
        state = CheckState.PRE_BETA)
public class VelocityA extends Check {

    private int ticks;
    private double threshold;
    private double setVelocity;
    private double velocitySetY;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (getData().getMovementProcessor().getComboModeTicks() > 0) {
                this.threshold = 0;
                return;
            }

            // If no position then the next velocity tick isn't running.
            if (!flying.hasPositionChanged()) {
                this.threshold -= Math.min(this.threshold, 0.0015D);
                return;
            }

            // Get their velocity that's confirmed by the transaction packet.
            double velocityY = getData().getHorizontalProcessor().getNewVelocityY();

            double deltaY = getData().getMovementProcessor().getDeltaY();
            double jumpHeight = 0.42F + (getData().getPotionProcessor().getJumpPotionAmplifier() * 0.1F);

            // set the velocity
            if (getData().getHorizontalProcessor().getVelocityTicks() == 1) {
                // if jumping or low velocity amount we ignore this shit.

                if (deltaY == jumpHeight || velocityY < .255 || velocityY > .7) {
                    return;
                }

                if (!exempt()) {
                    // set velocity if not exempt.
                    this.velocitySetY = velocityY;

                    this.setVelocity = velocityY;

                    // set the tick.
                    this.ticks = 0;
                }
            }

            // check the version
            boolean above1_8 = getData().getProtocolVersion() > 47;

            // make sure its above 0
            if (this.velocitySetY > 0.0) {

                // count the ticks until 3. (fly will get the rest after roughly 2-3 ticks)
                if (++this.ticks > 6) {
                    // reset velocity so it isn't still going.
                    this.velocitySetY = 0;
                    return;
                }

                boolean zeroThree = getData().getMovementProcessor().getTicksSincePosition() > 0;

                double maxOffsetChange = zeroThree ? above1_8 ? 0.06D : 0.03D : 1E-7D;

                if ((getData().getCollisionWorldProcessor().getCollidingHorizontallyTimer().getDelta() < 7
                        || getData().getMovementProcessor().getClientWallCollision().getDelta() < 7)
                        && getData().getMovementProcessor().getDeltaXZ() < .1D) {
                    maxOffsetChange = (maxOffsetChange + (above1_8 ? 0.06D : 0.03D));
                }

                // set the velocity to a different variable, so we can set the prediction next tick.
                double predicted = this.velocitySetY;

                if (Math.abs(deltaY - predicted) > maxOffsetChange) {
                    if (Math.abs(deltaY - this.setVelocity) < 1E-7D) {
                        predicted = this.setVelocity;
                    }
                }


                // get the final offset.
                double offset = Math.abs(deltaY - predicted);

                double ratio = deltaY / predicted;

                double thresholdIncrease = deltaY == 0 && offset == predicted ? 0.35 : deltaY < 0 ? 0.15 : 1.0;

                // if the offset is greater than the max offset change then they are cheating 100%
                if (!exempt() && offset > maxOffsetChange && predicted > 0 &&
                        getData().getHorizontalProcessor().getLastSplitTime() > 20
                        && deltaY != jumpHeight) {

                    if ((this.threshold += thresholdIncrease) > 15.75) {
                        this.fail("Invalid vertical velocity predictions",
                                "offset=" + offset,
                                "deltaY=" + deltaY,
                                "velocityY=" + this.velocitySetY,
                                "amount/ratio=" + ratio,
                                "tick=" + this.ticks);

                        this.velocitySetY = 0;

                        if (Anticheat.INSTANCE.getConfigValues().isSimulateVelocity()) {
                            this.getData().getHorizontalProcessor().getVelocitySimulator().triggerSimulator(true);
                        }
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, thresholdIncrease != 1.0 ? 0.04 : .02);
                }

                // Do minecraft gravity.
                this.velocitySetY = (predicted - MagicValues.VERTICAL_SUBTRACTED) * MagicValues.VERTICAL_MULTIPLIER;

                // if below 0.005 it's always 0.
                if (Math.abs(this.velocitySetY) < MagicValues.MIN_VERTICAL_1_8) this.velocitySetY = 0.0;
            }
        }
    }


    public boolean exempt() {
        if (getData().getActionProcessor().isTeleportingV2()
                || (getData().getLastWorldChange().isSet() && getData().getLastWorldChange().getDelta() < 10)
                || getData().getCollisionWorldProcessor().getCollideHorizontalTicks() > 0
                || (getData().getMovementProcessor().getClientWallCollision().isSet()
                && getData().getMovementProcessor().getClientWallCollision().getDelta() < 10)
                || (getData().getLastEnderPearl().isSet() && getData().getLastEnderPearl().getDelta() < 20)
                || (getData().getHorizontalProcessor().getVelocitySimulator().getLastVelocitySimulatedTimer().isSet()
                && getData().getHorizontalProcessor().getVelocitySimulator().getLastVelocitySimulatedTimer().getDelta() < 20)
                || (getData().getLastTeleport().isSet() && getData().getLastTeleport().getDelta() < 7)
                || getData().getCollisionProcessor().getLiquidTicks() > 0
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().getBlockProcessor().getLastWebUpdateTick() < 7
                || getData().getBlockProcessor().getLastCombatWallTicks() < 20
                || getData().getCollisionWorldProcessor().isHopper()
                || getData().isBedrock()
                || getData().getCollisionWorldProcessor().getHopperTicks() > 0
                || getData().getActionProcessor().getRespawnTimer().getDelta() < 20
                || getData().getSetBackProcessor().getLastDead() > 0
                || getData().getCollisionWorldProcessor().getCauldronTicks() > 0
                || getData().getCollisionWorldProcessor().getMountTicks() > 0
                || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(2)
                || getData().getCollisionWorldProcessor().getBlockAboveTicks() > 0
                || getData().getCollisionWorldProcessor().isBlockAbove()
                || getData().getCollisionWorldProcessor().getIceTicks() > 0
                || (getData().getLastFallDamageTimer().isSet() && getData().getLastFallDamageTimer().getDelta() < 9)
                || (getData().getFishingRodTimer().isSet() && getData().getFishingRodTimer().getDelta() < 9)
                || getData().getPotionProcessor().getJumpPotionAmplifier() > 3
                || getData().getPotionProcessor().isJumpPotion()
                || getData().getCollisionProcessor().getWebTicks() > 0
                || getData().getCollisionProcessor().isWebInside()
                || getData().getCollisionProcessor().isWeb()
                || getData().getCollisionProcessor().isWebFullCheck()
                || getData().getCollisionProcessor().getWebFullTicks() > 0
                || (getData().getLastFireTickTimer().isSet() && getData().getLastFireTickTimer().getDelta() < 20)
                || (!getData().getPotionProcessor().isJumpPotion() && getData().getPotionProcessor().getJumpPotionTicks() > 0)
                || (getData().getPotionProcessor().isJumpPotion() && getData().getPotionProcessor().getJumpPotionTicks() < 1)
                || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                || getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0
                || getData().getCollisionWorldProcessor().isHalfBlock()
                || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                || getData().getPotionProcessor().getPoisonTicks() > 0
                || getData().getPotionProcessor().isPoisonPotion()
                || getData().generalCancel()
                || getData().isEnderDragon()
                || (getData().getMovementProcessor().getClientWallCollision().isSet()
                && getData().getMovementProcessor().getClientWallCollision().getDelta() < 10)) {
            this.threshold -= Math.min(this.threshold, 0.003);
            return true;
        }

        return false;
    }
}