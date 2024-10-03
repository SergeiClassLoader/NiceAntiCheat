package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "R",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Player is sending impossible slot packets",
        punishmentVL = 1,
        state = CheckState.RELEASE)
public class BadPacketsR extends Check {

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
                WrapperPlayClientHeldItemChange heldItemChange =
                        new WrapperPlayClientHeldItemChange(event.getPacketReceiveEvent());

                if (heldItemChange.getSlot() < 0 || heldItemChange.getSlot() > 8) {
                    this.fail("slot="+heldItemChange.getSlot());
                }
            }
        }
    }
}