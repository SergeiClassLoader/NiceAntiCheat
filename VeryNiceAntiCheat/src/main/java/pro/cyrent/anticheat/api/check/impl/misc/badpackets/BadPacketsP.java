package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "P",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Spamming release item packets",
        punishmentVL = 20,
        punishable = false,
        state = CheckState.BETA)
public class BadPacketsP extends Check {
    private int ticks;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
                if (getData().generalCancel()
                        || getData().isBedrock()) {
                    this.ticks = 0;
                    return;
                }

                WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event.getPacketReceiveEvent());

                if (digging.getAction() == DiggingAction.RELEASE_USE_ITEM) {
                    assert getData().getPlayer().getItemInHand() != null;

                    if (getData().isSword(getData().getPlayer().getItemInHand())) {
                        this.ticks++;

                        if (this.ticks > 40) {
                            this.fail("amountSent="+this.ticks);
                        }
                    }
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {

                if (getData().generalCancel()
                        || getData().getLastWorldChange().hasNotPassed(20)
                        || getData().getLastTeleport().hasNotPassed(20)
                        || getData().getActionProcessor().isTeleportingV3()
                        || getData().getActionProcessor().isTeleportingReal()
                        || getData().getActionProcessor().isTeleporting()
                        || getData().getTransactionProcessor().getTransactionPingDrop() > 100) {
                    this.ticks = 0;
                    return;
                }

                assert getData().getPlayer().getItemInHand() != null;

                if (getData().isSword(getData().getPlayer().getItemInHand())) {
                    this.ticks = 0;
                }
            }
        }
    }
}
