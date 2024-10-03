package pro.cyrent.anticheat.api.check.impl.movement.speed;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import org.bukkit.Material;
import pro.cyrent.anticheat.api.check.*;

import java.util.ArrayList;
import java.util.List;

@CheckInformation(
        name = "Speed",
        subName = "B",
        checkNameEnum = CheckName.SPEED,
        checkType = CheckType.MOVEMENT,
        description = "Detects if the player moves too quickly (backup check)",
        punishmentVL = 12,
        state = CheckState.RELEASE)
public class SpeedB extends Check {
    private int bypass;
    private double waterBypassCheck;
    private boolean secondChance;
    private double buffer;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            List<String> scenarios = new ArrayList<>();

            boolean clientGround = getData().getMovementProcessor().getTo().isOnGround();
            boolean previousGround = getData().getMovementProcessor().getFrom().isOnGround();

            int airTicks = getData().getMovementProcessor().getAirTicks();
            int groundTicks = getData().getMovementProcessor().getGroundTicks();

            double deltaXZ = getData().getMovementProcessor().getDeltaXZ();

            if (clientGround) {
                scenarios.add("Ground");
            } else {
                scenarios.add("Air");
            }

            // The base max speed for being off the ground & on the ground.
            double maxMovementSpeed = clientGround ? 0.2875D : 0.3605D;

            // Apply client ground & air tick max motions.
            if (clientGround) {
                switch (groundTicks) {
                    case 1: {
                        // First tick somehow is slightly slower than the next ticks.
                        maxMovementSpeed = 0.33D;
                        scenarios.add("Landed");
                        break;
                    }

                    // Next tick increase significantly.
                    case 2: {
                        maxMovementSpeed = 0.5D;
                        break;
                    }

                    // Lower the ticks each slowly.
                    case 3: {
                        maxMovementSpeed = 0.4D;
                        break;
                    }

                    // Lowers again.
                    case 4:
                    case 5:
                    case 6: {
                        maxMovementSpeed = 0.335D;
                        break;
                    }
                }
            } else if (airTicks == 1) {
                // First jump air tick can be a massive amount, after that it's usually fine.
                maxMovementSpeed = 0.615D;

                double deltaY = getData().getMovementProcessor().getDeltaY();

                if (deltaY > 0.0) {
                    scenarios.add("Jumped");
                }

                if (deltaY < 0.0) {
                    scenarios.add("Fall");
                }

                if (deltaY == 0) {
                    scenarios.add("No-Motion");
                }
            }

            // Block above max movement
            if (getData().getCollisionWorldProcessor().getBlockAboveTimer().getDelta() < 2
                    && getData().getCollisionWorldProcessor().getBlockAboveTimer().isSet()) {

                // Client ground speeds up recently head hitting
                if (clientGround && groundTicks < 7) {
                    maxMovementSpeed = 0.52D;
                    scenarios.add("Head-Hit-Ground");
                    // Air Speed when head hitting is slightly quicker
                } else if (!clientGround && !previousGround && airTicks > 0) {
                    maxMovementSpeed = 0.42D;
                    scenarios.add("Head-Hit-Air");
                }

                // Jumping increases speed
                if (!clientGround && previousGround) {
                    maxMovementSpeed = 0.7D;
                    scenarios.add("Head-Hit-Jumped");
                }
            }

            // Half blocks increase overall speed
            if (getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                    || getData().getCollisionWorldProcessor().getCarpetTicks() > 0
                    || getData().getCollisionWorldProcessor().isDoor()
                    || getData().getCollisionWorldProcessor().getWallTicks() > 0
                    || getData().getCollisionWorldProcessor().getCauldronTicks() > 0
                    || getData().getCollisionWorldProcessor().isSkull()
                    || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                    || getData().getCollisionWorldProcessor().isBed()
                    || getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0
                    && getData().getMovementProcessor().getDeltaY() > 0) {
                maxMovementSpeed = 0.75D;
                scenarios.add("Half-Block");
            }

            // Inside water their movement speed can vary, so we make it quite high.
            if (getData().getCollisionProcessor().getLiquidTicks() > 0
                    && !getData().getCollisionProcessor().isLava()) {
                maxMovementSpeed = 0.85D;
                scenarios.add("Lava");
            }

