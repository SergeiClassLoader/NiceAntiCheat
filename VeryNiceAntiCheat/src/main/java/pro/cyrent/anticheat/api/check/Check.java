package pro.cyrent.anticheat.api.check;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.command.commands.sub.LogsCommand;
import pro.cyrent.anticheat.api.database.api.InputData;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.processor.basic.MovementProcessor;
import pro.cyrent.anticheat.util.auth.HTTPUtil;
import pro.cyrent.anticheat.util.discord.DiscordWebhook;
import pro.cyrent.anticheat.util.nik.BarUtil;
import pro.cyrent.anticheat.util.stats.StatsUtil;
import pro.cyrent.lumos.LumosAPI;
import pro.cyrent.lumos.event.PlayerFlagEvent;
import pro.cyrent.lumos.event.PlayerPunishedEvent;

import java.io.IOException;
import java.util.*;

@Getter
@Setter
public class Check extends Event {

    @Setter
    private String checkName;
    private String checkType;
    private CheckType type;
    private CheckState state;
    private CheckName checkNameEnum;
    private boolean experimental;
    private boolean enabled;
    private boolean punishable;
    private double punishmentVL;
    public PlayerData data;
    private String description;

    private String readingCheckName;
    private String readingCheckType;

    private double violations;

    public String command = "/ban %PLAYER% cheating";

    public Check() {
        CheckInformation checkInformation = getClass().getAnnotation(CheckInformation.class);
        this.checkName = checkInformation.name();
        this.checkType = checkInformation.subName();
        this.type = checkInformation.checkType();
        this.experimental = checkInformation.experimental();
        this.enabled = checkInformation.enabled();
        this.punishable = !checkInformation.experimental()
                && checkInformation.punishable();
        this.punishmentVL = checkInformation.punishmentVL();
        this.state = checkInformation.state();
        this.description = checkInformation.description();
        this.checkNameEnum = checkInformation.checkNameEnum();
    }


