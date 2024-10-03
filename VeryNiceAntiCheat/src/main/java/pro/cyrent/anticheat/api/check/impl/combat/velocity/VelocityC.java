package pro.cyrent.anticheat.api.check.impl.combat.velocity;

import org.bukkit.Material;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.api.check.data.VelocityCheckData;
import pro.cyrent.anticheat.impl.processor.prediction.horizontal.HorizontalProcessor;
import org.bukkit.enchantments.Enchantment;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Velocity",
        subName = "C",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.VELOCITY,
        description = "Detects invalid horizontal velocity movements based off predictions",
        punishmentVL = 3,
        state = CheckState.RELEASE)
public class VelocityC extends Check {

    private double threshold, zeroThreshold;
    private int exemptTicks, bypassTicks;

    @Override
    public void onVelocityDetection(VelocityCheckData data) {
        if (data != null) {

            double max = data.getMaxOffset() + (data.getVelocityTick() > 6 ? 7.5E-5 : 0);

            if (getData().getHorizontalProcessor().getLastEdgeFix() < 15
                    || getData().getActionProcessor().getLastSneakTick() < 20
                    && getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0) {
                max = .1D;
            }


            HorizontalProcessor.BruteForcedData speedData =
                    getData().getHorizontalProcessor().getBruteForcedData();

            if (speedData == null) return;

            if (this.preExempt()) {
                this.threshold -= Math.min(this.threshold, 0.0015);
                return;
            }

            boolean invalid = false;

            if (data.getSmallest() == 0.0 && getData().getMovementProcessor().getDeltaY() > 0.0
                    && getData().getMovementProcessor().getDeltaXZ() == 0.0
                    && getData().getVelocityProcessor().getExtraVelocityTicks() > 40) {
                if (!postExempt()) {
                    invalid = true;
                    this.zeroThreshold++;
                }
            } else {
                this.zeroThreshold -= Math.min(this.zeroThreshold, .75);
            }

            if (data.getSmallest() != Double.MAX_VALUE && (data.getSmallest() > max || this.zeroThreshold > 11.5
                    && invalid)) {

                if (this.postExempt()) {
                    this.threshold -= Math.min(this.threshold, 0.0015);
                    return;
                }

                double maxThreshold = 1.0;

                if (data.getSmallest() > 0.10) {
                    maxThreshold = 2;
                }

                if ((this.threshold += maxThreshold) > 3.5) {

                    if (Anticheat.INSTANCE.getConfigValues().isSimulateVelocity()) {
                        this.getData().getHorizontalProcessor().getVelocitySimulator().triggerSimulator(false);
                    }

                    this.fail(
                            "smallest=" + data.getSmallest(),
                            "tickModification=" + data.getVelocityTick(),
                            "size=" + data.getListSize(),
                            "threshold=" + this.threshold,
                            "yaw=" + getData().getHorizontalProcessor().getYaw(),
                            "using=" + speedData.isUsing(),
                            "sprint=" + speedData.isSprint(),
                            "jump=" + speedData.isJump(),
                            "slowDown=" + speedData.isHitSlowdown(),
                            "sneak=" + speedData.isSneaking(),
                            "strafe=" + speedData.getStrafe(),
                            "forward=" + speedData.getForward(),
                            "protocolID=" + getData().getProtocolVersion(),
                            "zeroMovementThreshold=" + this.zeroThreshold,
                            "speedAmplifier=" + getData().getPotionProcessor().getSpeedPotionAmplifier(),
                            "speedTicks=" + getData().getPotionProcessor().getSpeedPotionTicks(),
                            "gameVersion=" + getData().getProtocolVersion(),
                            "omniSprint="+speedData.isOmniSprinting());
                }
            } else {
                this.threshold -= Math.min(this.threshold, 0.0015);
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


    public boolean postExempt() {
        if (getData().getActionProcessor().isTeleportingV2()
                || (getData().getLastTeleport().isSet() && getData().getLastTeleport().getDelta() < 9)
                || (getData().getLastEnderPearl().isSet() && getData().getLastEnderPearl().getDelta() < 20)
                || getData().getCollisionProcessor().getLiquidTicks() > 0
                || getData().getCollisionWorldProcessor().getCauldronTicks() > 0
                || getData().getCollisionProcessor().getMountTicks() > 0
                || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(2)
                || getData().getCollisionWorldProcessor().getBlockAboveTicks() > 0
                || getData().getCollisionWorldProcessor().isSoulSand()
                || getData().getLastExplosionTimer().hasNotPassed(40)
                || getData().getCollisionWorldProcessor().getIceTicks() > 0
                || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                || (getData().getPotionProcessor().getSlownessAmplifier() == 0 && getData().getPotionProcessor().isJumpPotion())
                || (getData().getLastFallDamageTimer().isSet() && getData().getLastFallDamageTimer().getDelta() < 10)
                || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                || getData().getCollisionProcessor().isWeb()
                || getData().getBlockProcessor().getLastCombatWallTicks() < 20
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().isBedrock()
                || getData().getCollisionWorldProcessor().getHopperTicks() > 0
                || getData().getActionProcessor().getRespawnTimer().getDelta() < 20
                || getData().getSetBackProcessor().getLastDead() > 0
                || getData().getSetBackProcessor().getSetBackTick() > 0
                || getData().getCollisionProcessor().isWebFullCheck()
                || (getData().getLastWorldChange().isSet() && getData().getLastWorldChange().getDelta() < 40)
                //TODO: check later if bypass...
                || getData().getCombatProcessor().isExemptPunchBow()
                || getData().getCollisionProcessor().getWebFullTicks() > 0
                || getData().getBlockProcessor().getLastWebUpdateTick() < 7
                || getData().getCollisionProcessor().getWebTicks() > 0
                || getData().getCollisionProcessor().isWebInside()
                || getData().generalCancel()
                || (getData().getHorizontalProcessor().getVelocitySimulator().getLastVelocitySimulatedTimer().isSet()
                && getData().getHorizontalProcessor().getVelocitySimulator().getLastVelocitySimulatedTimer().getDelta() < 20)
                || getData().getCollisionWorldProcessor().getWallTicks() > 0
                || (getData().getLastExplosionTimer().isSet() && getData().getLastExplosionTimer().getDelta() < 10)
                || getData().getPotionProcessor().getPoisonTicks() > 0
                || getData().getPotionProcessor().isPoisonPotion()
                || getData().getVelocityProcessor().getExtraVelocityTicks() < 7
                || getData().getCollisionWorldProcessor().isCollidingHorizontal()
                || getData().getCollisionWorldProcessor().getCollideHorizontalTicks() > 0
                || getData().isEnderDragon()
                || (getData().getMovementProcessor().getClientWallCollision().isSet()
                && getData().getMovementProcessor().getClientWallCollision().getDelta() < 40)) {
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