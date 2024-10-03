package pro.cyrent.anticheat.api.check.impl.misc.netanalysis;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "Connection",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.NET_ANALYSIS,
        description = "Detects if a player is delaying connection in combat (backtrack)",
        punishable = false,
        experimental = true,
        state = CheckState.BETA)
public class ConnectionA extends Check {

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            if (getData().getBackTrackProcessor().isFlaggedCheckA()) {

                this.fail(
                        "Combat connection was analyzed");

                getData().getBackTrackProcessor().setFlaggedCheckA(false);
            }
        }
    }
}