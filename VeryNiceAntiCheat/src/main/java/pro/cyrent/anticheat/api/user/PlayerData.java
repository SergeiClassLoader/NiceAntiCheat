package pro.cyrent.anticheat.api.user;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.potion.Potion;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.Check;
import pro.cyrent.anticheat.api.check.CheckManager;
import pro.cyrent.anticheat.api.check.impl.combat.entity.utils.EntityHelper1_8;
import pro.cyrent.anticheat.api.check.impl.combat.velocity.VelocityA;
import pro.cyrent.anticheat.api.check.impl.movement.fly.FlyA;
import pro.cyrent.anticheat.api.check.impl.movement.invalidmove.InvalidMoveG;
import pro.cyrent.anticheat.api.command.commands.sub.LogsCommand;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.event.EventBus;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.impl.processor.actions.*;
import pro.cyrent.anticheat.impl.processor.basic.CustomPayloadProcessor;
import pro.cyrent.anticheat.impl.processor.basic.MovementProcessor;
import pro.cyrent.anticheat.impl.processor.combat.*;
import pro.cyrent.anticheat.impl.processor.combat.*;
import pro.cyrent.anticheat.impl.processor.connection.TransactionProcessor;
import pro.cyrent.anticheat.impl.processor.connection.backtrack.BackTrackProcessor;
import pro.cyrent.anticheat.impl.processor.connection.backtrack.ConnectionProcessor;
import pro.cyrent.anticheat.impl.processor.connection.tracker.ReachProcessorTest;
import pro.cyrent.anticheat.impl.processor.debug.PacketDebugProcessor;
import pro.cyrent.anticheat.impl.processor.entity.EntityTrackingProcessor;
import pro.cyrent.anticheat.impl.processor.fixes.DesyncProcessor;
import pro.cyrent.anticheat.impl.processor.fixes.GhostBlockProcessor;
import pro.cyrent.anticheat.impl.processor.fixes.NettyProcessor;
import pro.cyrent.anticheat.impl.processor.prediction.horizontal.HorizontalProcessor;
import pro.cyrent.anticheat.impl.processor.prediction.movement.MovementPredictionProcessor;
import pro.cyrent.anticheat.impl.processor.prediction.noslowdown.NoSlowDownProcessor;
import pro.cyrent.anticheat.impl.processor.world.*;
import pro.cyrent.anticheat.util.auth.HTTPUtil;
import pro.cyrent.anticheat.util.block.box.BoundingBox;
import pro.cyrent.anticheat.util.bukkit.RunUtils;
import pro.cyrent.anticheat.util.config.ConfigValues;
import pro.cyrent.anticheat.util.event.EventTimer;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.math.MathUtil;
import pro.cyrent.anticheat.util.stats.StatsUtil;
import pro.cyrent.anticheat.util.task.TransactionTickHolder;
import pro.cyrent.anticheat.util.thread.Thread;
import pro.cyrent.anticheat.util.time.TimeUtils;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Vector3d;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@Setter
public class PlayerData {
    private final UUID uuid;
    private Player player;
    private final String username;
    private final long timestamp = System.currentTimeMillis();
    private final EventBus eventBus = new EventBus();
    private CheckManager checkManager;

    private int sneakCollideTicks;
    private int airBelowSneakTicks;

    private int lastRotate;

    private PlayerData forcedUser = null;
    private int serverTick, lastServerTick;

    private Optional<Vector3d> reachVector = Optional.empty();

    private int lastTransaction, lastFlying;

    private final EventTimer lastMovementCancelTimer = new EventTimer(20, this);

    private int setBackTicks;
    private boolean ignoreSetback = false;

    private PlayerData targetMonitorUser;
    private boolean onlineOnly = false;

    private long lastTimerTime;

    public List<LogsCommand.WebResultTop> results;

    public int pageSize = 45; // Number of players per page
    public int currentPage = 0; // Current page

    private boolean misplace = false, enderDragon;
    private int punchPower;
    private boolean usedPunch;
    private int lastAttackedBot;
    private long lastFlag;

    private boolean didBotCommand = false;
    private boolean bedrock = false;

    private boolean lookingAtBot, botRunning = false;
    public EntityHelper1_8 entityHelper1_8;
    private final List<Integer> currentSpawnedEntites = new CopyOnWriteArrayList<>();

    private boolean bypass = false;

