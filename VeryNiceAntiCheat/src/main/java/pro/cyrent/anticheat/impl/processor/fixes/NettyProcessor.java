package pro.cyrent.anticheat.impl.processor.fixes;

import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.Getter;

@Getter
public class NettyProcessor extends Event {
    private final PlayerData data;
    private double lastPositionX, lastPositionY, lastPositionZ;

    private float lastYaw, lastPitch;

    public NettyProcessor(PlayerData user) {
        this.data = user;
    }

    public void run(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {

            if (event.isMovement()) {
                WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

                float yaw = Math.abs(MathUtil.wrapAngleTo180_float(flying.getLocation().getYaw()));
                float pitch = Math.abs(flying.getLocation().getPitch());

                double positionX = Math.abs(flying.getLocation().getX());
                double positionY = Math.abs(flying.getLocation().getY());
                double positionZ = Math.abs(flying.getLocation().getZ());

                double deltaX = Math.abs(positionX - this.lastPositionX);
                double deltaY = Math.abs(positionY - this.lastPositionY);
                double deltaZ = Math.abs(positionZ - this.lastPositionZ);

                boolean invalidPosition = positionX >= 3.0E7D || positionY >= 3.0E7D || positionZ >= 3.0E7D;
                boolean invalidDelta = deltaX >= 3.0E7D || deltaY >= 3.0E7D || deltaZ >= 3.0E7D;

                float deltaYaw = Math.abs(MathUtil.wrapAngleTo180_float(flying.getLocation().getYaw() - this.lastYaw));
                float deltaPitch = Math.abs(flying.getLocation().getPitch() - this.lastPitch);

                boolean invalidHeadMovements = yaw > 100000 || pitch > 100000;
                boolean invalidHeadDelta = deltaYaw > 100000 || deltaPitch > 100000;


                if (invalidPosition || invalidDelta || invalidHeadMovements || invalidHeadDelta) {
                    // execute the punishment logic
                    getData().kickPlayer("attempting too crash server (hacking?)");
                }

                if (flying.hasPositionChanged()) {
                    this.lastPositionX = positionX;
                    this.lastPositionY = positionY;
                    this.lastPositionZ = positionZ;
                }

                if (flying.hasRotationChanged()) {
                    this.lastPitch = pitch;
                    this.lastYaw = yaw;
                }
            }
        }
    }
}
