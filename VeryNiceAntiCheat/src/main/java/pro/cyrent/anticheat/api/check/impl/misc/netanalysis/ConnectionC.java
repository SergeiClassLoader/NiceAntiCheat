package pro.cyrent.anticheat.api.check.impl.misc.netanalysis;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "Connection",
        subName = "C",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.NET_ANALYSIS,
        description = "Detects if a player is sending movements with a delayed connection",
        punishable = false,
        experimental = true,
        enabled = false,
        state = CheckState.ALPHA)
public class ConnectionC extends Check {

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            if (getData().getBackTrackProcessor().isFlaggedCheckC()) {
                this.fail("Connection was analyzed");
                getData().getBackTrackProcessor().setFlaggedCheckC(false);
            }
        }
    }
}