    private FlyingLocation validLocation = new FlyingLocation();
    private int lastFailedLocationTick;

    //set bounding box for player blocks
    private BoundingBox boundingBox = new BoundingBox(0, 0, 0, 0, 0, 0);
    private BoundingBox lastBoundingBox = new BoundingBox(0, 0, 0, 0, 0, 0);

    //bukkitlistener
    private Location lastBlockBreakLocation;

    private Block blockPlaced, lastBlockPlaced, blockAgainst;
    private BlockPlaceEvent lastBlockPlaceEvent, lastLastBlockPlace;

    private boolean validateFirstTick = false;

    private BukkitRunnable cpsMonitorRunnable;

    private int ticksSinceBow;
    private int ticksSinceEat;

    private int satiatedServerTick;
    private int regenServerTick;

    //updated
    private final EventTimer pistonUpdateTimer = new EventTimer(20, this);
    private final EventTimer lastWorldChange = new EventTimer(20, this);
    private final EventTimer lastBowBoostTimer = new EventTimer(20, this);
    private final EventTimer lastProjectileDamage = new EventTimer(20, this);
    private final EventTimer lastTeleport = new EventTimer(20, this);
    private final EventTimer lastEnderPearl = new EventTimer(20, this);
    private final EventTimer fishingRodTimer = new EventTimer(20, this);
    private final EventTimer lastCactusDamageTimer = new EventTimer(20, this);
    private final EventTimer WitherTimer = new EventTimer(20, this);

    //old event timers
    private EventTimer lastBlockPlaceCancelTimer = new EventTimer(20, this),
            lastBlockPlaceTimer = new EventTimer(20, this),
            lastShotByArrowTimer = new EventTimer(20, this),
            lastBlockBreakTimer = new EventTimer(20, this),
            lastFallDamageTimer = new EventTimer(20, this),
            lastFireTickTimer = new EventTimer(20, this),
            lastSuffocationTimer = new EventTimer(20, this),
            lastExplosionTimer = new EventTimer(40, this),
            lastGotAttacked = new EventTimer(20, this);

    private final AtomicReference<Short> receivingFrame = new AtomicReference<>();


    //eventtimer for stitch check (bad packet i)
    private final EventTimer badPacketTimer = new EventTimer(20, this);

    private final List<ItemType> armorTypes = Arrays.asList(
            ItemTypes.LEATHER_CHESTPLATE,
            ItemTypes.LEATHER_LEGGINGS,
            ItemTypes.LEATHER_BOOTS,
            ItemTypes.LEATHER_HELMET,
            ItemTypes.CHAINMAIL_CHESTPLATE,
            ItemTypes.CHAINMAIL_LEGGINGS,
            ItemTypes.CHAINMAIL_BOOTS,
            ItemTypes.CHAINMAIL_HELMET,
            ItemTypes.IRON_CHESTPLATE,
            ItemTypes.IRON_LEGGINGS,
            ItemTypes.IRON_BOOTS,
            ItemTypes.IRON_HELMET,
            ItemTypes.GOLDEN_CHESTPLATE,
            ItemTypes.GOLDEN_LEGGINGS,
            ItemTypes.GOLDEN_BOOTS,
            ItemTypes.GOLDEN_HELMET,
            ItemTypes.DIAMOND_CHESTPLATE,
            ItemTypes.DIAMOND_LEGGINGS,
            ItemTypes.DIAMOND_BOOTS,
            ItemTypes.DIAMOND_HELMET
    );

    //login shit
    private String loginTime = "NULL";
    private long loginMilis;

    //GUI
    private String lastUIName = "NULL";
    public List<Check> nextPageChecks = new ArrayList<>();

    //check info
    private Check lastFlaggedCheck = null;
    private String checkName = "Forcefully banned", checkType = "Command";
    private double checkViolation = 0, checkPunishVL = 0;

    //logs
    private WeakHashMap<Check, Double> flaggedChecks = new WeakHashMap<>();

    private List<Check> recentlyFlagged = new CopyOnWriteArrayList<>();
    private List<LogsCommand.WebResult> sessionLogs = new CopyOnWriteArrayList<>();

    //crash command
    private PlayerData targetCrashPlayer;

    //transaction shit/reach shit
    private TransactionTickHolder tickHolder;


