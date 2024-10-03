package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.api.check.data.PredictionData;

@CheckInformation(
        name = "InvalidMove",
        subName = "U",
        checkNameEnum = CheckName.INVALID_MOVE,
        checkType = CheckType.MOVEMENT,
        experimental = true,
        description = "Detects invalid horizontal movements based off predictions",
        state = CheckState.DEV)
public class InvalidMoveU extends Check {

    private double threshold;
    private boolean secondChance;
    private double bypass, waterBypassCheck;
    private int exemptTicks;

    @Override
    public void onPrediction(PredictionData data) {

        int airTicks = getData().getMovementProcessor().getAirTicks();
        int groundTicks = getData().getMovementProcessor().getGroundTicks();

        boolean invalid = data.getOffset() > 1E-15 && data.getOffset() < 1E-7;

        if (!exempt(data) && invalid) {

            this.getData().getSetBackProcessor().setLastInvalidTick(50);

            if (++this.threshold > 25.5) {

                if (getData().getMovementProcessor().isLastHeldItemExempt()) {
                    return;
                }

                this.getData().sendDevAlert("InvalidMove U",
                        "offset=" + data.getOffset(),
                        "airTicks=" + airTicks,
                        "groundTicks=" + groundTicks,
                        "velocityTick=" + data.getVelocityTick(),
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
                        "fastMath=" + data.isFastMath(),
                        "omni-sprint="+data.getData().isOmniSprinting());
            }
        } else {
            this.threshold -= Math.min(this.threshold, 0.01);
        }
    }

    private boolean exempt(PredictionData data) {
        if (getData().getMovementProcessor().getPositionTicks() < 7
                || getData().isBedrock()
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().getMovementProcessor().getLastFlightTimer().getDelta() < 20
                && getData().getMovementProcessor().getLastFlightTimer().isSet()
                || getData().getCollisionWorldProcessor().getSoulSandTicks() > 0
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 10
                || getData().getSetBackProcessor().getSetBackTick() > 0
                || getData().getActionProcessor().isTeleportingV2()
                || getData().getActionProcessor().getRespawnTimer().getDelta() < 10
                && getData().getActionProcessor().getRespawnTimer().isSet()
                || getData().getPotionProcessor().getSpeedPotionAmplifier() > 100
                || getData().getPotionProcessor().getSpeedPotionAmplifier() == 0 && getData().getPotionProcessor().isSpeedPotion()
                || getData().getPotionProcessor().getSlownessAmplifier() == 0 && getData().getPotionProcessor().isSlownessPotion()
                || getData().getLastExplosionTimer().hasNotPassed(40)
                && getData().getLastExplosionTimer().isSet()
                || getData().getCollisionProcessor().getLiquidTicks() > 0
                || getData().getPotionProcessor().getInvalidPotionAmpliferTicks() <= 40
                || getData().getCollisionWorldProcessor().isCollidingHorizontal()
                || getData().getCollisionWorldProcessor().isClimbing()
                || getData().getPistonUpdateTimer().getDelta() < 3 && getData().getPistonUpdateTimer().isSet()
                || getData().getHorizontalProcessor().getVelocitySimulator()
                .getLastVelocitySimulatedTimer().getDelta() < 10
                && getData().getHorizontalProcessor().getVelocitySimulator()
                .getLastVelocitySimulatedTimer().isSet()
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

        if ((getData().getMovementProcessor().getClientWallCollision().getDelta() < 3
                && getData().getMovementProcessor().getClientWallCollision().isSet())
                && (getData().getCollisionWorldProcessor().getCollidingHorizontallyTimer().getDelta() < 3
                && getData().getCollisionWorldProcessor().getCollidingHorizontallyTimer().isSet())
                || getData().getCollisionWorldProcessor().isCollidingHorizontal()) {
            return true;
        }

        if (getData().generalCancel()) {

            return true;
        }

        if (getData().getGhostBlockProcessor().getLastGhostBlockTimer().hasNotPassed(0)
                && getData().getGhostBlockProcessor().getLastGhostBlockTimer().isSet()) {
            return true;
        }


        if (getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0
                && getData().getMovementProcessor().getDeltaY() > 0
                && (getData().getMovementProcessor().getDeltaY() % 0.015625 == 0)) {
            return true;
        }


        if (!this.getData().getPotionProcessor().isSpeedPotion()
                && getData().getPotionProcessor().getSpeedPotionTicks() > 0
                || getData().getActionProcessor().getLastWalkSpeedTimer().getDelta() < 20
                && getData().getActionProcessor().getLastWalkSpeedTimer().isSet()) {
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

        if (data.getData() == null) {
            return true;
        }


        if ((getData().getVelocityProcessor().getServerVelocityTicks() <= 20
                || getData().getHorizontalProcessor().getVelocityTicks() < 20
                || getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 20)
                && (getData().getCollisionProcessor().getWebTicks() > 0
                || getData().getBlockProcessor().getLastWebUpdateTick() < 7
                || getData().getCollisionProcessor().getWebFullTicks() > 0)) {
            return true;
        }

        if (data.getVelocityTick() <= 20) {
            return true;
        }

        return false;
    }
}