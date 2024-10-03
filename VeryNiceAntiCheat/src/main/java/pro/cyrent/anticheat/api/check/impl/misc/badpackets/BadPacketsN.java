package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.Anticheat;
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
        subName = "N",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Checks if the player never sends interactions while blocking",
        punishmentVL = 20,
        punishable = false,
        state = CheckState.BETA)
public class BadPacketsN extends Check {

    private double threshold;

    private int attackTick;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {
                ++this.attackTick;

                if (getData().generalCancel()
                        || getData().isBedrock()) {
                    this.threshold = 0;
                    return;
                }

                if (Anticheat.INSTANCE.getVersionSupport().getClientProtocol(getData()) < 47
                        || Anticheat.INSTANCE.getServerVersion() > 18) {
                    this.threshold = 0;
                    return;
                }

                //this.getCheckBuffer() looks so disgusting
                if (this.attackTick <= 2) {
                    if (this.threshold >= 35) {
                        this.fail("threshold=" + threshold);
                    } else {
                        this.threshold -= Math.min(this.threshold, .005);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .01);
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
                if (this.attackTick <= 2) {
                    if (getData().getPlayer().getItemInHand() != null) {
                        if (getData().isSword(getData().getPlayer().getItemInHand())) {
                            this.threshold++;
                        }
                    }
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interactEntity =
                        new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                Entity entity = SpigotReflectionUtil.getEntityById(interactEntity.getEntityId());

                if (entity instanceof Player) {

                    if (getData().getPlayer().getItemInHand() != null
                            && getData().isSword(getData().getPlayer().getItemInHand())) {

                        if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) {
                            if (interactEntity.getTarget().isPresent()) {
                                this.threshold = 0;
                            }
                        }

                        if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.INTERACT) {
                            if (Anticheat.INSTANCE.getVersionSupport().getClientProtocol(getData()) < 47) {
                                this.threshold = 0;
                            }
                        }

                        if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                            this.attackTick = 0;
                        }
                    }
                }
            }
        }
    }
}
