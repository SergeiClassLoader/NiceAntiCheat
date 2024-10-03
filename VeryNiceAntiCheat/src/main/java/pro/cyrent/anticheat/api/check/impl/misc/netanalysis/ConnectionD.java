package pro.cyrent.anticheat.api.check.impl.misc.netanalysis;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "Connection",
        subName = "D",
        checkNameEnum = CheckName.NET_ANALYSIS,
        checkType = CheckType.MISC,
        description = "Detects if a player is delaying Transactions but not KeepAlives",
        punishable = false,
        experimental = true,
        state = CheckState.BETA)
public class ConnectionD extends Check {

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            if (getData().getBackTrackProcessor().isFlaggedCheckD()) {
                this.fail("KeepAlive Time=" + getData().getTransactionProcessor().getKeepAlivePing(),
                        "Transaction Time=" + getData().getTransactionProcessor().getTransactionPing(),
                        "Post Time=" + getData().getTransactionProcessor().getPostTransactionPing());

                getData().getBackTrackProcessor().setFlaggedCheckD(false);
            }
        }
    }
}