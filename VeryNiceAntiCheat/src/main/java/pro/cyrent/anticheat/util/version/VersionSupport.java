package pro.cyrent.anticheat.util.version;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.version.support.VanillaSupport;
import pro.cyrent.anticheat.util.version.support.ViaVersionSupport;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class VersionSupport {
    private IVersion iVersion = new VanillaSupport();

    public VersionSupport() {
        boolean doRunnable = true;

        // ignore runnable if we found our hook already
        if (this.findSupport() || iVersion instanceof VanillaSupport) {
            doRunnable = false;
            this.iVersion.onLoad();
        }


        // should we do the runnable to check for a late load
        if (doRunnable) {

            // looks for 10 seconds
            new BukkitRunnable() {
                int i = 0;

                @Override
                public void run() {
                    if (i++ > 1 || findSupport()) {
                        i = 0;
                        iVersion.onLoad();

                        this.cancel();
                    }
                }
            }.runTaskTimerAsynchronously(Anticheat.INSTANCE.getPlugin(), 20L, 20L);
        }
    }

    boolean findSupport() {
        if (this.isPluginEnabled("ViaVersion")) {
            this.iVersion = new ViaVersionSupport();
            return true;
        }

        return false;
    }

    public int getClientProtocol(PlayerData user) {
        return (user.protocolVersion = this.iVersion.getClientVersion(user));
    }

    boolean isPluginEnabled(String s) {
        return Bukkit.getPluginManager().getPlugin(s) != null && Bukkit.getPluginManager().getPlugin(s).isEnabled();
    }

    public Versions getClientVersion(PlayerData user) {
        switch (Anticheat.INSTANCE.getVersionSupport().getClientProtocol(user)) {

            case 4:
                return Versions.V1_7_5;

            case 5:
                return Versions.V1_7_10;

            case 47:
                return Versions.V1_8;

            case 107:
                return Versions.V1_9;

            case 108:
                return Versions.V1_9_1;

            case 109:
                return Versions.V1_9_2;

            case 110:
                return Versions.V1_9_3; // 1.9.3 - 1.9.4

            case 210:
                return Versions.V1_10;

            case 315:
                return Versions.V1_11;

            case 316:
                return Versions.V1_11_1;

            case 335:
                return Versions.V1_12;

            case 338:
                return Versions.V1_12_1;

            case 340:
                return Versions.V1_12_2;

            case 393:
                return Versions.V1_13;

            case 401:
                return Versions.V1_13_1;

            case 404:
                return Versions.V1_13_2;

            case 477:
                return Versions.V1_14;

            case 480:
                return Versions.V1_14_1;

            case 485:
                return Versions.V1_14_2;

            case 490:
                return Versions.V1_14_3;

            case 498:
                return Versions.V1_14_4;

            case 573:
                return Versions.V1_15;

            case 575:
                return Versions.V1_15_1;

            case 578:
                return Versions.V1_15_2;

            case 735:
                return Versions.V1_16;

            case 736:
                return Versions.V1_16_1;

            case 751:
                return Versions.V1_16_2;

            case 753:
                return Versions.V1_16_3;

            case 754:
                return Versions.V1_16_4;

            case 755:
                return Versions.V1_17;

            case 756:
                return Versions.V1_17_1;

            case 757:
                return Versions.V1_18;

            case 758:
                return Versions.V1_18_2;

            case 759:
                return Versions.V1_19;

            case 760:
                return Versions.V1_19_1;

            case 761:
                return Versions.V1_19_3;

            case 762:
                return Versions.V1_19_4;

            case 763:
                return Versions.V1_20;

            case 764:
                return Versions.V1_20_2;

            case 765:
                return Versions.V1_20_3;
        }

        return Versions.UNKNOWN;
    }

    public enum Versions {
        UNKNOWN,
        V1_7_5,
        V1_7_10,
        V1_8,
        V1_9,
        V1_9_1,
        V1_9_2,
        V1_9_3,
        V1_10,
        V1_11,
        V1_11_1,
        V1_12,
        V1_12_1,
        V1_12_2,
        V1_13,
        V1_13_1,
        V1_13_2,
        V1_14,
        V1_14_1,
        V1_14_2,
        V1_14_3,
        V1_14_4,
        V1_15,
        V1_15_1,
        V1_15_2,
        V1_16,
        V1_16_1,
        V1_16_2,
        V1_16_3,
        V1_16_4,
        V1_17,
        V1_17_1,
        V1_18,
        V1_18_2,
        V1_19,
        V1_19_1,
        V1_19_3,
        V1_19_4,
        V1_20,
        V1_20_2,
        V1_20_3,
    }
}
