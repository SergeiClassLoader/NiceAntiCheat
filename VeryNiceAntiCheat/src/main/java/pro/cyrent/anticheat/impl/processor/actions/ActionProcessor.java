package pro.cyrent.anticheat.impl.processor.actions;


import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.impl.misc.netanalysis.ConnectionK;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.event.EventTimer;
import pro.cyrent.anticheat.util.evicting.EvictingList;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.Enchantment;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import pro.cyrent.anticheat.util.minecraftmaths.MinecraftMath;
import pro.cyrent.anticheat.util.nms.WatchableIndexUtil;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

@Getter
@Setter
public class ActionProcessor extends Event {
    private final PlayerData data;

    private final Deque<TeleportData> teleportDataList = new EvictingList<>(1000);
    private int teleportTicks;

    private int newTeleportTicks;

    private boolean sentTeleportOnce = false;

    private int lastPositionTick;

    private final EventTimer explosionTimer;
    private final EventTimer lastVehicleTimer, lastWalkSpeedTimer, lastInventoryOpenTimer, respawnTimer;

    private int lastTeleport;

    private double lastReportedExplosionMotion = 0, explosionVertical = 0;

    private FlyingLocation lastServerPositionLocation = null;

    private int actionsUntilConfirmed;
    private int lastLatencyPing;

    @Getter
    private final List<Vector3d> posLocs = new ArrayList<>();

    private int slotI = -1;
    private int digTick;

    private boolean sprinting = false, sentTeleport = false, sprintingServer = false, sneaking = false, lastSprinting = false;

    private int lastSprintTick, lastSneakTick;

    private int sneakTick;

    private float walkSpeed = 0.1F, lastWalkSpeed = 0.1F;

    private int gamemode = 0;

    private double failTimes, breakSpeedMultiplier = 1.0F;

    private boolean allowFlight = false;

    private boolean dead = false, previouslyDead = false;

    private int lastServerPositionTick, serverPositionTicks;

    private int lastAbility;

    private int badPackets;

    private int positionTick, confirmPositionTick;

    private int maxTeleportInvalids;

    private byte teleportMask;

    private boolean recentInvalidTeleport = false, possiblyTeleporting = false;

    private final List<Enchantment> enchantmentList = Collections.singletonList(Enchantment.builder()
            .type(EnchantmentTypes.BLAST_PROTECTION)
            .level(Integer.MAX_VALUE)
            .build());

    private com.github.retrooper.packetevents.protocol.world.Location fromLocation = null;

    private int sprintingConfirmed;

    private boolean isSwimming, lastSprintingConfirm;
    public final Deque<Vector> locations = new LinkedList<>();

    private boolean lastDead;

    private Queue<TeleportData> teleportDataQueue = new ConcurrentLinkedQueue<>();

    //new shit

    private final Deque<Long> ids = new LinkedList<>();
    private Long next, lastNext;

    public ActionProcessor(PlayerData user) {
        this.data = user;
        this.lastVehicleTimer = new EventTimer(20, user);
        this.lastWalkSpeedTimer = new EventTimer(20, user);
        this.lastInventoryOpenTimer = new EventTimer(20, user);
        this.respawnTimer = new EventTimer(20, user);
        this.explosionTimer = new EventTimer(20, user);
    }

