package pro.cyrent.anticheat.api.check.impl.misc.netanalysis;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInformation(
        name = "Connection",
        subName = "E",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.NET_ANALYSIS,
        description = "Detects if a player is blinking high amounts while in fights",
        punishable = false,
        experimental = true,
        enabled = false,
        state = CheckState.ALPHA)
public class ConnectionE extends Check {

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
                if (getData().getBackTrackProcessor().isFlaggedCheckE()) {
                    this.fail("Combat connection was analyzed");
                    getData().getBackTrackProcessor().setFlaggedCheckE(false);
                }
            }
        }
    }
}