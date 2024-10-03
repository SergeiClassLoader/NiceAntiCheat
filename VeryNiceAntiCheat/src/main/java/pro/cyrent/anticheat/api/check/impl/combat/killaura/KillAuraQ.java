package pro.cyrent.anticheat.api.check.impl.combat.killaura;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckInformation(
        name = "KillAura",
        subName = "Q",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.KILL_AURA,
        description = "Detects if the player attacks while sending status packets",
        experimental = true,
        state = CheckState.BETA)
public class KillAuraQ extends Check {

    private double threshold;
    private boolean status = false;
    private String type;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {
            this.status = false;
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interactEntity = new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

            if (getData().generalCancel()) {
                return;
            }

            if (getData().getProtocolVersion() > 47) {
                return;
            }

            if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (this.status) {
                    if (++this.threshold > 2) {
                        this.fail("statusType="+this.type,
                                "threshold="+this.threshold);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .01);
                }
            }
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLIENT_STATUS) {
            WrapperPlayClientClientStatus clientStatus = new WrapperPlayClientClientStatus(event.getPacketReceiveEvent());
            this.status = true;
            this.type = clientStatus.getAction().name();
        }
    }
}
