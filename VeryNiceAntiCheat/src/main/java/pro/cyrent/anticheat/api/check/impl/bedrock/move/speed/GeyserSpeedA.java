package pro.cyrent.anticheat.api.check.impl.bedrock.move.speed;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "GeyserSpeed",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.GEYSER_SPEED,
        description = "Detects if the player is moving too quickly (bedrock only)",
        punishmentVL = 20,
        state = CheckState.ALPHA)
public class GeyserSpeedA extends Check {


    private double threshold = Double.MAX_VALUE;
    private int bypass;
    private double waterBypassCheck;
    private boolean secondChance;

    private double buffer;

    @Override
    public void onPacket(PacketEvent event) {

        if (!getData().isBedrock()) {
            return;
        }

        if (event.isMovement()) {

            if (getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                    || getData().getMovementProcessor().getLastFlightTimer().hasNotPassed(20)
                    || getData().getLastTeleport().getDelta() < 10
                    && getData().getSetBackProcessor().getSetBackTick() <= 0
                    || getData().getCollisionProcessor().getNearBoatTicks() > 0
                    || getData().getCollisionProcessor().isWeb()
                    || getData().getCollisionWorldProcessor().isClimbing()
                    || getData().getPotionProcessor().getSpeedPotionAmplifier() > 2
                    || getData().getLastEnderPearl().getDelta() < 20
                    || this.getData().getActionProcessor().getWalkSpeed() != 0.1F
                    || getData().getLastExplosionTimer().hasNotPassed(40)
                    || getData().getCollisionProcessor()
                    .getLastEnderDragonNearTimer().hasNotPassed(20)
                    || getData().getCollisionWorldProcessor().getIceTicks() > 0
                    || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(20)) {
                this.buffer = 0;
                return;
            }

            if (getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 20
                    || getData().getVelocityProcessor().getServerVelocityTicks() <= 20) {
                return;
            }

            if (getData().generalCancel()) {
                return;
            }

            if (getData().getVelocityProcessor()
                    .getExtraVelocityTicks() < 60) {
                return;
            }

            if (getData()
                    .getGhostBlockProcessor().getLastGhostBlockTimer().getDelta() < 10) {
                return;
            }

            if (getData().getMovementProcessor().getLastGhostBlockAboveTick() > 0
                    && this.bypass < 10) {
                this.bypass++;
                return;
            }

            if (getData().getGhostBlockProcessor().getLiquidBypass() > 0) {

                if ((this.waterBypassCheck += 1) < 50) {
                    this.buffer = 0;
                    return;
                } else {
                    if (!this.secondChance) {
                        this.waterBypassCheck /= 1.25;
                        this.secondChance = true;
                    }
                }
            }

            if (getData().getActionProcessor().isTeleportingV2() && getData().getSetBackProcessor().getSetBackTick() <= 0) {
                return;
            }

            if (getData().getActionProcessor().isTeleportingV2() && getData().getSetBackProcessor().getSetBackTick() <= 0
                    && (getData().getCollisionWorldProcessor().getAnvilTicks() > 0
                    || getData().getCollisionWorldProcessor().isBed()
                    || getData().getCollisionWorldProcessor().isSkull()
                    || getData().getCollisionWorldProcessor().isDoor())) {
                this.buffer = 0;
                return;
            }

            if (!this.getData().getPotionProcessor().isSpeedPotion()
                    && getData().getPotionProcessor().getSpeedPotionTicks() > 0) {
                this.buffer = 0;
                return;
            }

            if (this.getData().getPotionProcessor().isSpeedPotion()
                    && getData().getPotionProcessor().getSpeedPotionTicks() < 1) {
                this.buffer = 0;
                return;
            }

            double deltaXZ = getData().getMovementProcessor().getDeltaXZ();

            if (!getData().getMovementProcessor().getTo().isOnGround()) {
                this.threshold = 0.36001F;
            }

            if (getData().getMovementProcessor().getBlockJumpTimer().hasNotPassed(20)) {
                this.threshold = this.threshold + 0.03;
            }

            if (getData().getMovementProcessor().getGroundTicks() > 12) {
                this.threshold = 0.36;
            } else if (getData().getMovementProcessor().getGroundTicks() > 7
                    && getData().getMovementProcessor().getGroundTicks() < 13) {
                this.threshold = 0.36;
            }

            if (getData()
                    .getBlockProcessor().getLastConfirmedBlockPlaceTimer().hasNotPassed(3)) {
                this.threshold = this.threshold + .10122f;
            }

            if (getData().getMovementProcessor().getTo().isOnGround()
                    && getData().getMovementProcessor().getFrom().isOnGround()
                    && !getData().getMovementProcessor().getFromFrom().isOnGround()) {
                this.threshold = 0.42;
            }

            if (!getData().getMovementProcessor().getTo().isOnGround()
                    && getData().getMovementProcessor().getFrom().isOnGround()) {
                this.threshold = 0.613;
            }

            if (getData().getPistonUpdateTimer().getDelta() < 20) {
                this.threshold = 1.2;
            }

            if (getData().getCollisionWorldProcessor().getIceTicks() > 0 &&
                    (getData().getCollisionWorldProcessor().getPistionTicks() > 0
                            || getData().getCollisionWorldProcessor().getSlimeTicks() > 0)) {
                this.threshold = 1.2;
            }

            if (getData().getCollisionWorldProcessor().isHalfBlock()
                    || getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                    || getData().getCollisionWorldProcessor().getCarpetTicks() > 0
                    || getData().getCollisionWorldProcessor().isDoor()
                    || getData().getCollisionWorldProcessor().getWallTicks() > 0
                    || getData().getCollisionWorldProcessor().getCauldronTicks() > 0
                    || getData().getCollisionWorldProcessor().isSkull()
                    || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                    || getData().getCollisionWorldProcessor().isBed()) {
                this.threshold = 0.75;
            }

            boolean ground = getData().getMovementProcessor().getTo().isOnGround();
            boolean lastGround = getData().getMovementProcessor().getFrom().isOnGround();

            if (getData().getCollisionWorldProcessor().getBlockAboveTimer().getDelta() < 2) {

                if (ground) {
                    this.threshold = 0.52;
                } else {
                    this.threshold = 0.42;
                }

                if (lastGround && !ground) {
                    this.threshold = 0.7;
                }
            }

            if (getData().getCollisionProcessor().getLiquidTicks() > 0
                    && !getData().getCollisionProcessor().isLava()) {
                this.threshold = 0.85;
            }

            if (getData().getCollisionProcessor().getLiquidTicks() > 0
                    && getData().getCollisionProcessor().isLava()) {
                this.threshold = 0.23;
            }

            if (getData().getCollisionWorldProcessor().getIceTicks() > 0
                    && getData().getCollisionWorldProcessor().getBlockAboveTicks() < 1) {
                this.threshold = 0.7;
            }

            if (getData().getCollisionWorldProcessor().getIceTicks() > 0
                    && getData().getCollisionWorldProcessor().getBlockAboveTicks() > 0) {
                this.threshold = 1.2;
            }

            if (getData().getPotionProcessor().isSpeedPotion()) {
                this.threshold += getData().getPotionProcessor().getSpeedPotionAmplifier() * 0.045f;
            }

            if (getData().getLastExplosionTimer().hasNotPassed(20)) {
                return;
            }

            if (getData().getVelocityProcessor()
                    .getLastVelocityData() != null) {

                if ((getData().getVelocityProcessor().getServerVelocityTicks() <= 20
                        || getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 20)
                        && getData().getLastFallDamageTimer().getDelta() > 20) {
                    threshold += getData().getVelocityProcessor()
                            .getLastVelocityData().getSpeed();
                }

                if ((getData().getVelocityProcessor().getServerVelocityTicks() <= 20
                        || getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 20)
                        && getData().isEnderDragon() && !getData().isUsedPunch()
                        && getData().getLastShotByArrowTimer().passed(20)) {
                    threshold += getData().getVelocityProcessor()
                            .getLastVelocityData().getSpeed();
                }
            }

            if (getData().getLastFallDamageTimer().getDelta() < 7) {
                this.threshold += 0.4;
            }

            if (getData().getVelocityProcessor().getExtraVelocityTicks() < 60) {
                this.threshold += 0.1;
            }

            if (getData().getCombatProcessor().isExemptPunchBow()) {
                return;
            }

            if (this.threshold == Double.MAX_VALUE || this.threshold == 0) {
                return;
            }

            if (deltaXZ > (this.threshold + 0.08)) {

                getData().getSetBackProcessor().setInvalid(true);

                if (++this.buffer > 9.5) {

                    this.fail("Moving too quickly (basic limit)",
                            "speed="+deltaXZ,
                            "speed-limit="+(threshold + 0.06),
                            "buffer="+buffer);

                }
            } else {
                this.buffer -= Math.min(this.buffer, 0.04);
            }
        }
    }
}
