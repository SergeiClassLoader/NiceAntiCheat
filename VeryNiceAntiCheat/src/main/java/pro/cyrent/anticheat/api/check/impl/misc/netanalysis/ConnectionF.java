package pro.cyrent.anticheat.api.check.impl.misc.netanalysis;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInformation(
        name = "Connection",
        subName = "F",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.NET_ANALYSIS,
        description = "Detects if a player is blinking constantly while in fights",
        punishable = false,
        experimental = true,
        state = CheckState.BETA)
public class ConnectionF extends Check {

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
                if (getData().getBackTrackProcessor().isFlaggedCheckF()) {
                    this.fail("Combat connection was analyzed");
                    getData().getBackTrackProcessor().setFlaggedCheckF(false);
                }
            }
        }
    }
}