package pro.cyrent.anticheat.api.check.impl.combat.killaura;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.entity.Entity;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "KillAura",
        subName = "E",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.KILL_AURA,
        punishmentVL = 5,
        description = "Detects if a players is sending multiple attacks at once (impossible)",
        state = CheckState.RELEASE)
public class KillAuraE extends Check {

    private Entity lastAttackedEntity;
    private int packets;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {

                WrapperPlayClientInteractEntity interactEntity =
                        new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                if (interactEntity.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

                Entity entity = SpigotReflectionUtil.getEntityById(interactEntity.getEntityId());

                if (entity != null) {

                    if (this.lastAttackedEntity != null && this.lastAttackedEntity != entity) {
                        this.packets++;

                        if (this.packets > 1) {
                            if (this.threshold++ > 9) {
                                this.fail("packets="+this.packets);
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, 0.75);
                        }
                    }
                    this.lastAttackedEntity = entity;
                }
            }

            if (event.isMovement() || event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
                this.packets = 0;
            }
        }
    }
}
