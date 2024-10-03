package pro.cyrent.anticheat.api.check.impl.combat.killaura;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "KillAura",
        subName = "C",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.KILL_AURA,
        punishmentVL = 5,
        description = "Detects if a player attacks without swinging (1.9+ clients)",
        state = CheckState.RELEASE)
public class KillAuraC extends Check {

    private boolean attack, swing;
    private int lastLagTick;

    private boolean nextTick = false;

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (getData().getProtocolVersion() <= 47 || getData().isBedrock()) {
            return;
        }

        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interactEntity = new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                if (interactEntity.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

                this.attack = true;
                this.nextTick = true;
            } else if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ANIMATION) {
                if (this.attack) {
                    this.swing = true;
                }
            } else if (event.isMovement()) {

                if (getData().getMovementProcessor().getSkippedPackets() > 0
                        || getData().generalCancel()) {
                    if (this.lastLagTick < 20) {
                        this.lastLagTick++;
                    }
                } else {
                    this.lastLagTick -= Math.min(this.lastLagTick, 1);
                }

                if (this.nextTick && this.lastLagTick < 1) {

                    if (this.swing) {
                        this.swing = false;
                        this.attack = false;
                        this.threshold -= Math.min(this.threshold, 0.03);
                    } else {
                        if (++this.threshold > 1.5) {
                            this.fail("swing=" + swing, "threshold=" + threshold);
                        }

                        this.attack = false;
                    }
                    this.nextTick = false;
                } else {
                    this.threshold -= Math.min(this.threshold, 0.03);
                }
            }

        }
    }
}
