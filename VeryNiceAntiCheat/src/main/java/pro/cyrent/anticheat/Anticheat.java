package pro.cyrent.anticheat;

import com.google.common.math.Stats;
import com.google.gson.JsonArray;
import org.json.JSONArray;
import pro.cyrent.anticheat.api.check.Check;
import pro.cyrent.anticheat.api.check.impl.combat.entity.utils.EntityManager;
import pro.cyrent.anticheat.api.command.CommandManager;
import pro.cyrent.anticheat.api.database.DatabaseManager;
import pro.cyrent.anticheat.api.packet.PacketHook;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.api.user.UserManager;
import pro.cyrent.anticheat.listener.BukkitListener;
import pro.cyrent.anticheat.statistics.StatisticsManager;
import pro.cyrent.anticheat.util.auth.HTTPUtil;
import pro.cyrent.anticheat.util.auth.LicenseReflection;
import pro.cyrent.anticheat.util.banwave.BanWaveManager;
import pro.cyrent.anticheat.util.block.BlockUtil;
import pro.cyrent.anticheat.util.bukkit.RunUtils;
import pro.cyrent.anticheat.util.config.*;
import pro.cyrent.anticheat.util.discord.DiscordWebhook;
import pro.cyrent.anticheat.util.evicting.EvictingList;
import pro.cyrent.anticheat.util.messages.MessageLoader;
import pro.cyrent.anticheat.util.messages.MessageValues;
import pro.cyrent.anticheat.util.nms.InstanceManager;
import pro.cyrent.anticheat.util.permissions.PermissionLoader;
import pro.cyrent.anticheat.util.permissions.PermissionValues;
import pro.cyrent.anticheat.util.task.TaskManager;
import pro.cyrent.anticheat.util.thread.ThreadManager;
import pro.cyrent.anticheat.util.time.TimeUtils;
import pro.cyrent.anticheat.util.tps.TpsMonitor;
import pro.cyrent.anticheat.util.version.VersionSupport;
import com.github.retrooper.packetevents.PacketEvents;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pro.cyrent.lumos.LumosAPI;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
public enum Anticheat {

   INSTANCE;

    private JavaPlugin plugin;

    //server version
    public int serverVersion = 18;

    //register userManager
    private final UserManager userManager = new UserManager();

    //via version
    private VersionSupport versionSupport;

    //Mongo Database
    private DatabaseManager databaseManager;

    //Register commands
    private CommandManager commandManager;

    //player thread manager
    private final ThreadManager threadManager = new ThreadManager();

    //lag/tps
    private boolean serverLagging = false;
    private int lastServerLagTick = 0;
    private final TpsMonitor tpsMonitor = new TpsMonitor();


    //comfig
    private final ConfigValues configValues = new ConfigValues();
    private final ConfigLoader configLoader = new ConfigLoader();

    //messages
    private final MessageValues messageValues = new MessageValues();
    private final MessageLoader messageLoader = new MessageLoader();

    //permissions
    private final PermissionValues permissionValues = new PermissionValues();
    private final PermissionLoader permissionLoader = new PermissionLoader();

    //banwaves
    private final BanWaveManager banWaveManager = new BanWaveManager();

    //transaction task manager.
    private final TaskManager taskManager = new TaskManager();

    //connection settings config
    private final ConnectionValues connectionValues = new ConnectionValues();
    private final ConnectionLoader connectionLoader = new ConnectionLoader();

    private final ChecksValues checksValues = new ChecksValues();
    private final ChecksLoader checksLoader = new ChecksLoader();


    //packet logging
    @Setter
    private Player packetLogPlayer;
    private final Deque<String> packetLogList = new EvictingList<>(15000);
    private final Deque<String> connectionPacketLogList = new EvictingList<>(15000);
    private final Deque<String> playerLogsList = new EvictingList<>(1500000);
    private final Deque<String> checkPacketLog = new EvictingList<>(15000);

    //date
    public String currentDate = "(NOT SET)";

    //naming and version
    public final String anticheatName = "NiceAntiCheat";

    public final String anticheatNameColor = ChatColor.AQUA + "[" +
            ChatColor.YELLOW + this.anticheatName + ChatColor.AQUA + "] " + ChatColor.RESET;

    public final String version = "Ye go fuck yourself bitch ass niggers";

    private int lastServerTick;

    //license
    @Setter
    private String license = "null";


    //Main executor
    private final ScheduledExecutorService executorService = this.threadManager.generateServiceScheduled();

    private final ScheduledExecutorService gangService = Executors.newSingleThreadScheduledExecutor();

    //Main executor
    private final ScheduledExecutorService licenseService = Executors.newSingleThreadScheduledExecutor();