    public void fail(String... data) {

        // this was exempting inf if the server lagged long enough.
        if (Anticheat.INSTANCE.getLastServerTick() > 0) {
            return;
        }

        if (getData().isBanned()
                || !this.enabled
                || getData().getPlayer().hasPermission(Anticheat.INSTANCE.getPermissionValues().getBypass())
                && !getData().getPlayer().isOp()) {
            return;
        }

        if (getData().getPlayer().isOp() && Anticheat.INSTANCE.getConfigValues().isAllowOp()) {
            return;
        }

        if (Anticheat.INSTANCE.getConfigValues().isLagbacks()
                && this.type == CheckType.MOVEMENT
                && Anticheat.INSTANCE.getConfigValues().isSafeMode()) {
            this.punishable = false;
        }

        if (this.type == CheckType.COMBAT
                && this.violations > 2) {
            getData().setLastFlag(System.currentTimeMillis());
        }

        if (this.experimental) {
            this.punishable = false;
        }


        getData().setCheckName(this.readingCheckName);
        getData().setCheckType(this.readingCheckType);
        getData().setCheckViolation(this.violations);
        getData().setCheckPunishVL(this.punishmentVL);
        getData().setLastFlaggedCheck(this);


        //kick for invalid via version found on flag.
        if (getData().getProtocolVersion() == -1) {
            getData().kickPlayerViaVersion(getData().getProtocolVersion());
            return;
        }

        //high ping flags check for some reason.................
        if (getData().getTransactionProcessor().getTransactionPing() > 800
                || getData().getTransactionProcessor().getTransactionPingDrop() > 500
                || getData().getTransactionProcessor().getKeepAlivePing() > 800) {

            //TODO: if you want people to not be at the logout location when flagged, use setbacks to prevent them
            // from trying to be at that location when logged out instead of kicking the player.

         /*   if (this.type == CheckType.MOVEMENT
                    && !this.checkName.equalsIgnoreCase("scaffold")) {
                getData().setBack();
            }*/


            getData().kickPlayer(
                    "having high ping while flagging check: " + this.getFriendlyName()
                            + ", " +
                            "transaction-ping: " + getData().getTransactionProcessor().getTransactionPing()
                            + ", " +
                            "keep-alive-ping: " + getData().getTransactionProcessor().getKeepAlivePing());

            StatsUtil.checkFriendlyNameKicks.add(this.getFriendlyName());
            return;
        }

        if (Anticheat.INSTANCE.getConfigValues().isLagbacks() && this.type == CheckType.MOVEMENT
                && !this.checkName.equalsIgnoreCase("scaffold")) {
            getData().setBack();
        }

        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilderFixedLogs = new StringBuilder();

        String primary = Anticheat.INSTANCE.getConfigValues().getPrimaryColorHover();
        String secondary = Anticheat.INSTANCE.getConfigValues().getSecondaryColorHover();
        String teleport = Anticheat.INSTANCE.getConfigValues().getTeleportTextColor();

        for (String s : data) {
            String fixed = primary + " - " + secondary + s;
            stringBuilder.append(fixed).append(",\n");
            stringBuilderFixedLogs.append(s).append(",\n");
        }

        String checktype = this.readingCheckType;

        //Experimental
        if (this.experimental) {
            checktype += "*";
        }

        /**
         * Requested by HyCraft
         */

        /** --------------------------- API FOR THE ANTI-CHEAT FLAG EVENT CANCELING! -------------------------------**/
        String alertData = stringBuilder.toString().trim();

        String allData = primary + "» " + secondary + "Description: " + primary + this.description + "\n\n"
                + primary + "» " + secondary + "Flag Information: \n" + alertData;
        PlayerFlagEvent event = new PlayerFlagEvent(
                getData().getPlayer(),
                this.readingCheckName, this.readingCheckType,
                this.checkNameEnum.name(), this.state.name,
                this.punishmentVL, this.violations,
                this.punishable, this.experimental, false, false, allData
        );
        LumosAPI.dispatchPlayerFlagEvent(event);
        if (event.isCanceled()) {
            return;
        }

        /** --------------------------------------------------------------------------------------------------------**/

        if (!this.punishable) {
            this.violations += 0.5;
        } else {
            this.violations += 1.0;
        }

        String alert = Anticheat.INSTANCE.getConfigValues().getAlertsMessage().replace("%VL%",
                        Double.toString(violations)).replace("%PLAYER%", getData().getPlayer().getName())
                .replace("%CHECK%", this.readingCheckName).replace("%CHECKTYPE%", checktype).
                replace("%STATE%", state.name).
                replace("%MAX-VL%", Double.toString(punishmentVL))
                .replace("%PREFIX%", Anticheat.INSTANCE.getConfigValues().getPrefix())
                .replace("%BAR_VL%", BarUtil.generateBar((Math.min(this.violations,
                        this.punishmentVL)), this.punishmentVL)).replace("%PING%",
                        Long.toString(getData()
                                .getTransactionProcessor().getTransactionPing()));

        TextComponent textComponent = new TextComponent(alert);



        if (!Anticheat.INSTANCE.getConfigValues().isDisableAlerts() && !event.isCancelMessage()) {
            Anticheat.INSTANCE.getUserManager().getUserMap().values().forEach(uuidUserEntry -> {
                if ((uuidUserEntry.getPlayer().isOp()
                        || uuidUserEntry.isDev(uuidUserEntry.getPlayer())
                        || uuidUserEntry.getPlayer().hasPermission(Anticheat.INSTANCE.getPermissionValues().getAlert()))
                        && uuidUserEntry.isAlerts()) {

                    if (Anticheat.INSTANCE.getConfigValues().isHover()) {
                        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder(allData
                                        + "\n\n" + teleport + "(Click to teleport)").create()));
                    }

                    if (Anticheat.INSTANCE.getConfigValues().isAlertCommand()) {
                        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                Anticheat.INSTANCE.getConfigValues()
                                        .getAlertCommandString().replace("%PLAYER%",
                                                getData().getUsername())));
                    }

                    if (!(Anticheat.INSTANCE.getConfigValues().isNoAlertMovement()
                            && this.type == CheckType.MOVEMENT && Anticheat.INSTANCE.getConfigValues().isLagbacks())) {
                        uuidUserEntry.getPlayer().spigot().sendMessage(textComponent);
                    }
                }
            });
        }


        if (Anticheat.INSTANCE.getConfigValues().isConsoleAlerts()) {
            //Console Alert
            if (Anticheat.INSTANCE.getConfigValues().isNoAlertMovement()
                    && this.type == CheckType.MOVEMENT
                    && Anticheat.INSTANCE.getConfigValues().isLagbacks()) {
                return;
            }
            Anticheat.INSTANCE.getPlugin().getServer().getConsoleSender().sendMessage(alert);
        }


        if (this.violations >= this.punishmentVL && !getData().isBanned()
                && this.punishable
                && !Anticheat.INSTANCE.getConfigValues().isDemonEye()
                && !Anticheat.INSTANCE.getConfigValues().isBanWave()
                && Anticheat.INSTANCE.getConfigValues().isPunish()) {

            //punish player method
            punishPlayer(getData(), false, 40, violations, checkName, checktype, punishmentVL, this);
        }

        getData().getFlaggedChecks().put(this, this.violations);

        if (!getData().getRecentlyFlagged().contains(this)) {
            getData().getRecentlyFlagged().add(this);
        }

        if (getData().getRecentlyFlagged().size() > 7) {
            getData().getRecentlyFlagged().remove(0);
        }

        StatsUtil.flagAmount++;

        this.addLogs(stringBuilderFixedLogs);
    }

    public void addLogs(StringBuilder stringBuilder) {

        MovementProcessor p = getData().getMovementProcessor();

        String addToEnd = "***[EXTRA DEBUG]**** deltaXZ=" + p.getDeltaXZ()
                + ", lastDeltaXZ=" + p.getLastDeltaXZ()
                + ", deltaY=" + p.getDeltaY()
                + ", lastDeltaY=" + p.getDeltaY()
                + ", tick=" + p.getTick()
                + ", ground=" + p.getTo().isOnGround()
                + ", lastGround=" + p.getFrom().isOnGround()
                + ", position=NULL"
                + ", lastPos=NULL"
                + ", lastLastPos=NULL"
                + ", transaction-ping=" + getData().getTransactionProcessor().getTransactionPing()
                + ", transaction-post-ping=" + getData().getTransactionProcessor().getPostTransactionPing()
                + ", version=" + Anticheat.INSTANCE.getVersionSupport().getClientVersion(getData())
                + ", protocol=" + getData().getProtocolVersion()
                + ", ***[DATE]***" + Anticheat.INSTANCE.getCurrentDate();

        String checkData = stringBuilder.toString().trim().replace("\n", "")
                .replace("Â", "") + addToEnd;

        Anticheat.INSTANCE.getDatabaseManager().getLogQueue().add(
                new InputData(
                        getData().getPlayer().getUniqueId().toString(),
                        getData().getPlayer().getName(),
                        this.checkName,
                        this.checkType,
                        //not sure
                        getData().getFlaggedChecks().getOrDefault(this, 1.0),
                        !this.punishable,
                        (int) getData().getTransactionProcessor().getTransactionPing(),
                        checkData,
                        Anticheat.INSTANCE.getCurrentDate(),
                        getData().getLoginTime(),
                        Anticheat.INSTANCE.timePlayer(getData().getLoginMilis()),
                        "false",
                        Anticheat.INSTANCE.getLicense()
                ));
        getData().getSessionLogs().add(new LogsCommand.WebResult(
                checkName, checkType,
                (int) (double) getData().getFlaggedChecks().getOrDefault(this, 1.0),
                experimental, null, checkData

        ));


        if (Anticheat.INSTANCE.getConfigValues().isOwnMongo()) {
            Anticheat.INSTANCE.getDatabaseManager().getCustomLogQueue().add(
                    new InputData(
                            getData().getPlayer().getUniqueId().toString(),
                            getData().getPlayer().getName(),
                            this.checkName,
                            this.checkType,
                            //not sure
                            getData().getFlaggedChecks().getOrDefault(this, 1.0),
                            !this.punishable,
                            (int) getData().getTransactionProcessor().getTransactionPing(),
                            checkData,
                            Anticheat.INSTANCE.getCurrentDate(),
                            getData().getLoginTime(),
                            Anticheat.INSTANCE.timePlayer(getData().getLoginMilis()),
                            "false",
                            Anticheat.INSTANCE.getLicense()
                    ));
        }
    }

    public String getFriendlyName() {
        return this.checkName + this.checkType;
    }

    public static void punishPlayer(PlayerData user, boolean doRandom, int time, double violations, String checkName,
                                    String checkType, double punishmentVL, Check check) {

        if (user.isBanned()) return;
        user.setBanned(true);
        LumosAPI.dispatchPlayerPunishedEvent(new PlayerPunishedEvent(user.getUuid(), checkName, checkType));
        Map<String, String> headers = new HashMap<>();
        headers.put("LumosKey", Anticheat.INSTANCE.getLicense());
        headers.put("uuid", user.getUuid().toString());
        headers.put("id", UUID.randomUUID().toString().substring(0, 6));
        headers.put("mode", "ADD");
        headers.put("info", checkName + " " + checkType + " VL " + violations);

        HTTPUtil.getResponse("https://backend.antiskid.club/service/ban", headers);


        new BukkitRunnable() {
            @Override
            public void run() {

                if (Anticheat.INSTANCE.getConfigValues().isUsingGlobalBans()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Anticheat.INSTANCE.getConfigValues()
                            .getPunishCommand()
                            .replace("%PLAYER%", user.getPlayer().getName())
                            .replace("%PREFIX%", Anticheat.INSTANCE.getConfigValues().getPrefix())
                            .replaceFirst("/", ""));
                } else if (check != null) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), check.getCommand()
                            .replace("%PLAYER%", user.getPlayer().getName())
                            .replace("%PREFIX%", Anticheat.INSTANCE.getConfigValues().getPrefix())
                            .replaceFirst("/", ""));
                } else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Anticheat.INSTANCE.getConfigValues()
                            .getPunishCommand()
                            .replace("%PLAYER%", user.getPlayer().getName())
                            .replace("%PREFIX%", Anticheat.INSTANCE.getConfigValues().getPrefix())
                            .replaceFirst("/", ""));
                }

                if (Anticheat.INSTANCE.getConfigValues().isAnnounce()) {
                    for (String message : Anticheat.INSTANCE.getConfigValues().getAnnounceMessage()) {

                        Bukkit.broadcastMessage(Anticheat.INSTANCE.getConfigLoader().convertColor(
                                message.replace("%LINE%", " ")
                                        .replace("%PLAYER%", user.getPlayer().getName())
                                        .replace("%PREFIX%", Anticheat.INSTANCE.getConfigValues().getPrefix())));
                    }
                }

            }
        }.runTaskLater(Anticheat.INSTANCE.getPlugin(), 1L);

        //discord bans
        if (Anticheat.INSTANCE.getConfigValues().isDiscord()) {

            String discordBanAlert = Anticheat.INSTANCE.getConfigValues().getDiscordBanMessage()
                    .replace("%MAX-VL%", Double.toString(punishmentVL))
                    .replace("%CHECK%", checkName)
                    .replace("%CHECKTYPE%", checkType)
                    .replace("%VL%", Double.toString(violations))
                    .replace("%PLAYER%", user.getPlayer().getName())
                    .replace("%PREFIX%", Anticheat.INSTANCE.getConfigValues().getPrefix());


            new BukkitRunnable() {
                @Override
                public void run() {
                    Anticheat.INSTANCE.getDiscordService().execute(() -> {
                        List<LogsCommand.WebResult> webResults = new ArrayList<>();

                        if (webResults.size() > 0) {
                            String begin =
                                    "All violations for "
                                            + user.getPlayer().getName() + " (" +
                                            webResults.size() + ")";


                            Map<String, Double> integerMap = new HashMap<>();

                            webResults.forEach(inputData -> {
                                String name = inputData.getName() + " " + inputData.getType();
                                integerMap.put(name, integerMap.getOrDefault(name, 0.0) + .5);
                            });

                            integerMap.forEach((s1, integer) -> PlayerData.banMessage += ("- " + s1 + " x" + integer + "\\n"));

                            Anticheat.INSTANCE.getDiscordWebhook().addEmbed(
                                    new DiscordWebhook.EmbedObject().setTitle(discordBanAlert).setDescription(begin
                                                    + "\\n" + PlayerData.banMessage.replace("null", ""))
                                            .setThumbnail("https://mc-heads.net/avatar/"
                                                    + user.getPlayer().getUniqueId()
                                                    + "/100/nohelm.png"));

                            try {
                                Anticheat.INSTANCE.getDiscordWebhook().execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Anticheat.INSTANCE.getDiscordWebhook().clearEmbed();

                            PlayerData.banMessage = "null";

                            integerMap.clear();
                            webResults.clear();
                        }
                    });
                }
            }.runTaskLater(Anticheat.INSTANCE.getPlugin(), 40L);
        }

        StatsUtil.banAmount++;
    }
}
