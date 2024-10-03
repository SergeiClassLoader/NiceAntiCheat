package pro.cyrent.anticheat.util.config;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.util.permissions.PermissionFile;

public class ConnectionLoader {

    public void load() {
        ConnectionSettingsFile.getInstance().setup(Anticheat.INSTANCE.getPlugin());
        ConnectionSettingsFile.getInstance().writeDefaults();

        Anticheat.INSTANCE.getConnectionValues().setPingKick(
                ConnectionSettingsFile.getInstance().getData().getBoolean("ConnectionMaxPing.Kick.Enabled"));

        Anticheat.INSTANCE.getConnectionValues().setConnectionPingThreshold(
                ConnectionSettingsFile.getInstance().getData().getInt("ConnectionMaxPing.PingThreshold.Max"));

        Anticheat.INSTANCE.getConnectionValues().setOverallSize(
                ConnectionSettingsFile.getInstance().getData().getInt("ConnectionQueue.Overall.Max"));

        Anticheat.INSTANCE.getConnectionValues().setUseOverall(
                ConnectionSettingsFile.getInstance().getData().getBoolean("ConnectionQueue.UseOverall"));

        Anticheat.INSTANCE.getConnectionValues().setFixAbuseThreshold(
                ConnectionSettingsFile.getInstance().getData().getInt("ConnectionQueue.Threshold.Max"));

        Anticheat.INSTANCE.getConnectionValues().setQueuedSizeCombat(
                ConnectionSettingsFile.getInstance().getData().getInt("ConnectionCombat.Queue.Max"));
        Anticheat.INSTANCE.getConnectionValues().setLastSentTransactionThreshold(
                ConnectionSettingsFile.getInstance().getData().getInt("ConnectionCombat.Threshold.Max"));

        Anticheat.INSTANCE.getConnectionValues().setConfirmTick(
                ConnectionSettingsFile.getInstance().getData().getInt("ConnectionConfirmation.Tick.Max"));

        Anticheat.INSTANCE.getConnectionValues().setLongDistance(
                ConnectionSettingsFile.getInstance().getData().getBoolean("NoConnectionResponse.Kick.Enabled"));

        Anticheat.INSTANCE.getConnectionValues().setLongDistance(
                ConnectionSettingsFile.getInstance().getData().getBoolean("OutOfOrder.Kick.Enabled"));

    }
}