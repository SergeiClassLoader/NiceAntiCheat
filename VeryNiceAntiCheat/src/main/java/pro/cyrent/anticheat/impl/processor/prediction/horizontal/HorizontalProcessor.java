package pro.cyrent.anticheat.impl.processor.prediction.horizontal;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.Check;
import pro.cyrent.anticheat.api.check.data.PredictionData;
import pro.cyrent.anticheat.api.check.data.VelocityCheckData;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.impl.processor.prediction.simulator.VelocitySimulator;
import pro.cyrent.anticheat.util.block.collide.CollisionUtil;
import pro.cyrent.anticheat.util.event.EventTimer;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.magic.MagicValues;
import pro.cyrent.anticheat.util.math.*;
import pro.cyrent.anticheat.util.minecraftmaths.MinecraftMath;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.World;
import pro.cyrent.anticheat.util.task.TransactionPacketAction;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class HorizontalProcessor extends Event {
    private final PlayerData data;

    private boolean storedOnce = false;
    private final EventTimer lagTeleportTimer, lastAttackTimer, lastSprintUpdateTimer, lastSetBackTimer;
    private int validPosTicks;
    private double lastServerMovementSpeed;
    private int blindTicks = 0;
    private double lagBackThreshold, bypassFix;
    private Motion lastVelocityMotion;
    private double movementStrafeFixThreshold;
    private int notPossible003, teleportTicks;
    private boolean forceTeleport, rotationTeleport;
    private int validTicks;
    private float lastMoveForward, lastMoveStrafe, yaw, fromYaw, pitch;
    private double lastBaseSpeed;
    private float lastBlockFriction;
    private int ticksToIgnore;
    private BruteForcedData bruteForcedData = null;
    private double currentX;
    private double currentZ;
    private double lastX;
    private double lastZ;
    public double deltaX;
    public double deltaZ;
    public double lastDeltaX;
    public double lastDeltaZ;
    private double lastDeltaY, lastLastDeltaY;
    private int clientCollideTicks;
    private int ticksSinceStopSprint, ticksSinceStartSprint, spammedSprintTicks;
    private boolean useSprintBruteForce, lastTickWasSprinting;
    private double predictedX, predictedZ;
    private double liquidBounceDetector;
    public boolean velocity;
    public double velocityX, velocityY, velocityZ;
    private boolean hitValidEntity;
    private boolean isLastHitPlayer;
    private boolean hadVelocityTick;
    private boolean onGround, lastGround;
    private int lastSplitTime;
    private int acceptTicks;
    private double liquidBounceBreakTicks;
    private final VelocitySimulator velocitySimulator;
    private FlyingLocation currentValidPosition;
    private final List<Boolean> pastFlyings = new CopyOnWriteArrayList<>();
    private final Deque<TransactionVelocityEntry> velocityEntries = new ArrayDeque<>();
    private int velocityTicks;
    public boolean confirmed;
    private int buffer;
    private int lastFastMath;
    private boolean usingFastMath = false, wasUsingFastMath = false;
    private boolean cachedUsingFastMath;
    private int possible003Exploits;
    private boolean position, lastPosition, lastLastPosition,
            lastLastLastPosition, lastLastLastLastPosition, lastLastLastLastLastPosition;
    private boolean zeroThree;
    private int delayedFlyingTicks;
    private List<TransactionVelocityEntry> entries = new ArrayList<>();
    private Runnable toConfirm;
    private double invalidGroundBuffer;
    private double partialTicks;
    private double possibleFix;
    private int legitTicks;
    private double deltaY, currentY, lastY;
    private double offset;
    private double newVelocityY = -1;
    private double lastInvalidStrafeOffset;
    private long scale = -1L;
    private int ticksNoOffset;
    private int lastGroundTicks;
    private int zeroThreeTicks, angleMovement;
    private Keys key = Keys.NOTHING, lastKey = Keys.NOTHING, keySilent = Keys.NOTHING;
    private FlyingLocation wallLocation = null;

    @Setter
    private int inWorldTicks;

    @Setter
    private int sprintActions = 0;

    @Setter
    private boolean ignoreTeleportSaving;

    @Setter
    private boolean teleportWithoutSettingTimers;

    private int lastEdgeFix;
    private boolean hadSpeed = false, hadSlow = false;

    @Setter
    private int serverTicksSinceBadTeleport;

    @Setter
    private boolean sprinting, wasSprinting;

    private boolean hasDesyncedSpeed = false, hasDesyncedSlow = false;
    private int desyncResetTime = 100;

    @Setter
    public FlyingLocation lastValidLocation, teleportLocation;

    public HorizontalProcessor(PlayerData user) {
        this.data = user;
        this.lagTeleportTimer = new EventTimer(20, user);
        this.lastAttackTimer = new EventTimer(20, user);
        this.lastSprintUpdateTimer = new EventTimer(20, user);
        this.velocitySimulator = new VelocitySimulator(user);
        this.lastSetBackTimer = new EventTimer(20, user);
    }

    public void onPost(PacketEvent event) {
        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (flying.hasPositionChanged()) {
                this.delayedFlyingTicks = 0;
            }

            this.zeroThree = false;
        }
    }

    public void reset() {
        this.validTicks = 30;
        this.forceTeleport = false;
    }

    public void handleTransaction() {
        if (this.toConfirm != null) {
            this.toConfirm.run();
            this.toConfirm = null;
        }
    }

    @Override
    public void onPacket(PacketEvent event) {

        //run velocity shit
        if (event.getPacketSendEvent() != null) {

            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
                WrapperPlayServerEntityVelocity velocity = new WrapperPlayServerEntityVelocity(event.getPacketSendEvent());

                if (velocity.getEntityId() == getData().getPlayer().getEntityId()) {
                    TransactionPacketAction pre = () -> {
                        this.velocityTicks = 0;
                        this.lastGroundTicks = 0;

                        //add entry on pre
                        this.velocityEntries.add(
                                new TransactionVelocityEntry(
                                        velocity.getVelocity().getX(), velocity.getVelocity().getY(), velocity.getVelocity().getZ()
                                )
                        );

                        this.newVelocityY = velocity.getVelocity().getY();

                        //set confirmation for splitting later
                        this.confirmed = true;
                    };

                    TransactionPacketAction post = () -> {

                        //remove if size is greater than 1 on post when confirmed.
                        if (this.velocityEntries.size() > 1) {
                            this.velocityEntries.removeFirst();
                        }

                        //set confirmation for splitting later
                        this.confirmed = false;
                    };

                    //send out confirmation on the same thread as reach.
                    getData().getTickHolder().confirmFunctionVel(pre);
                    getData().getTickHolder().confirmFunctionAndTickVelocity(post);
                    CompletableFuture.runAsync(() -> this.getData().getTickHolder().pushVelTick());
                }
            }
        }


        if (event.getPacketReceiveEvent() != null) {

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
                WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event.getPacketReceiveEvent());

                if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING) {
                    this.sprinting = true;
                    this.sprintActions++;
                    this.ticksSinceStartSprint = 0;
                    this.lastSprintUpdateTimer.reset();

                    if (this.hasDesyncedSlow) {
                        this.hasDesyncedSlow = false;
                        this.desyncResetTime = 0;
                    }

                    if (this.hasDesyncedSpeed) {
                        this.hasDesyncedSpeed = false;
                        this.desyncResetTime = 0;
                    }
                }

                if (action.getAction() == WrapperPlayClientEntityAction.Action.STOP_SPRINTING) {
                    this.sprinting = false;
                    this.sprintActions++;
                    this.ticksSinceStopSprint = 0;
                    this.lastSprintUpdateTimer.reset();
                }
            }

            if (event.isMovement()) {
                ++this.desyncResetTime;
                this.velocitySimulator.onTick(System.currentTimeMillis());

                WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

                if (getData().getProtocolVersion() > 47) {
                    if (!this.lastPosition || !this.lastLastPosition
                            || !this.lastLastLastPosition || !this.lastLastLastLastPosition
                            || !this.lastLastLastLastLastPosition
                            || getData().getMovementProcessor().getDeltaXZ() < 0.1
                            || this.getData().getMovementProcessor().getTicksSincePosition() > 0) {
                        this.zeroThree = true;
                    }
                } else {
                    if (!this.lastPosition
                            || !this.lastLastPosition) {
                        this.zeroThree = true;
                    }
                }

                World world = getData().getPlayer().getWorld();

                if (world != null && this.teleportLocation != null) {
                    this.teleportLocation.setWorld(world.getName());
                }

                this.delayedFlyingTicks++;

                this.lastLastLastLastLastPosition = this.lastLastLastLastPosition;
                this.lastLastLastLastPosition = this.lastLastLastPosition;
                this.lastLastLastPosition = this.lastLastPosition;
                this.lastLastPosition = this.lastPosition;
                this.lastPosition = this.position;
                this.position = flying.hasPositionChanged();

                this.pastFlyings.add(flying.hasPositionChanged());


                if (!this.position || !this.lastPosition || !this.lastLastPosition) {
                    this.zeroThreeTicks = 0;
                } else {
                    this.zeroThreeTicks++;
                }

                if (this.pastFlyings.size() >= (getData().getProtocolVersion() > 47
                        ? 6 : 4)) {
                    this.pastFlyings.remove(0);
                }

                if (this.ticksSinceStartSprint < 10 && this.ticksSinceStopSprint < 10) {
                    this.spammedSprintTicks++;
                } else {
                    this.spammedSprintTicks = 0;
                }

                this.ticksSinceStopSprint++;
                this.ticksSinceStartSprint++;
                this.inWorldTicks++;

                this.lastGround = this.onGround;
                this.onGround = flying.isOnGround();

                this.lastSplitTime++;

                if (this.teleportLocation != null && world != null) {
                    for (int i = -1; i < 1; i++) {
                        FlyingLocation t = new FlyingLocation(world.getName(), this.teleportLocation.getPosX(),
                                this.teleportLocation.getPosY() + i, this.teleportLocation.getPosZ());

                        Material material = Anticheat.INSTANCE.getInstanceManager().getInstance()
                                .getType(world, t.getPosX(), t.getPosY(), t.getPosZ());

                        if (material == Material.AIR) {
                            this.wallLocation = t;
                            break;
                        } else if (this.wallLocation != null) {
                            this.teleportLocation = this.wallLocation;
                        }
                    }
                }

                this.handle(event, flying);

                if (this.confirmed) {
                    this.velocityEntries.clear();
                }

                this.hitValidEntity = false;
                this.isLastHitPlayer = false;
                this.lastTickWasSprinting = this.sprinting;
                this.wasSprinting = this.sprinting;

                if (flying.hasPositionChanged()) {
                    this.velocityTicks++;

                    if (!flying.isOnGround()) {
                        this.lastGroundTicks++;
                    }
                }
            }
        }
    }

    public void handle(PacketEvent event, WrapperPlayClientPlayerFlying currentFlying) {

        WrapperPlayClientPlayerFlying wrappedInFlyingPacket = getData().getMovementProcessor().getFlyingPacket();
        WrapperPlayClientPlayerFlying lastFlying = getData().getMovementProcessor().getLastFlyingPacket();
        WrapperPlayClientPlayerFlying lastLastLast = getData().getMovementProcessor().getLastLastFlyingPacket();

        if (wrappedInFlyingPacket == null || lastFlying == null || lastLastLast == null) return;

        if (currentFlying.hasPositionChanged()) {
            this.lastX = this.currentX;
            this.lastY = this.currentY;
            this.lastZ = this.currentZ;

            this.currentX = currentFlying.getLocation().getX();
            this.currentY = currentFlying.getLocation().getY();
            this.currentZ = currentFlying.getLocation().getZ();

            this.lastDeltaX = this.deltaX;
            this.lastDeltaZ = this.deltaZ;

            this.deltaX = (this.currentX - this.lastX);

            this.lastLastDeltaY = this.lastDeltaY;
            this.lastDeltaY = this.deltaY;
            this.deltaY = (this.currentY - this.lastY);

            this.deltaZ = (this.currentZ - this.lastZ);

            FlyingLocation current = new FlyingLocation(this.currentX, 0, this.currentZ);
            FlyingLocation last = new FlyingLocation(this.lastX, 0, this.lastZ);

            // prolly will get abused who fucking knows!
            if ((getData().getActionProcessor().getLastSneakTick() < 5 || getData().getActionProcessor().isSneaking())
                    && (getData().getMovementProcessor().getTo()
                    .isOnGround() || getData().getMovementProcessor().getFrom().isOnGround())) {

                if (CollisionUtil.isNearEdge(getData().getMovementProcessor().getTo())) {
                    this.lastEdgeFix = 0;
                }

                if (CollisionUtil.isNearNewEdge(getData(), getData().getMovementProcessor().getTo(),
                        getData().getMovementProcessor().getFrom())) {
                    this.lastEdgeFix = 0;
                } else {
                    if (getData().getSneakCollideTicks() > 0) {
                        getData().setSneakCollideTicks(getData().getSneakCollideTicks() - 1);
                    }
                }
            }

            if (CollisionUtil.isNearWall(current) ||
                    CollisionUtil.isNearWall(last)) {
                this.clientCollideTicks = 0;
            }


            this.lastEdgeFix++;
            this.clientCollideTicks++;
        }

        if (currentFlying.hasRotationChanged()) {
            this.fromYaw = this.yaw;
            this.yaw = currentFlying.getLocation().getYaw();
            this.pitch = currentFlying.getLocation().getPitch();
        }

        if ((currentFlying.hasPositionChanged() && wrappedInFlyingPacket.hasPositionChanged() && lastFlying.hasPositionChanged())
                || (this.isUsingItem() && wrappedInFlyingPacket.hasPositionChanged())) {
            this.validPosTicks++;
        } else {
            this.validPosTicks = 0;
        }

        World world = getData().getPlayer().getWorld();

        if (world == null) return;

        if (this.forceTeleport && this.teleportLocation != null) {

            if ((world != null
                    && this.teleportLocation.getWorld() != null
                    && !world.getName()
                    .equalsIgnoreCase(this.teleportLocation.getWorld()))) {
                //       this.invalidMoveTicks = 0;
                this.teleportTicks = 0;
                this.forceTeleport = false;
                this.ignoreTeleportSaving = false;
                this.teleportWithoutSettingTimers = false;
                return;
            }

            //   user.getMiscData().forceTeleportTimer.resetBoth();

            // teleport every 2 ticks, is, so they don't get more glitched or even kicked for more packets
            if (this.partialTicks++ > 2) {
                this.partialTicks = 0;

                float yaw = this.getData().getMovementProcessor().getTo().getYaw();

                if (this.rotationTeleport) {
                    yaw = this.getData().getMovementProcessor().getTo().getYaw()
                            + ThreadLocalRandom.current().nextInt(15);
                }

                this.teleportLocation.setYaw(yaw);
                this.teleportLocation.setPitch(this.getData().getMovementProcessor().getTo().getPitch());

                boolean fullyAir = true;

                for (int i = 0; i < 1; i++) {
                    Material material = Anticheat.INSTANCE.getInstanceManager().getInstance()
                            .getType(world,
                                    this.teleportLocation.getPosX(),
                                    this.teleportLocation.getPosY() - i,
                                    this.teleportLocation.getPosZ()
                            );

                    if (material != null && material != Material.AIR) {
                        fullyAir = false;
                        break;
                    }
                }


                if (fullyAir) {

                    double preLocationY = this.teleportLocation.getPosY();
                    this.teleportLocation = this.teleportLocation.add(0, -.2f, 0);

                    boolean foundPossiblePhaseBlock = false;

                    for (int i = -1; i < 3; i++) {
                        Material material = Anticheat.INSTANCE.getInstanceManager().getInstance()
                                .getType(world, this.teleportLocation.getPosX(),
                                        this.teleportLocation.getPosY() - i,
                                        this.teleportLocation.getPosZ()
                                );

                        if (material != null && material != Material.AIR) {
                            foundPossiblePhaseBlock = true;
                            break;
                        }
                    }

                    if (foundPossiblePhaseBlock) {
                        this.teleportLocation.setPosY(preLocationY);
                    }
                }

                if (++this.lagBackThreshold > 65) {
                    this.getData().kickPlayer("Being lagged back too many times.");
                }

                //Bukkit.broadcastMessage("teleport a");

                this.lastSetBackTimer.reset();
                if(this.teleportLocation.getWorld() == null || !this.teleportLocation.getWorld().equalsIgnoreCase(world.getName())) {
                    Anticheat.INSTANCE.getInstanceManager().getInstance().teleport(getData(), this.teleportLocation
                            .toLocation(world));
                }
            }

            // reset this system
            if (this.getData().getMovementProcessor().getDeltaXZ() < 1 &&
                    this.teleportTicks++ > (15 + this.getData().getTransactionProcessor().getPingTicks())) {
                this.teleportTicks = 0;
                this.forceTeleport = false;
                this.rotationTeleport = false;
                this.ignoreTeleportSaving = false;
                this.teleportWithoutSettingTimers = false;
                this.serverTicksSinceBadTeleport = 0;
            }
        }


        if (this.getData().getMovementProcessor().getDeltaXZ() > .2
                && getData().getBlockProcessor().isHasPlacedBlock()) {
            this.notPossible003 += this.notPossible003 < 5 ? 2 : 0;
        } else {
            this.notPossible003 -= this.notPossible003 > 0 ? 1 : 0;
        }

        if (this.validPosTicks < 3 && this.notPossible003 > 4) {
            this.possible003Exploits += this.possible003Exploits < 20 ? 1 : 0;
        } else {
            this.possible003Exploits -= this.possible003Exploits > 0 ? 1 : 0;
        }

        boolean timerPacket = this.possible003Exploits > 5;

        if (this.ticksToIgnore-- > 0) return;

        if ((!this.getData().getPotionProcessor().isSpeedPotion() && this.getData().getPotionProcessor().getSpeedPotionTicks() > 0)
                || (this.getData().getPotionProcessor().isSlownessPotion() && getData().getPotionProcessor().getSlownessTicks() < 1)
                || (this.getData().getPotionProcessor().isSpeedPotion() && getData().getPotionProcessor().getSpeedPotionTicks() < 1)
                || getData().getPotionProcessor().isSlownessPotion() != getData().getPotionProcessor().isLastSlowness()
                || getData().getPotionProcessor().isSpeedPotion() != getData().getPotionProcessor().isLastSpeed()
                || (!this.getData().getPotionProcessor().isSlownessPotion() && this.getData().getPotionProcessor().getSlownessTicks() > 0)) {
            this.ticksToIgnore = 1;
            return;
        }

        Motion realMotion = new Motion(
                this.deltaX, 0, this.deltaZ
        );

        double smallest = Double.MAX_VALUE;

        float blockFriction = Anticheat.INSTANCE.getInstanceManager().getInstance().getSlipperiness(getData(),
                getData().getMovementProcessor().getTo()
                        .toLocation(world));

        float blockFrictionFrom = Anticheat.INSTANCE.getInstanceManager().getInstance().getSlipperiness(getData(),
                getData().getMovementProcessor().getFrom()
                        .toLocation(world));

        float blockFrictionLast = Anticheat.INSTANCE.getInstanceManager().getInstance().getSlipperiness(getData(),
                getData().getMovementProcessor().getFromFrom()
                        .toLocation(world));

        if (blockFrictionFrom != blockFriction
                || blockFrictionFrom != blockFrictionLast
                || blockFriction != blockFrictionLast) {
            this.ticksToIgnore = 20;
            return;
        }


        FlyingLocation toLocation = getData().getMovementProcessor().getTo();

        int loops = 0;
        double pastMotionX = 0, pastMotionZ = 0, bruteMotionX = 0, bruteMotionZ = 0;

        iteration:
        {

            Motion motion = new Motion(
                    this.lastDeltaX, 0, this.lastDeltaZ
            );

            boolean velocityExpected = false;
            List<TransactionVelocityEntry> entries = new ArrayList<>();

            // add null otherwise loop will have nothing if there are no entries
            entries.add(null);

            // transaction split
            if (this.confirmed) {
                this.lastSplitTime = 0;
                if (!this.velocityEntries.isEmpty()) {
                    entries.addAll(this.velocityEntries);
                    velocityExpected = true;
                }
                this.confirmed = false;
            } else {
                if (!this.velocityEntries.isEmpty()) {
                    TransactionVelocityEntry velocityEntry = this.velocityEntries.peekLast();

                    if (velocityEntry != null) {
                        entries.add(velocityEntry);
                        velocityExpected = true;
                    }
                }
            }

            this.entries = entries;

            if ((currentFlying.hasPositionChanged() && lastFlying.hasPositionChanged() && lastLastLast.hasPositionChanged())
                    || timerPacket) {
                // Run no slow down processing on pre before horizontal engine is started.
                this.getData().getNoSlowDownProcessor().onPrePredictionProcess(getData(),
                        entries,
                        wrappedInFlyingPacket,
                        this.delayedFlyingTicks,
                        this.zeroThree,
                        this.usingFastMath,
                        timerPacket);

                // run keep sprint checking.
                this.getData().getMovementPredictionProcessor().onPrePredictionProcess(getData(),
                        true, false, true,
                        entries,
                        wrappedInFlyingPacket,
                        this.delayedFlyingTicks,
                        this.zeroThree,
                        this.usingFastMath,
                        timerPacket);
            }

            if (getData().generalCancelLessAbuse()) {
                this.validTicks = 0;
            } else {
                this.validTicks++;
            }

            if (this.validTicks > 60
                    && this.getData().getActionProcessor().getServerPositionTicks() > 20
                    && this.getData().getLastTeleport().getDelta() > 20
                    && this.getData().getMovementProcessor().getDeltaXZ() < .37) {
                this.storeLocation();
            }

            if (currentFlying.hasPositionChanged()) {
                if (!this.storedOnce || this.teleportLocation == null
                        || getData().getMovementProcessor().getPositionTicks() <= 10) {
                    this.storeLocationFirstTick();
                }
            }

            boolean takingVelocity = this.getData().getVelocityProcessor().getServerVelocityTicks() <= 10
                    || getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 10
                    || this.velocityTicks < 7;

            double angle = Math.abs(MathUtil.getAngleRotation(
                    this.getData().getMovementProcessor().getTo().clone(),
                    this.getData().getMovementProcessor().getFrom().clone()));

            boolean cancelledVelocity = getData().getVelocityProcessor().isTriggerVelocitySimulations()
                    || getData().getVelocityProcessor().getTriggerTime() > 0;

            // this is the prediction engine; it uses something called brute-forcing
            // it will iterate through a bunche of operations until it finds a result,
            // not all of these gets executed at once.
            // it really depends on what's going on
            // and what it can match to the current client,
            // for example, it's impossible to be sprinting and sneaking at the same time,
            // so sprinting and sneaking won't be checked against each other,
            // if they do that they will flag

            if ((currentFlying.hasPositionChanged() && lastFlying.hasPositionChanged() && lastLastLast.hasPositionChanged())
                    || timerPacket) {

                int fastMathIterations = 2;

                List<BruteForcedData> pastData = new ArrayList<>();
                BruteForcedData lastForceData = null;

                boolean typeAttack = getData().getCombatProcessor().isAttack();

                boolean sprintReset = this.lastSprintUpdateTimer.getDelta() <= 2
                        && this.getData().getCombatProcessor().getLastUseEntityTimer().getDelta() <= 2;

                float[] blockFrictionLoop = new float[]{blockFriction, blockFrictionFrom, blockFrictionLast};

                boolean[] hitLoop = (typeAttack && this.sprinting ? MagicValues.TRUE_FALSE : MagicValues.FALSE);

                if (sprintReset || this.getData().getCombatProcessor().getCancelHits() > 0) {
                    hitLoop = MagicValues.TRUE_FALSE;
                }

                boolean[] speedDesyncLoop = this.hasDesyncedSpeed && this.hadSpeed || this.desyncResetTime < 40
                        ? MagicValues.TRUE_FALSE : MagicValues.FALSE;

                boolean[] slowDesyncLoop = this.hasDesyncedSlow && this.hadSlow || this.desyncResetTime < 40
                        ? MagicValues.TRUE_FALSE : MagicValues.FALSE;

                boolean invalidIteration = this.possibleFix >= 4.5;
                boolean invalidSet = false;
                boolean velocityIteration = false;
                boolean triggeredOmni = false;

                if (angle < 100 && getData().getActionProcessor().isSprinting()
                        && getData().getActionProcessor().isLastSprinting()
                        && getData().getActionProcessor().getLastSprintTick() > 4) {
                    this.angleMovement++;
                } else {
                    this.angleMovement = 0;
                }

                boolean checkSprint = this.angleMovement > 7.0;

                for (int fastMathType = 0; fastMathType <= fastMathIterations; fastMathType++) {

                    predictionLoop:
                    {

                        for (TransactionVelocityEntry nextVelocity : entries) {
                            for (int forwardBrutes = -1; forwardBrutes < 2; ++forwardBrutes) {
                                for (int strafeBrutes = -1; strafeBrutes < 2; ++strafeBrutes) {
                                    for (boolean using : this.getUsingIteration()) {
                                        for (boolean sprint : getSprintIteration()) {
                                            for (boolean sneaking : this.getSneakingIteration(sprint)) {
                                                for (boolean hitSlowdown : hitLoop) {
                                                    for (boolean jump : getJumpingIteration(sprint)) {
                                                        for (float currentBlockFriction : blockFrictionLoop) {
                                                            for (boolean speedDesync : speedDesyncLoop) {
                                                                for (boolean slowDesync : slowDesyncLoop) {

                                                                    motion.set(this.lastDeltaX, 0, this.lastDeltaZ);

                                                                    boolean ground = this.lastGround;

                                                                    float forward = forwardBrutes;
                                                                    float strafe = strafeBrutes;

                                                                    // cant sprint in this condition, did this, so it flags omni sprint
                                                                    if (!takingVelocity && nextVelocity == null) {
                                                                        if ((sprint && forward <= 0) && angle < 100 && checkSprint) {
                                                                            triggeredOmni = true;
                                                                            continue;
                                                                        }
                                                                    }

                                                                    // walkSpeed / 2 is the same as the nms move speed
                                                                    double attributeSpeed =
                                                                            getData().getMovementProcessor()
                                                                            .getCachedWalkSpeed() / 2;

                                                                    // apply the clients sprint speed value
                                                                    if (sprint) {
                                                                        attributeSpeed += attributeSpeed * .30000001192092896;
                                                                    }

                                                                    // apply the current speed potion multiplier to the move speed
                                                                    if (this.getData().getPotionProcessor().isSpeedPotion()
                                                                              && !speedDesync) {
                                                                        attributeSpeed += this.getData().
                                                                                getPotionProcessor().getSpeedPotionAmplifier()
                                                                                * 0.20000000298023224D * attributeSpeed;
                                                                    }

                                                                    // apply the current slowness potion multiplier to the move speed
                                                                    if (this.getData().getPotionProcessor().isSlownessPotion()
                                                                            && !slowDesync) {
                                                                        attributeSpeed += this.getData().getPotionProcessor()
                                                                                .getSlownessAmplifier() *
                                                                                -.15000000596046448D * attributeSpeed;
                                                                    }

                                                                    // using logic
                                                                    if (using) {
                                                                        forward *= 0.2D;
                                                                        strafe *= 0.2D;
                                                                    }

                                                                    boolean realSneak = getData().getProtocolVersion() <= 47
                                                                            ? sneaking && !jump : sneaking;

                                                                    if (realSneak) {
                                                                        forward *= (float) 0.3D;
                                                                        strafe *= (float) 0.3D;
                                                                    }

                                                                    // forward & strafe logic
                                                                    forward *= 0.98F;
                                                                    strafe *= 0.98F;

                                                                    //ground frict here
                                                                    boolean onGround = lastLastLast.isOnGround();


                                                                    if (onGround) {
                                                                        // default ground friction calculations from the client
                                                                        motion.getMotionX().multiply(currentBlockFriction * 0.91F);
                                                                        motion.getMotionZ().multiply(currentBlockFriction * 0.91F);
                                                                    } else {
                                                                        // in air friction
                                                                        motion.getMotionX().multiply(0.91F);
                                                                        motion.getMotionZ().multiply(0.91F);
                                                                    }

                                                                    if (nextVelocity != null) {
                                                                        motion.getMotionX().set(nextVelocity.getX());
                                                                        motion.getMotionZ().set(nextVelocity.getZ());
                                                                        velocityIteration = true;
                                                                        velocityExpected = false;
                                                                    }

                                                                    if (!velocityIteration && velocityExpected
                                                                            && invalidIteration) {
                                                                        invalidSet = true;
                                                                    }


                                                                    // add hit slowdown to the current prediction
                                                                    if (hitSlowdown) {
                                                                        motion.getMotionX().multiply(0.6D);
                                                                        motion.getMotionZ().multiply(0.6D);
                                                                    }

                                                                    // round the prediction
                                                                    motion.round();

                                                                    float slipperiness = 0.91F;

                                                                    // apply the blocks below friction
                                                                    if (ground)
                                                                        slipperiness = currentBlockFriction * 0.91F;

                                                                    float moveSpeed = (float) attributeSpeed;
                                                                    final float moveFlyingFriction;

                                                                    // apply the move speed multiplier if on ground
                                                                    if (ground) {
                                                                        float moveSpeedMultiplier = 0.16277136F /
                                                                                (slipperiness * slipperiness * slipperiness);

                                                                        // 1.13+ only
                                                                        if (getData().getProtocolVersion() > 404) {
                                                                            moveSpeedMultiplier =
                                                                                    0.21600002F / (currentBlockFriction
                                                                                            * currentBlockFriction * currentBlockFriction);
                                                                        }

                                                                        moveFlyingFriction = moveSpeed * moveSpeedMultiplier;

                                                                        // append the acceleration from sprint jumping
                                                                        if (jump && sprint) {
                                                                            final float radians = this.yaw * 0.017453292F;

                                                                            if (getData().getProtocolVersion() > 404) {
                                                                                motion.getMotionX().subtract(
                                                                                        MathHelper_1_20.sinFixed(fastMathType,
                                                                                                radians) * 0.2F);
                                                                                motion.getMotionZ().add(
                                                                                        MathHelper_1_20.cosFixed(fastMathType,
                                                                                                radians) * 0.2F);
                                                                            } else {
                                                                                motion.getMotionX().subtract(
                                                                                        MinecraftMath.sin(fastMathType,
                                                                                                radians) * 0.2F);

                                                                                motion.getMotionZ().add(
                                                                                        MinecraftMath.cos(fastMathType,
                                                                                                radians) * 0.2F);
                                                                            }
                                                                        }
                                                                    } else {
                                                                        // apply the air movement acceleration
                                                                        moveFlyingFriction = (float)
                                                                                (sprint ? ((double) 0.02F +
                                                                                        (double) 0.02F * 0.3D) : 0.02F);
                                                                    }

                                                                    float friction =
                                                                            getData().getCollisionProcessor().isLavaFully()
                                                                                    ? 0.02F
                                                                                    : moveFlyingFriction;

                                                                    MoveFlyingResult moveFlyingResult =
                                                                            getData().getProtocolVersion() > 404
                                                                                    ? MagicValues.moveFlying(
                                                                                    this.yaw, forward, strafe, friction,
                                                                                    fastMathType
                                                                            ) : MagicValues.moveFlying(getData(),
                                                                                    forward, strafe, friction,
                                                                                    fastMathType,
                                                                                    this.yaw
                                                                            );

                                                                    // apply the move flying we got above
                                                                    motion.apply(moveFlyingResult);

                                                                    // checks web collisions.
                                                                    if (getData()
                                                                            .getCollisionProcessor().isWeb()
                                                                            && getData()
                                                                            .getCollisionProcessor().getWebTicks() > 2
                                                                    ) {
                                                                        motion.getMotionX().set(0.0);
                                                                        motion.getMotionZ().set(0.0);
                                                                    }

                                                                    // calculate the delta X & Y to generate the square root
                                                                    double distance = realMotion.distanceSquared(motion);

                                                                    // check if the current is the smaller than the last prediction
                                                                    if (distance < smallest) {
                                                                        smallest = distance;

                                                                        this.lastMoveForward = forward;
                                                                        this.lastMoveStrafe = strafe;
                                                                        loops++;

                                                                        bruteMotionX = motion.getMotionX().get();
                                                                        bruteMotionZ = motion.getMotionZ().get();

                                                                        if (velocityIteration
                                                                                // transactions being cancelled, use current motion
                                                                                || cancelledVelocity) {
                                                                            this.lastVelocityMotion = motion.clone();
                                                                        }

                                                                        // if the delta is lower than this, break the loop
                                                                        if (distance < 1E-7) {
                                                                            pastMotionX = motion.getMotionX().get();
                                                                            pastMotionZ = motion.getMotionZ().get();

                                                                            this.calculateNextKey(forward, strafe);
                                                                            this.calculateIterator(velocityIteration,
                                                                                    velocityExpected, distance);

                                                                            BruteForcedData data = new BruteForcedData(
                                                                                    distance, smallest,
                                                                                    sprint, jump,
                                                                                    using, hitSlowdown,
                                                                                    ground, sneaking,
                                                                                    forward, strafe,
                                                                                    0,
                                                                                    loops, velocityExpected,
                                                                                    this.hadVelocityTick, velocityIteration,
                                                                                    invalidSet,
                                                                                    triggeredOmni
                                                                            );

                                                                            // add our possible prediction
                                                                            // to the data list
                                                                            pastData.add(data);

                                                                            // run our fast-math emulator
                                                                            this.handleFastMath(smallest, fastMathType);

                                                                            lastForceData = data;

                                                                            break predictionLoop;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                double temp = Double.MAX_VALUE;
                BruteForcedData forcedData = null;

                if (this.usingFastMath) {

                    // we got samples, find the smallest
                    for (BruteForcedData data : pastData) {
                        if (data.getSmallest() < temp) {
                            temp = data.getSmallest();
                            forcedData = data;
                        }
                    }

                    // no samples where found use the last known data
                    if (temp == Double.MAX_VALUE) {
                        temp = smallest;
                        forcedData = lastForceData;
                    }

                } else {
                    // not using fast-math so run as normal, ignoring all above
                    temp = smallest;
                    forcedData = lastForceData;
                }

                if (forcedData != null) {
                    this.bruteForcedData = forcedData;
                    this.hadVelocityTick = false;
                    smallest = temp;
                }
            }
        }

        if (this.lastVelocityMotion != null) {
            this.velocitySimulator.runSimulator(this.lastVelocityMotion, System.currentTimeMillis());
        }

        if ((currentFlying.hasPositionChanged() && lastFlying.hasPositionChanged() && lastLastLast.hasPositionChanged())
                || timerPacket) {

            boolean allFlyings = StreamUtil.allMatch(this.pastFlyings, aBoolean -> aBoolean);

            boolean dumbAssEdgeCaseFix = this.lastEdgeFix < 15
                    || getData().getActionProcessor().getLastSneakTick() < 20
                    && getData().getCollisionWorldProcessor().getHalfBlockTicks() > 0;

            double max = dumbAssEdgeCaseFix ? 0.08D : getData().getProtocolVersion() > 47 || this.usingFastMath
                    || this.lastFastMath > 0 && this.velocityTicks < 20 ? 7E-5 : 1E-7;

            if (this.zeroThree && !timerPacket) {

                int delay = this.delayedFlyingTicks;

                if (delay > 4) {
                    delay = 4;
                }

                max = max + (getData().getProtocolVersion() > 47
                        ? (0.06D * (this.delayedFlyingTicks > 0 ? delay : 1))
                        : (0.03D * (this.delayedFlyingTicks > 0 ? delay : 1)));
            }

            int posTick = getData().getMovementProcessor().getTicksSincePosition();

            if (posTick > 4) {
                posTick = 4;
            }

            if (!timerPacket && posTick > 0) {
                max = max + (getData().getProtocolVersion() > 47
                        ? (0.06D * getData().getMovementProcessor().getTicksSincePosition())
                        : (0.03D * getData().getMovementProcessor().getTicksSincePosition()));
            }


            // if prediction offset is less than the max offset.
            if (smallest <= 1E-7 &&
                    // teleporting
                    getData().getSetBackProcessor().getLastInvalidTick() == 0 &&
                    this.getData().getGhostBlockProcessor().getLastGhostBlockTimer().getDelta() > 15
                    && this.getData().getActionProcessor().getServerPositionTicks() > 20
                    && getData().getActionProcessor().getServerPositionTicks() > 40
                    && getData().getLastTeleport().getDelta() > 20
                    && !getData().getActionProcessor().isTeleportingV3()
                    && getData().getSetBackTicks() == 0
                    // setback is not called & sent valid position packet
                    && !this.forceTeleport && this.validPosTicks > 2) {
                this.toConfirm = this::storeLocation;
            }

            this.hadSpeed = getData().getPotionProcessor().isSpeedPotion();
            this.hadSlow = getData().getPotionProcessor().isSlownessPotion();

            if (!this.hadSpeed) {
                this.hasDesyncedSpeed = false;
                this.desyncResetTime = 0;
            }

            if (!this.hadSlow) {
                this.hasDesyncedSlow = false;
                this.desyncResetTime = 0;
            }

            if (smallest > max
                    && this.hadSpeed
                    && getData().getLastWorldChange().getPositionDelta() < 100) {
                this.hasDesyncedSpeed = true;
            }

            // Bukkit.broadcastMessage(""+smallest);

            boolean move = this.getData().getMovementProcessor().getDeltaXZ() > .18
                    || this.velocityTicks < 20;

            if (this.ticksNoOffset == 0) {
                if (this.velocityTicks <= 2 && this.zeroThreeTicks > 4
                        && !getData().getCollisionWorldProcessor().isCollidingHorizontal()
                        && getData().getMovementProcessor().getClientWallCollision().getDelta() > 7
                        && smallest > 0
                        && getData().getMovementProcessor().getDeltaYAbs() > 0
                        && getData().getMovementProcessor().getSkippedPackets() == 0) {
                    if (++this.possibleFix >= 7) {
                        this.possibleFix = 6;
                    }
                } else {
                    this.possibleFix -= Math.min(this.possibleFix, .2);
                }
            } else {
                this.possibleFix -= Math.min(this.possibleFix, .2);
            }

            if (this.velocityTicks < (getData().getProtocolVersion() > 404
                    ? 5 : 7)) {

                if (smallest != Double.MAX_VALUE && allFlyings
                        && this.pastFlyings.size() >= (getData().getProtocolVersion() > 47
                        ? 5 : 3)) {

                    double pastMotion = MathUtil.hypot(bruteMotionX, bruteMotionZ);
                    double ratio = Math.abs(getData().getMovementProcessor().getDeltaXZ() / pastMotion);

                    // velocity check
                    double finalSmallest = smallest;
                    double finalMax = max;

                    this.offset = finalSmallest;

                    Collection<Check> checkData = getData().getCachedChecks();

                    for (Check check : checkData) {

                        if (check == null) continue;

                        VelocityCheckData velocityCheckData = new VelocityCheckData(finalSmallest, this.velocityTicks,
                                this.velocityEntries.size(), ratio,
                                finalMax);

                        if (check.isEnabled()) {
                            check.onVelocityDetection(velocityCheckData);
                        }
                    }
                }
            }

            if (timerPacket || move && this.clientCollideTicks > 3
                    && this.getData().getCollisionWorldProcessor().getCollidingHorizontallyTimer().passed(3)) {


                if (smallest != Double.MAX_VALUE
                        && this.clientCollideTicks > 10
                        && getData().getCollisionWorldProcessor().getHalfBlockTicks() < 1
                        && getData().getCollisionProcessor().getLiquidTicks() < 1
                        && getData().getCollisionWorldProcessor().getSlimeTicks() < 1
                        && getData().getCollisionWorldProcessor().getIceTicks() < 1
                        && !getData().getCollisionWorldProcessor().isBlockAbove()
                        && getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 10) {
                    if (smallest < 1.0E-9 && smallest > 9.9E-12
                            && getData().getProtocolVersion() == 47) {
                        ++this.liquidBounceBreakTicks;
                    } else {
                        this.liquidBounceBreakTicks -= Math.min(this.liquidBounceBreakTicks, 0.005);
                    }
                }

                //  Bukkit.broadcastMessage(""+smallest);

                if (smallest != Double.MAX_VALUE
                        && this.clientCollideTicks > 20
                        && !getData().getCollisionWorldProcessor().isCollidingHorizontal()
                        && getData().getCollisionWorldProcessor().getHalfBlockTicks() < 1
                        && getData().getCollisionProcessor().getLiquidTicks() < 1
                        && getData().getCollisionWorldProcessor().getSlimeTicks() < 1
                        && getData().getCollisionWorldProcessor().getIceTicks() < 1
                        && !getData().getCollisionWorldProcessor().isBlockAbove()
                        && getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3) {
                    if (smallest < 1E-9 && smallest > 1E-13
                            && getData().getProtocolVersion() == 47) {
                        this.liquidBounceDetector++;
                        getData().getSetBackProcessor().setInvalid(true);
                    } else {
                        this.liquidBounceDetector = Math.min(this.liquidBounceDetector, 0.085);
                    }
                }

                if (this.acceptTicks > 0) {
                    this.acceptTicks--;
                }

                if (this.acceptTicks < 1
                        && getData().getCollisionWorldProcessor().isGround()
                        && getData().getMovementProcessor().getTo().isOnGround()) {
                    this.currentValidPosition = new FlyingLocation(toLocation.getWorld(),
                            toLocation.getPosX(), toLocation.getPosY(), toLocation.getPosZ(),
                            toLocation.getYaw(), toLocation.getPitch());
                }

                boolean bruteMove = this.getData().getMovementProcessor()
                        .getDeltaXZ() > MathUtil.hypot(bruteMotionX, bruteMotionZ) || this.velocityTicks < 20;


                if (bruteMove
                        && this.clientCollideTicks > 3
                        && !getData().getCollisionWorldProcessor().isCollidingHorizontal()
                        && smallest != Double.MAX_VALUE
                        && smallest > max) {

                    this.acceptTicks = 7;

                    // movement check
                    PredictionData invalidData2 = new PredictionData(smallest, true, this.getData(),
                            this.bruteForcedData, this.velocityTicks, this.getData().getMovementProcessor()
                            .getDeltaXZ(), MathUtil.hypot(bruteMotionX, bruteMotionZ), this.usingFastMath, max);
                    Collection<Check> checkData = getData().getCachedChecks();

                    for (Check check : checkData) {

                        if (check == null) continue;

                        if (check.isEnabled()) {
                            check.onPrediction(invalidData2);
                        }
                    }

                } else {

                    if (this.acceptTicks > 0) {
                        this.acceptTicks--;
                    }

                    if (this.acceptTicks < 1
                            && getData().getCollisionWorldProcessor().isGround()
                            && getData().getMovementProcessor().getTo().isOnGround()) {
                        this.currentValidPosition = new FlyingLocation(toLocation.getWorld(),
                                toLocation.getPosX(), toLocation.getPosY(), toLocation.getPosZ(),
                                toLocation.getYaw(), toLocation.getPitch());
                    }

                    PredictionData invalidData2 = new PredictionData(smallest, false, this.getData(),
                            this.bruteForcedData, this.velocityTicks, this.getData().getMovementProcessor()
                            .getDeltaXZ(), MathUtil.hypot(bruteMotionX, bruteMotionZ), this.usingFastMath, max);
                    Collection<Check> checkData = getData().getCachedChecks();

                    for (Check check : checkData) {

                        if (check == null) continue;

                        if (check.isEnabled()) {
                            check.onPrediction(invalidData2);
                        }
                    }
                }
            } else {

                this.movementStrafeFixThreshold -= Math.min(this.movementStrafeFixThreshold, 0.01);

                if (this.acceptTicks > 0) {
                    this.acceptTicks--;
                }

                if (this.acceptTicks < 1
                        && getData().getCollisionWorldProcessor().isGround()
                        && getData().getMovementProcessor().getTo().isOnGround()) {
                    this.currentValidPosition = new FlyingLocation(toLocation.getWorld(),
                            toLocation.getPosX(), toLocation.getPosY(), toLocation.getPosZ(), toLocation.getYaw(), toLocation.getPitch());
                }

                PredictionData invalidData2 = new PredictionData(smallest, false, this.getData(),
                        this.bruteForcedData, this.velocityTicks, this.getData().getMovementProcessor()
                        .getDeltaXZ(), MathUtil.hypot(bruteMotionX, bruteMotionZ), this.usingFastMath, max);
                Collection<Check> checkData = getData().getCachedChecks();

                for (Check check : checkData) {

                    if (check == null) continue;

                    if (check.isEnabled()) {
                        check.onPrediction(invalidData2);
                    }
                }
            }

            this.predictedX = pastMotionX;
            this.predictedZ = pastMotionZ;
        }
    }

    public void calculateIterator(boolean iterator, boolean expect, double distance) {
        if (!iterator && expect && distance > 0.0) {
            this.ticksNoOffset = 0;
        } else {
            this.ticksNoOffset++;
        }
    }

    private void calculateNextKey(float moveForward, float moveStrafing) {
        Keys keys = Keys.NOTHING;

        if (moveForward == 0.98f && moveStrafing == 0f) {
            keys = Keys.W;
        }

        if (moveForward == -0.98f && moveStrafing == 0f) {
            keys = Keys.S;
        }

        if (moveForward == 0f && moveStrafing == -0.98f) {
            keys = Keys.D;
        }

        if (moveForward == 0f && moveStrafing == 0.98f) {
            keys = Keys.A;
        }

        if (moveForward == 0.98f && moveStrafing == 0.98f) {
            keys = Keys.W_A;
        }

        if (moveForward == 0.98f && moveStrafing == -0.98f) {
            keys = Keys.W_D;
        }

        if (moveForward == -0.98f && moveStrafing == -0.98f) {
            keys = Keys.S_D;
        }

        if (moveForward == -0.98f && moveStrafing == 0.98f) {
            keys = Keys.S_A;
        }

        if (keys != Keys.NOTHING) {
            this.lastKey = this.key;
            this.key = keys;
        }
    }

    public enum Keys {
        W_D,
        D,
        S_D,
        S,
        S_A,
        A,
        W_A,
        W,
        NOTHING
    }

    public boolean isStrafing() {
        return this.key == Keys.A
                || this.key == Keys.D
                || this.key == Keys.S
                || this.key == Keys.S_D
                || this.key == Keys.S_A;
    }

    private void handleFastMath(double smallest, int math) {

        if (math == 0) {
            this.wasUsingFastMath = this.usingFastMath;

            if (smallest < 1E-7 && smallest > 1E-17) {
                this.usingFastMath = true;
                this.lastFastMath = 7;
            } else {
                this.usingFastMath = false;
                this.lastFastMath -= Math.min(this.lastFastMath, 1);
            }
        }

        this.cachedUsingFastMath = this.usingFastMath;
    }


    private boolean[] getJumpingIteration(boolean sprinting) {
        if (getData().getVelocityProcessor().getServerVelocityTicks() < (20 +
                this.getData().getTransactionProcessor().getPingTicks())
                || !this.velocityEntries.isEmpty()) {
            return MagicValues.TRUE_FALSE;
        }
        return sprinting ? MagicValues.TRUE_FALSE : MagicValues.FALSE;
    }

    private boolean[] getSneakingIteration(boolean sprinting) {
        if (getData().getVelocityProcessor().getServerVelocityTicks() < (20 +
                this.getData().getTransactionProcessor().getPingTicks())
                || getData().getProtocolVersion() > 47) {
            return MagicValues.TRUE_FALSE;
        }

        return MagicValues.TRUE_FALSE;
    }

    private boolean[] getUsingIteration() {
        if (getData().getVelocityProcessor().getServerVelocityTicks() < (20 +
                this.getData().getTransactionProcessor().getPingTicks())) {
            return MagicValues.TRUE_FALSE;
        }

        return MagicValues.TRUE_FALSE;
    }

    private boolean[] getSprintIteration() {
   /*     if (getData().getVelocityProcessor().getServerVelocityTicks() < (20 +
                this.getData().getTransactionProcessor().getPingTicks())) {
            return MagicValues.TRUE_FALSE;
        }


        return this.sprinting ? MagicValues.TRUE_FALSE : this.ticksSinceStopSprint < 10
                || this.lastSprintUpdateTimer.getDelta() < 20
                || this.ticksSinceStartSprint < 10 ?
                MagicValues.TRUE_FALSE : MagicValues.FALSE;*/

        return MagicValues.TRUE_FALSE;
    }


    // buggy
    public boolean isUsingItem() {
        return false;
    }

    @Getter
    @AllArgsConstructor
    public static final class BruteForcedData {
        private final double distance, smallest;
        private boolean sprint, jump, using, hitSlowdown, ground, sneaking;
        private float forward, strafe;
        private int fastMath, loops;
        private boolean velocityTick, hadVelocityTick, preVelocity, invalidIteration, omniSprinting;

        public String toString() {
            return "[distance=" + this.distance + ", smallest=" + this.smallest
                    + ", sprint=" + this.sprint + ", jump=" + this.jump + ", using=" + this.using +
                    ", hitSlowdown=" + this.hitSlowdown + ", ground="
                    + this.ground + ", sneaking=" + this.sneaking + ", forward=" +
                    this.forward + ", strafe=" + this.strafe + ", fastMath=" + this.fastMath
                    + ", loops=" + this.loops + "]";
        }
    }


    @Getter
    @AllArgsConstructor
    public static final class TransactionVelocityEntry {
        private final double x, y, z;
    }


    public void triggerRotationTeleport() {

        if ((getVelocitySimulator().isSimulatorRunning())) return;

        if (!this.forceTeleport && this.validTicks > 60
                && this.getData().getLastTeleport().passed(100)
                && this.getData().getGhostBlockProcessor().getLastGhostBlockTimer().passed(20)
                && this.inWorldTicks > 100
                && this.teleportLocation != null
                && this.teleportLocation.getWorld() != null
                && this.getData().getPlayer().getWorld() != null
                && !this.getData().generalCancel()
                && this.getData().getPlayer().getHealth() > 2
                && !this.getData().generalCancel()) {
            this.forceTeleport = true;
            this.rotationTeleport = true;
        }
    }

    public void triggerTeleportNoChecks() {

        if (!this.forceTeleport
                && this.teleportLocation != null
                && this.teleportLocation.getWorld() != null
                && this.getData().getPlayer().getWorld().getName() != null
                && !this.getData().generalCancel()) {
            this.forceTeleport = true;
        }

        if (this.teleportLocation == null || this.lastValidLocation == null) {
            if (++this.bypassFix > 500) {
                getData().kickPlayer("Being teleported with a null location too many times.");
            }
        }
    }

    public void triggerTeleport() {

        if ((getVelocitySimulator().isSimulatorRunning())) return;

        if (!this.forceTeleport && this.validTicks > 60
                && this.getData().getLastTeleport().passed(7)
                && this.getData().getGhostBlockProcessor().getLastGhostBlockTimer().passed(20)
                && this.inWorldTicks > 5
                && this.teleportLocation != null
                && this.teleportLocation.getWorld() != null
                && this.getData().getPlayer().getWorld().getName() != null
                && !this.getData().generalCancelLessAbuse()
                && this.getData().getPlayer().getHealth() != 0) {
            this.forceTeleport = true;
        }
    }

    private void storeLocation() {

        if (getData().generalCancelLessAbuse()
                // if currently setting back then don't store locations
                || getData().getSetBackProcessor().getSetBackTick() > 0
                // if current movement is invalid don't store
                || getData().getSetBackProcessor().getLastInvalidTick() > 0
                || getData().getActionProcessor().getTeleportTicks() <= 10
                || this.getData().getMovementProcessor().getToNull() == null
                // if blinking then ignore
                || getData().getSetBackTicks() > 0
                // when player hasn't sent 1 single position packet then ignore.
                || this.getData().getMovementProcessor().getPositionTicks() < 1) {
            return;
        }



        this.lastValidLocation = this.getData().getMovementProcessor().getToNull().clone();
        this.lastValidLocation.setWorld(getData().getPlayer().getWorld().getName());

        this.teleportLocation = this.lastValidLocation.clone();
        this.teleportLocation.setPosY(getData().getMovementProcessor().getToNull().getPosY());
    }

    private void storeLocationFirstTick() {

        if (this.getData().getMovementProcessor().getToNull() != null) {

            this.lastValidLocation = this.getData().getMovementProcessor().getToNull().clone();
            this.lastValidLocation.setWorld(getData().getPlayer().getWorld().getName());

            this.teleportLocation = this.lastValidLocation.clone();
            this.teleportLocation.setPosY(getData().getMovementProcessor().getToNull().getPosY());

            this.storedOnce = true;
        }
    }
}