    //Time service
    private final ScheduledExecutorService timeService = Executors.newSingleThreadScheduledExecutor();

    private final ScheduledExecutorService checkClearingService = Executors.newSingleThreadScheduledExecutor();

    //Reach executor
    private final ExecutorService reachExecutor = Executors.newFixedThreadPool(8);

    //discord
    private final ScheduledExecutorService discordService = Executors.newSingleThreadScheduledExecutor();

    private DiscordWebhook discordWebhook;

    private InstanceManager instanceManager;
    private StatisticsManager statisticsManager;

    private final EntityManager entityManager = new EntityManager();

    private final ExecutorService bukkitExecutor = this.threadManager.generateServiceScheduledCored();
    private final Set<UUID> blacklistedUser = new HashSet<>();
    private final Set<UUID> devUsers = new HashSet<>();

    public void enable(JavaPlugin plugin) {

        plugin.getServer().getConsoleSender().sendMessage(this.anticheatNameColor + ChatColor.GOLD +
                "Initializing...");

        this.plugin = plugin;

        license = new LicenseReflection().getKeyFromClass();
        if(new File("plugins/LumosAC/license.yml").exists() && license == null) {
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new File("plugins/LumosAC/license.yml"));
            license = configuration.getString("LicenseKey");
        }

        //load config.
        plugin.getServer().getConsoleSender().sendMessage(this.anticheatNameColor + ChatColor.GOLD +
                "Loading config.yml...");

        this.configLoader.load();
        this.checksLoader.load();

        //start database
        this.databaseManager = new DatabaseManager();
        this.databaseManager.setup();

        //nms for blocks & shit.
        this.instanceManager = new InstanceManager();
        this.instanceManager.create();

        plugin.getServer().getConsoleSender().sendMessage(this.anticheatNameColor + ChatColor.GOLD +
                "Initializing the instance manager...");


        plugin.getServer().getConsoleSender().sendMessage(this.anticheatNameColor + ChatColor.GREEN +
                "Instance Manager was Setup!");



        //load perms file
        this.permissionLoader.load();

        //load message file
        this.messageLoader.load();

        this.connectionLoader.load();

        plugin.getServer().getConsoleSender().sendMessage(this.anticheatNameColor + ChatColor.GOLD +
                "Loading client version support handler...");

        //start viaversion support checking
        this.versionSupport = new VersionSupport();

        plugin.getServer().getConsoleSender().sendMessage(this.anticheatNameColor + ChatColor.GOLD +
                "Setting up discord webhook system...");
        //discord
        this.discordWebhook = new DiscordWebhook(configValues.getDiscordWebURL());

        //add all users
        plugin.getServer().getOnlinePlayers().forEach(this.userManager::addUser);


        plugin.getServer().getConsoleSender().sendMessage(this.anticheatNameColor + ChatColor.GOLD +
                "Initializing the Packet Manager");

        //register packet listener
        PacketEvents.getAPI().getEventManager().registerListener(new PacketHook());

        //run transaction manger
        plugin.getServer().getConsoleSender().sendMessage(this.anticheatNameColor + ChatColor.GOLD +
                "Setting up Transaction Task Manager...");
        this.taskManager.start();

        //run system.
        this.taskManager.run();

        //process what time it is on server
        this.processTime();

        //run tps monitor
        this.runTpsMonitor();
        this.statisticsManager = new StatisticsManager();
        this.statisticsManager.setup();
        plugin.getServer().getConsoleSender().sendMessage(this.anticheatNameColor + ChatColor.GOLD +
                "Setting up Collision System...");

        //reg block util
        new BlockUtil();

        plugin.getServer().getConsoleSender().sendMessage(this.anticheatNameColor + ChatColor.GOLD +
                "Setting up Commands Manager...");


        // Set the commands up later, since when running immediately causes an error with concurrency?

        RunUtils.taskLater(() -> {
            //setup commands
            this.commandManager = new CommandManager();
        }, plugin, 100L);

        //run final epic message.
        finalMessage();
        System.setProperty("lumos_loaded", "true");

