package pro.cyrent.anticheat.util.permissions;


import pro.cyrent.anticheat.Anticheat;

public class PermissionLoader {

    public void load() {
        PermissionFile.getInstance().setup(Anticheat.INSTANCE.getPlugin());
        PermissionFile.getInstance().writeDefaults();

        Anticheat.INSTANCE.getPermissionValues().setDefaultCommand(PermissionFile.getInstance().getData().getString("Permission.DefaultCommand"));

        Anticheat.INSTANCE.getPermissionValues().setAlert(PermissionFile.getInstance().getData().getString("Permission.Alert"));

        Anticheat.INSTANCE.getPermissionValues().setBypass(PermissionFile.getInstance().getData().getString("Permission.Bypass"));

        Anticheat.INSTANCE.getPermissionValues().setAlertCommand(PermissionFile.getInstance().getData().getString("Permission.Command.Alert"));

        Anticheat.INSTANCE.getPermissionValues().setGuiCommand(PermissionFile.getInstance().getData().getString("Permission.Command.GUI"));

        Anticheat.INSTANCE.getPermissionValues().setStatsGuiCommand(PermissionFile.getInstance().getData().getString("Permission.Command.Stats"));

        Anticheat.INSTANCE.getPermissionValues().setForcebanCommand(PermissionFile.getInstance().getData().getString("Permission.Command.ForceBan"));

        Anticheat.INSTANCE.getPermissionValues().setPingCommand(PermissionFile.getInstance().getData().getString("Permission.Command.Ping"));

        Anticheat.INSTANCE.getPermissionValues().setLogsCommand(PermissionFile.getInstance().getData().getString("Permission.Command.Logs"));

        Anticheat.INSTANCE.getPermissionValues().setClearLogsCommand(PermissionFile.getInstance().getData().getString("Permission.Command.ClearLogs"));

        Anticheat.INSTANCE.getPermissionValues().setReloadCommand(PermissionFile.getInstance().getData().getString("Permission.Command.Reload"));

        Anticheat.INSTANCE.getPermissionValues().setCrashCommand(PermissionFile.getInstance().getData().getString("Permission.Command.Crash"));

        Anticheat.INSTANCE.getPermissionValues().setBanwaveCommand(PermissionFile.getInstance().getData().getString("Permission.Command.BanWave"));

        Anticheat.INSTANCE.getPermissionValues().setTestKnockbackCommand(PermissionFile.getInstance().getData().getString("Permission.Command.ForceKnockBack"));

        Anticheat.INSTANCE.getPermissionValues().setInfoCommand(PermissionFile.getInstance().getData().getString("Permission.Command.PlayerInformation"));

        Anticheat.INSTANCE.getPermissionValues().setDebugCommand(PermissionFile.getInstance().getData().getString("Permission.Command.Debug"));

        Anticheat.INSTANCE.getPermissionValues().setVersionCommand(PermissionFile.getInstance().getData().getString("Permission.Command.Version"));

        Anticheat.INSTANCE.getPermissionValues().setPacketLogCommand(PermissionFile.getInstance().getData().getString("Permission.Command.PacketLog"));

        Anticheat.INSTANCE.getPermissionValues().setForceBot(PermissionFile.getInstance().getData().getString("Permission.Command.ForceBot"));

        Anticheat.INSTANCE.getPermissionValues().setTop(PermissionFile.getInstance().getData().getString("Permission.Command.Top"));

        Anticheat.INSTANCE.getPermissionValues().setCpsCommand(PermissionFile.getInstance().getData().getString("Permission.Command.Cps"));

        Anticheat.INSTANCE.getPermissionValues().setDevAlerts(PermissionFile.getInstance().getData().getString("Permission.Command.DevAlertsNig"));

        Anticheat.INSTANCE.getPermissionValues().setPanelCommand(PermissionFile.getInstance().getData().getString("Permission.Command.Panel"));

    }
}