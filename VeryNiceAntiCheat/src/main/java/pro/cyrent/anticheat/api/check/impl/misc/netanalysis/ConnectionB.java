package pro.cyrent.anticheat.api.check.impl.misc.netanalysis;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "Connection",
        subName = "B",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.NET_ANALYSIS,
        description = "Player is delaying packets excessive amounts while in combat",
        punishable = false,
        enabled = false,
        experimental = true,
        state = CheckState.ALPHA)
public class ConnectionB extends Check {
    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            if (getData().getBackTrackProcessor().isFlaggedCheckB()) {
                this.fail("Combat connection was analyzed");
                getData().getBackTrackProcessor().setFlaggedCheckB(false);
            }
        }
    }
}