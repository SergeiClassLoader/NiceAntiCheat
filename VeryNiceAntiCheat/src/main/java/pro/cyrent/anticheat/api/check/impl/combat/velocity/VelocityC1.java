package pro.cyrent.anticheat.api.check.impl.combat.velocity;


import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.api.check.data.PredictionData;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Velocity",
        subName = "C1",
        checkNameEnum = CheckName.VELOCITY,
        checkType = CheckType.COMBAT,
        description = "Detects invalid horizontal movements based off predictions",
        state = CheckState.PRE_RELEASE)
public class VelocityC1 extends Check {

    private double threshold;
    private boolean secondChance;
    private double bypass, waterBypassCheck;
    private int bypassTicks, exemptTicks;

    @Override
    public void onPrediction(PredictionData data) {
        int airTicks = getData().getMovementProcessor().getAirTicks();
        int groundTicks = getData().getMovementProcessor().getGroundTicks();

        if (data.getData() == null) {
            return;
        }

        if ((getData().getVelocityProcessor().getServerVelocityTicks() <= 20
                || getData().getHorizontalProcessor().getVelocityTicks() < 20
                || getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 20)
                && (getData().getCollisionProcessor().getWebTicks() > 0
                || getData().getBlockProcessor().getLastWebUpdateTick() < 7
                || getData().getCollisionProcessor().getWebFullTicks() > 0)) {
            return;
        }

        if (data.getVelocityTick() > 6 && data.getVelocityTick() < 20) {

            if (preVelocityExempt()) {
                return;
            }

            // May not get all velocity but just incase of falses....
            double max = data.getMax() + 7.5E-5;
            boolean invalid = data.getOffset() >= max;

            if (invalid) {

                if (postVelocityExempt() || finalExempt()) {
                    return;
                }

                this.getData().getSetBackProcessor().setLastInvalidTick(50);

                if (++this.threshold > 4.5) {


                    if (Anticheat.INSTANCE.getConfigValues().isSimulateVelocity()) {
                        this.getData().getHorizontalProcessor().getVelocitySimulator().triggerSimulator(false);
                    }
                    
                    this.fail(
                            "offset=" + data.getOffset(),
                            "airTicks=" + airTicks,
                            "groundTicks=" + groundTicks,
                            "velocityTicks=" + data.getVelocityTick(),
                            "yaw=" + getData().getHorizontalProcessor().getYaw(),
                            "using=" + data.getData().isUsing(),
                            "sprint=" + data.getData().isSprint(),
                            "jump=" + data.getData().isJump(),
                            "slowDown=" + data.getData().isHitSlowdown(),
                            "sneak=" + data.getData().isSneaking(),
                            "strafe=" + data.getData().getStrafe(),
                            "forward=" + data.getData().getForward(),
                            "pastMotion=" + data.getPastMotion(),
                            "motion=" + data.getDeltaXZ(),
                            "rotationTick=" + getData().getMovementProcessor().getRotatingTicks(),
                            "speedAmplifier="+getData().getPotionProcessor().getSpeedPotionAmplifier(),
                            "speedTicks="+getData().getPotionProcessor().getSpeedPotionTicks(),
                            "fastMath=" + data.isFastMath(),
                            "omniSprint="+data.getData().isOmniSprinting());

                }

            } else {
                this.threshold -= Math.min(this.threshold, 0.01);
            }
        }
    }

