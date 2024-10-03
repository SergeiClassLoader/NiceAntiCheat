package pro.cyrent.anticheat.api.check.impl.combat.killaura;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "KillAura",
        subName = "F",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.KILL_AURA,
        punishmentVL = 35,
        punishable = false,
        description = "Detects if the player is keep sprinting (basic)",
        state = CheckState.RELEASE)
public class KillAuraF extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {

                if (getData().generalCancel()
                        || getData().getActionProcessor().isTeleporting()
                        || getData().getVelocityProcessor().getServerVelocityTicks() <= 10
                        || getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 10
                        || getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                        || getData().getCollisionWorldProcessor().isHalfBlock()
                        || getData().getCollisionProcessor().getLiquidTicks() > 0
                        || getData().getCollisionWorldProcessor().isCarpet()
                        || getData().getActionProcessor().getTeleportTicks() < 4
                        || getData().getCollisionWorldProcessor().getBlockAboveTicks() > 0
                        || getData().getCollisionProcessor().getMountTicks() > 0
                        || getData().getPotionProcessor().getSpeedPotionAmplifier() == 0 && getData().getPotionProcessor().isSpeedPotion()
                        || getData().getPotionProcessor().getSlownessAmplifier() == 0 && getData().getPotionProcessor().isSlownessPotion()
                        || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                        || getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() < 2
                        || getData().getLastBlockPlaceCancelTimer().getDelta() < 10
                        || getData().getCollisionWorldProcessor().getAnvilTicks() > 0
                        || getData().getMovementProcessor().getClientWallCollision().getDelta() < 10
                        || getData().getCollisionProcessor().isWeb()
                        || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                        || getData().getCollisionWorldProcessor().getIceTicks() > 0
                        || getData().getCollisionWorldProcessor().isBed()
                        || getData().getPotionProcessor().isSlownessPotion()
                        || getData().getCollisionWorldProcessor().getSoulSandTicks() > 0
                        || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                        || getData().getCollisionWorldProcessor().getCollidingHorizontallyTimer().hasNotPassed(8)
                        || getData().getPotionProcessor().isSpeedPotion()
                        && getData().getPotionProcessor().getSpeedPotionTicks() < 1
                        || !getData().getPotionProcessor().isSpeedPotion()
                        && getData().getPotionProcessor().getSpeedPotionTicks() > 0) {
                    return;
                }

                if (getData().getCombatProcessor().getLastPlayerAttack() < 2) {
                    double deltaXZ = getData().getMovementProcessor().getDeltaXZ();
                    double lastDeltaXZ = getData().getMovementProcessor().getLastDeltaXZ();
                    double deltaYaw = getData().getMovementProcessor().getDeltaYawAbs();

                    double accel = Math.abs(deltaXZ - lastDeltaXZ);

                    double maxSpeed = 0.23 + (getData().getPotionProcessor().getSpeedPotionAmplifier() * 0.055);

                    if (deltaYaw > 0.2 && accel <= 0.01
                            && deltaXZ > maxSpeed && lastDeltaXZ > .18) {

                        // let me know if this falses please thx - demon
                        if (this.threshold++ > 8.75) {
                            this.fail(
                                    "yaw="+deltaYaw,
                                    "accel="+accel,
                                    "deltaXZ="+deltaXZ,
                                    "lastDeltaXZ="+lastDeltaXZ);
                        }
                    } else {

                        // this also could probably be more strict
                        this.threshold -= Math.min(this.threshold, .25);
                    }
                }
            }
        }
    }
}