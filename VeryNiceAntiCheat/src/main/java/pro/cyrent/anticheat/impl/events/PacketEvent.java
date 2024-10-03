package pro.cyrent.anticheat.impl.events;

import pro.cyrent.anticheat.api.user.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class PacketEvent {
    private final PlayerData data;
    private final com.github.retrooper.packetevents.event.PacketEvent packetInstance;
    private final PacketTypeCommon type;
    private final PacketReceiveEvent packetReceiveEvent;
    private final PacketSendEvent packetSendEvent;
    private final Direction direction;
    private final long timestamp = System.currentTimeMillis();

    public enum Direction {
        CLIENT, SERVER
    }

    public boolean isPlace() {
        return this.type == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT;
    }

    public boolean isRotation() {
        return this.type == PacketType.Play.Client.PLAYER_ROTATION
                || this.type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION;
    }

    public boolean isPosition() {
        return this.type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION
                || this.type == PacketType.Play.Client.PLAYER_POSITION;
    }

    public boolean isMovement() {
        return this.type == PacketType.Play.Client.PLAYER_FLYING
                || this.type == PacketType.Play.Client.PLAYER_ROTATION
                || this.type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION
                || this.type == PacketType.Play.Client.PLAYER_POSITION;
    }
}
