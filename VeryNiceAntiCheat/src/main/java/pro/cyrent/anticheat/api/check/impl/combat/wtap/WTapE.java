package pro.cyrent.anticheat.api.check.impl.combat.wtap;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

@CheckInformation(
        name = "WTap",
        subName = "E",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.W_TAP,
        description = "Detects if a player sends sprint actions too consistently",
        punishmentVL = 10,
        punishable = false,
        experimental = true,
        state = CheckState.PRE_RELEASE)
public class WTapE extends Check {

    private double threshold;
    private int lastStartSent, lastStopSent, lastDelta;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {

            int delta = Math.abs(this.lastStartSent - this.lastStopSent);

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 2
                    && getData().getCombatProcessor().getLastUseEntityTimer().isSet()
                    && !getData().getActionProcessor().isTeleportingV2()
                    && getData().getActionProcessor().getRespawnTimer().getDelta() > 40
                    && getData().getActionProcessor().getRespawnTimer().isSet()
                    && !getData().generalCancel()) {

                // detects wtaps that send with small delta changes. (can false?)
                if (delta <= 1 && this.lastDelta != delta) {
                    if (++this.threshold > 9.5) {
                        this.fail(
                                "threshold="+this.threshold,
                                "delta="+delta);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .0725);
                }
            }

            this.lastDelta = delta;
            this.lastStopSent++;
            this.lastStartSent++;
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event.getPacketReceiveEvent());

            if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING) {
                this.lastStartSent = 0;
            }

            if (action.getAction() == WrapperPlayClientEntityAction.Action.STOP_SPRINTING) {
                this.lastStopSent = 0;
            }
        }
    }
}
