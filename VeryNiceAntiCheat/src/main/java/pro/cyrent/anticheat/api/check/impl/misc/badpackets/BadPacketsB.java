package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "B",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if the player blocks or digs while attacking (with a sword)",
        punishmentVL = 10,
        state = CheckState.RELEASE)
public class BadPacketsB extends Check {

    private double threshold;

    private boolean digging, blocking;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            PacketTypeCommon packetType = event.getPacketReceiveEvent().getPacketType();

            if (packetType == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
                if (getData().getPlayer().getItemInHand() != null) {
                    if (getData().isSword(getData().getPlayer().getItemInHand())) {
                        this.blocking = true;
                    }
                }
            }

            if (packetType == PacketType.Play.Client.PLAYER_DIGGING) {
                WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event.getPacketReceiveEvent());

                if (digging.getAction() == DiggingAction.RELEASE_USE_ITEM) {
                    if (getData().getPlayer().getItemInHand() != null) {
                        if (getData().isSword(getData().getPlayer().getItemInHand())) {
                            this.digging = true;
                        }
                    }
                }
            }

            if (packetType == PacketType.Play.Client.CLOSE_WINDOW || packetType == PacketType.Play.Server.OPEN_WINDOW) {
                this.blocking = this.digging = false;
            }

            if (event.isMovement()) {
                this.digging = this.blocking = false;
            }

            if (packetType == PacketType.Play.Client.INTERACT_ENTITY) {

                if (getData().generalCancel()
                        || getData().isBedrock()
                        || getData().getMovementProcessor().getSkippedPackets() > 0
                        || getData().getMovementProcessor().getLastFlyingPauseTimer().getDelta() < 10
                        || Anticheat.INSTANCE.getVersionSupport().getClientProtocol(getData()) > 47
                        || getData().getActionProcessor().isTeleportingV2()) {
                    this.threshold = 0;
                    return;
                }

                WrapperPlayClientInteractEntity interactEntity = new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                    if (this.digging || this.blocking) {
                        if (++this.threshold > 5) {
                            this.fail("threshold=" + threshold);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .025);
                    }
                }
            }
        }
    }
}
