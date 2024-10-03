package pro.cyrent.anticheat.api.command.commands;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.command.commands.sub.*;

import pro.cyrent.anticheat.api.user.PlayerData;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class MainCommand extends BukkitCommand {
    private final String line = Anticheat.INSTANCE.getMessageValues().getLineMessage();
    private final AlertsCommand alertsCommand = new AlertsCommand();
    private final ForceBanCommand forceBanCommand = new ForceBanCommand();
    private final ForceKickCommand forceKickCommand = new ForceKickCommand();
    private final GuiCommand guiCommand = new GuiCommand();
    private final StatsGUICommand statsGUICommand = new StatsGUICommand();
    private final PingCommand pingCommand = new PingCommand();
    private final ReloadCommand reloadCommand = new ReloadCommand();

    private final DevAlertsCommand devAlertsCommand = new DevAlertsCommand();
    private final CpsCommand cpsCommand = new CpsCommand();
    private final PanelCommand panelCommand = new PanelCommand();

    private final DebugCommand debugCommand = new DebugCommand();

    private final LogsCommand logsCommand = new LogsCommand();
    private final LookupCommand lookupCommand = new LookupCommand();
    private final ClearLogsCommand clearLogsCommand = new ClearLogsCommand();
    private final BanWaveCommand banWaveCommand = new BanWaveCommand();
    private final CrashGUI crashCommand = new CrashGUI();

    private final TestKnockBackCommand testKnockBackCommand = new TestKnockBackCommand();

    private final TopViolationsCommand topViolationsCommand = new TopViolationsCommand();

    private final VersionCommand
            versionCommand = new VersionCommand();

    private final TestKillAuraCommand
            killAuraCommand = new TestKillAuraCommand();
    private final RotateCommand
            rotateCommand = new RotateCommand();


    private final InfoCommand infoCommand = new InfoCommand();

    private final PacketLogCommand
            packetLogCommand = new PacketLogCommand();



    public MainCommand(String name) {
        super(name);
        this.description = "Main anticheat command.";
        this.usageMessage = "/" + name;
        this.setAliases(new ArrayList<>());
    }

    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase(Anticheat.INSTANCE.getConfigValues().getCommandName())) {

            if (commandSender instanceof Player || commandSender instanceof ConsoleCommandSender) {

                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;

                    PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(player);

                    if (user != null) {
                        if(Anticheat.INSTANCE.getBlacklistedUser().contains(user.getUuid())) {
                            return false;
                        }


                        boolean isDev = user.isDev(player);

                        if (isDev
                                || player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getDefaultCommand())
                                || player.isOp()) {

                            if (args.length < 1) {
                                commandSender.sendMessage("\n");
                                commandSender.sendMessage(Anticheat.INSTANCE.getMessageValues().getBeginCommandLine());
                                commandSender.sendMessage(line);

                                Anticheat.INSTANCE.getCommandManager().getCommandList().forEach(command -> {

                                    if (command.getUsage() == null) return;

                                    if (command.getUsage().equalsIgnoreCase("velocity")) {
                                        return;
                                    }

                                    TextComponent textComponent =
                                            new TextComponent(ChatColor.GRAY + "» " + ChatColor.WHITE
                                            + "/" + command.getCommand() + ChatColor.GRAY + " - " + ChatColor.RED
                                            + command.getDescription());
                                    textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new ComponentBuilder((command.getUsage() != null
                                                    ? ChatColor.RED + command.getUsage()
                                                    : ChatColor.WHITE + "No usage found.")).create()));
                                    player.spigot().sendMessage(textComponent);
                                });

                                commandSender.sendMessage(line);
                            } else {
                                String s = args[0];
                                boolean found = false;
                                if (s.equalsIgnoreCase("alerts") &&
                                        (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getAlertCommand()) || isDev)) {
                                    found = true;
                                    alertsCommand.execute(args, s, commandSender);
                                } else if (s.equalsIgnoreCase("forceban") &&
                                        (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getForcebanCommand()) || isDev)) {
                                    found = true;
                                    forceBanCommand.execute(args, s, commandSender);
                                }else if (s.equalsIgnoreCase("forcekick") &&
                                        (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getForcebanCommand()) || isDev)) { //Purposely make only those that can ban kick
                                    found = true;
                                    forceKickCommand.execute(args, s, commandSender);
                                } else if (s.equalsIgnoreCase("gui")  &&
                                        (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getGuiCommand()) || isDev)) {
                                    found = true;
                                    guiCommand.execute(args, s, commandSender);
                                } else if (s.equalsIgnoreCase("stats")  &&
                                        (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getStatsGuiCommand()) || isDev)) {
                                    found = true;
                                    statsGUICommand.execute(args, s, commandSender);
                                } else if (s.equalsIgnoreCase("ping") &&
                                        (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getPingCommand()) || isDev)) {
                                    found = true;
                                    pingCommand.execute(args, s, commandSender);
                                } else if (s.equalsIgnoreCase("logs")  &&
                                        (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getLogsCommand()) || isDev)) {
                                    found = true;
                                    logsCommand.execute(args, s, commandSender);
                                } else if (s.equalsIgnoreCase("lookup")  &&
                                        (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getLogsCommand()) || isDev)) {
                                    found = true;
                                    lookupCommand.execute(args, s, commandSender);
                                } else if (s.equalsIgnoreCase("clearlogs")  &&
                                        (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getClearLogsCommand()) || isDev)) {
                                    found = true;
                                    clearLogsCommand.execute(args, s, commandSender);
                                } else if (s.equalsIgnoreCase("reload")  &&
                                        (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getReloadCommand()) || isDev)) {
                                    found = true;
                                    reloadCommand.execute(args, s, commandSender);
                                } else if (s.equalsIgnoreCase("crash")  &&
                                        (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getCrashCommand()) || isDev)) {
                                    found = true;
                                    crashCommand.execute(args, s, commandSender);
                                } else if (s.equalsIgnoreCase("dev") && isDev) {
                                    found = true;
                                    devAlertsCommand.execute(args, s, commandSender);
                                } else if (s.equalsIgnoreCase("devtest") && isDev) {
                                    found = true;
                                    Bukkit.getScheduler().runTaskAsynchronously(Anticheat.INSTANCE.getPlugin(), () -> {
                                        Anticheat.INSTANCE.getStatisticsManager().collectInformation();
                                        Anticheat.INSTANCE.getStatisticsManager().upload();
                                    });
                                } else if (s.equalsIgnoreCase("debug") &&
                                        (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getDebugCommand()) || isDev)) {
                                    found = true;
                                    debugCommand.execute(args, s, commandSender);
                                } else if (s.equalsIgnoreCase("banwave") &&
                                        (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getBanwaveCommand()) || isDev)) {
                                    found = true;
                                    banWaveCommand.execute(args, s, commandSender);
                                } else if (s.equalsIgnoreCase("testkb")  &&
                                        (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getTestKnockbackCommand()) || isDev)) {
                                    testKnockBackCommand.execute(args, s, commandSender);
                                    found = true;
                                } else if (s.equalsIgnoreCase("info")
                                        && (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getInfoCommand()) || isDev)) {
                                    infoCommand.execute(args, s, commandSender);
                                    found = true;
                                } else if (s.equalsIgnoreCase("version")
                                        && (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getVersionCommand()) || isDev)) {
                                    versionCommand.execute(args, s, commandSender);
                                    found = true;
                                } else if (s.equalsIgnoreCase("packetlog")
                                        && (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getPacketLogCommand()) || isDev)) {
                                    packetLogCommand.execute(args, s, commandSender);
                                    found = true;
                                } else if (s.equalsIgnoreCase("forcebot")
                                        && (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getForceBot()) || isDev)) {
                                    killAuraCommand.execute(args, s, commandSender);
                                    found = true;
                                } else if (s.equalsIgnoreCase("rotate")
                                        && (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getForceBot()) || isDev)) {
                                    rotateCommand.execute(args, s, commandSender);

                                    found = true;
                                } else if (s.equalsIgnoreCase("top")
                                        && (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getTop()) || isDev)) {
                                    topViolationsCommand.execute(args, s, commandSender);
                                    found = true;
                                } else if (s.equalsIgnoreCase("cps")
                                        && (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getCpsCommand()) || isDev)) {
                                    cpsCommand.execute(args, s, commandSender);
                                    found = true;
                                }else if (s.equalsIgnoreCase("panel")
                                        && (player.hasPermission(Anticheat.INSTANCE.getPermissionValues().getPanelCommand()) || isDev)) {
                                    panelCommand.execute(args, s, commandSender);
                                    found = true;
                                }

                                if (!found) commandSender.sendMessage(Anticheat.INSTANCE.getMessageValues().getSubCommandNoExist());
                            }
                        } else {
                            commandSender.sendMessage(Anticheat.INSTANCE.getMessageValues().getNoPermission());
                        }
                    }
                } else {
                    if (args.length < 1) {
                        commandSender.sendMessage("\n");
                        commandSender.sendMessage(Anticheat.INSTANCE.getMessageValues().getBeginCommandLine());
                        commandSender.sendMessage(line);

                        Anticheat.INSTANCE.getCommandManager().getCommandList().forEach(command -> {

                            if (command.getUsage() == null) return;

                            if (command.getUsage().equalsIgnoreCase("velocity")) {
                                return;
                            }

                            if (command.getCommand().contains("forceban")
                                    || command.getCommand().contains("ping")
                                    || command.getCommand().contains("logs")
                                    || command.getCommand().contains("banwave")
                                    || command.getCommand().contains("testkb")
                                    || command.getCommand().contains("forcebot")
                                    || command.getCommand().contains("version")
                                    || command.getCommand().contains("reload")) {

                                if (command.getCommand().equalsIgnoreCase("ping")) {
                                    return;
                                }

                                TextComponent textComponent = new TextComponent(ChatColor.GRAY + "» " + ChatColor.WHITE
                                        + "/" + command.getCommand() + ChatColor.GRAY + " - " + ChatColor.RED
                                        + command.getDescription());

                                // Convert the TextComponent to JSON format
                                String json = ComponentSerializer.toString(textComponent);

                                // Send the JSON-formatted message to the console
                                Bukkit.getServer().getConsoleSender().sendMessage(json);

                            }
                        });

                        commandSender.sendMessage(line);
                    } else {
                        String s = args[0];
                        boolean found = false;

                        if (s.equalsIgnoreCase("forceban")) {
                            found = true;
                            forceBanCommand.execute(args, s, commandSender);
                        } if (s.equalsIgnoreCase("ping")) {
                            found = true;
                            pingCommand.execute(args, s, commandSender);
                        } else if (s.equalsIgnoreCase("logs")) {
                            found = true;
                            logsCommand.execute(args, s, commandSender);
                        } else if (s.equalsIgnoreCase("reload")) {
                            found = true;
                            reloadCommand.execute(args, s, commandSender);
                        } else if (s.equalsIgnoreCase("banwave")) {
                            found = true;
                            banWaveCommand.execute(args, s, commandSender);
                        } else if (s.equalsIgnoreCase("testkb")) {
                            testKnockBackCommand.execute(args, s, commandSender);
                            found = true;
                        } else if (s.equalsIgnoreCase("version")) {
                            versionCommand.execute(args, s, commandSender);
                            found = true;
                        } else if (s.equalsIgnoreCase("forcebot")) {
                            killAuraCommand.execute(args, s, commandSender);
                            found = true;
                        }

                        if (!found) commandSender.sendMessage(Anticheat.INSTANCE.getMessageValues().getSubCommandNoExist());
                    }
                }
            }
        }
        return false;
    }
}