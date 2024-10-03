package pro.cyrent.anticheat.api.check.impl.misc.inventory;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Inventory",
        subName = "B",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.INVENTORY,
        description = "Detects if the player attacks while clicking in their inventory",
        state = CheckState.RELEASE)
public class InventoryB extends Check {

    private double threshold;
    private boolean clickWindow;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {
            this.clickWindow = false;
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interactEntity = new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

            if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (this.clickWindow) {
                    if (++this.threshold > 1) {
                        this.fail("threshold="+this.threshold);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .05);
                }
            }
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            this.clickWindow = true;
        }
    }
}
