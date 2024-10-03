package pro.cyrent.anticheat.api.check.impl.combat.wtap;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "WTap",
        subName = "B",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.W_TAP,
        description = "Checks for invalid sprint packet order.",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.RELEASE)
public class WTapB extends Check {

    private int sprintTick, stopSprintTick;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()
                || event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {

            if (getData()
                    .getMovementProcessor().getLastFlyingPauseTimer().hasNotPassed(20)
                    || getData().generalCancel()
                    || getData().isBedrock()
                    || getData().getTransactionProcessor().getTransactionPingDrop() > 100) {
                this.threshold = 0;
                return;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 2) {
                if (this.sprintTick == this.stopSprintTick && this.sprintTick != 0) {
                    if (++this.threshold > 3) {
                        this.fail("startTicks=" + sprintTick, "stopTicks=" + stopSprintTick, "threshold=" + threshold);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .05);
                }
            }

            this.sprintTick = this.stopSprintTick = 0;
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event.getPacketReceiveEvent());

            if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING) {
                this.sprintTick++;
            }

            if (action.getAction() == WrapperPlayClientEntityAction.Action.STOP_SPRINTING) {
                this.stopSprintTick++;
            }
        }
    }
}