    //debug command
    private List<String> debugType = Arrays.asList("movement", "reach");
    private String debugSet = "null";
    private boolean debugMode = false;
    private PlayerData debuggedUser = null;

    private double reachDistance;
    private boolean validHitbox, reach = false;
    private String reachFighting = "null";

    //check stuff.
    public static String banMessage;
    private boolean banned = false;
    private boolean alerts = false, devAlerts = false;

    private boolean kicked = false;

    //via ver protocol id
    public int protocolVersion = -1;

    //idk
    private ClientVersion version;


    //processors
    private MovementProcessor movementProcessor;
    private TransactionProcessor transactionProcessor;
    private WorldProcessor worldProcessor;
    private ActionProcessor actionProcessor;
    private CollisionProcessor collisionProcessor;
    private TeleportProcessor teleportProcessor;
    private TestPotionProcessor potionProcessor;
    private VelocityProcessor velocityProcessor;
    private CombatProcessor combatProcessor;
    private DesyncProcessor desyncProcessor;
    private NettyProcessor nettyProcessor;
    private ReachProcessorTest reachProcessor;
    private BackTrackProcessor backTrackProcessor;
    private ConnectionProcessor connectionProcessor;
    private BlockProcessor blockProcessor;
    private GhostBlockProcessor ghostBlockProcessor;
    private PacketDebugProcessor packetDebugProcessor;
    private CustomPayloadProcessor customPayloadProcessor;
    private SensitivityProcessor sensitivityProcessor;
    private CinematicProcessor cinematicProcessor;
    private SetBackProcessor setBackProcessor;
    private EntityTrackingProcessor entityTrackingProcessor;
    private ScaffoldProcessor scaffoldProcessor;
    private MovementPredictionProcessor movementPredictionProcessor;
    private ClickProcessor clickProcessor;

    private CollisionWorldProcessor collisionWorldProcessor;

    private Thread thread;

    //run last.
    private HorizontalProcessor horizontalProcessor;
    private NoSlowDownProcessor noSlowDownProcessor;

    private Collection<Check> cachedChecks;

    public PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.player = player;
        this.username = player.getName();
        this.lastTimerTime = System.currentTimeMillis();

        StatsUtil.overallPlayersJoined++;

        this.thread = Anticheat.INSTANCE.getThreadManager().generate();

        if (this.thread == null) {
            this.kickPlayer("invalid thread when injecting the player: " + this.username);
            return;
        }

        //login logs shit
        this.loginMilis = System.currentTimeMillis();
        this.loginTime = Anticheat.INSTANCE.getCurrentDate();

        //reach and tranny
        this.tickHolder = new TransactionTickHolder(this);

        //reg version for chunk/world proccess
        this.version = PacketEvents.getAPI().getPlayerManager().getClientVersion(player);

        //register processors before checks
        this.registerProcessors();

        //run check manager last
        this.checkManager = new CheckManager(this);

        if (this.isDev(player)) {
            this.alerts = true;
        }

        //register entity checks.
        this.entityHelper1_8 = new EntityHelper1_8();

        if (isDev(this.player)) {
            RunUtils.taskLater(() -> {
                this.alerts = true;
                this.devAlerts = true;
                this.getPlayer().sendMessage("");
                this.getPlayer().sendMessage("");

                this.getPlayer().sendMessage(" Server is running "
                        + Anticheat.INSTANCE.getConfigValues().getPrefix()
                        + " v" + Anticheat.INSTANCE.getVersion());
                this.getPlayer().sendMessage(ChatColor.WHITE + " Developed by"
                        + ChatColor.GREEN + " Rhys, Demon, Incognito");

                this.getPlayer().sendMessage("");
                this.getPlayer().sendMessage("");
            }, Anticheat.INSTANCE.getPlugin(), 100L);
        }

