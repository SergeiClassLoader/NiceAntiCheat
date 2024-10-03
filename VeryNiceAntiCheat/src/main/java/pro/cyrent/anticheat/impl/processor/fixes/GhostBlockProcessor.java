package pro.cyrent.anticheat.impl.processor.fixes;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.impl.processor.prediction.horizontal.HorizontalProcessor;
import pro.cyrent.anticheat.util.block.BlockUtil;
import pro.cyrent.anticheat.util.bukkit.RunUtils;
import pro.cyrent.anticheat.util.event.EventTimer;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.magic.MagicValues;
import pro.cyrent.anticheat.util.map.EvictingMap;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Material.*;
import static org.bukkit.Material.TRIPWIRE_HOOK;


@Getter
@Setter
public class GhostBlockProcessor extends Event {
    private final PlayerData data;

    private final Map<Integer, Long> validBlocks = new EvictingMap<>(12);

    private int lagBackTicks;
    private int liquidGhostChecks;
    private int liquidTotal;
    private int lastInvalidTick;
    private int verbose;
    private int teleports;
    private int teleportingTicks;
    private int liquidBypass;
    private int blackPeroplPresent;
    private int bypassTicks;
    private int correctionTick;
    private int funny;
    private int serverAirTicks;

    private double lastPredictionY;
    private double liquidThreshold;
    private double invalidChunkTicks;
    private double damageAmountFall = 0.5;
    private double lastPositionYCached;
    private double lastSilentTicks;
    private double fallDamageTicks;

    //fall damage
    private float fallDamage;

    private boolean setFallDamage;
    private boolean didTeleportForDamage;
    private boolean lastFoundAnything = false;

    private long lastFlying;
    private long lastBlockUpdate;
    private final long reset = TimeUnit.SECONDS.toMillis(6L);

    private Location lastClosestGround = null, cachedLocation = null, lastTeleportedLocation = null;

    private final EventTimer lastGhostBlockTimer;
    private final EventTimer invalidMovement;

    private double invalidChunkThreshold;

    public GhostBlockProcessor(PlayerData user) {
        this.data = user;
        this.lastGhostBlockTimer = new EventTimer(20, data);
        this.invalidMovement = new EventTimer(20, data);
    }

