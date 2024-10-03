package pro.cyrent.anticheat.api.check.impl.movement.speed;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.math.MathUtil;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Speed",
        subName = "C",
        checkNameEnum = CheckName.SPEED,
        checkType = CheckType.MOVEMENT,
        description = "Detects if the player moves too quickly in liquids",
        punishable = false,
        punishmentVL = 5,
        state = CheckState.RELEASE)
public class SpeedC extends Check {

    private double threshold;
    private int timeSinceNoLiquid;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            boolean water = getData().getCollisionProcessor().isWater();
            boolean lava = getData().getCollisionProcessor().isLava();

            if (water || lava) {
                this.timeSinceNoLiquid++;

                double deltaXZ = getData().getMovementProcessor().getDeltaXZ();

                int airTicks = getData().getMovementProcessor().getAirTicks();
                int groundTicks = getData().getMovementProcessor().getGroundTicks();

                // Set base max speeds for liquid types
                double maxWaterMovement = .15;
                double maxLavaMovement = 0.102D;

                // Ground long term in water is slower
                if (groundTicks > 4 && water) {
                    maxWaterMovement = .105;
                }

                // Handle Water
                if (water) {

                    // Check if they were not recently in liquids and give them extra leniency.
                    if (this.timeSinceNoLiquid < 20) {
                        if (airTicks > 0) {
                            maxWaterMovement = .36;
                        }
                    } else {
                        switch (airTicks) {
                            case 1: {
                                maxWaterMovement = 0.36;
                                break;
                            }

                            case 2: {
                                maxWaterMovement = 0.29;
                                break;
                            }

                            case 3: {
                                maxWaterMovement = 0.2;
                                break;
                            }
                        }
                    }

                    if (this.timeSinceNoLiquid < 20) {
                        if (groundTicks > 0) {
                            maxWaterMovement = 0.2895;
                        }
                    } else {
                        switch (groundTicks) {
                            case 1: {
                                maxWaterMovement = 0.2895;
                                break;
                            }

                            case 2: {
                                maxWaterMovement = 0.2;
                                break;
                            }

                            case 3: {
                                maxWaterMovement = 0.17;
                                break;
                            }

                            case 4: {
                                maxWaterMovement = 0.15;
                                break;
                            }
                        }
                    }
                } else {
                    // Handle Lava.
                    if (this.timeSinceNoLiquid <= 20) {
                        if (airTicks > 0) {
                            maxLavaMovement = .36;
                        }

                        if (groundTicks > 0) {
                            maxLavaMovement = 0.2873;
                        }
                    } else {
                        if (airTicks > 0) {
                            maxLavaMovement = 0.1;
                        } else {
                            maxLavaMovement = 0.0895;
                        }
                    }
                }

                int level = MathUtil.getDepthStriderLevel(getData());

                // Ignore higher level depth strider cuz idc
                if (level > 3) {
                    this.threshold = 0;
                    return;
                }

                if (level != 0) {
                    // Ground limits for depth strider
                    if (groundTicks > 0) {
                        if (level == 1) {
                            maxWaterMovement += 0.105;
                        } else {
                            maxWaterMovement += 0.2;
                        }
                    } else {
                        // Set air speed for depth strider with basic calc
                        maxWaterMovement += (level * 0.125);
                    }
                }

                // Speed adds a small amount onto their max movement, so we add this shit
                if (getData().getPotionProcessor().getSpeedPotionTicks() > 0) {
                    maxLavaMovement += getData().getPotionProcessor().getSpeedPotionAmplifier() * 0.074;
                    maxWaterMovement += getData().getPotionProcessor().getSpeedPotionAmplifier() * 0.074;
                }

                // Inside flowing liquid add extra speed
                if (getData().getCollisionProcessor().getFlowingTicks() < 20) {
                    maxWaterMovement += .3;
                    maxWaterMovement += .3;
                }

                // Newer versions allow quicker liquid movement speeds.
                if (getData().getProtocolVersion() > 47) {
                    maxWaterMovement += .25;
                    maxLavaMovement += 0.06;
                }

                // Invalid movement.
                boolean invalid = (deltaXZ >= maxWaterMovement && water || deltaXZ >= maxLavaMovement && lava)
                        && !exempting();

                // Fail and detect.
                if (invalid) {
                    if (++this.threshold > 4.5) {
                        this.fail("deltaXZ="+deltaXZ,
                                "max-speed=" + (lava ? maxLavaMovement : maxWaterMovement),
                                "airTick="+airTicks,
                                "groundTicks="+groundTicks);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .0225);
                }
            } else {
                // Reset when not in liquids.
                this.timeSinceNoLiquid = 0;
            }
        }
    }

    private boolean exempting() {
        if (getData().generalCancel()
                || this.getData().getActionProcessor().getWalkSpeed() != 0.1F
                || getData().isBedrock()
                || getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0
                || getData().getHorizontalProcessor().getLastSetBackTimer().getDelta() < 5
                || getData().getVelocityProcessor().getVelocityATicks() < 20
                || getData().getSetBackProcessor().getSetBackTick() > 0
                || (getData().getActionProcessor().getRespawnTimer().isSet() && getData().getActionProcessor().getRespawnTimer().getDelta() < 10)
                || (getData().getActionProcessor().getLastWalkSpeedTimer().isSet() && getData().getActionProcessor().getLastWalkSpeedTimer().getDelta() < 20)
                || getData().getVelocityProcessor().getExtraVelocityTicks() < 40
                || (getData().getHorizontalProcessor().getVelocitySimulator().getLastVelocitySimulatedTimer().isSet() && getData().getHorizontalProcessor().getVelocitySimulator().getLastVelocitySimulatedTimer().getDelta() < 6)
                || (getData().getActionProcessor().getLastVehicleTimer().isSet() && getData().getActionProcessor().getLastVehicleTimer().getDelta() < 20)
                || (getData().getPotionProcessor().getSpeedPotionAmplifier() == 0 && getData().getPotionProcessor().isSpeedPotion())
                || (getData().getPotionProcessor().getSlownessAmplifier() == 0 && getData().getPotionProcessor().isSlownessPotion())
                || getData().getPotionProcessor().getSpeedPotionAmplifier() > 4
                || getData().getActionProcessor().isTeleportingV2()) {
            this.threshold = 0;
            return true;
        }

        if (!this.getData().getPotionProcessor().isSpeedPotion()
                && getData().getPotionProcessor().getSpeedPotionTicks() > 0) {
            this.threshold = 0;
            return true;
        }

        if (this.getData().getPotionProcessor().isSpeedPotion()
                && getData().getPotionProcessor().getSpeedPotionTicks() < 1) {
            this.threshold = 0;
            return true;
        }

        if (getData().getCollisionProcessor().isWater()) {
            if (getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 20
                    || getData().getVelocityProcessor().getServerVelocityTicks() <= 20) {
                this.threshold = 0;
                return true;
            }
        }

        if (getData().getCollisionProcessor().isLava()) {
            if ((getData().getLastFireTickTimer().isSet() && getData().getLastFireTickTimer().getDelta() < 20)
                    && (getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 20
                    || getData().getVelocityProcessor().getServerVelocityTicks() <= 20)) {
                this.threshold = 0;
            }
        }

        if (getData().getLastProjectileDamage().isSet() && getData().getLastProjectileDamage().getDelta() < 40) {
            this.threshold = 0;
            return true;
        }

        return false;
    }
}
