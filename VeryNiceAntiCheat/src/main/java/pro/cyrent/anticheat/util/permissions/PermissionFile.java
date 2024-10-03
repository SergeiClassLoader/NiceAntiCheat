package pro.cyrent.anticheat.util.permissions;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class PermissionFile {

    private PermissionFile() {}

    static PermissionFile instance = new PermissionFile();

    public static PermissionFile getInstance() {
        return instance;
    }

    private FileConfiguration config;
    private FileConfiguration data;
    private File dfile;

    public void setup(Plugin p) {
        config = p.getConfig();
        dfile = new File("plugins/LumosAC/permissions.yml");

        if (!dfile.exists()) {
            try {
                dfile.createNewFile();
            } catch (IOException ignored) {
            }
        }

        data = YamlConfiguration.loadConfiguration(dfile);

    }

    public FileConfiguration getData() {
        return data;
    }


    public void writeDefaults() {

        if (!data.contains("Permission.DefaultCommand")) data.set("Permission.DefaultCommand",
                "lumos.command");

        if (!data.contains("Permission.Alert")) data.set("Permission.Alert",
                "lumos.alerts");

        if (!data.contains("Permission.Bypass")) data.set("Permission.Bypass",
                "lumos.bypass");

        if (!data.contains("Permission.Command.Alert")) data.set("Permission.Command.Alert",
                "lumos.command.alert");

        if (!data.contains("Permission.Command.GUI")) data.set("Permission.Command.GUI",
                "lumos.command.gui");
        if (!data.contains("Permission.Command.Stats")) data.set("Permission.Command.Stats",
                "lumos.command.stats");

        if (!data.contains("Permission.Command.ForceBan")) data.set("Permission.Command.ForceBan",
                "lumos.command.forceban");

        if (!data.contains("Permission.Command.Ping")) data.set("Permission.Command.Ping",
                "lumos.command.ping");

        if (!data.contains("Permission.Command.Logs")) data.set("Permission.Command.Logs",
                "lumos.command.logs");

        if (!data.contains("Permission.Command.ClearLogs")) data.set("Permission.Command.ClearLogs",
                "lumos.command.clearlogs");

        if (!data.contains("Permission.Command.Reload")) data.set("Permission.Command.Reload",
                "lumos.command.reload");

        if (!data.contains("Permission.Command.Crash")) data.set("Permission.Command.Crash",
                "lumos.command.crash");

        if (!data.contains("Permission.Command.BanWave")) data.set("Permission.Command.BanWave",
                "lumos.command.banwave");

        if (!data.contains("Permission.Command.ForceKnockBack")) data.set("Permission.Command.ForceKnockBack",
                "lumos.command.kb");

        if (!data.contains("Permission.Command.PlayerInformation")) data.set("Permission.Command.PlayerInformation",
                "lumos.command.info");

        if (!data.contains("Permission.Command.PacketLog")) data.set("Permission.Command.Debug",
                "lumos.command.debug");

        if (!data.contains("Permission.Command.PacketLog")) data.set("Permission.Command.Version",
                "lumos.command.version");

        if (!data.contains("Permission.Command.PacketLog")) data.set("Permission.Command.PacketLog",
                "lumos.command.packetlog");

        if (!data.contains("Permission.Command.ForceBot")) data.set("Permission.Command.ForceBot",
                "lumos.command.forcebot");

        if (!data.contains("Permission.Command.Top")) data.set("Permission.Command.Top",
                "lumos.command.top");

        if (!data.contains("Permission.Command.Rotate")) data.set("Permission.Command.Rotate",
                "lumos.command.rotate");

        if (!data.contains("Permission.Command.Cps")) data.set("Permission.Command.Cps",
                "lumos.command.cps");

        if (!data.contains("Permission.Command.DevAlertsNig")) data.set("Permission.Command.DevAlertsNig",
                "lumos.command.devalertsnig");
        if (!data.contains("Permission.Command.Panel")) data.set("Permission.Command.Panel",
                "lumos.command.panel");

        saveData();
    }

    public void saveData() {
        try {
            data.save(dfile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadData() {
        this.data = YamlConfiguration.loadConfiguration(dfile);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}