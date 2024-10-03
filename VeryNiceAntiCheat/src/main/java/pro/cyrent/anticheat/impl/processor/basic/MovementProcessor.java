package pro.cyrent.anticheat.impl.processor.basic;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.impl.combat.entity.EntityA;
import pro.cyrent.anticheat.api.check.impl.combat.entity.utils.EntityData;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.block.collide.CollisionUtil;
import pro.cyrent.anticheat.util.event.EventTimer;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSlotStateChange;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class MovementProcessor extends Event {
    private final PlayerData data;

    private final FlyingLocation to = new FlyingLocation();
    private final FlyingLocation from = new FlyingLocation();
    private final FlyingLocation fromFrom = new FlyingLocation();

    private FlyingLocation toNull = null;
    private float cachedWalkSpeed = 0.2F;

    private int lastGhostBlockAboveTick;
    private boolean lastHeldItemExempt = false;
    private int ticksSincePosition, lastHeldInvalidItemTick;
    private final EventTimer blockJumpTimer;
    private int viaMCPThreshold;
    private boolean clientWallFix = false;
    private boolean bouncedOnSlime, bouncedOnBed;
    private WrapperPlayClientPlayerFlying flyingPacket, lastFlyingPacket, lastLastFlyingPacket;
    private int tick;
    private int positionTicks;
    private int lastSwitched, lastHeldSlot;
    private int airTicks, groundTicks;
    private double deltaX, deltaY, deltaZ, deltaXZ;
    private boolean hadPosition, lastHadPosition, positionGround;
    private double lastlastLastDeltaY, lastLastDeltaY, lastDeltaY, lastDeltaX, lastDeltaZ,
            lastDeltaXZ, deltaXAbs, deltaZAbs, deltaYAbs;
    private int rotatingTicks;
    private float lastDeltaYaw, lastDeltaPitch, lastDeltaYawAbs, lastDeltaPitchAbs, deltaYaw,
            deltaPitch, deltaYawAbs, deltaPitchAbs, yawAccel, pitchAccel, yawDeltaClamped;
    private long lastFlying;
    private final EventTimer clientWallCollision;
    private final EventTimer lastPunchBowInHandTimer;
    private long lastUpdateBorder = System.currentTimeMillis();
    private double minBorderX, maxBorderX, minBorderZ, maxBorderZ;
    private int lastNearBorderUpdate = 100;
    private double comboThreshold;
    private int lastDamageTick, comboModeTicks;
    private int skippedPackets;
    private boolean skipNextCollision = false;
    private final FlyingLocation lastSlimeLocation = new FlyingLocation();
    private final FlyingLocation lastBedLocation = new FlyingLocation();
    private final EventTimer lastFlyingPauseTimer, lastFlightTimer;
    private float currentYawClamped;
    private double blockJumpAcelleration, lastBlockY = -1337;

    public MovementProcessor(PlayerData user) {
        this.data = user;
        this.lastFlyingPauseTimer = new EventTimer(20, user);
        this.lastFlightTimer = new EventTimer(20, user);
        this.clientWallCollision = new EventTimer(20, user);
        this.lastPunchBowInHandTimer = new EventTimer(20, user);
        this.blockJumpTimer = new EventTimer(20, user);
    }

    private void handleLevelChange(WrapperPlayClientPlayerFlying wrapped) {
        if (!wrapped.hasPositionChanged()) return;

        if (this.blockJumpTimer.passed()) {
            this.blockJumpAcelleration = 0;
        }

        if (this.to.isOnGround() && wrapped.getLocation().getY() % 0.015625 == 0) {

            this.lastBlockY = (int) wrapped.getLocation().getY();
        } else {
            double delta = wrapped.getLocation().getY() - this.lastBlockY;

            if (delta == 1.0) {
                this.blockJumpTimer.reset();

                this.blockJumpAcelleration += this.blockJumpAcelleration < .225 ? .06 : 0;
            }
        }
    }

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getType() == PacketType.Play.Client.WINDOW_CONFIRMATION
                || event.getType() == PacketType.Play.Client.PONG) {
            this.updateCacheStates();
        }

        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            this.updateCacheStates();

            this.cachedWalkSpeed = getData().getPlayer().getWalkSpeed();

            this.lastLastFlyingPacket = this.lastFlyingPacket;
            this.lastFlyingPacket = this.flyingPacket;
            this.flyingPacket = flying;

            this.lastSwitched++;
            this.tick++;

            if (flying.hasRotationChanged()) {
                this.currentYawClamped = MathUtil.wrapAngleTo180_float(flying.getLocation().getYaw());
            }

            if (flying.hasPositionChanged()) {
                this.ticksSincePosition = 0;
            } else {
                this.ticksSincePosition++;
            }

            EntityA entityA = (EntityA) getData().getCheckManager().forClass(EntityA.class);

            if (entityA != null && !entityA.isEnabled()
                    && Anticheat.INSTANCE.getEntityManager().getEntityMap().size() > 0) {
                Anticheat.INSTANCE.getEntityManager().remove(getData(), getData().getForcedUser(),
                        EntityData.EntityType.ADVANCED);
            }

            if (getData().getPlayer().getNoDamageTicks() < 1 && this.lastDamageTick < 1
                    && (getData().getVelocityProcessor().getServerVelocityTicks() <= 10
                    || getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 10)) {
                if (++this.comboThreshold > 3) {
                    comboModeTicks = 20;
                }
            } else {
                this.comboThreshold -= Math.min(this.comboThreshold, 0.25);
                this.comboModeTicks -= Math.min(this.comboModeTicks, 1);
            }

            this.lastDamageTick = getData().getPlayer().getNoDamageTicks();

            if (flying.hasPositionChanged()) {
                this.positionTicks++;
            }

            long now = System.currentTimeMillis();

            //Flying packet lag
            long flyingDifference = Math.abs(now - this.lastFlying);

            if (flyingDifference < 10L || flyingDifference > 1000L) {
                this.lastFlyingPauseTimer.reset();
            }

            if (flyingDifference < 10L) {
                if (this.skippedPackets < 20) this.skippedPackets++;
            } else {
                if (this.skippedPackets > 0) this.skippedPackets--;
            }

            boolean ground = flying.isOnGround();

            double x = flying.getLocation().getX();
            double y = flying.getLocation().getY();
            double z = flying.getLocation().getZ();

            float pitch = flying.getLocation().getPitch();
            float yaw = flying.getLocation().getYaw();

            this.positionGround = y >= 0.0 && y % 0.015625 == 0.0;

            this.fromFrom.setWorld(this.from.getWorld());
            this.from.setWorld(to.getWorld());
            this.to.setWorld(getData().getPlayer().getWorld().getName());

            this.fromFrom.setOnGround(this.from.isOnGround());
            this.from.setOnGround(this.to.isOnGround());
            this.to.setOnGround(ground);

            this.fromFrom.setTick(this.from.getTick());
            this.from.setTick(this.to.getTick());
            this.to.setTick(this.tick);

            this.lastHadPosition = this.hadPosition;
            this.hadPosition = flying.hasPositionChanged();


            if (flying.hasPositionChanged()) {

                this.fromFrom.setPosX(this.from.getPosX());
                this.fromFrom.setPosY(this.from.getPosY());
                this.fromFrom.setPosZ(this.from.getPosZ());

                this.from.setPosX(this.to.getPosX());
                this.from.setPosY(this.to.getPosY());
                this.from.setPosZ(this.to.getPosZ());

                this.to.setPosX(x);
                this.to.setPosY(y);
                this.to.setPosZ(z);

                if (this.to.getPosY() != 0 && this.to.getPosZ() != 0 && this.to.getPosX() != 0) {
                    this.toNull = this.to;
                }
            }

            this.lastLastDeltaY = this.lastDeltaY;

            this.lastDeltaX = this.deltaX;
            this.lastDeltaY = this.deltaY;
            this.lastDeltaZ = this.deltaZ;

            this.deltaY = this.to.getPosY() - this.from.getPosY();
            this.deltaX = this.to.getPosX() - this.from.getPosX();
            this.deltaZ = this.to.getPosZ() - this.from.getPosZ();

            this.deltaXAbs = Math.abs(this.deltaX);
            this.deltaZAbs = Math.abs(this.deltaZ);
            this.deltaYAbs = Math.abs(this.deltaY);

            this.lastDeltaXZ = this.deltaXZ;

            this.deltaXZ = MathUtil.hypot(this.deltaXAbs, this.deltaZAbs);

            if (flying.hasRotationChanged()) {
                this.rotatingTicks++;
                this.fromFrom.setYaw(this.from.getYaw());
                this.fromFrom.setPitch(this.from.getPitch());

                this.from.setYaw(this.to.getYaw());
                this.from.setPitch(this.to.getPitch());

                this.to.setPitch(pitch);
                this.to.setYaw(yaw);

                this.lastDeltaYaw = this.deltaYaw;
                this.lastDeltaPitch = this.deltaPitch;

                this.deltaYaw = this.to.getYaw() - this.from.getYaw();
                this.deltaPitch = this.to.getPitch() - this.from.getPitch();

                this.lastDeltaYawAbs = this.deltaYawAbs;
                this.lastDeltaPitchAbs = this.deltaPitchAbs;

                this.deltaYawAbs = Math.abs(this.to.getYaw() - this.from.getYaw());
                this.deltaPitchAbs = Math.abs(this.to.getPitch() - this.from.getPitch());

                this.yawAccel = Math.abs(this.deltaYawAbs - this.lastDeltaYawAbs);
                this.pitchAccel = Math.abs(this.deltaPitchAbs - this.lastDeltaPitchAbs);
            }

            this.yawDeltaClamped = Math.abs(MathUtil.wrapAngleTo180_float(this.deltaYaw));

            //air, ground, ticks
            if (ground) {
                this.airTicks = 0;
                this.groundTicks += this.groundTicks < 20 ? 1 : 0;
            } else {
                this.groundTicks = 0;
                this.airTicks += this.airTicks < 20 ? 1 : 0;
            }

            //last flight timer
            if (getData().getPlayer().getAllowFlight()
                    || getData().getPlayer().getGameMode() == GameMode.SPECTATOR
                    || getData().getPlayer().getGameMode() == GameMode.CREATIVE) {
                this.lastFlightTimer.reset();
            }

            ItemStack itemInHand = getData().getPlayer().getItemInHand();

            if (itemInHand != null && itemInHand.getType() == Material.BOW) {
                if (itemInHand.containsEnchantment(Enchantment.ARROW_KNOCKBACK)) {
                    getData().setPunchPower(itemInHand.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK));
                    this.lastPunchBowInHandTimer.reset();
                }
            }

            if (getData().getCollisionWorldProcessor().isSlime()) {
                this.lastSlimeLocation.setFlyingLocation(this.to.clone());
                this.bouncedOnSlime = true;
            }

            if (getData().getCollisionWorldProcessor().isBed()
                    && Anticheat.INSTANCE.getVersionSupport().getClientProtocol(getData()) >= 335) {
                this.lastBedLocation.setFlyingLocation(this.to.clone());
                this.bouncedOnBed = true;
            }


            if (getData().getDebugSet().equalsIgnoreCase("movement")
                    && getData().isDebugMode()
                    && getData().getDebuggedUser() != null) {

                MovementProcessor p = getData().getDebuggedUser().getMovementProcessor();

                if (p.flyingPacket == null) return;

                getData().debug("deltaXZ=" + p.getDeltaXZ()
                        + ", deltaY=" + p.getDeltaY()
                        + ", tick=" + p.getTick()
                        + ", ground=" + p.getTo().isOnGround()
                        + ", lastGround=" + p.getFrom().isOnGround()
                        + ", chunk-load=" + getData().getDebuggedUser()
                        .getCollisionProcessor().isChunkLoaded()
                        + ", teleporting=" + getData().getDebuggedUser()
                        .getActionProcessor().isTeleportingV2()
                        + ", teleporting-type-2=" + getData().getDebuggedUser()
                        .getActionProcessor().isTeleportingReal()
                        + ", position=" + p.flyingPacket.hasPositionChanged()
                        + ", rotating=" + p.flyingPacket.hasRotationChanged()
                        + ", protocol=" + getData().getDebuggedUser().protocolVersion
                        + ", (ticks <= 0)=" + (p.getTick() <= 0));
            }

            if (getData().getDebugSet().equalsIgnoreCase("reach")
                    && getData().isDebugMode()
                    && getData().getDebuggedUser() != null) {

                if (getData().getDebuggedUser().isReach()) {

                    getData().debug("distance=" + getData().getDebuggedUser().getReachDistance()
                            + ", hitbox=" + getData().getDebuggedUser().isValidHitbox()
                            + ", attacking=" + getData().getDebuggedUser().getReachFighting());

                    getData().getDebuggedUser().setReach(false);
                }
            }

            if (this.bouncedOnSlime) {
                if (this.to.isOnGround() && this.from.isOnGround()
                        && getData().getCollisionWorldProcessor().getSlimeTicks() < 1) {
                    this.bouncedOnSlime = false;
                }

                double distXZ = this.lastSlimeLocation.distanceSquaredXZ(this.to);
                double distY = this.lastSlimeLocation.deltaYAbs(this.to);

                if (distXZ > 70) {
                    this.bouncedOnSlime = false;
                }

                if (distY > 52.0) {
                    this.bouncedOnSlime = false;
                }
            }

            if (this.bouncedOnBed) {
                if (this.to.isOnGround() && this.from.isOnGround() && this.fromFrom.isOnGround()
                        && !getData().getCollisionWorldProcessor().isBed()) {
                    this.bouncedOnBed = false;
                }

                double distXZ = this.lastBedLocation.distanceSquaredXZ(this.to);
                double distY = this.lastBedLocation.deltaYAbs(this.to);

                if (distXZ > 70) {
                    this.bouncedOnBed = false;
                }

                if (distY > 52.0) {
                    this.bouncedOnBed = false;
                }
            }

            if (getData().getPlayer().getItemInHand() != null && (getData().getPlayer().getItemInHand().getType() == Material.LADDER
                    || getData().getPlayer().getItemInHand().getType() == Material.VINE
                    || getData().getPlayer().getItemInHand().getType() == Material.WEB
                    || getData().getPlayer().getItemInHand().getType() == Material.ICE
                    || getData().getPlayer().getItemInHand().getType() == Material.PACKED_ICE
                    || getData().getPlayer().getItemInHand().getType() == Material.WATER
                    || getData().getPlayer().getItemInHand().getType() == Material.WATER_BUCKET
                    || getData().getPlayer().getItemInHand().getType() == Material.LAVA
                    || getData().getPlayer().getItemInHand().getType() == Material.LAVA_BUCKET
                    || getData().getPlayer().getItemInHand().getType() == Material.SLIME_BLOCK)) {
                this.lastHeldInvalidItemTick = 20;
            } else {
                this.lastHeldInvalidItemTick -= Math.min(this.lastHeldInvalidItemTick, 1);
            }

            this.lastHeldItemExempt = (getData().getLastBlockPlaceCancelTimer().getDelta() < 20
                    || getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() < 7)
                    && this.lastHeldInvalidItemTick > 0;

            //client wall collisions
            this.processWallCollisions();
            this.handleLevelChange(flying);

            this.lastFlying = now;
        }

        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.SLOT_STATE_CHANGE) {
                WrapperPlayClientSlotStateChange slotStateChange =
                        new WrapperPlayClientSlotStateChange(event.getPacketReceiveEvent());

                if (slotStateChange.getSlot() != this.lastHeldSlot) {
                    this.lastSwitched = 0;
                }

                this.lastHeldSlot = slotStateChange.getSlot();
            }
        }
    }

    private void updateCacheStates() {
        this.updateBorderControl();
    }


    private void updateBorderControl() {

        long now = System.currentTimeMillis();

        // Update border & check it only every 1 second to reduce performance.
        if ((now - this.lastUpdateBorder) >= 1000L) {

            World world = getData().getPlayer().getWorld();

            if (world == null) return;

            WorldBorder worldBorder = world.getWorldBorder();

            if (worldBorder == null) return;

            Location borderCenter = worldBorder.getCenter();

            if (borderCenter == null) return;

            double borderSize = worldBorder.getSize() / 2;

            this.minBorderX = borderCenter.getX() - borderSize;
            this.maxBorderX = borderCenter.getX() + borderSize;
            this.minBorderZ = borderCenter.getZ() - borderSize;
            this.maxBorderZ = borderCenter.getZ() + borderSize;

            this.lastUpdateBorder = now;
        }

        FlyingLocation playerLocation = getData().getMovementProcessor().getTo();

        double currentX = playerLocation.getPosX();
        double currentZ = playerLocation.getPosZ();

        double distance = 1.0;

        boolean validMinX = (currentX >= this.minBorderX - distance
                && currentX <= this.minBorderX + distance);
        boolean validMaxX = (currentX >= this.maxBorderX - distance
                && currentX <= this.maxBorderX + distance);

        boolean validMinZ = (currentZ >= this.minBorderZ - distance
                && currentZ <= this.minBorderZ + distance);
        boolean validMaxZ = (currentZ >= this.maxBorderZ - distance
                && currentZ <= this.maxBorderZ + distance);

        if (validMinX || validMaxX || validMinZ || validMaxZ) {
            this.lastNearBorderUpdate = 0;
        }
    }

    public void processWallCollisions() {
        if (getData().getCollisionProcessor().isChunkLoaded()) {
            if (getData().getCollisionWorldProcessor().getCollidingHorizontallyTimer().getDelta() > 20) {

                if (CollisionUtil.isNearWall(this.to) || CollisionUtil.isNearWall(this.from)) {

                    if (getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() < 7
                            || getData().getLastBlockPlaceTimer().getDelta() < 7) {
                        this.skipNextCollision = true;
                        return;
                    }

                    if (getData().getVelocityProcessor().getVelocityTicksConfirmed() > 20) {
                        if (this.to.getPosZ() == this.from.getPosZ()
                                || this.to.getPosX() == this.from.getPosX()) {
                            this.skipNextCollision = true;
                            return;
                        }
                    }

                    if (this.skipNextCollision) {
                        this.skipNextCollision = false;
                        return;
                    }

                    this.clientWallCollision.reset();

                    World world = getData().getPlayer().getWorld();

                    if (world == null) {
                        return;
                    }

                    Location location = getData().getMovementProcessor()
                            .getTo().toLocation(world);

                    if (location == null) return;

                    int radius = 2;

                    for (int xx = location.getBlockX() - radius; xx <= location.getBlockX() + radius; xx++) {
                        for (int yy = location.getBlockY() - radius; yy <= location.getBlockY() + radius; yy++) {
                            for (int zz = location.getBlockZ() - radius; zz <= location.getBlockZ() + radius; zz++) {

                                Anticheat.INSTANCE.getInstanceManager().getInstance().sendBlockUpdate(getData(), xx, yy, zz);
                            }
                        }
                    }
                }
            }
        }
    }
}