    private void handleEquipment(Equipment equipment, ClientVersion clientVersion) {
        ItemStack itemStack = equipment.getItem();
        if (itemStack == null) return;

        if (itemStack.getAmount() > 1) {
            itemStack.setAmount(1);
            equipment.setItem(itemStack);
        }

        if (itemStack.isDamageableItem()) {
            itemStack.setDamageValue(0);
            itemStack.setLegacyData((short) 0);
            equipment.setItem(itemStack);
        }

        if (itemStack.isEnchanted(clientVersion)) {
            itemStack.setEnchantments(this.enchantmentList, clientVersion);
            equipment.setItem(itemStack);
        }
    }

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketSendEvent() != null) {
            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
                WrapperPlayServerEntityMetadata entityMetadata =
                        new WrapperPlayServerEntityMetadata(event.getPacketSendEvent());

                if (entityMetadata.getEntityId() == getData().getPlayer().getEntityId()) {

                    EntityData watchable = WatchableIndexUtil.getIndex(entityMetadata.getEntityMetadata(), 0);

                    if (watchable != null) {
                        Object zeroBitField = watchable.getValue();

                        if (zeroBitField instanceof Byte) {
                            byte field = (byte) zeroBitField;

                            boolean isSwimming = (field & 0x10) == 0x10;
                            boolean isSprinting = (field & 0x8) == 0x8;

                            getData().getTransactionProcessor().confirmPre(() -> {
                                this.isSwimming = isSwimming;
                                this.lastSprintingConfirm = isSprinting;
                                this.lastSprintTick = 0;
                                //        Bukkit.broadcastMessage("update swim");
                            });
                        }
                    }
                }
            }

            if (Anticheat.INSTANCE.getConfigValues().isAntiEquipment()) {
                if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.ENTITY_EQUIPMENT) {
                    WrapperPlayServerEntityEquipment packet = new
                            WrapperPlayServerEntityEquipment(event.getPacketSendEvent());


                    List<Equipment> equipmentList = packet.getEquipment();
                    if (equipmentList.isEmpty()) {
                        return;
                    }

                    // Prevents ESP's from seeing enchants & other shit.
                    for (Equipment equipment : equipmentList) {
                        handleEquipment(equipment, packet.getClientVersion());
                    }

                    packet.setEquipment(equipmentList);
                    event.getPacketSendEvent().markForReEncode(true);
                }
            }
        }

        if (event.getPacketReceiveEvent() != null) {

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
                WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event.getPacketReceiveEvent());

                if (digging.getAction() == DiggingAction.START_DIGGING
                        || digging.getAction() == DiggingAction.CANCELLED_DIGGING
                        || digging.getAction() == DiggingAction.FINISHED_DIGGING) {

                    if (digging.getBlockPosition() != null) {
                        this.digTick = 0;
                    }
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
                WrapperPlayClientTabComplete wrapper = new WrapperPlayClientTabComplete(event.getPacketReceiveEvent());
                String text = wrapper.getText();
                final int length = text.length();

                // general length limit
                if (length > 256) {
                    event.getPacketReceiveEvent().setCancelled(true);
                    getData().kickPlayer("Attempting to crash the server with an invalid tab complete.");
                    return;
                }

                // paper's patch
                final int index;
                if (text.length() > 64 && ((index = text.indexOf(' ')) == -1 || index >= 64)) {
                    event.getPacketReceiveEvent().setCancelled(true);
                    getData().kickPlayer("Attempting to crash the server with an invalid tab complete. [2]");
                    return;
                }
            }

            if (event.isMovement()) {
                WrapperPlayClientPlayerFlying playerFlying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

                if (this.lastSprinting != this.sprinting) {
                    this.lastSprintTick = 0;
                }

                //this.checkMovement = Anticheat.INSTANCE.getInstanceManager().getInstance().checkMovement(getData());

                this.positionTick++;
                this.confirmPositionTick++;

                boolean dead = getData().getPlayer().isDead();

                // hopefully fixes dumb lagback shit thingy when dying
                if (dead) {
                    getData().getActionProcessor().setLastServerPositionLocation(null);
                } else if (this.lastDead && playerFlying.hasPositionChanged()) {
                    getData().getActionProcessor().setLastServerPositionLocation(new
                            FlyingLocation(getData().getPlayer().getWorld().getName(),
                            getData().getMovementProcessor().getTo().getPosX(),
                            getData().getMovementProcessor().getTo().getPosY(),
                            getData().getMovementProcessor().getTo().getPosZ()));
                }

                if (playerFlying.hasPositionChanged()) {
                    this.lastDead = dead;
                }

                this.lastSprinting = this.sprinting;
                this.lastSprintTick++;
                this.lastSneakTick++;
                this.digTick++;

                if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 2) {
                    this.digTick = 20;
                }

                if (++this.lastAbility < 60 || this.getData().getMovementProcessor().getTick() < 20) {
                    GameMode gameMode = getData().getPlayer().getGameMode();

                    if (gameMode != null) {
                        if (gameMode != GameMode.SURVIVAL
                                && getData().getPlayer().getGameMode() != GameMode.ADVENTURE) {
                            if (gameMode == GameMode.CREATIVE) {
                                this.gamemode = 1;
                            } else {
                                this.gamemode = 3;
                            }
                        }
                    }

                    this.allowFlight = getData().getPlayer().getAllowFlight();
                }

                if (!this.sneaking) {
                    this.sneakTick -= Math.min(this.sneakTick, 1);
                } else {
                    if (this.sneakTick < 20) {
                        this.sneakTick++;
                    }
                }

                if (getData().getPlayer().getVehicle() != null || getData().getPlayer().isInsideVehicle()) {
                    this.lastVehicleTimer.reset();
                }

                if (playerFlying.hasPositionChanged()) {

                    if (playerFlying.hasRotationChanged()) {

                        if (!this.teleportDataQueue.isEmpty()) {

                            TeleportData teleportData = this.teleportDataQueue.poll();

                            if (teleportData != null) {

                                double distanceX = Math.abs(teleportData.x - playerFlying.getLocation().getX());
                                double distanceY = Math.abs(teleportData.y - playerFlying.getLocation().getY());
                                double distanceZ = Math.abs(teleportData.z - playerFlying.getLocation().getZ());

                                if (distanceX < 1E-7 && distanceY < 1E-7 && distanceZ < 1E-7) {
                                    this.possiblyTeleporting = true;
                                } else {
                                    this.possiblyTeleporting = false;
                                }
                            } else {
                                this.possiblyTeleporting = false;
                            }
                        } else {
                            this.possiblyTeleporting = false;
                        }
                    } else {
                        this.possiblyTeleporting = false;
                    }


                    this.lastServerPositionTick = 0;
                    this.serverPositionTicks++;

                    if (this.sentTeleport) {
                        int delta = Math.abs(this.positionTick - this.confirmPositionTick);

                        // this shouldn't run more then like 3 or 5 times at max, but we safe here.
                        if (++this.maxTeleportInvalids > 10 && delta > 10) {
                            // set back after this amount because their ping spoofing and desyncing.
                            getData().setBack();
                        }

                        if (this.positionTick < 20 && this.confirmPositionTick < 20) {
                            this.sentTeleport = false;
                            this.maxTeleportInvalids = 0;
                        }
                    }


                    boolean legacy = this.getData().getProtocolVersion() < 47;
                    boolean ground = !legacy && playerFlying.isOnGround();
                    boolean moving = playerFlying.hasPositionChanged();
                    boolean rotating = playerFlying.hasRotationChanged();
                    com.github.retrooper.packetevents.protocol.world.Location
                            location = playerFlying.getLocation();

                    if (!this.locations.isEmpty() && this.fromLocation != null) {
                        Vector position = new Vector(location.getX(), location.getY(), location.getZ());
                        Vector previousPosition = new Vector(this.fromLocation.getX(),
                                this.fromLocation.getY(),
                                this.fromLocation.getZ());

                        for (Vector teleport : this.locations) {
                            double distance = teleport.distance(position);
                            double distancePrevious = teleport.distance(previousPosition);

                            if ((distancePrevious <= 1.0E-7 || distance <= 1.0E-7)
                                    && moving && rotating && !ground) {
                                this.newTeleportTicks = 0;
                                this.locations.poll();
                                break;
                            }
                        }
                    }

                    //  this.setPossiblyTeleporting(this.isTeleportingForReal());

                    this.teleportTicks++;

                    if (!this.teleportDataList.isEmpty() && this.fromLocation != null) {

                        for (TeleportData teleportData : this.teleportDataList) {

                            if (teleportData == null) continue;

                            double offsetX = Math.abs(teleportData.getX() - playerFlying.getLocation().getX());
                            double offsetY = Math.abs(teleportData.getY() - playerFlying.getLocation().getY());
                            double offsetZ = Math.abs(teleportData.getZ() - playerFlying.getLocation().getZ());

                            double offsetXT = Math.abs(teleportData.getX() - this.fromLocation.getX());
                            double offsetYT = Math.abs(teleportData.getY() - this.fromLocation.getY());
                            double offsetZT = Math.abs(teleportData.getZ() - this.fromLocation.getZ());

                            boolean above1_13 = getData().getProtocolVersion() > 404
                                    && getData().getCollisionWorldProcessor().getBlockAboveTimer().getDelta() < 20;
                            double maxYOffset = above1_13 ? 0.5 : 0.03D;

                            boolean invalid = offsetXT <= 0.03 && offsetYT <= maxYOffset && offsetZT <= 0.03 ||
                                    offsetX <= 0.03 && offsetY <= maxYOffset && offsetZ <= 0.03;

                            if (invalid) {
                                this.teleportTicks = 0;
                                this.teleportDataList.remove(teleportData);
                                break;
                            }
                        }
                    }

                    this.fromLocation = playerFlying.getLocation();
                }

                if (playerFlying.hasPositionChanged()) {

                    if (isTeleportingV3()
                            && (getData().getCollisionWorldProcessor().getCollideHorizontalTicks() > 0
                            || getData().getCollisionWorldProcessor().isGround()
                            || getData().getMovementProcessor().getClientWallCollision().getDelta() < 7)) {

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
                                    Anticheat.INSTANCE.getInstanceManager().getInstance()
                                            .sendBlockUpdate(getData(), xx, yy, zz);
                                }
                            }
                        }
                    }
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
                WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event.getPacketReceiveEvent());

                if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING) {
                    this.sprinting = true;
                    this.lastSprintTick = 0;
                }

                if (action.getAction() == WrapperPlayClientEntityAction.Action.STOP_SPRINTING) {
                    this.sprinting = false;
                    this.lastSprintTick = 0;
                }

                if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SNEAKING) {
                    this.sneaking = true;
                    this.lastSneakTick = 0;
                }

                if (action.getAction() == WrapperPlayClientEntityAction.Action.STOP_SNEAKING) {
                    this.sneaking = false;
                    this.lastSneakTick = 0;
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLIENT_STATUS) {
                WrapperPlayClientClientStatus status = new WrapperPlayClientClientStatus(event.getPacketReceiveEvent());

                if (status.getAction() == WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT) {
                    this.lastInventoryOpenTimer.reset();
                }
            }
        }

        if (event.getPacketSendEvent() != null) {

            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.EXPLOSION) {
                WrapperPlayServerExplosion explosion = new WrapperPlayServerExplosion(event.getPacketSendEvent());

                FlyingLocation to = getData().getMovementProcessor().getTo();
                Vector3d explosionPosition = explosion.getPosition();

                double xDist = Math.abs(explosionPosition.x - to.getPosX());
                double yDist = Math.abs(explosionPosition.y - to.getPosY());
                double zDist = Math.abs(explosionPosition.z - to.getPosZ());

                boolean closeDistance = xDist <= 4 || yDist <= 5 || zDist <= 4;

                // check distance
                if (closeDistance) {

                    // confirm explosion.
                    getData().getTransactionProcessor().confirmPre(() -> {
                        this.explosionTimer.reset();

                        double distance = explosion.getPlayerMotion().x
                                + explosion.getPlayerMotion().y
                                + explosion.getPlayerMotion().z *
                                (explosion.getStrength() * 2.0F);

                        this.explosionVertical = explosion.getPlayerMotion().getY() *
                                (explosion.getStrength() == 0 ? 1 : explosion.getStrength());
                        this.lastReportedExplosionMotion = distance;
                    });
                }
            }

            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
                WrapperPlayServerPlayerPositionAndLook playerPositionAndLook =
                        new WrapperPlayServerPlayerPositionAndLook(event.getPacketSendEvent());

                this.lastServerPositionLocation = new FlyingLocation(getData().getPlayer().getWorld().getName(),
                        playerPositionAndLook.getX(),
                        playerPositionAndLook.getY(), playerPositionAndLook.getZ(), playerPositionAndLook.getYaw(),
                        playerPositionAndLook.getPitch());

                this.teleportDataList.add(new TeleportData(
                        playerPositionAndLook.getX(),
                        playerPositionAndLook.getY(),
                        playerPositionAndLook.getZ(),
                        playerPositionAndLook.getYaw(),
                        playerPositionAndLook.getPitch(),
                        getData().getMovementProcessor().getTick()));

                // Set the location on the first teleport instead of the first flying position.
                if (!this.sentTeleportOnce) {
                    getData().getHorizontalProcessor().lastValidLocation =
                            this.getData().getMovementProcessor().getTo().clone();

                    getData().getHorizontalProcessor().lastValidLocation.
                            setWorld(getData().getPlayer().getWorld().getName());

                    getData().getHorizontalProcessor().teleportLocation =
                            getData().getHorizontalProcessor()
                                    .getLastValidLocation().clone();


                    this.sentTeleportOnce = true;
                }

                this.teleportDataQueue.add(new TeleportData(
                        playerPositionAndLook.getX(),
                        playerPositionAndLook.getY(),
                        playerPositionAndLook.getZ()
                        ,
                        playerPositionAndLook.getYaw(),
                        playerPositionAndLook.getPitch(),
                        getData().getMovementProcessor().getTick()));


                Vector3d pos = new Vector3d(playerPositionAndLook.getX(),
                        playerPositionAndLook.getY(), playerPositionAndLook.getZ());

                FlyingLocation flyingLocation = getData().getMovementProcessor().getTo();

                if (getData().getProtocolVersion() <= 47) {
                    if (playerPositionAndLook.isRelativeFlag(RelativeFlag.X)) {
                        pos = pos.add(new Vector3d(flyingLocation.getPosX(), 0.0, 0.0));
                    }

                    if (playerPositionAndLook.isRelativeFlag(RelativeFlag.Y)) {
                        pos = pos.add(new Vector3d(0.0, flyingLocation.getPosY(), 0.0));
                    }

                    if (playerPositionAndLook.isRelativeFlag(RelativeFlag.Z)) {
                        pos = pos.add(new Vector3d(0.0, 0.0, flyingLocation.getPosZ()));
                    }

                    playerPositionAndLook.setX(pos.getX());
                    playerPositionAndLook.setY(pos.getY());
                    playerPositionAndLook.setZ(pos.getZ());
                    playerPositionAndLook.setRelativeMask((byte) 0);
                }

                double x = pos.getX();
                double y = getData().getProtocolVersion() >= 47 ? pos.getY() : pos.getY() - 1.62F;
                double z = pos.getZ();

                getData().getTransactionProcessor().confirmPre(() -> {
                    this.confirmPositionTick = 0;
                    this.locations.add(new Vector(x, y, z));
                });



                this.sentTeleport = true;

                this.positionTick = 0;

                this.teleportMask = playerPositionAndLook.getRelativeFlags().getMask();

                this.lastServerPositionTick++;

                this.lastPositionTick = 0;
                this.serverPositionTicks = 0;
            }

            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.RESPAWN) {

                getData().getTransactionProcessor().confirmPre(this.respawnTimer::reset);
            }

            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.PLAYER_ABILITIES) {
                WrapperPlayServerPlayerAbilities abilities = new
                        WrapperPlayServerPlayerAbilities(event.getPacketSendEvent());

                getData().getTransactionProcessor().confirmPre(() -> {
                    this.allowFlight = abilities.isFlightAllowed();

                    this.lastWalkSpeed = this.walkSpeed;
                    this.walkSpeed = abilities.getFOVModifier();

                    if (this.lastWalkSpeed != walkSpeed) {
                        this.lastWalkSpeedTimer.reset();
                    }

                    this.lastAbility = 0;
                });
            }

            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.ATTACH_ENTITY) {
                WrapperPlayServerAttachEntity attachEntity = new WrapperPlayServerAttachEntity(event.getPacketSendEvent());

                if (attachEntity.getAttachedId() == getData().getPlayer().getEntityId()
                        && attachEntity.getHoldingId() != getData().getPlayer().getEntityId()) {
                    this.getLastVehicleTimer().reset();
                }
            }
        }

        if (event.getPacketSendEvent() != null) {

            ConnectionK connectionK = (ConnectionK) getData().getCheckManager().forClass(ConnectionK.class);

            if (connectionK != null && connectionK.isEnabled()) {

                // check that a server keep alive is sent.
                if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.KEEP_ALIVE) {
                    WrapperPlayServerKeepAlive keepAlive = new WrapperPlayServerKeepAlive(event.getPacketSendEvent());

                    // Allow only versions 1.8 - 1.9.4
                    if (getData().getProtocolVersion() >= 47
                            && getData().getProtocolVersion() < 210) {
                        // Add the ids
                        this.ids.add(keepAlive.getId());

                        // encode the id from the keep alive.
                        String encoded = Long.toString(keepAlive.getId());

                        // Send an invalid ResourcePack packet to the client
                        // This acts as another way of tracking the player's connection.
                        // This also is invalid, so it will never display for the user.
                        getData().getTransactionProcessor().sendPacket(new
                                WrapperPlayServerResourcePackSend(null,
                                "level://" + Math.random() + "/resources.zip",
                                encoded, false,
                                null
                        ));
                    }
                }
            }
        }

        if (event.getPacketReceiveEvent() != null) {

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.RESOURCE_PACK_STATUS) {
                WrapperPlayClientResourcePackStatus sts = new
                        WrapperPlayClientResourcePackStatus(event.getPacketReceiveEvent());

                ConnectionK connectionK = (ConnectionK) getData().getCheckManager().forClass(ConnectionK.class);

                if (connectionK != null && connectionK.isEnabled() && getData().getProtocolVersion() >= 47
                        && getData().getProtocolVersion() < 210) {

                    if (!isNumeric(sts.getHash())) {
                        return;
                    }

                    if (sts.getResult() != WrapperPlayClientResourcePackStatus.Result.FAILED_DOWNLOAD) {
                        return;
                    }

                    if (ids.isEmpty() && getData().getMovementProcessor().getTick() > 120) {
                        return;
                    }

                    final long var = Long.decode(sts.getHash());

                    if (!ids.contains(var)) {
                        return;
                    }

                    long id = ids.poll();

                    if (id != var && ids.contains(var)) {
                        while (id != var && !ids.isEmpty()) {
                            id = ids.poll();
                        }
                    } else {
                        next = var;
                        ids.remove(var);
                    }
                }
            } else if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
                WrapperPlayClientKeepAlive ka =
                        new WrapperPlayClientKeepAlive(event.getPacketReceiveEvent());

                ConnectionK connectionK = (ConnectionK) getData().getCheckManager().forClass(ConnectionK.class);

                if (connectionK != null && connectionK.isEnabled() && getData().getProtocolVersion() >= 47
                        && getData().getProtocolVersion() < 210) {

                    if (next == null) {
                        return;
                    }

                    if (next == ka.getId()) {
                        // Just to prevent any false's as I've seen random occurrences.
                        this.failTimes -= Math.min(this.failTimes, 0.1);
                    } else if (getData().getMovementProcessor().getTick() > 250
                            && getData().getProtocolVersion() < 210) {


                        // These ID is sent out by one of the BadPackets checks, so we exempt for it.
                        // If we don't it false.
                        if (next == -723743 || ka.getId() == -723743) return;

                        // Used the stored last next ID and if they are equals it means their game is like paused
                        // Or lagging this fixes that problem, and does lower its detectability at all.
                        if (next.equals(this.lastNext)) {
                            return;
                        }

                        // 5 threshold just to be safe.
                        if (++this.failTimes > 5.0) {
                            connectionK.fail("Invalid keep-alive desync with the server packet threads",
                                    "id=" + ka.getId() + ", next-id=" + this.next + ", last-next-id="+this.lastNext);
                        }
                    }

                    this.lastNext = next;
                }
            }
        }
    }

    public void handlePostFlying() {
        ++this.newTeleportTicks;
    }

    private static Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    public boolean isTeleportingForReal() {
        return this.newTeleportTicks == 0;
    }


    public boolean isDigging() {
        return this.digTick < 20;
    }

    public void updateAttributes(int entityID, List<WrapperPlayServerUpdateAttributes.Property> objects) {
        if (entityID == getData().getPlayer().getEntityId()) {
            for (WrapperPlayServerUpdateAttributes.Property snapshotWrapper : objects) {
                final String key = snapshotWrapper.getKey();
                // Attribute limits defined by https://minecraft.wiki/w/Attribute
                // These seem to be clamped on the client, but not the server
                switch (key) {
                    case "minecraft:player.block_break_speed":
                        setBreakSpeedMultiplier(MinecraftMath.clamp(snapshotWrapper.getValue(), 0, 1024));
                        break;
                }
            }
        }
    }

    public boolean isTeleportingReal() {
        return this.isTeleportingForReal();
    }

    public boolean isTeleporting() {
        return this.isPossiblyTeleporting();
    }

    public boolean isTeleportingV2() {
        return this.teleportTicks <= 0;
    }

    public boolean isTeleportingV3() {
        return this.teleportTicks <= 1;
    }

    @Getter @AllArgsConstructor
    public static final class TeleportData {
        private final double x;
        private final double y;
        private final double z;
        private final float yaw;
        private final float pitch;
        private final int tick;
    }
}
