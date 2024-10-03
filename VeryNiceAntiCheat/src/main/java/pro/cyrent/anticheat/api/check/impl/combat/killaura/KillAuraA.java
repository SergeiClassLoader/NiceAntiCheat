package pro.cyrent.anticheat.api.check.impl.combat.killaura;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "KillAura",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.KILL_AURA,
        description = "Detects if a player attacks with no movements being sent",
        state = CheckState.PRE_RELEASE)
public class KillAuraA extends Check {

    private int lastTicks;
    private int ticks;
    private int ticksToReset;

    private int threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            this.ticks = 0;
            this.ticksToReset = 0;
        }

        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
                this.lastTicks = this.ticks;
                this.ticks++;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interactEntity = new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                int max = Anticheat.INSTANCE.getVersionSupport().getClientProtocol(getData()) > 47 ? 30 : 6;

                if (interactEntity.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

                if (this.ticksToReset++ > max && this.ticks != this.lastTicks && this.ticks > 20) {
                    if (++this.threshold > 10) {
                        this.fail("threshold=" + threshold);
                        event.getPacketReceiveEvent().setCancelled(true);
                        this.threshold = 10;
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, 1);
                }
            }
        }
    }
}
