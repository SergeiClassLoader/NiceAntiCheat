package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckInformation(
        name = "BadPackets",
        subName = "X",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if a player sends constant interact_at vectors",
        punishmentVL = 10,
        punishable = false,
        experimental = true,
        state = CheckState.ALPHA)
public class BadPacketsX extends Check {

    private double threshold;
    private int lastPlace;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {

            if (event.isMovement()) {
                this.lastPlace -= Math.min(this.lastPlace, 1);
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interactEntity = new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) {
                    if (interactEntity.getTarget().isPresent()) {

                        if (getData().getMovementProcessor().getDeltaYawAbs() > 0.5F
                                && getData().getMovementProcessor().getDeltaPitchAbs() > 0.5F) {

                            Vector3f vector3f = interactEntity.getTarget().get();

                            if (this.lastPlace < 17) {
                                return;
                            }

                            // raven auto block patch
                            if (vector3f.x == 0.4 && vector3f.z != 0.4 || vector3f.z == 0.4
                                    && vector3f.x != 0.4) {
                                if (++this.threshold > 10) {
                                    this.fail("threshold=" + threshold);
                                }
                            } else {
                                this.threshold -= Math.min(this.threshold, .5);
                            }
                        }
                    }
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {

                if (getData().getPlayer().getItemInHand() == null) return;

                if (getData().isSword(getData().getPlayer().getItemInHand())) {
                    this.lastPlace = 20;
                }
            }
        }
    }
}