    public boolean processPre(WrapperPlayClientPlayerFlying flying, PacketEvent event) {

        if (getData().getPlayer().getWorld() == null) return false;

        FlyingLocation to = getData().getMovementProcessor().getTo();

        if (to == null) {
            return false;
        }

        this.lastSilentTicks++;

        // if not teleporting && world change hasn't happened recently.
        if (!getData().getActionProcessor().isTeleportingV2()
                && getData().getLastWorldChange().getDelta() > 20) {

            double deltaYAbs = getData().getMovementProcessor().getDeltaYAbs();
            double deltaXZ = getData().getMovementProcessor().getDeltaXZ();

            // Set invalid movement if they go this quickly.
            if (deltaXZ > .8 || deltaYAbs > 1) {
                this.invalidMovement.resetBoth();
            }

            boolean newCOn = getData().getMovementProcessor().getPositionTicks() <= 2;

            // check if threshold is greater than 40, or they recently joined.
            if (this.funny++ >= 40 || newCOn) {
                this.funny = 0;

                // Check the invalid movement delta is greater than 60
                if (this.invalidMovement.getServerDelta() > 60 || newCOn) {

                    HorizontalProcessor horizontalProcessor = getData().getHorizontalProcessor();

                    if (horizontalProcessor != null) {

                        // Get their current valid position
                        FlyingLocation currentValidPosition = horizontalProcessor.getCurrentValidPosition();
                        // Get current to location.
                        FlyingLocation currentToLocation = getData().getMovementProcessor().getToNull();

                        if (currentValidPosition != null) {
                            // Set the current valid position to a bukkit location
                            Location currentLocation = currentValidPosition.toLocation(getData().getPlayer().getWorld());

                            if (currentLocation != null) {
                                // Set the cached location to the closest ground location.
                                this.cachedLocation = getClosestGroundLocation(getData(), currentLocation);
                            }
                        } else if (currentToLocation != null) {

                            // Handle case where current valid position is null
                            Location currentLocation = currentToLocation.toLocation(getData().getPlayer().getWorld());

                            if (currentLocation != null) {
                                this.cachedLocation = getClosestGroundLocation(getData(), currentLocation);
                            }
                        }
                    }
                }
            }
        }

        // Shit server ground method.
        boolean serverGround = getData().getCollisionProcessor().getMovingUpFix() > 7
                && !getData().getMovementProcessor().isBouncedOnSlime() && !getData().getMovementProcessor().isBouncedOnBed()
                && (getData().getCollisionProcessor().isServerGround()
                || getData().getCollisionProcessor().isLastServerGround()) ? locationBasedGround()
                : (getData().getCollisionProcessor().isServerGround()
                || getData().getCollisionProcessor().isLastServerGround());

        // get server air ticks.
        if (serverGround) {
            this.serverAirTicks = 0;
        } else {
            if (this.serverAirTicks < 20) this.serverAirTicks++;
        }

        // get the client ground & ground state based on the position y.
        boolean clientGround = to.isOnGround();
        boolean positionGround = to.getPosY() % 0.015625 == 0;

        // if flying packet has changed position handle invalid chunk problem.
        if (flying.hasPositionChanged()) {
            this.handleNewInvalidChunk(getData());
        }

        // Ghost block support ignores block placements/cancels of placements, etc.
        if (Anticheat.INSTANCE.getConfigValues().isGhostBlockSupport()) {
            if (getData().getBlockProcessor()
                    .getLastConfirmedBlockPlaceTimer().getDelta() < 20
                    || getData().getLastBlockPlaceCancelTimer().getDelta() < 20
                    || getData().getLastBlockPlaceTimer().getDelta() < 20) {
                this.lagBackTicks = 0;
                this.blackPeroplPresent = 0;
                return false;
            }
        }

        // Exempt near boats, and under certain scenarios
        if (getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().getPlayer().getWorld() == null
                || getData().getMovementProcessor().getPositionTicks() < 4
                || getData().getBlockProcessor()
                .getLastConfirmedBlockPlaceTimer().getDelta() < 7
                || getData().getLastBlockPlaceCancelTimer().getDelta() < 7
                || getData().getLastEnderPearl().getDelta() < 3
                || getData().getLastTeleport().getDelta() < 5 && this.lastGhostBlockTimer.getDelta() > 5
                || getData().getLastWorldChange().hasNotPassed(20)
                || getData().generalCancel()) {
            this.lagBackTicks = 0;
            this.blackPeroplPresent = 0;
            return false;
        }

        // if last ghost block timer has gone past the delta set the thresold to 0.
        if (this.lastGhostBlockTimer.getDelta() > 120) {
            this.blackPeroplPresent = 0;
        }

        // get the last closest ground location
        if (this.lastClosestGround != null) {
            // if this has passed then set it to be null.
            if (this.lastGhostBlockTimer.passed(20)) {
                this.lastClosestGround = null;
            } else {

                // if the ghost block is currently happening
                if (this.lastGhostBlockTimer.getDelta() < 5) {
                    double currentX = Math.abs(getData().getMovementProcessor().getTo().getPosX());
                    double currentY = Math.abs(getData().getMovementProcessor().getTo().getPosY());
                    double currentZ = Math.abs(getData().getMovementProcessor().getTo().getPosZ());

                    double teleportX = Math.abs(this.lastClosestGround.getX());
                    double teleportY = Math.abs(this.lastClosestGround.getY());
                    double teleportZ = Math.abs(this.lastClosestGround.getZ());

                    double offsetX = Math.abs(currentX - teleportX);
                    double offsetY = Math.abs(currentY - teleportY);
                    double offsetZ = Math.abs(currentZ - teleportZ);

                    double distance = offsetX + offsetY + offsetZ;

                    // check the distance,
                    // if it's too much then we kick for silently trying to bypass by accepting the teleport.
                    if (offsetX > .2 || offsetY > 1.2 || offsetZ > .2 || distance > 2.8) {
                        //anti silent accept.
                        this.lastSilentTicks = 0;

                        if (this.blackPeroplPresent++ > (distance > 3.0 ? 18 : 30)) {
                            getData().kickPlayer("Silently accepting teleports to bypass");
                        }
                    }
                }
            }
        }

        // Get the rounded to position X Y and Z.
        int x = (int) Math.round(to.getPosX());
        int z = (int) Math.round(to.getPosY());
        int y = (int) Math.round(to.getPosZ());

        // Get the moduloY value.
        double moduloY = getData().getMovementProcessor().getTo().getPosY() % 0.015625;

        // Handle ghost water & ghost block above the players head.
        this.handlePrediction(moduloY);
        this.handleBlockAbovePrediction();

        // Handle fall damage prediction.
        if (Anticheat.INSTANCE.getConfigValues().isGhostFallDamage()) {

            // If client ground position, and their on the server ground.
            if (moduloY == 0 && serverGround) {
                // If fall damage, and did a teleport for the damage continue.
                if (this.setFallDamage && this.didTeleportForDamage) {
                    this.setFallDamage = false;
                    this.didTeleportForDamage = false;

                    // set the fall damage + extra.
                    this.fallDamage = (float) Math.abs((this.lastPositionYCached
                            - getData().getMovementProcessor().getTo().getPosY())) + 5.0F;

                    // If lands in certain blocks it doesn't do the fall damage.
                    if (getData().getCollisionProcessor().getLiquidTicks() < 1
                            && getData().getCollisionProcessor().getWebTicks() < 1
                            && getData().getCollisionWorldProcessor().getSlimeTicks() < 1) {
                        getData().getPlayer().setFallDistance(this.fallDamage);
                    }

                    // Reset fall damage after being done.
                    this.fallDamage = 0;
                }

                // if on the server ground for more than 10 ticks reset fall damage.
                if (this.getData().getCollisionWorldProcessor().getServerGroundTicks() > 10) {
                    this.fallDamage = 0;
                }
            }

            // if not server ground then set the fall damage for the player.
            if (!serverGround) {
                this.setFallDamage = true;
            }
        }


        // Check if the valid blocks that are put in are equal to the rounded x y and z to prevent
        // Players from falsely being teleported down at the start of matches (removed blocks below them)
        boolean validPosition = this.validBlocks.containsKey(x) && this.validBlocks.containsKey(y)
                && this.validBlocks.containsKey(z);

        // If their on the server ground & their position matches ground, and the client says they are on ground
        // We cache the valid blocks to stop any future falses from say cages on skywars.
        if (getData().getCollisionWorldProcessor().isGround() && positionGround && clientGround) {
            this.validBlocks.put(x, event.getTimestamp());
            this.validBlocks.put(y, event.getTimestamp());
            this.validBlocks.put(z, event.getTimestamp());

            // check below & above the player.
            for (boolean nigger : MagicValues.TRUE_FALSE) {
                this.validBlocks.put((nigger ? y + 1 : y - 1), event.getTimestamp());
            }
        }

        // if this happens do not continue.
        if (validPosition) {
            this.lagBackTicks = -1;
            return false;
        }

        // ^^^^ used to allow teleport flight to work. (Montior this exemption!!!!)


        // trigger ghost block
        // if not currently server ground, server air ticks are greater than 2, and not teleporting while client ground
        // then we continue and cache the last position and set the trigger tick time.
        if (!serverGround
                && !locationBasedGround() // check this incase.
                && !getData().getCollisionWorldProcessor().isGround()
                && this.serverAirTicks >= 2 // must be = to 2 or greater
                // (for false positive checking & to stop bypasses.)
                && clientGround && this.lagBackTicks < 1) {
            this.lastPositionYCached = getData().getMovementProcessor().getFrom().getPosY();
            this.lagBackTicks = 3;
        }


        // Certain half blocks, world change, climbable, ender-pearls, boats, recently ghost block teleported.
        if (getData().getCollisionWorldProcessor().getLillyPadTicks() > 0
                || getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                || getData().getLastEnderPearl().hasNotPassed(20)
                || getData().getMovementProcessor().getPositionTicks() < 4
                || getData().getPistonUpdateTimer().getDelta() < 10
                || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                || getData().getCollisionWorldProcessor().getCarpetTicks() > 0
                || getData().getCollisionWorldProcessor().isEnderPortal()
                || getData().getCollisionWorldProcessor().isNetherPortal()
                || getData().getLastTeleport().hasNotPassed(20)
                && this.getLastGhostBlockTimer().getDelta() > 20
                || getData().generalCancel()
                || getData().getLastWorldChange().getDelta() < 40 + getData().getTransactionProcessor().getPingTicks()
                || getData().getCollisionProcessor().getNearBoatTicks() > 0) {
            this.lagBackTicks = -1;
            return false;
        }

        return true;
    }

