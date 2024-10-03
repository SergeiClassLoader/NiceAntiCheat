package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import org.bukkit.GameMode;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "E",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if the player sends creative packets in survival",
        punishmentVL = 1,
        state = CheckState.RELEASE)
public class BadPacketsE extends Check {

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            PacketTypeCommon packetType = event.getPacketReceiveEvent().getPacketType();

            if (getData().generalCancel()
                    || getData().getMovementProcessor().getLastFlightTimer().getDelta() < 40
                    + getData().getTransactionProcessor().getPingTicks()
                    && getData().getMovementProcessor().getLastFlightTimer().isSet()
                    || getData().getPlayer().getGameMode() == GameMode.CREATIVE) return;

            if (packetType == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION) {
                this.fail("Sending creative inventory actions while in survival");
            }
        }
    }
}
