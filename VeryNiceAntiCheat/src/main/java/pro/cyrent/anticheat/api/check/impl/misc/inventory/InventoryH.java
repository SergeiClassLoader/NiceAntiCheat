package pro.cyrent.anticheat.api.check.impl.misc.inventory;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "Inventory",
        subName = "H",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.INVENTORY,
        description = "Refill for Slinky & Quick Delta Speeds",
        state = CheckState.RELEASE)
public class InventoryH extends Check {

    private long lastClick;
    private int prevSlot = -1;
    private boolean isShiftClick;
    private double buffer;

    @Override
    public void onPacket(PacketEvent event) {

        if (getData().isBedrock()) return;

        if (event.getPacketSendEvent() != null) {
            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.CLOSE_WINDOW) {
                this.isShiftClick = false;
            }
        }

        if (event.getPacketReceiveEvent() == null) return;

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
            this.isShiftClick = false;
        }


        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {

            WrapperPlayClientClickWindow clientClickWindow = new WrapperPlayClientClickWindow(event.getPacketReceiveEvent());

            if (getData().generalCancel() || clientClickWindow.getWindowId() != 0
                    || getData().getVelocityProcessor().getServerVelocityTicks() > 400
                    || getData().getActionProcessor().getLastInventoryOpenTimer().getDelta() > 150) {
                return;
            }

            long now = System.currentTimeMillis();

            if (clientClickWindow.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.PICKUP
                    || clientClickWindow.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.QUICK_CRAFT) {
                this.isShiftClick = true;
            }

            if (clientClickWindow.getWindowClickType() != WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE) {
                return;
            }

            if (clientClickWindow.getButton() == 1 || clientClickWindow.getButton() == 0) {

                if (clientClickWindow.getSlot() == this.prevSlot) {
                    this.isShiftClick = true;
                    return;
                }

                long delta = (now - this.lastClick);
                this.lastClick = now;
                this.prevSlot = clientClickWindow.getSlot();

                if (clientClickWindow.getSlot() == -999) {
                    this.isShiftClick = true;
                }

                if (this.isShiftClick) return;

                if (delta <= 20L) {
                    if (this.buffer++ >= 5) {
                        this.buffer = 0;

                        String item = "N/A";

                        if (clientClickWindow.getCarriedItemStack() != null && clientClickWindow.getCarriedItemStack()
                                .getType() != null) {
                            item = clientClickWindow.getCarriedItemStack().getType().getName().getKey();
                        }

                        this.fail("delta=" + delta,
                                "item=" + item
                        );
                    }
                } else {
                    this.buffer -= this.buffer > 0 ? .45 : 0;
                }
            }
        }
    }
}
