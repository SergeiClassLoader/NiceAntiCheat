package pro.cyrent.anticheat.api.check.impl.bedrock.move.speed;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.api.check.data.PredictionData;
import org.bukkit.Material;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "GeyserSpeed",
        subName = "B",
        checkNameEnum = CheckName.GEYSER_SPEED,
        checkType = CheckType.MOVEMENT,
        description = "Detects invalid horizontal movements based off predictions",
        state = CheckState.BETA)
public class GeyserSpeedB extends Check {

    private double threshold;
    private boolean secondChance;
    private double bypass, waterBypassCheck;

    @Override
    public void onPrediction(PredictionData data) {

        if (!getData().isBedrock()) {
            return;
        }

        boolean returnVelocity = (getData().getVelocityProcessor().getServerVelocityTicks() <= 20
                || getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 20);

        if (getData().getMovementProcessor().getPositionTicks() < 7
                || getData().getMovementProcessor().getRotatingTicks() < 3
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().getMovementProcessor().getLastFlightTimer().getDelta() < 20
                || getData().getCollisionWorldProcessor().getSoulSandTicks() > 0
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getLastExplosionTimer().hasNotPassed(40)
                || getData().getCollisionProcessor().getLiquidTicks() > 0
                && !getData().getCollisionProcessor().isLava()
                || getData().getCollisionWorldProcessor().isCollidingHorizontal()
                || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                || getData().getCollisionWorldProcessor().getIceTicks() > 0
                || getData().getCollisionWorldProcessor().isClimbing()
                || getData().getPistonUpdateTimer().getDelta() < 3
                || getData().getCollisionProcessor()
                .getLastEnderDragonNearTimer().hasNotPassed(20)
                || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(20)) {

            return;
        }

        
        if (getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                || getData().getCollisionWorldProcessor().getIceTicks() > 0) {
            if (getData().getPotionProcessor().getSpeedPotionAmplifier() > 3) {
                this.threshold = 0;
                return;
            }
        }

        if (getData().getMovementProcessor().getClientWallCollision().getDelta() < 3) {
            return;
        }

        if (getData().generalCancel()) {
            return;
        }

        if (getData().getGhostBlockProcessor().getLastGhostBlockTimer().hasNotPassed(5)) {
            return;
        }

        if (getData().getActionProcessor().isTeleporting()) {
            return;
        }

        if (getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0
                && (getData().getMovementProcessor().getDeltaY() == 0.5
                || getData().getMovementProcessor().getLastDeltaY() == 0.5)) {
            return;
        }


        if (!this.getData().getPotionProcessor().isSpeedPotion()
                && getData().getPotionProcessor().getSpeedPotionTicks() > 0) {
            this.threshold = 0;
            return;
        }

        if (this.getData().getPotionProcessor().isSpeedPotion()
                && getData().getPotionProcessor().getSpeedPotionTicks() < 1) {
            this.threshold = 0;
            return;
        }

        if (getData().getMovementProcessor().getLastGhostBlockAboveTick() >= 19
                && this.bypass < 10) {
            this.bypass++;
            return;
        }

        if (getData().getGhostBlockProcessor().getLiquidBypass() > 0) {

            if ((this.waterBypassCheck += 1) < 50) {
                this.threshold = 0;
                return;
            } else {
                if (!this.secondChance) {
                    this.waterBypassCheck /= 1.25;
                    this.secondChance = true;
                }
            }
        }

        int airTicks = getData().getMovementProcessor().getAirTicks();
        int groundTicks = getData().getMovementProcessor().getGroundTicks();

        if (data.getData() == null) {
            return;
        }

        if (returnVelocity) {
            return;
        }

        //ass fix for bedrockers!
        double max = 1E-3;
        
        if (data.getOffset() >= max && getData().getMovementProcessor().getDeltaXZ() > 0.23) {

            if (++this.threshold > 15.0) {

                if (getData().getPlayer().getItemInHand() != null
                        && (getData().getLastBlockPlaceCancelTimer().getDelta() < 20
                        || getData().getBlockProcessor()
                        .getLastConfirmedCancelPlaceTimer().getDelta() < 20
                        && getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() > 10)
                        && (getData().getPlayer().getItemInHand().getType() == Material.LADDER
                        || getData().getPlayer().getItemInHand().getType() == Material.VINE
                        || getData().getPlayer().getItemInHand().getType() == Material.WEB
                        || getData().getPlayer().getItemInHand().getType() == Material.ICE
                        || getData().getPlayer().getItemInHand().getType() == Material.PACKED_ICE
                        || getData().getPlayer().getItemInHand().getType() == Material.WATER
                        || getData().getPlayer().getItemInHand().getType() == Material.WATER_BUCKET
                        || getData().getPlayer().getItemInHand().getType() == Material.LAVA
                        || getData().getPlayer().getItemInHand().getType() == Material.LAVA_BUCKET
                        || getData().getPlayer().getItemInHand().getType() == Material.SLIME_BLOCK)) {
                    getData().setBack();
                    if (!Anticheat.INSTANCE.getConfigValues().isGhostBlockSupport()) {
                        getData().consoleLogLagBack("Speed A, offset=" + data.getOffset());
                    }
                    return;
                }

                this.fail("Not following minecraft prediction",
                        "offset=" + data.getOffset(),
                        "airTicks=" + airTicks,
                        "groundTicks=" + groundTicks,
                        "confirmedVelocityTick=" + getData().getVelocityProcessor().getVelocityTicksConfirmed(),
                        "serverVelocityTick=" + getData().getVelocityProcessor().getServerVelocityTicks(),
                        "velocityTick=" + data.getVelocityTick(),
                        "yaw=" + getData().getHorizontalProcessor().getYaw(),
                        "using=" + data.getData().isUsing(),
                        "sprint=" + data.getData().isSprint(),
                        "jump=" + data.getData().isJump(),
                        "slowDown=" + data.getData().isHitSlowdown(),
                        "sneak=" + data.getData().isSneaking(),
                        "strafe=" + data.getData().getStrafe(),
                        "forward=" + data.getData().getForward(),
                        "pastMotion="+ data.getPastMotion(),
                        "motion="+data.getDeltaXZ(),
                        "fastMath="+ data.isFastMath());

            }
        } else {
            this.threshold -= Math.min(this.threshold, 0.07);
        }
    }
}