    public void processPost(boolean serverGround, long now) {
        // If the lag back system is set continue.
        if (this.lagBackTicks > 0) {

            // If the player recently exploded we exempt
            // as some players have false flagged the detection in weird circumstances
            // where they server ground but explode idk fml
            if (getData().getLastExplosionTimer().getDelta() < 20 && serverGround
                    || getData().getActionProcessor().getExplosionTimer().getDelta() < 20 && serverGround) {
                this.lagBackTicks = 0;
                return;
            }

            // if teleported by server or did a respawn or in a invalid chunk.
            if (getData().getLastTeleport().getDelta() < 20
                    || getData().getPistonUpdateTimer().getDelta() < 10
                    || getData().getCollisionWorldProcessor().getPistionTicks() > 0
                    || getData().getActionProcessor().getRespawnTimer().getDelta() < 10
                    || this.lastInvalidTick < 10) {
                this.lagBackTicks = 0;
                return;
            }

            // half blocks.
            if (getData().getCollisionWorldProcessor().isSnowHasIncompleteLayer()
                    || getData().getCollisionWorldProcessor().isBed()
                    || getData().getCollisionWorldProcessor().isHalfBlock()
                    || getData().getLastWorldChange().getDelta() < 40
                    || getData().getCollisionWorldProcessor().isCarpet()) {
                this.lagBackTicks = 0;
                return;
            }

            if (getData().getCollisionProcessor().getLiquidTicks() > 0
                    && !getData().getCollisionWorldProcessor().isLiquid()
                    || (getData().getCollisionWorldProcessor().getLiquidTicks() > 15
                    || getData().getCollisionProcessor().getLiquidFullyTicks() > 15)
                    && !getData().getCollisionWorldProcessor().isWater()
                    && !getData().getCollisionProcessor().isWaterFully()) {
                this.lagBackTicks = 0;
                return;
            }

            //Removed post server ground check as it was being abused.

            // Make the tick go down, and if the lag back tick is less than 2 continue
            if (this.lagBackTicks-- < 2) {

                // find the closest ground location, if we need to
                Location groundLocation = getProperLocation(getData().getPlayer().getWorld());

                // if the ground location can't be found or the last cached location is equal to null
                // Kick the player
                if (this.cachedLocation == null || groundLocation == null) {
                    if (++this.bypassTicks > 2) {
                        getData().kickPlayer("Invalid/null ground state found");
                    }
                    return;
                }

                // If the player recently teleported and their teleport location isn't null, but it wasn't a ghost block
                // Use the last server position location?
                if (getData().getLastTeleport().getDelta() < (40 + getData().getTransactionProcessor().getPingTicks())
                        && getData().getActionProcessor().getLastServerPositionLocation() != null
                        && this.getLastGhostBlockTimer().getDelta() > 40) {
                    groundLocation = getData().getActionProcessor()
                            .getLastServerPositionLocation().toLocation(getData().getPlayer().getWorld());
                }

                // if location is not currently set, make sure to set it
                if (this.lastClosestGround == null || groundLocation != this.lastClosestGround) {
                    this.lastClosestGround = groundLocation;
                }

                // ghost block support ignores canceled placements more often.
                if (getData().getLastBlockPlaceCancelTimer().hasNotPassed(
                        Anticheat.INSTANCE.getConfigValues().isGhostBlockSupport() ? 15 : 3)) {
                    this.verbose++;

                    if (this.verbose > 20) {

                        // set ghost fall damage.
                        if (Anticheat.INSTANCE.getConfigValues().isGhostFallDamage()) {
                            this.didTeleportForDamage = true;
                        }


                        // If set backs are not running, teleport to the closest ground location
                        // Else we set back the player instead.

                        // reset the timer
                        this.lastGhostBlockTimer.reset();

                        if (getData().getSetBackTicks() < 1
                                && getData().getSetBackProcessor().getSetBackTick() < 1) {
                            Location finalGroundLocation = groundLocation;
                            RunUtils.task(() -> getData().getPlayer().teleport(finalGroundLocation.clone().add(0, .42F, 0),
                                    PlayerTeleportEvent.TeleportCause.UNKNOWN));
                        } else {
                            this.getData().specialSetBack();
                            return;
                        }

                        World world = getData().getPlayer().getWorld();

                        if (world == null) {
                            return;
                        }

                        // block update to update blocks if we trigger ghost blocks.
                        if ((System.currentTimeMillis() - this.lastBlockUpdate) >= 1000L) {
                            this.lastBlockUpdate = System.currentTimeMillis();

                            Location location = getData().getMovementProcessor()
                                    .getTo().toLocation(world);

                            int radius = 3;

                            for (int xx = location.getBlockX() - radius; xx <= location.getBlockX() + radius; xx++) {
                                for (int yy = location.getBlockY() - radius; yy <= location.getBlockY() + radius; yy++) {
                                    for (int zz = location.getBlockZ() - radius; zz <= location.getBlockZ() + radius; zz++) {

                                        Anticheat.INSTANCE.getInstanceManager()
                                                .getInstance().sendBlockUpdate(
                                                        getData(), xx, yy, zz
                                                );
                                    }
                                }
                            }
                        }

                        // If the verbose is greater than 20 we count up the teleports.
                        if (this.verbose > 20) {
                            this.teleports++;
                        }
                    }
                } else {
                    // verbose down
                    this.verbose -= Math.min(this.verbose, 1);

                    // up teleports
                    this.teleports++;

                    // set ghost fall damage.
                    if (Anticheat.INSTANCE.getConfigValues().isGhostFallDamage()) {
                        this.didTeleportForDamage = true;
                    }

                    // reset ghost block timer.
                    this.lastGhostBlockTimer.reset();


                 //   getData().debug("failed ghost blocks.");

                    // teleport to the ground location
                    if (getData().getSetBackTicks() < 1
                            && getData().getSetBackProcessor().getSetBackTick() < 1) {
                        Location finalGroundLocation1 = groundLocation;
                        RunUtils.task(() -> getData().getPlayer().teleport(finalGroundLocation1.clone().add(0, .42F, 0),
                                PlayerTeleportEvent.TeleportCause.UNKNOWN));
                    } else {
                        // use set back instead.
                        this.getData().specialSetBack();
                        return;
                    }


                    World world = getData().getPlayer().getWorld();

                    if (world == null) {
                        return;
                    }

                    if ((System.currentTimeMillis() - this.lastBlockUpdate) >= 1000L) {
                        this.lastBlockUpdate = System.currentTimeMillis();

                        Location location = getData().getMovementProcessor()
                                .getTo().toLocation(world);

                        int radius = 3;

                        for (int xx = location.getBlockX() - radius; xx <= location.getBlockX() + radius; xx++) {
                            for (int yy = location.getBlockY() - radius; yy <= location.getBlockY() + radius; yy++) {
                                for (int zz = location.getBlockZ() - radius; zz <= location.getBlockZ() + radius; zz++) {

                                    Anticheat.INSTANCE.getInstanceManager()
                                            .getInstance().sendBlockUpdate(
                                                    getData(), xx, yy, zz
                                            );
                                }
                            }
                        }
                    }
                }

                // if teleported too many times we kick the player.
                if (this.teleports > 20) {
                    this.teleports = 0;

                    getData().kickPlayer("Too many ghost-block teleports.");
                }
            }
        }

        // lower tp ticks
        this.teleportingTicks -= this.teleportingTicks > 0 ? 1 : 0;
        this.lastFlying = now;
    }

