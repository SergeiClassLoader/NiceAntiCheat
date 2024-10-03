package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import pro.cyrent.anticheat.api.check.CheckInformation;

@CheckInformation(
        name = "BadPackets",
        subName = "G1",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects cheats that use 'OldAnimations' mod but are done incorrectly.",
        punishable = false,
        experimental = true,
        state = CheckState.PRE_BETA)
public class BadPacketsG1 extends Check {

    private double threshold;
    private boolean digging = false;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
                WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event.getPacketReceiveEvent());

                if (digging.getAction() == DiggingAction.START_DIGGING) {
                    this.digging = true;
                } else if (digging.getAction() == DiggingAction.CANCELLED_DIGGING
                        || digging.getAction() == DiggingAction.FINISHED_DIGGING) {
                    this.digging = false;
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ANIMATION) {

                if (getData().generalCancel() || getData().getProtocolVersion() != 47) {
                    this.threshold = 0;
                    return;
                }

                if (getData().getPlayer().isBlocking() && this.digging) {
                    if (++this.threshold > 20.0) {
                        this.fail("Mining/swinging while blocking with a sword.");
                    }
                } else {
                    this.threshold = 0;
                }
            }
        }
    }
}
