package pro.cyrent.anticheat.api.check.impl.combat.killaura;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "KillAura",
        subName = "B",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.KILL_AURA,
        punishmentVL = 5,
        description = "Detects if a player attacks without swinging",
        state = CheckState.RELEASE)
public class KillAuraB extends Check {

    private boolean swing;
    private int lastLagTick;

    private int threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            this.swing = false;

            if (getData().getMovementProcessor().getSkippedPackets() > 1
                    || getData().getMovementProcessor().getLastFlyingPauseTimer().getDelta() < 10
                    || getData().generalCancel()) {
                this.lastLagTick = 20;
                this.threshold = 0;
            } else {
                this.lastLagTick -= Math.min(this.lastLagTick, 1);
            }
        }

        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ANIMATION) {
                this.swing = true;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interactEntity = new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                if (interactEntity.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

                if (Anticheat.INSTANCE.getVersionSupport().getClientProtocol(getData()) > 47) {
                    return;
                }

                if (!this.swing && this.lastLagTick < 1) {
                    if (++this.threshold > 1) {
                        this.fail("swing=" + swing, "threshold=" + threshold);
                    }
                } else {
                    this.threshold = 0;
                }
            }
        }
    }
}