    @Override
    public void onPacket(PacketEvent event) {

        if (event.isMovement() && event.getPacketReceiveEvent() != null) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            boolean serverGround = getData().getCollisionProcessor().getMovingUpFix() > 7
                    && !getData().getMovementProcessor().isBouncedOnSlime() && (getData().getCollisionProcessor().isServerGround()
                    || getData().getCollisionProcessor().isLastServerGround()) ? locationBasedGround()
                    : (getData().getCollisionProcessor().isServerGround()
                    || getData().getCollisionProcessor().isLastServerGround());

            // If pre has continued through, then we run post which flags ghost blocks.
            // Pre will check ghost blocks beforehand, while post mitigates for them after pre is confirmed.
            if (this.processPre(flying, event)) {
                this.processPost(serverGround, System.currentTimeMillis());
            }
        }
    }

    public boolean locationBasedGround() {
        double max = 0.5;

        if (Math.abs(getData().getMovementProcessor().getDeltaY()) > .005D) {
            max = 0.3;
        }

        boolean ground = false;

        Location location = getData().getMovementProcessor().getTo().toLocation(getData().getPlayer().getWorld());

        ground:
        {
            for (double x = -max; x <= max; x += max) {
                for (double z = -max; z <= max; z += max) {
                    for (double y = 0; y < .5; y += 0.1) {

                        Location currentLocation = location.clone().add(x, -y, z);
                        Material material = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(
                                getData().getPlayer().getWorld(),
                                currentLocation.getX(), currentLocation.getY(), currentLocation.getZ()
                        );

                        boolean isFenceGate = BlockUtil.isFenceGate(material);

                        if (isFenceGate) {
                            Block block = BlockUtil.getBlock(currentLocation);

                            if (block != null && (block.getData() & 0x4) == 0) {
                                isFenceGate = false;
                            }
                        }

                        if (material != Material.AIR
                                && material != REDSTONE_WIRE
                                && material != TRIPWIRE
                                && material != STRING
                                && isFenceGate
                                && material != TRIPWIRE_HOOK
                                && material != Material.LAVA
                                && material != Material.STATIONARY_LAVA
                                && material != Material.WATER
                                && material != Material.STATIONARY_WATER) {
                            ground = true;
                            break ground;
                        }
                    }
                }
            }
        }

        return ground;
    }


    public Location getProperLocation(World world) {

        Location location = getData().getMovementProcessor().getTo().toLocation(world);

        // If the player recently silent accepted teleport the player to the last closet ground.
        if (this.lastSilentTicks < 10 && this.lastClosestGround != null
                && this.lastClosestGround.getWorld() == location.getWorld()) {
            return this.lastClosestGround;
        }

        // ^ if this above fails, move to this check
        if (this.cachedLocation != null && this.invalidMovement.getDelta() < 60
                && location.getWorld() == this.cachedLocation.getWorld()) {
            return this.cachedLocation;
        }

        // If it recently found anything & the locations not null.
        // Move the player down to their next location.
        if (this.lastFoundAnything && this.lastTeleportedLocation != null
                && location.getWorld() == this.lastTeleportedLocation.getWorld()) {
            this.lastFoundAnything = false;
            return getClosestGroundLocation(getData(), this.lastTeleportedLocation);
        }

        // Set the players closest ground if nothing else works, which in theory will return the one above this
        // Causing the player to keep going down (usually good for when their over the void)
        return getClosestGroundLocation(getData(), location);
    }

    private void handleBlockAbovePrediction() {

        if (this.getData().getMovementProcessor().getTick() < 60) return;

        if (!getData().getCollisionWorldProcessor().isBlockAbove()) {

            double deltaY = getData().getMovementProcessor().getDeltaY();

            boolean clientGround = getData().getMovementProcessor().getTo().isOnGround();

            boolean invalid = deltaY == BLOCK_ABOVE_MOTIONS[0]
                    || deltaY == BLOCK_ABOVE_MOTIONS[1]
                    || deltaY == BLOCK_ABOVE_MOTIONS[2];

            if (!clientGround && deltaY >= 0) {

                if (invalid) {

                    boolean movingUp = getData().getCollisionProcessor().getMovingTicks() > 0;

                    // Exempts
                    if (getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                            || getData().getCollisionProcessor().getLiquidTicks() > 0
                            || getData().getCollisionWorldProcessor().getStairTicks() > 0 && movingUp
                            || getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0 && movingUp
                            || getData().generalCancel()
                            || getData().getMovementProcessor().getTick() < 10
                            || getData().getVelocityProcessor().getServerVelocityTicks() < 20
                            || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(20)) {
                        getData().getMovementProcessor().setLastGhostBlockAboveTick(0);
                        return;
                    }

                    World world = getData().getPlayer().getWorld();

                    if (world == null) {
                        return;
                    }

                    Location location = getData().getMovementProcessor()
                            .getTo().toLocation(world);

                    int radius = 3;

                    for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
                        for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                            for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {

                                if (getData().getBlockProcessor().getLastCombatWallTicks() > 20) {
                                    Anticheat.INSTANCE.getInstanceManager()
                                            .getInstance().sendBlockUpdate(
                                                    getData(), x, y, z
                                            );
                                }

                                getData().getMovementProcessor().setLastGhostBlockAboveTick(20);
                            }
                        }
                    }
                }
            }

            if (getData()
                    .getMovementProcessor().getLastGhostBlockAboveTick() > 0) {
                getData()
                        .getMovementProcessor().setLastGhostBlockAboveTick(getData()
                                .getMovementProcessor().getLastGhostBlockAboveTick() - 1);
            }
        }
    }

    private void handlePrediction(double moduloY) {
        double deltaY = getData().getMovementProcessor().getDeltaY();

        // Wait
        if (this.getData().getMovementProcessor().getTick() < 60) return;

        // Reset on server ground
        if (this.getData().getCollisionProcessor().isServerGround() && moduloY == 0) {
            this.liquidThreshold = 0;
            this.liquidTotal = 0;
        }

        //Prediction calculations
        double predictedDist = (this.lastPredictionY - 0.08D) * 0.9800000190734863D;

        //Check if the prediction is rounded, this is done in the client but we need to make it here as well
        if (Math.abs(predictedDist) <= 0.005D) {
            predictedDist = 0;
        }

        //Delta between the last 2 ticks of the prediction in the air
        double prediction = Math.abs(deltaY - predictedDist);

        boolean clientGround = getData().getMovementProcessor().getTo().isOnGround();
        boolean lastClientGround = getData().getMovementProcessor().getFrom().isOnGround();

        //Reset when on client ground, if they spoof their loss
        if (clientGround || lastClientGround) {
            this.liquidGhostChecks = 0;
        }

        //Check 2 ticks client ground
        if (!clientGround && !lastClientGround) {

            //Check if the prediction is over a specific amount
            if (prediction > 1E-12) {

                //Buffer to be safe
                if (this.liquidThreshold++ > 4) {
                    this.liquidThreshold = 0;

                    boolean movingUp = getData().getCollisionProcessor().getMovingTicks() > 0
                            && getData().getMovementProcessor().getDeltaY() < 0.05;

                    // Exempts
                    if (getData().getCollisionWorldProcessor().getClimbableTicks() > 0
                            || getData().getCollisionWorldProcessor().getLiquidTicks() > 0
                            || getData().getCollisionWorldProcessor().getStairTicks() > 0 && movingUp
                            || getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0 && movingUp
                            || getData().generalCancel()
                            || (getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() < 10
                            || getData().getLastBlockPlaceTimer().getDelta() < 10)
                            && getData().getMovementProcessor().getDeltaXZ() < .2
                            || getData().getMovementProcessor().getTick() < 100
                            || getData().getVelocityProcessor().getVelocityTicksConfirmed() < 20
                            || getData().getVelocityProcessor().getServerVelocityTicks() < 20
                            || getData().getActionProcessor().getLastVehicleTimer().hasNotPassed(20)) {
                        this.liquidThreshold = 0;
                        this.liquidTotal = 0;
                        return;
                    }


                    double absDelta = Math.abs(deltaY);

                    if ((deltaY > 0 && deltaY < .115) || (deltaY < 0 && absDelta > 0.005 && absDelta < .115)
                            || (deltaY > .07 && deltaY < .2)) {

                        if (this.liquidTotal++ > 5) return;

                        World world = getData().getPlayer().getWorld();

                        if (world == null) {
                            return;
                        }

                        Location location = getData().getMovementProcessor()
                                .getTo().toLocation(world);

                        if ((System.currentTimeMillis() - this.lastBlockUpdate) >= 1000L) {
                            this.lastBlockUpdate = System.currentTimeMillis();

                            int radius = 2;

                            for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
                                for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                                    for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {


                                        if (getData().getBlockProcessor().getLastCombatWallTicks() > 20) {
                                            Anticheat.INSTANCE.getInstanceManager()
                                                    .getInstance().sendBlockUpdate(
                                                            getData(), x, y, z
                                                    );
                                        }

                                        this.liquidBypass = 20;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                this.liquidThreshold -= this.liquidThreshold > 0 ? .50 : 0;
                this.liquidBypass -= this.liquidBypass > 0 ? 1 : 0;
            }
        } else {
            this.liquidThreshold = 0;
            this.liquidBypass = 0;
        }

        this.lastPredictionY = deltaY;
    }

    private void handleNewInvalidChunk(PlayerData data) {
        if (data != null) {

            this.lastInvalidTick++;

            if (data.getPlayer().getWorld() == null) return;

            if (getData().getCollisionWorldProcessor().isLiquid()
                    || getData().getCollisionProcessor().isWaterFully()
                    || getData().getCollisionProcessor().isWater()
                    || getData().getCollisionProcessor().isLava()
                    || getData().getCollisionProcessor().getLiquidTicks() > 0
                    || getData().getCollisionWorldProcessor().getLiquidTicks() > 0
                    || getData().getLastExplosionTimer().isSet() && getData().getLastExplosionTimer()
                    .getDelta() < 20 + getData().getTransactionProcessor().getPingTicks()
                    || getData().getActionProcessor().getExplosionTimer().getDelta() < 20
                    + getData().getTransactionProcessor().getPingTicks()
                    && getData().getActionProcessor().getExplosionTimer().isSet()
                    || getData().getCollisionProcessor().isLavaFully()
                    || getData().getLastTeleport().getDelta() < 2
                    || getData().getCollisionProcessor().getLiquidTicks() > 0
                    || getData().getVelocityProcessor().getVelocityATicks() < 10
                    && getData().getVelocityProcessor().getVelocityDataPre() != null
                    && getData().getVelocityProcessor().getVelocityDataPre().getY() > 0.7
                    || getData().getCollisionWorldProcessor().isSlime()
                    || getData().getCollisionWorldProcessor().getSlimeTicks() > 0
                    || getData().getActionProcessor().getRespawnTimer().getDelta() < 10
                    + getData().getTransactionProcessor().getPingTicks()
                    || getData().getCollisionProcessor().isWeb()
                    || getData().getCollisionProcessor().getWebTicks() > 0
                    || getData().getCollisionProcessor().isWebFullCheck()
                    || getData().getCollisionWorldProcessor().isWebFullCheck()
                    || getData().getCollisionProcessor().isWebInside()) {
                this.invalidChunkThreshold -= Math.min(this.invalidChunkThreshold, .005);
                return;
            }


            double deltaY = data.getMovementProcessor().getDeltaY();

            double motion = (-.1D * 0.9800000190734863D);

            //todo: make not abuse.
            final boolean invalid = Math.abs(deltaY - motion) < 1E-4
                    || !getData().getCollisionProcessor().isChunkLoaded();


            if (invalid && getData().getActionProcessor().getLastServerPositionLocation() != null) {
                this.lastInvalidTick = 0;

                if (++this.invalidChunkThreshold > 2.5) {

                   // getData().debug("failed invalid chunk motion movements.");

                    RunUtils.taskLater(() ->
                                    getData().getPlayer().teleport(
                                            getData().getActionProcessor().getLastServerPositionLocation().toLocation
                                                    (getData().getPlayer().getWorld()),
                                            PlayerTeleportEvent.TeleportCause.UNKNOWN),
                            1L);
                }
            } else {
                this.invalidChunkThreshold -= Math.min(this.invalidChunkThreshold, .025);
            }
        }
    }
    public Location getClosestGroundLocation(PlayerData user, Location customLocation) {
        Location fixedLocation = customLocation.clone();

        World world = user.getPlayer().getWorld();
        boolean foundAnything = false;

        Material materialBelow = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(
                world, customLocation.getX(), customLocation.getY() - 1, customLocation.getZ()
        );

        Material materialBelowHigh = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(
                world, customLocation.getX(), customLocation.getY() - 0.5, customLocation.getZ()
        );

        Material materialBelowLow = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(
                world, customLocation.getX(), customLocation.getY() - 2, customLocation.getZ()
        );

        Material materialCurrent = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(
                world, customLocation.getX(), customLocation.getY(), customLocation.getZ()
        );

        boolean isBlinked = getData().getSetBackTicks() > 0;

        if (materialCurrent != Material.AIR || materialBelow != AIR
                || materialBelowHigh != AIR || materialBelowLow != AIR) {
            foundAnything = true;
        }

        if (!foundAnything) {
            fixedLocation.setY(customLocation.getY() - (!isBlinked ? 1F
                    : (MagicValues.VERTICAL_SUBTRACTED * MagicValues.VERTICAL_MULTIPLIER)));
        }

        this.lastFoundAnything = true;

        Location fixed = fixedLocation.clone();

        if (fixed.getX() == 0 && fixed.getZ() == 0
                && getData().getHorizontalProcessor().getTeleportLocation() != null) {
            // Should fix ghost block shit?
            fixed.setX(getData().getHorizontalProcessor().getTeleportLocation().getPosX());
            fixed.setZ(getData().getHorizontalProcessor().getTeleportLocation().getPosZ());
        }

        this.lastTeleportedLocation = fixed;
        return fixedLocation.clone();
    }



    private static final double[] BLOCK_ABOVE_MOTIONS = new double[]{
            //Solid Block
            .20000004768371582D,
            //Half Block
            .28000006079673767D,
            //3 Blocks Up
            .033890786745502055D
    };
}
