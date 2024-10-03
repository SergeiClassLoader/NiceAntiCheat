package pro.cyrent.anticheat.api.check.impl.misc.netanalysis;

import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Connection",
        subName = "K",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.NET_ANALYSIS,
        description = "Detects invalid thread desync with keep alive packets (funny vape packet)",
        punishmentVL = 40.0,
        punishable = false,
        state = CheckState.ALPHA)
public class ConnectionK extends Check {

}
