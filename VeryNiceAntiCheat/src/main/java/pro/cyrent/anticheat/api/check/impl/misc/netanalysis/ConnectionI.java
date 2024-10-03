package pro.cyrent.anticheat.api.check.impl.misc.netanalysis;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInformation(
        name = "Connection",
        subName = "I",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.NET_ANALYSIS,
        description = "Detects constant flying lag while in combat but not in combat.",
        punishable = false,
        experimental = true,
        state = CheckState.BETA)
public class ConnectionI extends Check {

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
                if (getData().getBackTrackProcessor().isFlaggedCheckI()) {
                    this.fail("Connection was analyzed");
                    getData().getBackTrackProcessor().setFlaggedCheckI(false);
                }
            }
        }
    }
}
