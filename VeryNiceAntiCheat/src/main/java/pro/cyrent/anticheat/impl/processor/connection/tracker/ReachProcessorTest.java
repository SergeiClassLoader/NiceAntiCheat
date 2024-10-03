package pro.cyrent.anticheat.impl.processor.connection.tracker;


import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.Check;
import pro.cyrent.anticheat.api.check.data.ReachData;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.block.box.HydroBB;
import pro.cyrent.anticheat.util.block.box.HydroMovingPosition;
import pro.cyrent.anticheat.util.block.box.ShitBox;
import pro.cyrent.anticheat.util.event.EventTimer;
import pro.cyrent.anticheat.util.hash.ConcurrentHashSet;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.minecraftmaths.MinecraftMath;
import pro.cyrent.anticheat.util.task.TransactionPacketAction;
import pro.cyrent.anticheat.util.vec.Vec3;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;



@Getter
@Setter
public class ReachProcessorTest extends Event {
    private final PlayerData data;

    public int lastEntityID;
    private boolean zeroThree;
    private int targetTick;

    public int skipped;

    private final EventTimer entityUpdateTransactionTimer, entityVelTimer;

    //0.000732421875
    private final double FAST_MATH_ERROR = 3.0D / 4096.0D;

    private WrapperPlayClientPlayerFlying flyingPacket, lastFlyingPacket, lastLastFlyingPacket;

    private boolean intersection;

    private final ConcurrentHashSet<TransactionPacketAction> postQueue = new ConcurrentHashSet<>();
    private final ConcurrentHashSet<TransactionPacketAction> preQueue = new ConcurrentHashSet<>();
    private double mapTimes;

    private final Queue<Short> preMap = new ConcurrentLinkedQueue<>();

    private boolean chicken, egg;

    private int trackTicks;
    private boolean attack, interact, swing;

    private NewTrackedEntity target, userTarget, targetNoReset;
    private Entity entityTarget;

    private Vector hitVector;

    private int ranTimes;

    private double lastDistance;

    private HydroBB targetBBData;
    private NewTrackedEntity.PossiblePosition possiblePosition;

    private int timeSincePositionSet;

    private Vector interactVector;

    private double distance, distanceNoReset;

    private int lastOffset;

    private int attackTick;

    private final FlyingLocation to = new FlyingLocation(0, 0, 0);
    private final FlyingLocation from = new FlyingLocation(0, 0, 0);
    private final FlyingLocation fromFrom = new FlyingLocation(0, 0, 0);

    private final EventTimer lastEntitySwitch, lastAttack;

    private int confirmReachTicks, fixReachTick;

    private int nextFlyingTickFix;

    private int validBoxTicks;

    private int spammedTicks;

    private boolean accuratePosition, moving;

    @Getter
    private final Map<Integer, NewTrackedEntity> tracked = new ConcurrentHashMap<>();

    public ReachProcessorTest(PlayerData user) {
        this.data = user;
        this.lastEntitySwitch = new EventTimer(20, user);
        this.lastAttack = new EventTimer(20, user);
        this.entityUpdateTransactionTimer = new EventTimer(20, user);
        this.entityVelTimer = new EventTimer(20, user);
    }