            // Ice next to pistons & or slime, ice with blocks above, piston updates increase speed significantly.
            if (getData().getCollisionWorldProcessor().getIceTicks() > 0 &&
                    (getData().getCollisionWorldProcessor().getPistionTicks() > 0
                            || getData().getCollisionWorldProcessor().getSlimeTicks() > 0)
                    || getData().getCollisionWorldProcessor().getIceTicks() > 0
                    && getData().getCollisionWorldProcessor().getBlockAboveTicks() > 0
                    || getData().getPistonUpdateTimer().getDelta() < 20
                    && getData().getPistonUpdateTimer().isSet()
                    || getData().getCollisionWorldProcessor().getPistionTicks() > 0) {
                maxMovementSpeed = 1.2D;
                scenarios.add("Piston/Ice/Slime/Head-Hit");
            }

            // minimal increase incase
            if (groundTicks >= 7) {
                maxMovementSpeed += 0.003D;
            }

            // Placing blocks
            if (getData()
                    .getBlockProcessor().getLastConfirmedBlockPlaceTimer().hasNotPassed(3)) {
                scenarios.add("Place");
                maxMovementSpeed += 0.10122D;
            }

            // Jumped up block momentum
            if (getData().getMovementProcessor().getBlockJumpTimer().isSet()
                    && getData().getMovementProcessor().getBlockJumpTimer().getDelta() < 20) {
                scenarios.add("Jump-Accel");
                maxMovementSpeed += getData().getMovementProcessor().getBlockJumpAcelleration();
            }

            // Ice & Half blocks increase speed
            if (getData().getCollisionWorldProcessor().getIceTicks() > 0
                    && getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0) {
                scenarios.add("Half-Block/Ice");
                maxMovementSpeed += 0.75D;
            }

            // Account for players taking velocity multiplied by an additional more to make sure.
            if (getData().getVelocityProcessor().getServerVelocityTicks() <= 40
                    || getData().getVelocityProcessor().getVelocityATicks() <= 40) {
                scenarios.add("Velocity");
                maxMovementSpeed += Math.abs(getData().getVelocityProcessor().getVelocityH() * 1.2D);
            }

            // Ender dragon attacks
            if ((getData().getVelocityProcessor().getServerVelocityTicks() <= 40
                    || getData().getVelocityProcessor().getVelocityATicks() <= 40)
                    && getData().isEnderDragon()) {
                scenarios.add("EnderDragon");
                maxMovementSpeed += Math.abs(getData().getVelocityProcessor().getVelocityH() * 3.0D);
            }

            // Explosions increase speed by a lot
            if (getData().getLastExplosionTimer().hasNotPassed(20)
                    && getData().getLastExplosionTimer().isSet()
                    || getData().getActionProcessor().getExplosionTimer().getDelta() < 20) {
                scenarios.add("Explosion");
                maxMovementSpeed += Math.abs(getData().getActionProcessor().getLastReportedExplosionMotion());
            }

            // Apply extra speed based on fall damage as players randomly get increased speeds.
            if (getData().getLastFallDamageTimer().getDelta() < 7 && getData().getLastFallDamageTimer().isSet()) {
                scenarios.add("Fall-Damage");
                maxMovementSpeed += 0.4D;
            }

            // Extra velocity happens add an amount to the max speed.
            if (getData().getVelocityProcessor().getExtraVelocityTicks() < 60) {
                scenarios.add("Extra-KB");
                maxMovementSpeed += 0.1D;
            }

            // Apply speed potions
            if (getData().getPotionProcessor().isSpeedPotion()) {
                maxMovementSpeed += getData().getPotionProcessor().getSpeedPotionAmplifier() * 0.0625D;
            }

            // Velocity recently happened, block placements, block above add additional amounts
            if (getData().getVelocityProcessor().getVelocityATicks() < 100
                    || getData().getVelocityProcessor().getServerVelocityTicks() < 100
                    || getData().getLastBlockPlaceCancelTimer().getDelta() < 20
                    && getData().getLastBlockPlaceCancelTimer().isSet()
                    || getData().getLastBlockPlaceTimer().getDelta() < 20 && getData().getLastBlockPlaceTimer().isSet()
                    || getData().getCollisionWorldProcessor().getBlockAboveTicks() > 0
                    || getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() < 20
                    && getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().isSet()) {
                if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                        .getUniqueId().toString().equals(getData().getUuid().toString())) {
                    Anticheat.INSTANCE.getCheckPacketLog().add(
                            "Speed B threshold increase (now adding 0.06 for placing/velocity)"+ "\n");
                }

