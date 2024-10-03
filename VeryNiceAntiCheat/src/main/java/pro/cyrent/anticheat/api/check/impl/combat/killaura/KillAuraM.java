package pro.cyrent.anticheat.api.check.impl.combat.killaura;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInformation(
        name = "KillAura",
        subName = "M",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.KILL_AURA,
        description = "Detects if a players attacks while tab completing in chat",
        punishmentVL = 3,
        state = CheckState.RELEASE)
public class KillAuraM extends Check {

    private boolean tabComplete = false;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {

            if (event.isMovement()) {
                this.tabComplete = false;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {

                if (getData().generalCancel() || getData().getActionProcessor().isTeleportingV2()) {
                    this.tabComplete = false;
                    return;
                }

                this.tabComplete = true;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {

                if (getData().generalCancel() || getData().getActionProcessor().isTeleportingV2()) {
                    this.tabComplete = false;
                    return;
                }

                if (this.tabComplete) {
                    if (++this.threshold > 2.5) {
                        this.fail("threshold=" + threshold);
                    }
                    this.tabComplete = false;
                } else {
                    this.threshold -= Math.min(this.threshold, .025);
                }
            }
        }
    }
}