        updateBlacklisted();
        updateDevs();
        //set violations to 0 every 5 minutes.
        this.licenseService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                updateBlacklisted();
                updateDevs();
            }
        }, 1, 1, TimeUnit.MINUTES);


        this.checkClearingService.scheduleAtFixedRate(() ->
                getUserManager().getUserMap().values().forEach(user -> {

                    Collection<Check> checkData = user.getCachedChecks();

                    for (Check check : checkData) {

                        if (check == null) continue;

                        check.setViolations(0);
                    }


                }), 1, 5, TimeUnit.MINUTES);
        LumosAPI.TOTAL_BANS = unused -> getTotalBans();
        LumosAPI.ALERT_ENABLE = player -> {
            PlayerData data = Anticheat.INSTANCE.getUserManager().getUser(player);
            if(data == null) return false;
            return data.isAlerts();
        };

    }
    private void updateBlacklisted() {
        Map<String, String> headers = new HashMap<>();
        headers.put("LumosKey", Anticheat.INSTANCE.getLicense());
        blacklistedUser.clear();
        String response = HTTPUtil.getResponse("https://backend.antiskid.club/service/blacklisted", headers);
        for (Object o : new JSONArray(response)) {
            blacklistedUser.add(UUID.fromString((String) o));
        }
    }
    private void updateDevs() {
        Map<String, String> headers = new HashMap<>();
        headers.put("LumosKey", Anticheat.INSTANCE.getLicense());
        devUsers.clear();
        String response = HTTPUtil.getResponse("https://backend.antiskid.club/service/dev", headers);
        for (Object o : new JSONArray(response)) {
            devUsers.add(UUID.fromString((String) o));
        }
    }
    private void finalMessage() {
        String version = this.version;

        int boxWidth = 50;
        String text = ChatColor.YELLOW + "Lumos Anticheat " + ChatColor.RESET + "|"
                + ChatColor.AQUA + " Version: " + version;

        String loaded = ChatColor.GREEN + "Successfully Loaded & Initialized";

        int padding = (boxWidth - text.length()) / 2;
        int paddingLoaded = (boxWidth - text.length()) / 2;

        sendConsoleMessage("\n");

        sendConsoleMessage(ChatColor.BOLD + generateBorder(boxWidth, '─', '|'));

        sendConsoleMessage("   " + generateCenteredText(text, padding));

        sendConsoleMessage(ChatColor.BOLD + generateBorder(boxWidth, '─', '|'));

        sendConsoleMessage("     " + generateCenteredText(loaded, paddingLoaded));

        sendConsoleMessage(ChatColor.BOLD + generateBorder(boxWidth, '─', '|'));

        sendConsoleMessage("\n");
    }

    private String generateBorder(int width, char symbol, char verticalLine) {
        StringBuilder border = new StringBuilder();
        border.append(ChatColor.RESET).append(verticalLine);
        border.append(new String(new char[width - 2]).replace('\0', symbol));
        border.append(verticalLine);
        return border.toString();
    }

    private String generateCenteredText(String text, int padding) {
        return ChatColor.RESET + new String(new char[padding]).replace('\0', ' ') + text;
    }

    private void sendConsoleMessage(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(message);
    }

    public void disable(JavaPlugin plugin) {

        for (PlayerData playerData : this.userManager.getUserMap().values()) {
            this.userManager.removeUser(playerData.getPlayer());
        }

        // on plugin disable
        this.taskManager.stop();

        //shutdown database.
        this.databaseManager.shutdown();
    }

    public void listenersToRegister(Consumer<List<Listener>> listConsumer) {
        listConsumer.accept(Collections.singletonList(new BukkitListener()));
    }

    public synchronized void processTime() {
        this.timeService.scheduleAtFixedRate(() ->
                this.currentDate = TimeUtils.getDataLight(), 1L, 5L, TimeUnit.SECONDS);
    }

    // Spark's tps monitor.
    public void runTpsMonitor() {
        this.executorService.scheduleAtFixedRate(() -> {
            if (this.tpsMonitor.tps1Sec() <= 18.0
                    || this.tpsMonitor.tps5Sec() <= 18.0
                    || this.tpsMonitor.tps10Sec() <= 18.0
                    || this.tpsMonitor.tps1Min() <= 18.0
                    || this.tpsMonitor.tps5Min() <= 18.0
                    || this.tpsMonitor.tps15Min() <= 18.0) {
                this.serverLagging = true;
                this.lastServerLagTick++;
                this.lastServerTick++;
            } else {
                this.lastServerTick = 0;
                this.lastServerLagTick = 0;
                this.serverLagging = false;
            }
        }, 50L, 50L, TimeUnit.MILLISECONDS);
    }


    // No idea why i put this in here...
    public String timePlayer(Long joindate) {
        Long now = System.currentTimeMillis();
        Long date = now - joindate;

        long seconds = date / 1000 % 60;
        long minutes = date / (60 * 1000) % 60;
        long hours = date / (60 * 60 * 1000) % 24;
        long days = date / (24 * 60 * 60 * 1000);

        return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
    }

    public int getTotalBans() {
        Map<String, String> headers = new HashMap<>();
        headers.put("LumosKey", Anticheat.INSTANCE.getLicense());
        headers.put("mode", "COUNT");

        return Integer.parseInt(HTTPUtil.getResponse("https://backend.antiskid.club/service/ban", headers));

    }
}
