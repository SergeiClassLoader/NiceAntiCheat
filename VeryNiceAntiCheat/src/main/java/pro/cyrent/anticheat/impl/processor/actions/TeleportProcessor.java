package pro.cyrent.anticheat.impl.processor.actions;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import lombok.Getter;
import lombok.Setter;
import pro.cyrent.anticheat.util.Callback;
import pro.cyrent.anticheat.util.TeleportData;
import pro.cyrent.anticheat.util.task.TaskData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;


@Getter
@Setter
public class TeleportProcessor extends Event {
    private final PlayerData data;
    public final ConcurrentLinkedQueue<TeleportData> pendingTeleports = new ConcurrentLinkedQueue<>();

    private int teleportTicks;
    private boolean isPossiblyTeleporting = false;

    private final Map<Integer, TaskData> tasks = new LinkedHashMap<>();

    public TeleportProcessor(PlayerData user) {
        this.data = user;
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketSendEvent() != null) {

            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
                WrapperPlayServerPlayerPositionAndLook teleport =
                        new WrapperPlayServerPlayerPositionAndLook(event.getPacketSendEvent());

                Vector3d pos = new Vector3d(teleport.getX(), teleport.getY(), teleport.getZ());

                if (getData().getProtocolVersion() <= 47 & getData().getMovementProcessor().getToNull() != null) {
                    if (teleport.isRelativeFlag(RelativeFlag.X)) {
                        pos = pos.add(new Vector3d(getData().getMovementProcessor().getToNull().getPosX(), 0, 0));
                    }

                    if (teleport.isRelativeFlag(RelativeFlag.Y)) {
                        pos = pos.add(new Vector3d(0, getData().getMovementProcessor().getToNull().getPosY(), 0));
                    }

                    if (teleport.isRelativeFlag(RelativeFlag.Z)) {
                        pos = pos.add(new Vector3d(0, 0, getData().getMovementProcessor().getToNull().getPosZ()));
                    }

                    teleport.setX(pos.getX());
                    teleport.setY(pos.getY());
                    teleport.setZ(pos.getZ());
                    teleport.setRelativeMask((byte) (teleport.getRelativeFlags().getMask() & 0b11000));
                }

                double x = pos.getX();
                double y = pos.getY();
                double z = pos.getZ();

                TeleportData teleportData = new TeleportData(new Vector3d(x, y, z));

                getData().getTransactionProcessor().confirmPre(() -> this.pendingTeleports.add(teleportData));
                getData().getTransactionProcessor().confirmPost(() -> queueToFlying(1, uid ->
                        this.pendingTeleports.remove(teleportData)));
            }
        } else if (event.getPacketReceiveEvent() != null && event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            boolean legacy = this.getData().getProtocolVersion() < 47;
            boolean ground = !legacy && flying.isOnGround();
            boolean moving = flying.hasPositionChanged();
            boolean rotating = flying.hasRotationChanged();
            Location location = flying.getLocation();

            if (!this.pendingTeleports.isEmpty()) {
                Vector3d position = new Vector3d(location.getX(), location.getY(), location.getZ());

                for (TeleportData teleport : this.pendingTeleports) {
                    double distance = teleport.getLocation().distance(position);

                    if (distance <= 1.0E-7 && moving && rotating && !ground) {
                        this.teleportTicks = 0;
                        break;
                    }
                }
            }

            this.isPossiblyTeleporting = this.isTeleporting();

        }
    }

    public boolean isTeleporting() {
        return this.teleportTicks <= 2;
    }

    public void postFlying() {
        int tick = getData().getMovementProcessor().getTick();

        if (this.tasks.containsKey(tick)) {
            this.tasks.remove(tick).consumeTask();
        }
    }

    public void postFlyingPosition() {
        ++this.teleportTicks;
    }

    public void queueToFlying(int delay, Callback<Integer> callback) {
        int key = getData().getMovementProcessor().getTick() + delay;
        if (this.tasks.containsKey(key)) {
            this.tasks.get(key).addTask(callback);
        } else {
            this.tasks.put(key, new TaskData(key, callback));
        }
    }
}
