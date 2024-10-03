package pro.cyrent.anticheat.api.check.impl.combat.wtap;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "WTap",
        subName = "F",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.W_TAP,
        description = "Detects invalid sprint packets",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.PRE_BETA)
public class WTapF extends Check {

    private double threshold;
    private int stage;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {

            if (getData().generalCancel()
                    || getData().getActionProcessor().isTeleportingV2()
                    || getData().getLastTeleport().getDelta() < 20 && getData().getLastTeleport().isSet()) {
                this.threshold = 0;
                return;
            }

            if (this.stage > 3) {
                if (++this.threshold > 2.0) {
                    this.fail("Invalid sprint packet order sent");
                }
            } else {
                this.threshold = 0;
            }
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event.getPacketReceiveEvent());

            if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING) {
                this.stage++;
            }

            if (action.getAction() == WrapperPlayClientEntityAction.Action.STOP_SPRINTING) {
                this.stage = 0;
            }
        }
    }
}
