package pro.cyrent.anticheat.api.check.impl.combat.killaura;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "KillAura",
        subName = "H",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.KILL_AURA,
        description = "Detects Post Attack Packets",
        state = CheckState.PRE_RELEASE)
public class KillAuraH extends Check {

    private double threshold;
    private long lastFlying;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {
                this.lastFlying = System.currentTimeMillis();
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity entity = new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                if (getData().getTransactionProcessor().getTransactionPing() > 300
                        || getData().getMovementProcessor().getLastFlyingPauseTimer().getDelta() < 40
                        || getData().generalCancel()
                        || getData().getProtocolVersion() > 47
                        || getData().getMovementProcessor().getSkippedPackets() > 0) {
                    this.threshold = 0;
                    return;
                }

                if (entity.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                    long now = System.currentTimeMillis();

                    long delta = Math.abs(now - this.lastFlying);

                    if (delta < 5L) {

                        // if they fail this legit wtf idk
                        if (++this.threshold > 17) {
                            this.fail(
                                    "delta="+delta,
                                    "threshold="+this.threshold);
                            this.threshold = 16;
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, 5.0);
                    }
                }
            }
        }
    }
}
