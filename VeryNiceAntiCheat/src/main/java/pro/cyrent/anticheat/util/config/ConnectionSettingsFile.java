package pro.cyrent.anticheat.util.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class ConnectionSettingsFile {

    private ConnectionSettingsFile() {}

    static ConnectionSettingsFile instance = new ConnectionSettingsFile();

    public static ConnectionSettingsFile getInstance() {
        return instance;
    }

    private FileConfiguration config;
    private FileConfiguration data;
    private File dfile;

    public void setup(Plugin p) {
        config = p.getConfig();
        dfile = new File("plugins/LumosAC/connection-settings.yml");

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

        data.options().header("ConnectionMaxPing Kick = Too high of ping for a long period\n"
                + "ConnectionConfirmation.Tick.Max = Not sending back connection confirmations while moving\n"
                + "NoConnectionResponse = Too long of a time with no connection response\n"
                + "ConnectionQueue = Overall high transaction queue size\n"
                + "ConnectionCombat = Too many transactions in the queue while in combat.");


        data.options().header("ConnectionConfirmation.Tick.Max = Not sending back connection confirmations while moving");

        if (!data.contains("ConnectionMaxPing.Kick.Enabled")) data.set("ConnectionMaxPing.Kick.Enabled", true);

        if (!data.contains("ConnectionMaxPing.PingThreshold.Max")) data.set("ConnectionMaxPing.PingThreshold.Max",
                450);
        if (!data.contains("ConnectionQueue.Overall.Max")) data.set("ConnectionQueue.Overall.Max",
                1500);
        if (!data.contains("ConnectionQueue.UseOverall")) data.set("ConnectionQueue.UseOverall", true);

        if (!data.contains("ConnectionQueue.Threshold.Max")) data.set("ConnectionQueue.Threshold.Max",
                3);


        if (!data.contains("ConnectionCombat.Queue.Max")) data.set("ConnectionCombat.Queue.Max",
                370);
        if (!data.contains("ConnectionCombat.Threshold.Max")) data.set("ConnectionCombat.Threshold.Max",
                40);
        if (!data.contains("ConnectionConfirmation.Tick.Max")) data.set("ConnectionConfirmation.Tick.Max",
                500);

        if (!data.contains("NoConnectionResponse.Kick.Enabled"))
            data.set("NoConnectionResponse.Kick.Enabled", true);

        if (!data.contains("OutOfOrder.Kick.Enabled"))
            data.set("OutOfOrder.Kick.Enabled", true);

        //ConnectionConfirm.Threshold.Max

       /* Anticheat.INSTANCE.getConnectionValues().setConnectionPingThreshold(
                PermissionFile.getInstance().getData().getInt("ConnectionMaxPing.PingThreshold.Max"));

        Anticheat.INSTANCE.getConnectionValues().setPreMapSize(
                PermissionFile.getInstance().getData().getInt("ConnectionQueue.PreMap.Max"));
        Anticheat.INSTANCE.getConnectionValues().setPostMapSize(
                PermissionFile.getInstance().getData().getInt("ConnectionQueue.PostMap.Max"));
        Anticheat.INSTANCE.getConnectionValues().setOverallSize(
                PermissionFile.getInstance().getData().getInt("ConnectionQueue.Overall.Max"));
        Anticheat.INSTANCE.getConnectionValues().setFixAbuseThreshold(
                PermissionFile.getInstance().getData().getInt("ConnectionQueue.Threshold.Max"));

        Anticheat.INSTANCE.getConnectionValues().setQueuedSizeCombat(
                PermissionFile.getInstance().getData().getInt("ConnectionCombat.Queue.Max"));
        Anticheat.INSTANCE.getConnectionValues().setLastSentTransactionThreshold(
                PermissionFile.getInstance().getData().getInt("ConnectionCombat.Threshold.Max"));


        Anticheat.INSTANCE.getConnectionValues().setConfirmTick(
                PermissionFile.getInstance().getData().getInt("ConnectionConfirm.Tick.Max"));
        Anticheat.INSTANCE.getConnectionValues().setConfirmTickThreshold(
                PermissionFile.getInstance().getData().getInt("ConnectionConfirm.Threshold.Max"));*/


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
