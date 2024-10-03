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
        subName = "D",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.KILL_AURA,
        punishmentVL = 5,
        description = "Detects if a players switches between 2 entities rapidly (impossible)",
        state = CheckState.RELEASE)
public class KillAuraD extends Check {

    private Entity lastAttackedEntity;
    private long lastTime;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {


            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interactEntity = new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                if (interactEntity.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

                Entity entity = SpigotReflectionUtil.getEntityById(interactEntity.getEntityId());

                if (entity != null) {

                    if (this.lastAttackedEntity != null && this.lastAttackedEntity != entity) {
                        long now = System.currentTimeMillis();

                        long difference = now - this.lastTime;

                        if (difference < 6L) {
                            if (++this.threshold > 9) {
                                this.fail("difference=" + difference, "threshold=" + threshold);
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, .5);
                        }

                        this.lastTime = System.currentTimeMillis();
                    }

                    this.lastAttackedEntity = entity;
                }
            }
        }
    }
}
