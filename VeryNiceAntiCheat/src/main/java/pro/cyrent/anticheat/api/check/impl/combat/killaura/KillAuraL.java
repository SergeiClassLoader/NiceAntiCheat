package pro.cyrent.anticheat.api.check.impl.combat.killaura;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInformation(
        name = "KillAura",
        subName = "L",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.KILL_AURA,
        description = "Detects if a players attacks while chatting/sending commands",
        punishmentVL = 3,
        punishable = false,
        enabled = false,
        state = CheckState.BETA)
public class KillAuraL extends Check {

    private boolean chat = false;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {

            if (event.isMovement()) {
                this.chat = false;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CHAT_COMMAND) {

                if (getData().generalCancel() || getData().getActionProcessor().isTeleportingV2()) {
                    this.chat = false;
                    return;
                }

                this.chat = true;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CHAT_MESSAGE) {

                if (getData().generalCancel() || getData().getActionProcessor().isTeleportingV2()) {
                    this.chat = false;
                    return;
                }

                this.chat = true;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {

                if (getData().generalCancel() || getData().getActionProcessor().isTeleportingV2()) {
                    this.chat = false;
                    return;
                }

                if (this.chat) {
                    if (++this.threshold > 1) {
                        this.fail("threshold=" + threshold);
                    }
                    this.chat = false;
                } else {
                    this.threshold -= Math.min(this.threshold, .025);
                }
            }
        }
    }
}
