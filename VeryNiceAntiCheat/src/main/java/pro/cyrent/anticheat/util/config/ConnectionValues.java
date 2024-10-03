package pro.cyrent.anticheat.util.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectionValues {

    private int connectionPingThreshold;

    private int postMapSize, preMapSize, overallSize;
    private int fixAbuseThreshold;

    private int queuedSizeCombat, lastSentTransactionThreshold;

    private int confirmTick;


    private boolean usePrePostMap, useOverall, pingKick, longDistance, OutOfOrder;
}