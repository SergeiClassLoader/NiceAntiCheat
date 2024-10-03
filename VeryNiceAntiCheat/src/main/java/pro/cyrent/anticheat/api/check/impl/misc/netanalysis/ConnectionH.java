package pro.cyrent.anticheat.api.check.impl.misc.netanalysis;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInformation(
        name = "Connection",
        subName = "H",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.NET_ANALYSIS,
        description = "Detects lag ranges (entropy)",
        punishable = false,
        experimental = true,
        state = CheckState.BETA)
public class ConnectionH extends Check {
    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
                if (getData().getBackTrackProcessor().isFlaggedCheckH()) {
                    this.fail("Combat connection was analyzed");

                    getData().getBackTrackProcessor().setFlaggedCheckH(false);
                }
            }
        }
    }
}