    private boolean finalExempt() {
        if (getData().getMovementProcessor().getPositionTicks() < 7
                || getData().isBedrock()
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().getMovementProcessor().getLastFlightTimer().getDelta() < 20
                || getData().getCollisionWorldProcessor().getSoulSandTicks() > 0
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getActionProcessor().isTeleportingV2()
                || getData().getMovementProcessor().getTick() < 100
                || getData().getLastTeleport().getDelta() < 9
                || getData().getPotionProcessor().getInvalidPotionAmpliferTicks() <= 40
                || getData().getLastEnderPearl().getDelta() < 20
                || getData().getCollisionWorldProcessor().getHopperTicks() > 0
                || getData().getActionProcessor().getRespawnTimer().getDelta() < 20
                || getData().getSetBackProcessor().getLastDead() > 0
                || getData().getBlockProcessor().getLastCombatWallTicks() < 20
                || getData().getPotionProcessor().getSpeedPotionAmplifier() > 100
                || getData().getPotionProcessor().getSpeedPotionAmplifier() == 0 && getData().getPotionProcessor().isSpeedPotion()
                || getData().getPotionProcessor().getSlownessAmplifier() == 0 && getData().getPotionProcessor().isSlownessPotion()
                || getData().getLastExplosionTimer().hasNotPassed(40)
                || getData().getCollisionProcessor().getLiquidTicks() > 0
                || getData().getCollisionWorldProcessor().isCollidingHorizontal()
                || getData().getCollisionWorldProcessor().isClimbing()
                || getData().getSetBackProcessor().getSetBackTick() > 0
                || getData().getPistonUpdateTimer().getDelta() < 3
                || getData().getHorizontalProcessor().getVelocitySimulator()
                .getLastVelocitySimulatedTimer().getDelta() < 10
                || getData().getCollisionProcessor()
                .getLastEnderDragonNearTimer().hasNotPassed(20)
                || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(5)) {
            return true;
        }

        if (getData().getPotionProcessor().isSpeedPotion()
                && getData().getPotionProcessor().getSpeedPotionTicks() < 1
                || !getData().getPotionProcessor().isSpeedPotion()
                && getData().getPotionProcessor().getSpeedPotionTicks() > 0
                || getData().getPotionProcessor().isJumpPotion()
                && getData().getPotionProcessor().getJumpPotionTicks() < 1
                || !getData().getPotionProcessor().isJumpPotion()
                && getData().getPotionProcessor().getJumpPotionTicks() > 0) {
            this.exemptTicks = 2;
            return true;
        }


        if (getData().getVelocityProcessor().getExtraVelocityTicks() < 40
                || getData().getFishingRodTimer().getDelta() < 20) {
            return true;
        }

        if (getData().getPotionProcessor().getSpeedPotionAmplifier()
                != getData().getPotionProcessor().getLastSpeedAmplifer()) {
            this.exemptTicks = 2;
            return true;
        }

        if (this.exemptTicks-- > 0) {
            return true;
        }

        //todo: fix block switching off and on causing falses, specifically slime, but ice can do it too
        if (getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                || getData().getCollisionWorldProcessor().getIceTicks() > 0) {
            if (getData().getPotionProcessor().getSpeedPotionAmplifier() > 3) {
                this.threshold = 0;
                return true;
            }
        }

        if (getData().getMovementProcessor().getClientWallCollision().getDelta() < 10
                || getData().getCollisionWorldProcessor().isCollidingHorizontal()) {
            return true;
        }

        if (getData().generalCancel()) {
            return true;
        }

        if (getData().getGhostBlockProcessor().getLastGhostBlockTimer().hasNotPassed(5)) {
            return true;
        }

        if (getData().getActionProcessor().isTeleporting()) {
            return true;
        }

        if (getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0
                && (getData().getMovementProcessor().getDeltaY() == 0.5
                || getData().getMovementProcessor().getLastDeltaY() == 0.5)) {
            return true;
        }


        if (!this.getData().getPotionProcessor().isSpeedPotion()
                && getData().getPotionProcessor().getSpeedPotionTicks() > 0
                || getData().getActionProcessor().getLastWalkSpeedTimer().getDelta() < 20) {
            this.threshold = 0;
            return true;
        }

        if (this.getData().getPotionProcessor().isSpeedPotion()
                && getData().getPotionProcessor().getSpeedPotionTicks() < 1) {
            this.threshold = 0;
            return true;
        }

        if (getData().getMovementProcessor().getLastGhostBlockAboveTick() >= 19
                && this.bypass < 10) {
            this.bypass++;
            return true;
        }

        if (getData().getGhostBlockProcessor().getLiquidBypass() > 0) {

            if ((this.waterBypassCheck += 1) < 50) {
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

    public boolean preVelocityExempt() {
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


    public boolean postVelocityExempt() {
        if (getData().getActionProcessor().isTeleportingV2()
                || getData().getCollisionProcessor().getLiquidTicks() > 0
                || getData().getCollisionWorldProcessor().getCauldronTicks() > 0
                || getData().getLastTeleport().getDelta() < 9
                || getData().getCollisionProcessor().getMountTicks() > 0
                || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(2)
                || getData().getCollisionWorldProcessor().getBlockAboveTicks() > 0
                || getData().getCollisionWorldProcessor().isSoulSand()
                || getData().getLastExplosionTimer().hasNotPassed(40)
                || getData().getCollisionWorldProcessor().getIceTicks() > 0
                || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                || getData().getPotionProcessor().getSpeedPotionAmplifier() == 0 && getData().getPotionProcessor().isSpeedPotion()
                || getData().getPotionProcessor().getSlownessAmplifier() == 0 && getData().getPotionProcessor().isSlownessPotion()
                || getData().getLastFallDamageTimer().getDelta() < 10
                || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                || getData().getCollisionProcessor().isWeb()
                || getData().isBedrock()
                || getData().getCollisionProcessor().isWebFullCheck()
                || getData().getLastWorldChange().getDelta() < 40
                //TODO: check later if bypass...
                || getData().getCombatProcessor().isExemptPunchBow()
                || getData().getCollisionProcessor().getWebFullTicks() > 0
                || getData().getBlockProcessor().getLastWebUpdateTick() < 7
                || getData().getCollisionProcessor().getWebTicks() > 0
                || getData().getCollisionProcessor().isWebInside()
                || getData().getPotionProcessor().getPoisonTicks() > 0
                || getData().getPotionProcessor().isPoisonPotion()
                || getData().generalCancel()
                || getData().getHorizontalProcessor().getVelocitySimulator()
                .getLastVelocitySimulatedTimer().getDelta() < 6
                || getData().getCollisionWorldProcessor().getWallTicks() > 0
                || getData().getLastExplosionTimer().getDelta() < 10
                || getData().getCollisionWorldProcessor().isCollidingHorizontal()
                || getData().getCollisionWorldProcessor().getCollideHorizontalTicks() > 0
                || getData().isEnderDragon()
                || getData().getMovementProcessor().getClientWallCollision().getDelta() < 40) {
            this.threshold -= Math.min(this.threshold, 0.0125);
            return true;
        }

        if (getData().getMovementProcessor().getLastGhostBlockAboveTick() > 0) {
            if (this.bypassTicks++ < 10) {
                this.threshold = 0;
                return true;
            }
        }

        return false;
    }
}

