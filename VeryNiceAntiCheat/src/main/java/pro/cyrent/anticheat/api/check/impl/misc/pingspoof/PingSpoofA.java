package pro.cyrent.anticheat.api.check.impl.misc.pingspoof;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "PingSpoof",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.PING_SPOOF,
        description = "Kicks for no ping from the keep alive after long periods",
        experimental = true,
        state = CheckState.PRE_ALPHA)
public class PingSpoofA extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            double transactionPing = getData().getTransactionProcessor().getTransactionPing();
            double keepAlivePing = getData().getTransactionProcessor().getKeepAlivePing();

            if (keepAlivePing == 0 && Math.abs(transactionPing - keepAlivePing) > 50
                    && !getData().generalCancel() && getData().getMovementProcessor().getTick() > 200) {

                if (++this.threshold >= 500) {

                    getData().sendDevAlert(
                            "keep-alive="+keepAlivePing,
                            "transaction"+transactionPing);
                    this.getData().kickPlayer(
                            "Ping Spoof A: Not having any ping request from the keep alive for too long.");
                }
            }
        }
    }
}
