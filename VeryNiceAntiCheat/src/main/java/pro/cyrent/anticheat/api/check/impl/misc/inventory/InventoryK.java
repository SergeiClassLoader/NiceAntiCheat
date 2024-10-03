package pro.cyrent.anticheat.api.check.impl.misc.inventory;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.*;
@CheckInformation(
        name = "Inventory",
        subName = "K",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.INVENTORY,
        experimental = true,
        description = "Detects no inventory opens, but clicking inside",
        state = CheckState.DEV)
public class InventoryK extends Check {

    private int lastOpenInventory;
    private boolean openInventory = false;
    private double threshold;
    private boolean failed = false;

    @Override
    public void onPacket(PacketEvent event) {

        if (getData().isBedrock()) return;

        if (event.getPacketSendEvent() != null) {
            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.CLOSE_WINDOW) {
                this.openInventory = this.failed = false;
            }
        }

        if (event.getPacketReceiveEvent() == null) return;

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLIENT_STATUS) {
            WrapperPlayClientClientStatus status = new WrapperPlayClientClientStatus(event.getPacketReceiveEvent());

            if (status.getAction() == WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT) {
                this.lastOpenInventory = 0;
                this.openInventory = true;
            }
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
            this.openInventory = this.failed = false;
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {

            WrapperPlayClientClickWindow clientClickWindow = new WrapperPlayClientClickWindow(event.getPacketReceiveEvent());

            if (getData().generalCancel()
                    // Game version 1.16 and above doesn't have the packet info for this shit check.
                    || getData().getProtocolVersion() >= 735
                    || getData().getMovementProcessor().getSkippedPackets() > 3) {
                return;
            }

            if (clientClickWindow.getWindowId() != 0) {
                return;
            }

            if (!this.openInventory && this.lastOpenInventory > 200) {
                if (!this.failed) {
                    this.failed = true;

                    if (++this.threshold > 10) {
                        this.fail("amount="+this.threshold,
                                "lastOpenedInventory="+this.lastOpenInventory);
                    }
                }
            } else {
                this.threshold -= Math.min(this.threshold, 2);
            }
        }

        if (event.isMovement()) {
            this.lastOpenInventory++;
        }
    }
}