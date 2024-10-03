package pro.cyrent.anticheat.api.check.impl.misc.badpackets;


import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "BadPackets",
        subName = "J1",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if a player sends an impossible action packets",
        punishable = false,
        experimental = true,
        state = CheckState.PRE_BETA)
public class BadPacketsJ1 extends Check {

    /**
     * THIS CHECK IS LITERALLY IMPOSSIBLE TO FALSE!
     * The client can never send any type of action packet as another entity id, it can only ever be themselves!
     */

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
                WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event.getPacketReceiveEvent());

                if (action.getEntityId() != getData().getPlayer().getEntityId() && !getData().generalCancel()) {
                    // Zibb ik ur gonna remove this but sometimes the description is too long thats why i put
                    // Another detail to decribe what the check does when flagged.
                    this.fail("Action entity id doesn't match the player",
                            "packet-entity-id="+action.getEntityId(),
                            "player-id="+getData().getPlayer().getEntityId());
                }
            }
        }
    }
}