    public void onTransaction(short action) {
        if (!this.preMap.isEmpty() && this.preMap.peek() != null) {
            short peek = this.preMap.peek();

            if (peek == action) {
                this.preMap.poll();

                Iterator<TransactionPacketAction> preIterator = this.preQueue.iterator();
                while (preIterator.hasNext()) {
                    TransactionPacketAction transactionAction = preIterator.next();
                    transactionAction.handle();
                    preIterator.remove();
                }

                Iterator<TransactionPacketAction> postIterator = this.postQueue.iterator();
                while (postIterator.hasNext()) {
                    TransactionPacketAction transactionAction = postIterator.next();
                    transactionAction.handle();
                    postIterator.remove();
                }
            }
        }
    }

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketSendEvent() == null) return;

        if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.SPAWN_PLAYER) {
            WrapperPlayServerSpawnPlayer spawnPlayer = new WrapperPlayServerSpawnPlayer(event.getPacketSendEvent());

            int x = spawnPlayer.posX,
                    y = spawnPlayer.posY,
                    z = spawnPlayer.posZ;

            //set location.
            FlyingLocation customLocation = new FlyingLocation(
                    x, y, z,
                    spawnPlayer.getYaw(), spawnPlayer.getPitch());

            NewTrackedEntity entityTracked = new NewTrackedEntity(spawnPlayer.getEntityId());


            // confirm pre, and post entity position on spawn.
            this.confirmPrePost(() -> entityTracked.initial(customLocation),
                    () -> this.tracked.put(spawnPlayer.getEntityId(), entityTracked));

        } else if (event.getPacketSendEvent().getPacketType()
                == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
            WrapperPlayServerEntityRelativeMove relativeMove =
                    new WrapperPlayServerEntityRelativeMove(event.getPacketSendEvent());

            this.runRelMove(null, relativeMove);
        } else if (event.getPacketSendEvent().getPacketType()
                == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
            WrapperPlayServerEntityRelativeMoveAndRotation relativeMoveAndRotation =
                    new WrapperPlayServerEntityRelativeMoveAndRotation(event.getPacketSendEvent());

            this.runRelMove(relativeMoveAndRotation, null);
        } else if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT) {
            WrapperPlayServerEntityTeleport teleport = new WrapperPlayServerEntityTeleport(event.getPacketSendEvent());

            final NewTrackedEntity entity = tracked.get(teleport.getEntityId());
            if (entity == null) return;

            final TransactionPacketAction pre = () -> {
                double x = teleport.posX, y = teleport.posY, z = teleport.posZ;

                entity.setConfirming(true);
                entity.setNextReach(new FlyingLocation(x / 32.0D, y / 32.0D, z / 32.0D));

                this.confirmReachTicks++;
                entity.teleports++;
            };

            final TransactionPacketAction post = () -> {

                double x1 = teleport.posX, y1 = teleport.posY, z1 = teleport.posZ;

                entity.serverPosX = x1;
                entity.serverPosY = y1;
                entity.serverPosZ = z1;

                final double x = entity.serverPosX / 32.0D;
                final double y = entity.serverPosY / 32.0D;
                final double z = entity.serverPosZ / 32.0D;

                for (final NewTrackedEntity.PossiblePosition reachPosition : entity.getPositions()) {

                    if (reachPosition.skip) {
                        reachPosition.skip = false;
                        continue;
                    }

                    double zeroThree = Anticheat.INSTANCE.serverVersion > 18
                            ? 0.06125D : 0.03125D;

                    if (Math.abs(reachPosition.posX - x) < zeroThree
                            && Math.abs(reachPosition.posY - y) < 0.015625D
                            && Math.abs(reachPosition.posZ - z) < zeroThree) {
                        reachPosition.setPositionAndRotation2(
                                reachPosition.posX, reachPosition.posY, reachPosition.posZ, 0, 0);
                    } else {
                        reachPosition.setPositionAndRotation2(x, y, z, 0, 0);
                    }
                }


                this.confirmReachTicks++;
                entity.setNextReach(null);
                entity.setConfirming(false);
            };

            if (this.shouldSpam(teleport.getEntityId())) {

                getData().getTickHolder().confirmFunctionAndTick(pre);
                getData().getTickHolder().confirmFunction(post);

                CompletableFuture.runAsync(() -> getData().getTickHolder().pushTick());

                entity.reallyUsingPrePost = true;
            } else {
                entity.reallyUsingPrePost = false;
                this.preQueue.add(pre);
                this.postQueue.add(post);
            }
        } else if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.DESTROY_ENTITIES) {
            WrapperPlayServerDestroyEntities serverDestroyEntities = new WrapperPlayServerDestroyEntities(event
                    .getPacketSendEvent());

            for (int entity : serverDestroyEntities.getEntityIds()) {
                tracked.remove(entity);
            }
        }
    }

    public void runRelMove(WrapperPlayServerEntityRelativeMoveAndRotation relativeMoveAndRotation,
                           WrapperPlayServerEntityRelativeMove relativeMove) {

        //Rel move
        if (relativeMove != null) {

            final NewTrackedEntity trackedEntity = tracked.get(relativeMove.getEntityId());

            if (trackedEntity == null) return;

            final double x = relativeMove.posX;
            final double y = relativeMove.posY;
            final double z = relativeMove.posZ;

            this.chicken = false;
            this.egg = false;

            final TransactionPacketAction pre = () -> {
                double newX = trackedEntity.serverPosX;
                double newY = trackedEntity.serverPosY;
                double newZ = trackedEntity.serverPosZ;

                newX += x;
                newY += y;
                newZ += z;

                trackedEntity.setConfirming(true);
                trackedEntity.setNextReach(new FlyingLocation(newX / 32.0D, newY / 32.0D, newZ / 32.0D));

                this.confirmReachTicks++;
                trackedEntity.trackedLocations++;
                this.chicken = true;
            };

            final TransactionPacketAction post = () -> {

                trackedEntity.serverPosX += x;
                trackedEntity.serverPosY += y;
                trackedEntity.serverPosZ += z;

                double var = 32.0D;

                for (final NewTrackedEntity.PossiblePosition reachPosition : trackedEntity.getPositions()) {
                    if (reachPosition.skip) {
                        reachPosition.skip = false;
                        continue;
                    }

                    reachPosition.setPositionAndRotation2(trackedEntity.serverPosX / var,
                            trackedEntity.serverPosY / var,
                            trackedEntity.serverPosZ / var, 0, 0);
                }


                trackedEntity.setNextReach(null);
                trackedEntity.setConfirming(false);

                this.confirmReachTicks++;
                this.egg = true;
            };


            if (this.shouldSpam(relativeMove.getEntityId())) {

                getData().getTickHolder().confirmFunctionAndTick(pre);
                getData().getTickHolder().confirmFunction(post);

                CompletableFuture.runAsync(() -> getData().getTickHolder().pushTick());

            } else {
                this.preQueue.add(pre);
                this.postQueue.add(post);
            }


            //Rel move & rotation
        } else if (relativeMoveAndRotation != null) {

            final NewTrackedEntity trackedEntity = tracked.get(relativeMoveAndRotation.getEntityId());

            if (trackedEntity == null) return;

            final double x = relativeMoveAndRotation.posX;
            final double y = relativeMoveAndRotation.posY;
            final double z = relativeMoveAndRotation.posZ;

            this.chicken = false;
            this.egg = false;

            final TransactionPacketAction pre = () -> {
                double newX = trackedEntity.serverPosX;
                double newY = trackedEntity.serverPosY;
                double newZ = trackedEntity.serverPosZ;

                newX += x;
                newY += y;
                newZ += z;

                trackedEntity.setConfirming(true);
                trackedEntity.setNextReach(new FlyingLocation(newX / 32.0D, newY / 32.0D, newZ / 32.0D));

                this.confirmReachTicks++;
                trackedEntity.trackedLocations++;
                this.chicken = true;
            };

            final TransactionPacketAction post = () -> {

                trackedEntity.serverPosX += x;
                trackedEntity.serverPosY += y;
                trackedEntity.serverPosZ += z;

                double var = 32.0D;

                for (final NewTrackedEntity.PossiblePosition reachPosition : trackedEntity.getPositions()) {
                    if (reachPosition.skip) {
                        reachPosition.skip = false;
                        continue;
                    }

                    reachPosition.setPositionAndRotation2(trackedEntity.serverPosX / var,
                            trackedEntity.serverPosY / var,
                            trackedEntity.serverPosZ / var, 0, 0);
                }


                trackedEntity.setNextReach(null);
                trackedEntity.setConfirming(false);

                this.confirmReachTicks++;
                this.egg = true;
            };


            if (this.shouldSpam(relativeMoveAndRotation.getEntityId())) {

                getData().getTickHolder().confirmFunctionAndTick(pre);
                getData().getTickHolder().confirmFunction(post);

                CompletableFuture.runAsync(() -> getData().getTickHolder().pushTick());

            } else {
                this.preQueue.add(pre);
                this.postQueue.add(post);
            }
        }
    }

    //run on post
    public void entityInterpolation(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interactEntity =
                    new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

            Entity entity = SpigotReflectionUtil.getEntityById(interactEntity.getEntityId());

            if (entity != null && this.lastEntityID != entity.getEntityId()) {
                this.spammedTicks = 0;
            }

            if (entity instanceof Player) {

                PlayerData playerData = Anticheat.INSTANCE.getUserManager()
                        .getUser(((Player) entity).getPlayer());

                if (playerData != null) {

                    if (this.tracked.containsKey(entity.getEntityId())) {

                        this.swing = false;

                        if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                            this.attack = true;
                            this.interact = false;
                            this.attackTick++;
                            this.lastAttack.reset();
                            this.target = this.tracked.get(interactEntity.getEntityId());
                        } else {
                            this.interact = true;
                            this.attack = false;
                        }

                        if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) {
                            if (interactEntity.getTarget().isPresent()) {
                                this.interactVector = new Vector(interactEntity.getTarget().get().getX(),
                                        interactEntity.getTarget().get().getY(), interactEntity.getTarget().get().getZ());
                                this.target = this.tracked.get(interactEntity.getEntityId());
                            }
                        }

                        if (this.lastEntityID != entity.getEntityId()) {
                            this.forceReset();
                            this.lastEntitySwitch.reset();
                        }

                        this.userTarget = this.tracked.get(getData().getPlayer().getEntityId());
                        this.targetNoReset = this.tracked.get(interactEntity.getEntityId());
                        this.entityTarget = entity;
                        this.lastEntityID = interactEntity.getEntityId();

                        if (this.target != null) {
                            this.targetTick = 0;
                        }
                    } else {
                 /*       Anticheat.INSTANCE.getPlugin().getServer().getConsoleSender().sendMessage(
                                Anticheat.INSTANCE.anticheatNameColor + ChatColor.RED
                                        + "players hits where canceled for not being found in the entity tracker. " +
                                        "(Player: " + getData().getUsername() + ")");*/

                        if (!Anticheat.INSTANCE.getConfigValues().isDisableBlock()) {
                            event.getPacketReceiveEvent().setCancelled(true);
                        }
                    }

                /*    if (this.target != null) {
                        if (target.getPositions() != null && target.getPositions().size() > 0) {
                            for (NewTrackedEntity.PossiblePosition possiblePosition : target.getPositions()) {
                                getData().debug("[attacked target position: " + possiblePosition.posX + " "
                                        + possiblePosition.posY + " " + possiblePosition.posZ);
                            }
                        }
                    }*/
                }
            }
        }

        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            ++this.timeSincePositionSet;

            if (this.shouldSpam(this.lastEntityID)) {
                this.spammedTicks++;
            } else {
                this.spammedTicks = 0;
            }

            this.lastLastFlyingPacket = this.lastFlyingPacket;
            this.lastFlyingPacket = this.flyingPacket;
            this.flyingPacket = flying;

            if (this.lastFlyingPacket == null || this.lastLastFlyingPacket == null) {
                return;
            }

            if (this.lastAttack.passedNoPing(30)) {
                this.attackTick = 0;
            }

            //fromFrom
            this.fromFrom.setYaw(this.to.getYaw());
            this.fromFrom.setPitch(this.to.getPitch());

            this.fromFrom.setPosX(this.to.getPosX());
            this.fromFrom.setPosY(this.to.getPosY());
            this.fromFrom.setPosZ(this.to.getPosZ());

            //from
            this.from.setYaw(this.to.getYaw());
            this.from.setPitch(this.to.getPitch());

            this.from.setPosX(this.to.getPosX());
            this.from.setPosY(this.to.getPosY());
            this.from.setPosZ(this.to.getPosZ());

            if (flying.hasRotationChanged()) {
                this.to.setYaw(flying.getLocation().getYaw());
                this.to.setPitch(flying.getLocation().getPitch());
            }

            if (flying.hasPositionChanged()) {
                this.to.setPosX(flying.getLocation().getX());
                this.to.setPosY(flying.getLocation().getY());
                this.to.setPosZ(flying.getLocation().getZ());
            }

            this.moving = flying.hasPositionChanged();

            this.run();

            for (NewTrackedEntity entity : this.tracked.values()) {

                final Set<NewTrackedEntity.PossiblePosition> newPositions = new HashSet<>(entity.getPositions());

                if (entity.isConfirming()) {

                    if (newPositions.size() > 4000) {
                        getData().kickPlayer("attempting to disable the reach check with lag (size over 4000)");
                        return;
                    }

                    for (NewTrackedEntity.PossiblePosition position : entity.getPositions()) {
                        final NewTrackedEntity.PossiblePosition rel = position.clone();

                        if (entity.getNextReach() == null) {
                            continue;
                        }

                        rel.setPositionAndRotation2(
                                entity.getNextReach().getPosX(),
                                entity.getNextReach().getPosY(),
                                entity.getNextReach().getPosZ(),
                                position.getRotationYaw(),
                                position.getRotationPitch()
                        );

                        rel.skip = true;

                        if (newPositions.size() > 3000) break;

                        newPositions.add(rel);
                        newPositions.add(position);
                    }
                }

                // run interpolation
                entity.update();

                // update
                entity.getPositions().clear();
                entity.getPositions().addAll(newPositions);
            }

            this.postTick();

            this.validBoxTicks -= Math.min(this.validBoxTicks, 1);
        }
    }

    public void run() {
        if (this.target != null) {
            this.target = null;

            AtomicInteger iterations = new AtomicInteger();

            ShitBox distance = new ShitBox(Double.MAX_VALUE);
            ShitBox distance003 = new ShitBox(Double.MAX_VALUE);

            //1.27, .4, .62 new values
            float[] eyeHeight = new float[]{1.62F, 1.54F};

           /* boolean possiblyOddEyeHeight = getData().getCollisionProcessor().getLiquidTicks() > 0
                    || getData().getCollisionProcessor().isDoor()
                    || getData().getCollisionProcessor().isHalfBlock()
                    || getData().getCollisionProcessor().getBlockAboveTimer().getDelta() < 20
                    || getData().getCollisionProcessor().getCollideHorizontalTicks() > 0;*/

            if (getData().getProtocolVersion() >= 393) {
                eyeHeight = new float[]{1.62F, 1.26999997616F, 0.6000000238418579F};
            } else if (getData().getProtocolVersion() > 47 && getData().getProtocolVersion() < 393) {
                eyeHeight = new float[]{1.62F, 1.54F, 0.6000000238418579F};
            }

            FlyingLocation location = this.from;

            HydroBB boundingBoxData = null;

            NewTrackedEntity.PossiblePosition gay = null;

            if (!this.tracked.containsKey(this.lastEntityID)) return;

            List<NewTrackedEntity.PossiblePosition> positions =
                    this.tracked.get(this.lastEntityID).getPositions();

            Vector playerHitVec = null;

            for (NewTrackedEntity.PossiblePosition position : new ConcurrentLinkedQueue<>(positions)) {

                if (position == null) continue;

                //fixes 1.7 as in 1.7 we could use TO location, but now we gotta use the previous in 1.8
                for (boolean usePrevious : new boolean[]{true, false}) {

                    //fixes the mod which players used to use to fix a bug where hits wouldn't register as good
                    for (boolean modFix : new boolean[]{true, false}) {

                        for (float eye : eyeHeight) {

                            // Fast math shit
                            for (int fastMath = 0; fastMath <= 2; fastMath++) {

                                HydroBB enemyBB = position.getEntityBoundingBox()
                                        .clone().expand(0.1F, 0.1F, 0.1F);

                                if (enemyBB == null)
                                    return;

                                // honestly dont think this is needed but just incase...
                                enemyBB.expand(this.FAST_MATH_ERROR, this.FAST_MATH_ERROR, this.FAST_MATH_ERROR);

                                //expand by 0.03 or there will be falses
                                double expand = getData().getProtocolVersion() > 47
                                        || Anticheat.INSTANCE.serverVersion > 18
                                        ? 0.06D : 0.03D;

                                enemyBB = enemyBB.expand(expand, expand, expand);

                                //Brute force from and to because 1.8+ uses FROM position, and 1.7 uses the TO position
                                float yaw = modFix ? from.getYaw() : usePrevious ? this.from.getYaw()
                                        : this.to.getYaw();
                                float pitch = modFix ? to.getPitch() : usePrevious ? this.from.getPitch()
                                        : this.to.getPitch();

                                // do the gay ray tracing shit
                                Vec3 eyeRot = MinecraftMath.getVectorForRotation(getData(), yaw, pitch, fastMath);

                                Vec3 eyePosition = new Vec3(
                                        location.getPosX(), location.getPosY() + eye, location.getPosZ()
                                );

                                Vec3 scaledEyeDir = eyePosition.addVector(
                                        eyeRot.xCoord * 6.0D, eyeRot.yCoord * 6.0D, eyeRot.zCoord * 6.0D
                                );

                                if (scaledEyeDir == null) {
                                    return;
                                }

                                if (enemyBB.isVecInside(eyePosition)) {
                                    return;
                                }

                                HydroMovingPosition objectMouseOver = enemyBB.calculateIntercept(
                                        eyePosition, scaledEyeDir
                                );

                                // check for bb intercept
                                if (objectMouseOver != null) {

                                    if (objectMouseOver.hitVec == null) return;

                                    // there is interception
                                    // get the distance
                                    // if smaller, use this one
                                    double calculated = objectMouseOver.hitVec.distanceTo(eyePosition);


                                    if (calculated < distance.get()) {
                                        distance.set(calculated);

                                        playerHitVec = new Vector(
                                                objectMouseOver.hitVec.xCoord,
                                                objectMouseOver.hitVec.yCoord,
                                                objectMouseOver.hitVec.zCoord);

                                        boundingBoxData = enemyBB;
                                        gay = position;
                                    }
                                } else if (positions.size() > 16) {
                                    this.tracked.get(this.lastEntityID).getPositions().remove(position);
                                }

                                iterations.incrementAndGet();
                            }
                        }
                    }
                }
            }

            if (this.spammedTicks > 20) {

                if (playerHitVec != null && distance.get() != Double.MAX_VALUE) {
                    this.hitVector = playerHitVec;
                }

                getData().setReachDistance(distance.get());
                getData().setValidHitbox(distance.get() != Double.MAX_VALUE);

                // can't find a distance? hitbox
                if (distance.get() == Double.MAX_VALUE) {

                    this.validBoxTicks = 6;

                    ReachData reachData = new ReachData(distance.get(), distance003.get(), false,
                            this.attack, this.interact, getData());
                    Collection<Check> checkData = getData().getCachedChecks();

                    for (Check check : checkData) {

                        if (check == null) continue;

                        if (check.isEnabled()) {
                            check.onReach(reachData);
                        }
                    }
                }


                if (distance.get() != Double.MAX_VALUE) {

                    //set reach here
                    this.distance = distance.get();

                    if (boundingBoxData != null) {
                        this.targetBBData = boundingBoxData;
                    }

                    if (gay != null) {
                        this.possiblePosition = gay;
                        this.timeSincePositionSet = 0;
                    }

                    ReachData reachData = new ReachData(distance.get(), distance003.get(),
                            true, this.attack, this.interact, getData());

                    Collection<Check> checkData = getData().getCachedChecks();

                    for (Check check : checkData) {

                        if (check == null) continue;

                        if (check.isEnabled() && positions.size() < 3) {
                            check.onReach(reachData);
                        }
                    }
                }
            }

            this.ranTimes++;
            this.targetTick++;
        }
    }

    // This will confirm pre and post on the join/player spawn.
    // Should help with any false's.
    private void confirmPrePost(Runnable start, Runnable end) {
        TransactionPacketAction pre = start::run;
        TransactionPacketAction post = end::run;

        getData().getTickHolder().confirmFunctionAndTick(pre);
        getData().getTickHolder().confirmFunction(post);
        CompletableFuture.runAsync(() -> getData().getTickHolder().pushTick());
    }

    public boolean shouldSpam(int id) {
        return this.lastEntityID != 0 && this.lastEntityID == id;
    }

    private void postTick() {
        this.accuratePosition = this.moving;
    }

    public void forceReset() {

        for (Map.Entry<Integer, NewTrackedEntity> entry : this.tracked.entrySet()) {
            NewTrackedEntity e = entry.getValue();
            e.trackedLocations = 0;
            e.teleports = 0;
        }

        this.trackTicks = 0;
    }
}