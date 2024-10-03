package pro.cyrent.anticheat.impl.processor.actions;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetBackProcessor extends Event {
    private final PlayerData data;

    private boolean isInvalid;
    private boolean set = false;

    private int kickThreshold;
    private int setBackTick;

    public double lastInvalidTick;
    private int lastDead;

    public SetBackProcessor(PlayerData user) {
        this.data = user;
    }

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasPositionChanged()) return;

            if (getData().getPlayer().getWorld() == null) return;

            if (this.getData().getMovementProcessor().getTo().isOnGround()
                    && this.getData().getCollisionWorldProcessor().isGround()) {
                this.set = true;
            }

            if (getData().getPlayer().isDead()) {
                this.lastDead = 20;
            } else {
                this.lastDead -= Math.min(this.lastDead, 1);
            }

            if (this.lastDead > 0) {
                this.setBackTick = 0;
            }

            if (getData().getMovementProcessor().getTo() != null
                    && getData().getMovementProcessor().getFrom() != null
                    && !getData().getMovementProcessor().getTo().getWorld().equals(getData().getMovementProcessor()
                    .getFrom().getWorld()) && this.setBackTick > 0) {
                this.setBackTick = 0;
            }

            if (getData().getLastTeleport().getPositionDelta() <= 10
                    || getData().getLastWorldChange().getPositionDelta() <= 10
                    || getData().getActionProcessor().getRespawnTimer().getPositionDelta() <= 10
                    || this.lastDead > 0) {
                if (getData().getHorizontalProcessor().getTeleportLocation() != null) {
                    getData().getHorizontalProcessor().getTeleportLocation().setPosX(flying.getLocation().getX());
                    getData().getHorizontalProcessor().getTeleportLocation().setPosY(flying.getLocation().getY());
                    getData().getHorizontalProcessor().getTeleportLocation().setPosZ(flying.getLocation().getZ());
                }

                this.setBackTick = 0;
            }

            if (this.setBackTick > 0) {

                if (!this.set) {
                    if (++this.kickThreshold > 18) {
                        this.getData().kickPlayer(
                                "Being lagged back multiple times but the ground location isn't set");
                    }
                } else {
                    this.kickThreshold = 0;
                }

                getData().getHorizontalProcessor().triggerTeleportNoChecks();

                this.setBackTick -= Math.min(this.setBackTick, 1);
            }
        }
    }
}