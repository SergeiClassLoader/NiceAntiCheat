package pro.cyrent.anticheat.api.check.impl.combat.wtap;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "WTap",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.W_TAP,
        description = "Checks for multiple sent sprint packets being sent at once",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.RELEASE)
public class WTapA extends Check {

    private int sprintTick, stopSprintTick;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {

            if (getData()
                    .getMovementProcessor().getLastFlyingPauseTimer().hasNotPassed(20)
                    || getData().generalCancel()
                    || getData().isBedrock()
                    || getData().getTransactionProcessor().getTransactionPingDrop() > 100) {
                this.threshold = 0;
                return;
            }


            if (this.sprintTick > 1 || this.stopSprintTick > 1) {
                if (++this.threshold > 3.5) {
                    this.fail("stop="+this.stopSprintTick,
                            "start="+this.sprintTick);
                }
            } else {
                this.threshold -= Math.min(this.threshold, .5);
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