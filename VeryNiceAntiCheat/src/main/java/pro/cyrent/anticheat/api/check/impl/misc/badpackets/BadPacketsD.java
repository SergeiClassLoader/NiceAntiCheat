package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSlotStateChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "D",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if the player sets their slot to the current slot (impossible)",
        state = CheckState.RELEASE)
public class BadPacketsD extends Check {

    private int slotId, lastSlotId;

    private int slotChanged;

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            PacketTypeCommon packetType = event.getPacketReceiveEvent().getPacketType();

            if (packetType == PacketType.Play.Client.SLOT_STATE_CHANGE) {
                WrapperPlayClientSlotStateChange slotStateChange =
                        new WrapperPlayClientSlotStateChange(event.getPacketReceiveEvent());

                if (this.slotChanged > 0) {
                    this.slotChanged--;
                    return;
                }

                if (this.lastSlotId == this.slotId) {
                    if (++this.threshold > 0.95) {
                        this.fail(
                                "slot="+this.slotId,
                                "lastSlot="+this.lastSlotId);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .001);
                }

                this.lastSlotId = this.slotId;
                this.slotId = slotStateChange.getSlot();
            }
        }

        if (event.getPacketSendEvent() != null) {

            PacketTypeCommon packetType = event.getPacketSendEvent().getPacketType();

            if (packetType == PacketType.Play.Server.SET_SLOT) {
                WrapperPlayServerSetSlot serverSetSlot = new WrapperPlayServerSetSlot(event.getPacketSendEvent());
                if (this.slotChanged != 1
                        && (serverSetSlot.getSlot() == this.slotId || serverSetSlot.getSlot() == this.lastSlotId)) {
                    this.slotChanged = 1;
                }
            }
        }
    }
}
