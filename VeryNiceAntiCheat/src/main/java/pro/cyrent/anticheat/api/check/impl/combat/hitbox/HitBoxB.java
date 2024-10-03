package pro.cyrent.anticheat.api.check.impl.combat.hitbox;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "HitBox",
        subName = "B",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.HITBOX,
        description = "Detects hitbox that modify packets to be incorrect.",
        punishmentVL = 1,
        state = CheckState.RELEASE)
public class HitBoxB extends Check {

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interactEntity = new
                        WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                Entity entity = SpigotReflectionUtil.getEntityById(interactEntity.getEntityId());

                if (entity instanceof Player) {

                    if (getData().generalCancel()) {
                        return;
                    }

                    boolean newerClient = getData().getProtocolVersion() > 47;

                    if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT
                            || interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.INTERACT) {

                        if (interactEntity.getTarget().isPresent()) {

                            double x = Math.abs(interactEntity.getTarget().get().getX());
                            double y = interactEntity.getTarget().get().getY();
                            double z = Math.abs(interactEntity.getTarget().get().getZ());

                            double maxXZ = newerClient ? 0.3001 : 0.4001;
                            double minY = newerClient ? 0.0 : -0.100001;
                            double maxY = newerClient ? 1.8001 : 1.9001;

                            if (x > maxXZ || y > maxY || y < minY || z > maxXZ) {
                                this.fail("x=" + x,
                                        "y=" + y,
                                        "z=" + z);
                            }
                        }
                    }
                }
            }
        }
    }
}
