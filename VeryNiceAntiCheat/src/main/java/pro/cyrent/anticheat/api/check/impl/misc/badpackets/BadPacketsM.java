package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "M",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Checks for invalid sword interaction packets",
        punishmentVL = 20,
        punishable = false,
        state = CheckState.BETA)
public class BadPacketsM extends Check {

    private double threshold;

    private int interactAtTick, interactTick;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {

                if (getData().generalCancel()
                        || getData().getProtocolVersion() < 47
                        || getData().isBedrock()) {
                    this.threshold = 0;
                    return;
                }

                if (this.interactTick != this.interactAtTick) {
                    if (++this.threshold > 7) {
                        this.fail("interactAt="+this.interactAtTick,
                                "tnteract="+this.interactTick);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .125);
                }

                this.interactTick = 0;
                this.interactAtTick = 0;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interactEntity =
                        new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                Entity entity = SpigotReflectionUtil.getEntityById(interactEntity.getEntityId());

                if (entity instanceof Player) {

                    if (getData().getPlayer().getItemInHand() != null
                            && getData().isSword(getData().getPlayer().getItemInHand())) {
                        if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT
                                || interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.INTERACT) {
                            if (interactEntity.getTarget().isPresent()) {
                                this.interactAtTick++;
                            } else {
                                this.interactTick++;
                            }
                        }
                    }
                }
            }
        }
    }
}
