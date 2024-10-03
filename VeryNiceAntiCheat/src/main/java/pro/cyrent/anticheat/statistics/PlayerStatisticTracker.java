package pro.cyrent.anticheat.statistics;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class PlayerStatisticTracker {

    @Getter
    private final Map<ClientVersion, Integer> clientVersionMap = new HashMap<>();

    @Getter
    private final Map<String, Integer> clientBrandMap = new HashMap<>();

    private final String INVALID_BRAND_1 = "<";
    private final String INVALID_BRAND_2 = ">";
    private final String INVALID_BRAND_3 = ":";
    private final String INVALID_BRAND_4 = "<";
    private final String INVALID_BRAND_5 = "<";
    private final String SPLIT = ":";
    private final String SPLIT_2 = ";";
    private final String LUNAR_CLIENT_BRAND = "lunarclient:";

    public void addClientVersion(ClientVersion version) {
        if (version == ClientVersion.UNKNOWN) return;

        this.clientVersionMap.put(version, this.clientVersionMap.getOrDefault(version, 0) + 1);
    }

    public void addClientBrand(String clientBrand) {

        String fixedBrand = clientBrand;

        // Fixes Lunar Client MC|Brand payload
        if (fixedBrand.startsWith(this.LUNAR_CLIENT_BRAND)) {
            fixedBrand = "lunarclient";
        }

        if (this.invalidClientBrand(fixedBrand)) return;

        this.clientBrandMap.put(fixedBrand, this.clientBrandMap.getOrDefault(fixedBrand, 0) + 1);
    }

    boolean invalidClientBrand(String clientBrand) {
        return clientBrand.contains(this.SPLIT) || clientBrand.contains(this.INVALID_BRAND_1)
                || clientBrand.contains(this.INVALID_BRAND_4) || clientBrand.contains(this.INVALID_BRAND_5)
                || clientBrand.contains(this.INVALID_BRAND_2) || clientBrand.contains(this.INVALID_BRAND_3);
    }

    public void clear() {
        this.clientBrandMap.clear();
        this.clientVersionMap.clear();
    }
}