                scenarios.add("Additional");
                maxMovementSpeed += 0.06D;
            }

            if (!exempt()) {

                if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                        .getUniqueId().toString().equals(getData().getUuid().toString())) {
                    Anticheat.INSTANCE.getCheckPacketLog().add(
                            "Speed B Now Running Check: deltaXZ: " + deltaXZ + " threshold: "
                                    + maxMovementSpeed + " buffer: " + this.buffer + "\n");
                }

                if (deltaXZ >= maxMovementSpeed) {

                    this.getData().getSetBackProcessor().setLastInvalidTick(50);

                    if (getData().getMovementProcessor().isLastHeldItemExempt()) {

                        if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                                .getUniqueId().toString().equals(getData().getUuid().toString())) {
                            Anticheat.INSTANCE.getCheckPacketLog().add(
                                    "Failing Speed B, Lag Back Return" + "\n");
                        }
                        return;
                    }

                    // original 7.5
                    if (++this.buffer > 1.95) {

                        this.fail(
                                "speed=" + deltaXZ,
                                "speed-limit=" + maxMovementSpeed,
                                "buffer=" + this.buffer,
                                "air-ticks=" + airTicks,
                                "ground-ticks=" + groundTicks,
                                "speed-amplifier=" + getData().getPotionProcessor().getSpeedPotionAmplifier(),
                                "speed-ticks=" + getData().getPotionProcessor().getSpeedPotionTicks(),
                                "tags="+scenarios);

                    }
                } else {
                    // originally 0.01
                    this.buffer -= Math.min(this.buffer, 0.003);
                }
            }
        }
    }

    private boolean exempt() {

        if (getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                || getData().getMovementProcessor().getLastFlightTimer().hasNotPassed(20)
                || (getData().getLastTeleport().isSet() && getData().getLastTeleport().getDelta() < 10
                && getData().getMovementProcessor().getTick() > 50)
                && getData().getSetBackProcessor().getSetBackTick() <= 0
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().isBedrock()
                || getData().getHorizontalProcessor().getLastSetBackTimer().getServerDelta() < 5
                || getData().getSetBackProcessor().getSetBackTick() > 0
                || (getData().getActionProcessor().getRespawnTimer().isSet()
                && getData().getActionProcessor().getRespawnTimer().getDelta() < 10)
                || getData().getCollisionProcessor().isWeb()
                || (getData().getPotionProcessor().getSpeedPotionAmplifier() == 0 && getData().getPotionProcessor().isSpeedPotion())
                || (getData().getPotionProcessor().getSlownessAmplifier() == 0 && getData().getPotionProcessor().isSlownessPotion())
                || getData().getCollisionWorldProcessor().isClimbing()
                || (getData().getPotionProcessor().getSpeedPotionTicks() < 1 && getData().getPotionProcessor().isSpeedPotion())
                || (getData().getPotionProcessor().getSpeedPotionTicks() > 0 && !getData().getPotionProcessor().isSpeedPotion())
                || getData().getPotionProcessor().getSpeedPotionAmplifier() > 3
                || (getData().getLastEnderPearl().isSet() && getData().getLastEnderPearl().getDelta() < 5)
                || this.getData().getActionProcessor().getWalkSpeed() != 0.1F
                || getData().getCollisionProcessor().getLastEnderDragonNearTimer().hasNotPassed(20)
                || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(7)) {

            this.buffer = 0;

            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Speed B Return Type One\n"
                                + "slimeTicks: " + getData().getCollisionWorldProcessor().getSlimeTicks()
                                + " flightTimer: " + getData().getMovementProcessor().getLastFlightTimer().getDelta()
                                + " teleportDelta: " + getData().getLastTeleport().getDelta()
                                + " setBack: " + ((getData().getLastTeleport().isSet() && getData().getLastTeleport().getDelta() < 3
                                && getData().getMovementProcessor().getTick() > 50)
                                && getData().getSetBackProcessor().getSetBackTick() <= 0)
                                + " setBackTimer: "+
                                (getData().getHorizontalProcessor().getLastSetBackTimer().getServerDelta() < 5)
                                + " nearBoat: " + getData().getCollisionProcessor().getNearBoatTicks()
                                + " web: " + getData().getCollisionProcessor().isWeb()
                                + " climbing: " + getData().getCollisionWorldProcessor().isClimbing()
                                + " amplifier: " + getData().getPotionProcessor().getSpeedPotionAmplifier()
                                + " enderPearl: " + getData().getLastEnderPearl().getDelta()
                                + " walkSpeed: " + this.getData().getActionProcessor().getWalkSpeed()
                                + " dragon: " + getData().getCollisionProcessor().getLastEnderDragonNearTimer().getDelta()
                                + " vehicle: " + getData().getActionProcessor().getLastVehicleTimer().getDelta() + "\n"
                );
            }

            return true;
        }

        if (getData().getActionProcessor().getTeleportTicks() <= 2
                || getData().getTeleportProcessor().isPossiblyTeleporting()
                || getData().getLastEnderPearl().getDelta() < 10
                && getData().getLastEnderPearl().isSet()) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Speed B Return Type Teleport/Enderpearl\n"
                                + "Teleporting: " + (getData().getActionProcessor().getTeleportTicks() <= 2)
                                + "Teleporting-Possible: "+ getData().getTeleportProcessor().isPossiblyTeleporting()
                                + " EnderPearlTime (less than 10 is true): " + getData().getLastEnderPearl().getDelta()+ "\n");
            }
            return true;
        }

        if (getData().getFishingRodTimer().getPositionDelta() <= 40
                && (getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 20
                || getData().getVelocityProcessor().getExtraVelocityTicks() < 60
                || getData().getVelocityProcessor().getServerVelocityTicks() <= 20)) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Speed B Return Rod Velocity\n");
            }
            return true;
        }

        if (getData().generalCancel()) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Speed B Return Type Three General Cancel\n");
            }
            return true;
        }

        if (getData()
                .getGhostBlockProcessor().getLastGhostBlockTimer().getDelta() < 10
                && getData().getGhostBlockProcessor().getLastGhostBlockTimer().isSet()) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Speed B Ghost Block Ticks\n");
            }
            return true;
        }

        if (getData().getMovementProcessor().getLastGhostBlockAboveTick() > 0
                && this.bypass < 10) {
            this.bypass++;
            return true;
        }

        if (getData().getGhostBlockProcessor().getLiquidBypass() > 0) {

            if ((this.waterBypassCheck += 1) < 50) {
                this.buffer = 0;
                return true;
            } else {
                if (!this.secondChance) {
                    this.waterBypassCheck /= 1.25;
                    this.secondChance = true;
                }
            }
        }

        if (getData().getSetBackProcessor().getSetBackTick() > 0) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Speed B Teleport Set Back Return\n");
            }
            return true;
        }

        if (getData().getActionProcessor().isTeleportingV3() && getData().getSetBackProcessor().getSetBackTick() <= 0
                && (getData().getCollisionWorldProcessor().getAnvilTicks() > 0
                || getData().getCollisionWorldProcessor().isBed()
                || getData().getCollisionWorldProcessor().isSkull()
                || getData().getCollisionWorldProcessor().isDoor())) {
            this.buffer = 0;
            //   Bukkit.broadcastMessage("return 2");

            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Speed B Teleport Set Back Near Half Blocks Return\n");
            }
            return true;
        }

        if (!this.getData().getPotionProcessor().isSpeedPotion()
                && getData().getPotionProcessor().getSpeedPotionTicks() > 0
                || getData().getActionProcessor().getLastWalkSpeedTimer().getDelta() < 20
                && getData().getActionProcessor().getLastWalkSpeedTimer().isSet()) {
            this.buffer = 0;
            return true;
        }

        if (this.getData().getPotionProcessor().isSpeedPotion()
                && getData().getPotionProcessor().getSpeedPotionTicks() < 1) {
            this.buffer = 0;
            return true;
        }

        if (getData().getCombatProcessor().isExemptPunchBow()) {
            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(getData().getUuid().toString())) {
                Anticheat.INSTANCE.getCheckPacketLog().add(
                        "Speed B Punch Bow Return\n");
            }
            return true;
        }


        return false;
    }
}