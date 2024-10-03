package pro.cyrent.anticheat.impl.processor.entity;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.player.PlayerUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public class EntityTrackingProcessor extends Event {
    private final PlayerData data;

    @Getter
    private final Set<Integer> cachedBoatEntities = new ConcurrentSkipListSet<>();

    public EntityTrackingProcessor(PlayerData user) {
        this.data = user;
    }

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketSendEvent() != null) {

            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.SPAWN_ENTITY) {
                WrapperPlayServerSpawnEntity spawnEntity = new WrapperPlayServerSpawnEntity(event.getPacketSendEvent());

                Entity entity = SpigotReflectionUtil.getEntityById(spawnEntity.getEntityId());

                if (entity == null) return;

                if (entity.getType() == org.bukkit.entity.EntityType.BOAT) {
                    this.cachedBoatEntities.add(spawnEntity.getEntityId());
                }
            }

            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.DESTROY_ENTITIES) {
                WrapperPlayServerDestroyEntities destroyEntities =
                        new WrapperPlayServerDestroyEntities(event.getPacketSendEvent());

                for (int entity : destroyEntities.getEntityIds()) {
                    this.cachedBoatEntities.remove(entity);
                }
            }
        }

        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {

                FlyingLocation location = getData().getMovementProcessor().getTo();


                // This will check most of the time when on ground or jumping (depends)
                if (Anticheat.INSTANCE.getTaskManager().getTick() % 5 == 0) {

                    for (Integer entityId : this.cachedBoatEntities) {
                        Entity entity = SpigotReflectionUtil.getEntityById(entityId);

                        if (entity == null) {
                            continue;
                        }

                        Boat boat = (Boat) entity;

                        double distanceSquared = Math.abs(location.distanceXZ(boat.getLocation()));

                        if (distanceSquared > 120) {
                            this.cachedBoatEntities.remove(entityId);
                            continue;
                        }

                        if (distanceSquared > 25) {
                            continue;
                        }

                        if (boat.getPassenger() != null && boat.getPassenger().getType() == EntityType.PLAYER) {
                            return;
                        }

                        this.freezeBoat(boat, getData().getPlayer());
                    }
                }
            }
        }
    }

    private void freezeBoat(Boat boat, Player player) {
        WrapperPlayServerEntityTeleport entityTeleport = new WrapperPlayServerEntityTeleport(boat.getEntityId(),
                new com.github.retrooper.packetevents.protocol.world.Location(boat.getLocation().getX(),
                        boat.getLocation().getY(), boat.getLocation().getZ(), boat.getLocation().getYaw(),
                        boat.getLocation().getPitch()), boat.isOnGround());

        PlayerUtil.sendPacket(entityTeleport, player);
    }
}