        if (this.player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getAlert()) || this.player.isOp()) {
            this.alerts = true;
            this.getPlayer().sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                    + ChatColor.GREEN + " Alerts Enabled!");
        }

        if (Anticheat.INSTANCE.getConfigValues().isBedrockSupport()) {
            if (this.player.getUniqueId().getMostSignificantBits() == 0) {
                this.bedrock = true;
            }
        }

        //fixes a bug on join where yaw isn't set.
        if (this.player.getLocation() != null) {
            this.horizontalProcessor.setYaw(this.player.getLocation().getYaw());
        }

        this.protocolVersion = Anticheat.INSTANCE.getVersionSupport().getClientProtocol(this);

        this.cachedChecks = this.checkManager.getChecks().values();
    }

    public void updatePlayerInstance() {
        if(((CraftPlayer)player).getHandle().playerConnection == null) {
            player = Bukkit.getPlayer(player.getUniqueId());
        }
    }

    public void setBack() {
        if (!Anticheat.INSTANCE.getConfigValues().isGhostBlockSupport()) {
            this.setBackProcessor.setSetBackTick(20);
           // this.debug("called normal set backs.");
        }
    }

    public boolean isSplashPotion(ItemStack itemStack) {
        if (itemStack.getType() == Material.POTION) {

            Potion potion = Potion.fromItemStack(itemStack);
            return potion != null && potion.isSplash();
        }

        return false;
    }

    public void specialSetBack() {
        this.setBackProcessor.setSetBackTick(20);
      //  this.debug("called the special set back option.");
    }

    private void registerProcessors() {
        this.eventBus.getEvents().add(this.nettyProcessor = new NettyProcessor(this));
        this.eventBus.getEvents().add(this.desyncProcessor = new DesyncProcessor(this));
        this.eventBus.getEvents().add(this.movementProcessor = new MovementProcessor(this));
        this.eventBus.getEvents().add(this.transactionProcessor = new TransactionProcessor(this));
        this.eventBus.getEvents().add(this.actionProcessor = new ActionProcessor(this));
        this.eventBus.getEvents().add(this.teleportProcessor = new TeleportProcessor(this));
        this.eventBus.getEvents().add(this.entityTrackingProcessor = new EntityTrackingProcessor(this));
        this.eventBus.getEvents().add(this.collisionProcessor = new CollisionProcessor(this));
        this.eventBus.getEvents().add(this.collisionWorldProcessor = new CollisionWorldProcessor(this));
        this.eventBus.getEvents().add(this.potionProcessor = new TestPotionProcessor(this));
        this.eventBus.getEvents().add(this.velocityProcessor = new VelocityProcessor(this));
        this.eventBus.getEvents().add(this.combatProcessor = new CombatProcessor(this));
        this.eventBus.getEvents().add(this.reachProcessor = new ReachProcessorTest(this));

        this.eventBus.getEvents().add(this.backTrackProcessor = new BackTrackProcessor(this));

        this.eventBus.getEvents().add(this.connectionProcessor = new ConnectionProcessor(this));
        this.eventBus.getEvents().add(this.blockProcessor = new BlockProcessor(this));
        this.eventBus.getEvents().add(this.ghostBlockProcessor = new GhostBlockProcessor(this));
        this.eventBus.getEvents().add(this.packetDebugProcessor = new PacketDebugProcessor(this));
        this.eventBus.getEvents().add(this.customPayloadProcessor = new CustomPayloadProcessor(this));
        this.eventBus.getEvents().add(this.sensitivityProcessor = new SensitivityProcessor(this));
        this.eventBus.getEvents().add(this.cinematicProcessor = new CinematicProcessor(this));
        this.eventBus.getEvents().add(this.setBackProcessor = new SetBackProcessor(this));
        this.eventBus.getEvents().add(this.scaffoldProcessor = new ScaffoldProcessor(this));
        this.eventBus.getEvents().add(this.movementPredictionProcessor = new MovementPredictionProcessor(this));
        this.eventBus.getEvents().add(this.clickProcessor = new ClickProcessor(this));

        this.eventBus.getEvents().add(this.horizontalProcessor = new HorizontalProcessor(this));


        //run after horizontal
        this.eventBus.getEvents().add(this.noSlowDownProcessor = new NoSlowDownProcessor(this));
    }

    public void handlePacketEvent(PacketEvent.Direction direction,
                                  PacketTypeCommon typeCommon,
                                  PacketReceiveEvent receiveEvent,
                                  PacketSendEvent packetSendEvent,
                                  com.github.retrooper.packetevents.event.PacketEvent packetEvent,
                                  boolean threaded) {

        PacketEvent globalEvent = new PacketEvent(this, packetEvent, typeCommon, receiveEvent,
                packetSendEvent, direction);

        if (!threaded) {
            //prevent crashers
            this.nettyProcessor.run(globalEvent);


            this.packetDebugProcessor.handlePacketDebugger(globalEvent);

            // if you add 1.17 double packet fix
            // do it here and return here if it's a movement packet, don't do it per check
            // Todo: add desync fix here.

            //Fixes 1.17 & newer clients when they right click which sends a packet breaking critical movement data used.
            //run before everything
            this.desyncProcessor.run(globalEvent);

            if (globalEvent.getPacketReceiveEvent() != null && globalEvent.isMovement()
                    && this.desyncProcessor != null && this.desyncProcessor.isInvalid()) {
                return;
            }

            this.backTrackProcessor.onPacketPre(globalEvent);

            //check for received transactions before everything
            if (globalEvent.getPacketReceiveEvent() != null) {

                if (globalEvent.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
                    WrapperPlayClientWindowConfirmation clientWindowConfirmation =
                            new WrapperPlayClientWindowConfirmation(globalEvent.getPacketReceiveEvent());

                    short id = clientWindowConfirmation.getActionId();

                    //Check and confirm all transactions.

                    this.getTickHolder().handlePacketAtPre(packetEvent.getTimestamp(), id);
                    this.reachProcessor.onTransaction(id);
                }
            }

            this.reachProcessor.entityInterpolation(globalEvent);

            // run all pre-processors first
            for (Event event : this.eventBus.getEvents()) {
                event.onPacket(globalEvent); //425
            }

            if (globalEvent.getPacketReceiveEvent() != null) {

                if (globalEvent.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
                    WrapperPlayClientWindowConfirmation clientWindowConfirmation =
                            new WrapperPlayClientWindowConfirmation(globalEvent.getPacketReceiveEvent());

                    short id = clientWindowConfirmation.getActionId();

                    this.getTickHolder().handlePacketAtPreVel(packetEvent.getTimestamp(), id);
                    this.getHorizontalProcessor().handleTransaction();

                    getCachedChecks().forEach(info -> info.onClientTransaction(this,
                            globalEvent.getTimestamp()));
                }
            }

            //run post processor
            this.getCombatProcessor().postFlying(globalEvent);

            // then run all checks
            // NOTE: Don't use foreach, this is a bit lighter on the cpu
            for (Check value : this.cachedChecks) {
                if (value.isEnabled()) {
                    value.onPacket(globalEvent);
                }
            }

            if (globalEvent.getPacketReceiveEvent() != null && globalEvent.isMovement()) {
                //run post check
                FlyA flyA1 = (FlyA) getCheckManager().forClass(FlyA.class);

                if (flyA1 != null && flyA1.isEnabled()) {
                    flyA1.onPost(globalEvent);
                }
            }

            InvalidMoveG invalidMoveG = (InvalidMoveG) getCheckManager().forClass(InvalidMoveG.class);

            if (invalidMoveG != null && invalidMoveG.isEnabled()) {
                invalidMoveG.onPost(globalEvent);
            }


            //post horizontal engine (flying)
            this.horizontalProcessor.onPost(globalEvent);
            //      this.simulationEngine.onPost(globalEvent);


            if (globalEvent.getPacketReceiveEvent() != null) {
                if (this.actionProcessor != null && globalEvent.isMovement()) {
                    this.actionProcessor.handlePostFlying();
                }
            }
        }
    }

    public void consoleLogLagBack(String checkNameType) {
        if (!Anticheat.INSTANCE.getConfigValues().isGhostBlockSupport()) {
            Anticheat.INSTANCE.getPlugin().getServer().getConsoleSender().sendMessage(
                    Anticheat.INSTANCE.anticheatNameColor + ChatColor.RED + " teleporting the player "
                            + ChatColor.GOLD + this.username + ChatColor.RED
                            + " to their last previous valid location for " + ChatColor.GOLD +
                            "Placing Ghost Blocks while flagging " + checkNameType);
        }

    }

    public boolean generalCancelLessAbuse() {
        return getMovementProcessor().getTick() < 10
                || !getCollisionProcessor().isChunkLoaded()
                || Anticheat.INSTANCE.isServerLagging()
                || this.player.isDead()
                || Anticheat.INSTANCE.getLastServerLagTick() > 0;
    }

    public boolean generalCancel() {

        return getMovementProcessor().getPositionTicks() <= 1
                || !getCollisionProcessor().isChunkLoaded()
                || Anticheat.INSTANCE.isServerLagging()
                || this.player.isDead()
                || getMovementProcessor().getLastFlightTimer().getDelta() < 10
                && getMovementProcessor().getLastFlightTimer().isSet()
                || Anticheat.INSTANCE.getLastServerLagTick() > 0;
    }

    public void closeInventory() {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.closeInventory();
            }
        }.runTask(Anticheat.INSTANCE.getPlugin());
    }

    public boolean isSword(ItemStack itemStack) {
        return itemStack.getType() == Material.WOOD_SWORD
                || itemStack.getType() == Material.STONE_SWORD
                || itemStack.getType() == Material.GOLD_SWORD
                || itemStack.getType() == Material.IRON_SWORD
                || itemStack.getType() == Material.DIAMOND_SWORD;
    }


    public boolean isUsableItem(ItemStack itemStack) {
        return //isSword(itemStack)
                itemStack.getType() == Material.MILK_BUCKET ||
                        isFood(itemStack) && this.player.getFoodLevel() != 20
                        || isGoldenApple(itemStack) || canPullBackBow(itemStack);
        //    || isDrinkablePotion(itemStack);
    }

    public boolean canPullBackBow(ItemStack bow) {

        if (getPlayer().getInventory() == null
                || getPlayer().getInventory().getContents() == null) return false;

        if (isBow(bow)) {

            ItemStack[] inventoryContents = getPlayer().getInventory().getContents();

            for (ItemStack itemStack : inventoryContents) {

                if (itemStack != null && itemStack.getType() == Material.ARROW) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isGoldenApple(ItemStack itemStack) {
        if (itemStack.getType() == Material.GOLDEN_APPLE) {
            return true;
        }

        return false;
    }

    public boolean isFood(ItemStack itemStack) {
        Material itemType = itemStack.getType();

        // Check if the item is one of the consumable foods
        if (itemType == Material.COOKED_BEEF
                || itemType == Material.COOKED_CHICKEN
                || itemType == Material.COOKED_FISH
                || itemType == Material.COOKED_MUTTON
                || itemType == Material.COOKED_RABBIT
                || itemType == Material.COOKIE
                || itemType == Material.GRILLED_PORK
                || itemType == Material.MELON
                || itemType == Material.MUSHROOM_SOUP
                || itemType == Material.POISONOUS_POTATO
                || itemType == Material.POTATO
                || itemType == Material.PUMPKIN_PIE
                || itemType == Material.RAW_BEEF
                || itemType == Material.RAW_CHICKEN
                || itemType == Material.RAW_FISH
                || itemType == Material.MUTTON
                || itemType == Material.PORK
                || itemType == Material.RABBIT
                || itemType == Material.APPLE
                || itemType == Material.BAKED_POTATO
                || itemType == Material.BREAD
                || itemType == Material.CARROT
                || itemType == Material.GOLDEN_CARROT) {
            return true;
        }

        return false;
    }

    public boolean isBow(ItemStack itemStack) {
        if (itemStack.getType() == Material.BOW) {
            return true;
        }

        return false;
    }

    public void sendDevAlert(String checkInfo, String... data) {
        StringBuilder stringBuilder = new StringBuilder(ChatColor.RED + "Flag information:");

        // Fetch configuration values once
        ConfigValues configValues = Anticheat.INSTANCE.getConfigValues();
        String primary = configValues.getPrimaryColorHover();
        String secondary = configValues.getSecondaryColorHover();
        String teleport = configValues.getTeleportTextColor();
        String prefix = configValues.getPrefix();
        String alertCommandString = configValues.getAlertCommandString();

        // Build the data string
        for (String s : data) {
            String fixed = primary + " - " + secondary + s;
            stringBuilder.append(fixed).append(",\n");
        }
        String alertData = stringBuilder.toString().trim();

        // Build the alert message once
        String alert = prefix + ChatColor.YELLOW + " " + this.username + ChatColor.RED + " recent actions have been canceled for "
                + ChatColor.AQUA + checkInfo;
        TextComponent textComponent = new TextComponent(alert);

        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(alertData + "\n\n" + teleport + "(Click to execute command)").create()));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                alertCommandString.replace("%PLAYER%", this.getUsername())));

        Map<UUID, PlayerData> userMap = Anticheat.INSTANCE.getUserManager().getUserMap();
        for (Map.Entry<UUID, PlayerData> entry : userMap.entrySet()) {
            PlayerData uuidUserEntry = entry.getValue();

            if ((uuidUserEntry.isDev(this.player)
                    || uuidUserEntry.getPlayer().hasPermission(Anticheat.INSTANCE.getPermissionValues().getDevAlerts()))
                    && uuidUserEntry.isDevAlerts()) {
                uuidUserEntry.getPlayer().spigot().sendMessage(textComponent);
            }
        }
    }

    public void kickPlayer(String consoleMessage) {

        if (generalCancel()) return;

        if (Anticheat.INSTANCE.getTpsMonitor().tps1Sec() <= 19.0
                || Anticheat.INSTANCE.getTpsMonitor().tps10Sec() <= 19.0
                || Anticheat.INSTANCE.getTpsMonitor().tps5Sec() <= 19.0
                || Anticheat.INSTANCE.getTpsMonitor().tps1Min() <= 19.0
                || Anticheat.INSTANCE.getTpsMonitor().tps5Min() <= 19.0
                || Anticheat.INSTANCE.getTpsMonitor().tps15Min() <= 19.0) {
            return;
        }

        if (this.kicked) return;
        this.kicked = true;


        RunUtils.task(() -> {
            Anticheat.INSTANCE.getPlugin().getServer().getConsoleSender().sendMessage(
                    Anticheat.INSTANCE.getConfigValues().getPrefix()
                            + " " + ChatColor.RED
                            + "Kicking " + ChatColor.GOLD + username + ChatColor.RED + " for " + consoleMessage);

            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(this.getUuid().toString())) {
                Anticheat.INSTANCE.getPacketLogList().add(
                        "[" + TimeUtils.getSystemTime2() + "] " + consoleMessage + " -- " + "Kicked by anticheat\n\n\n"
                );
            }

            player.kickPlayer("Disconnected.");

            StatsUtil.kickAmount++;
        });
    }

    public void viaMcpCommand() {
        if (this.kicked) return;
        this.kicked = true;

        RunUtils.task(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Anticheat.INSTANCE.getConfigValues()
                .getViaCommand()
                .replace("%PLAYER%", getPlayer().getName())
                .replace("%PREFIX%", Anticheat.INSTANCE.getConfigValues().getPrefix())
                .replaceFirst("/", "")));
    }

    public void kickPlayerViaVersion(int id) {

        if (this.kicked) return;
        this.kicked = true;


        RunUtils.task(() -> {
            Anticheat.INSTANCE.getPlugin().getServer().getConsoleSender().sendMessage(Anticheat.INSTANCE.getConfigValues().getPrefix()
                    + " " + ChatColor.RED
                    + "Kicking " + ChatColor.GOLD + username + ChatColor.RED + " for joining before ViaVersion has fully hooked" +
                    "/unknown protocol version (kicking to prevent falses) (protocol id: " + id + ")");

            if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                    .getUniqueId().toString().equals(this.getUuid().toString())) {
                Anticheat.INSTANCE.getPacketLogList().add(
                        "[" + TimeUtils.getSystemTime2() + "] " + "Via Version Hook Issue" + " -- " + "Kicked by anticheat\n\n\n"
                );
            }

            player.kickPlayer("[ViaVersion] Couldn't verify your game protocol, please reconnect.");
        });
    }

    public boolean isDev(Player player) {

        //Incognito
        return player.getUniqueId().toString().equals("638ea077-6dd2-4386-8db8-0d96817939ae")
                || player.getUniqueId().toString().equals("638ea0776dd243868db80d96817939ae")

                //Dodged
                || player.getUniqueId().toString().equalsIgnoreCase("d57d4dfa67b14331b40cab2e03068e88")
                || player.getUniqueId().toString().equalsIgnoreCase("d57d4dfa-67b1-4331-b40c-ab2e03068e88")
                //Dodged
                || player.getUniqueId().toString().equalsIgnoreCase("285c25e374f647e081a64e74ceb54ed3")
                || player.getUniqueId().toString().equalsIgnoreCase("285c25e3-74f6-47e0-81a6-4e74ceb54ed3")

                //Alfie
                || player.getUniqueId().toString().equalsIgnoreCase("a4434163f43e4b1798f23b08ee4f5513")
                || player.getUniqueId().toString().equalsIgnoreCase("a4434163-f43e-4b17-98f2-3b08ee4f5513")

                || Anticheat.INSTANCE.getDevUsers().contains(player.getUniqueId());
        //Test
    }

    public void debug(String s) {
        getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "Info" + ChatColor.GRAY + "] "
                + ChatColor.WHITE + s);
    }

    public void debug(Object s) {
        if (this.isDev(this.player)) {
            getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "Info" + ChatColor.GRAY + "] "
                    + ChatColor.WHITE + s);
        }
    }

    public boolean isOptifineZooming() {
        return this.cinematicProcessor.isCinematic() || this.sensitivityProcessor.getOptifineTicks() > 0;
    }


    /**
     * before you say anything, yes i took this from sparky cuz i was lazy
     */

    public void startCPSMonitor() {
        this.cpsMonitorRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (targetMonitorUser == null || !targetMonitorUser.getPlayer().isOnline()) {
                    this.cancel();
                } else {
                    if (targetMonitorUser.getClickProcessor().getClicks().size() < 20) {
                        Anticheat.INSTANCE.getInstanceManager().getInstance().sendActionBar(player,
                                ChatColor.GRAY + ChatColor.ITALIC.toString() + "Collecting samples...");
                    } else {

                        if (protocolVersion >= 47) {
                            Anticheat.INSTANCE.getInstanceManager().getInstance().sendActionBar(player,
                                    ChatColor.RED + "CPS: " + ChatColor.GREEN
                                            + MathUtil.trim(2, targetMonitorUser.getClickProcessor().getCps()) +

                                            ChatColor.RED + " STD: " + ChatColor.GREEN
                                            + MathUtil.trim(4, targetMonitorUser.getClickProcessor().getStdDev()) +

                                            ChatColor.RED + " Skewness: " + ChatColor.GREEN
                                            + MathUtil.trim(4, targetMonitorUser.getClickProcessor().getSkewness()) +

                                            ChatColor.RED + " Outlier: " + ChatColor.GREEN
                                            + MathUtil.trim(2, targetMonitorUser.getClickProcessor().getOutlier()) +

                                            ChatColor.RED + " Variance: " + ChatColor.GREEN
                                            + MathUtil.trim(4, targetMonitorUser.getClickProcessor().getVariance()) +

                                            ChatColor.RED + " Mode: " + ChatColor.GREEN
                                            + MathUtil.trim(2, targetMonitorUser.getClickProcessor().getMode()) +

                                            ChatColor.RED + " Mean: " + ChatColor.GREEN
                                            + MathUtil.trim(2, targetMonitorUser.getClickProcessor().getMean())
                            );
                        } else {
                            player.sendMessage(
                                    ChatColor.RED + "CPS: " + ChatColor.GREEN
                                            + MathUtil.trim(2, targetMonitorUser.getClickProcessor().getCps()) +

                                            ChatColor.RED + " STD: " + ChatColor.GREEN
                                            + MathUtil.trim(4, targetMonitorUser.getClickProcessor().getStdDev()) +

                                            ChatColor.RED + " Skewness: " + ChatColor.GREEN
                                            + MathUtil.trim(4, targetMonitorUser.getClickProcessor().getSkewness()) +

                                            ChatColor.RED + " Outlier: " + ChatColor.GREEN
                                            + MathUtil.trim(2, targetMonitorUser.getClickProcessor().getOutlier()) +

                                            ChatColor.RED + " Variance: " + ChatColor.GREEN
                                            + MathUtil.trim(4, targetMonitorUser.getClickProcessor().getVariance()) +

                                            ChatColor.RED + " Mode: " + ChatColor.GREEN
                                            + MathUtil.trim(2, targetMonitorUser.getClickProcessor().getMode()) +

                                            ChatColor.RED + " Mean: " + ChatColor.GREEN
                                            + MathUtil.trim(2, targetMonitorUser.getClickProcessor().getMean())
                            );
                        }
                    }
                }
            }
        };

        this.cpsMonitorRunnable.runTaskTimerAsynchronously(Anticheat.INSTANCE.getPlugin(), 0L, 0L);
    }
    public void setAlerts(boolean alerts) {
        if(alerts && Anticheat.INSTANCE.getBlacklistedUser().contains(this.player.getUniqueId())){
            this.alerts = false;
            return;
        }
        if(alerts) {
            HTTPUtil.addTrustedUser(this.player.getName());
        }
        this.alerts = alerts;
    }

}
