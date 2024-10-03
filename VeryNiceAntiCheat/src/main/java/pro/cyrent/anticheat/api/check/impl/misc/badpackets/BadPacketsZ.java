package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckInformation(
        name = "BadPackets",
        subName = "Z",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if the player is using no item release on swords in fights",
        punishmentVL = 10,
        punishable = false,
        experimental = true,
        state = CheckState.BETA)
public class BadPacketsZ extends Check {

    private int times;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() == null) {
            return;
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interactEntity = new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

            if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (getData().getNoSlowDownProcessor().isBlockingPacketsA1()
                        && getData().getPlayer().getItemInHand() != null
                        && getData().isSword(getData().getPlayer().getItemInHand())) {
                    ++this.times;

                    if (this.times >= 20) {
                        this.fail("times="+this.times);
                        this.times = 0;
                    }

                    getData().getNoSlowDownProcessor().setBlockingPacketsA1(false);
                }
            }
        }
    }
}
