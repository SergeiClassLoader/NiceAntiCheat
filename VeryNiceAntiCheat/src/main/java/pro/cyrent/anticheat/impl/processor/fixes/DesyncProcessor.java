package pro.cyrent.anticheat.impl.processor.fixes;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.event.EventTimer;
import pro.cyrent.anticheat.util.location.Vector;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DesyncProcessor extends Event {
    private final PlayerData data;

    private Vector lastPosition = new Vector(0, 0, 0);
    private int ticksSinceDetect;
    private boolean invalid = false;
    private int fix, kys;
    private long lastStopFix;
    private int lastBlockPlacementTick;
    private int serverTicks, lastServerTick, lastServerBowTick;
    private int exemptTicks;

    private final EventTimer lastCanceledBowEventTimer;

    private boolean viaMCP = false;
    private double viaMCPThreshold;


    public DesyncProcessor(PlayerData user) {
        this.data = user;
        this.lastCanceledBowEventTimer = new EventTimer(20, user);
    }

    public void run(PacketEvent event) {
        if (event.isMovement()) {

            if (Anticheat.INSTANCE.getVersionSupport().getClientProtocol(getData()) < 755) {
                this.invalid = false;
                return;
            }

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            this.ticksSinceDetect++;
            this.invalid = false;

            Vector current = new Vector(
                    flying.getLocation().getX(),
                    flying.getLocation().getY(),
                    flying.getLocation().getZ()
            );

            if (this.lastPosition != null
                    && flying.hasPositionChanged()
                    && flying.hasRotationChanged()
                    // && (System.currentTimeMillis() - this.lastStopFix) > 5000L
                    && flying.isOnGround() == this.getData().getMovementProcessor().getTo().isOnGround()
                    && this.lastPosition.squareDistanceTo(current) < 4e-8) {
                this.ticksSinceDetect = 0;
                this.invalid = true;
                this.fix = 0;
            }

            if (this.fix < 1) {
                if (this.kys++ >= 8) {
                    this.kys = 8;
                    this.lastStopFix = System.currentTimeMillis();
                }
            } else {
                this.kys -= this.kys > 0 ? 3 : 0;
            }

            if (!flying.hasPositionChanged()
                    && !flying.hasRotationChanged()
                    && flying.isOnGround() == this.getData().getMovementProcessor().getTo().isOnGround()
                    && !getData().getActionProcessor().isTeleportingV2()
                    && getData().getLastTeleport().getDelta() > 40
                    && getData().getMovementProcessor().getTick() > 80
                    && !this.viaMCP
                    && getData().getProtocolVersion() > 47) {
                this.viaMCP = true;

                if (Anticheat.INSTANCE.getConfigValues().isKickForViaMCP()) {
                    getData().viaMcpCommand();
                }
            }

            this.exemptTicks -= Math.min(this.exemptTicks, 1);

            this.lastPosition = this.clampVector(current);
        }

        if (event.isPlace()) {
            this.lastBlockPlacementTick = 0;
        }

        if (event.getPacketSendEvent() != null) {
            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION) {
                this.lastBlockPlacementTick++;
                this.serverTicks++;

                if (this.serverTicks > 2000) {
                    this.serverTicks = 0;
                }
            }
        }
    }

    private Vector clampVector(Vector toClamp) {
        double x = clampMin(toClamp.getX(), -3.0E7D, 3.0E7D);
        double y = clampMin(toClamp.getY(), -2.0E7D, 2.0E7D);
        double z = clampMin(toClamp.getZ(), -3.0E7D, 3.0E7D);

        return new Vector(x, y, z);
    }

    private double clampMin(double d, double d2, double d3) {
        if (d < d2) {
            return d2;
        }
        return Math.min(d, d3);
    }
}
