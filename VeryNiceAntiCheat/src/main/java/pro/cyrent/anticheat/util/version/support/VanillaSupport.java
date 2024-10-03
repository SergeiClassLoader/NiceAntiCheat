package pro.cyrent.anticheat.util.version.support;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.version.IVersion;

public class VanillaSupport implements IVersion {

    private int serverToProtocol;

    @Override
    public int getClientVersion(PlayerData user) {
        return this.serverToProtocol;
    }

    @Override
    public void onLoad() {
        Anticheat.INSTANCE.getPlugin().getLogger()
                .info("Didn't find any protocol support plugins, not hooking into anything.");

        this.serverToProtocol = this.serverToProtocol();
    }

    int serverToProtocol() {

        switch (Anticheat.INSTANCE.getServerVersion()) {

            case 17: {
                return 5;
            }

            case 18: {
                return 47;
            }

            case 122: {
                return 340;
            }
        }

        return -1;
    }
}
