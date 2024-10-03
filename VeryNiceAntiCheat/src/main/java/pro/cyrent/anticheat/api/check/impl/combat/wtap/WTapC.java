package pro.cyrent.anticheat.api.check.impl.combat.wtap;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "WTap",
        subName = "C",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.W_TAP,
        description = "Detects if a player sends sprint actions while attacking",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.RELEASE)
public class WTapC extends Check {

    private double threshold;
    private boolean sent = false;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interactEntity = new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

            if (getData()
                    .getMovementProcessor().getLastFlyingPauseTimer().hasNotPassed(20)
                    || getData().generalCancel()
                    || getData().isBedrock()
                    || getData().getTransactionProcessor().getTransactionPingDrop() > 100) {
                this.threshold = 0;
                return;
            }

            if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (this.sent) {
                    if (++this.threshold > 4) {
                        this.fail("threshold=" + threshold);
                    }

                    this.sent = false;
                } else {
                    this.threshold -= Math.min(this.threshold, .07);
                }
            }
        } else if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event.getPacketReceiveEvent());

            if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING) {
                this.sent = true;
            }

            if (action.getAction() == WrapperPlayClientEntityAction.Action.STOP_SPRINTING) {
                this.sent = true;
            }
        } else if (event.isMovement()) {
            this.sent = false;
        }
    }
}