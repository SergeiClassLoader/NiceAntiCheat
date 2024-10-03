package pro.cyrent.anticheat.api.check.impl.misc.inventory;

import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import org.bukkit.Material;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "Inventory",
        subName = "I",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.INVENTORY,
        description = "Detects a mass majority of refills",
        state = CheckState.RELEASE)
public class InventoryI extends Check {

    private int lastSlot;
    private long lastClick;
    private int ticks;

    private double buffer;
    private int lastTick;
    private boolean firstClick = true;

    @Override
    public void onPacket(PacketEvent event) {

        if (getData().isBedrock()) return;

        if (event.getPacketSendEvent() != null) {
            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.CLOSE_WINDOW) {
                this.firstClick = true;
            }
        }

        if (event.getPacketReceiveEvent() == null) return;

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
            this.firstClick = true;
        }

        if (event.isMovement()) {
            this.ticks++;
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {

            WrapperPlayClientClickWindow clientClickWindow = new WrapperPlayClientClickWindow(event.getPacketReceiveEvent());

            if (getData().generalCancel() || clientClickWindow.getWindowId() != 0
                    || getData().getMovementProcessor().getSkippedPackets() > 3
                    || getData().getVelocityProcessor().getServerVelocityTicks() > 400
                    || getData().getActionProcessor().getLastInventoryOpenTimer().getDelta() > 150) {
                return;
            }

            long now = System.currentTimeMillis();

            // these slots are super easy to click when opening the inventory
            boolean reduceBuffer = clientClickWindow.getSlot() == 13
                    || clientClickWindow.getSlot() == 22
                    || clientClickWindow.getSlot() == 12
                    || clientClickWindow.getSlot() == 21
                    || clientClickWindow.getSlot() == 14
                    || clientClickWindow.getSlot() == 23;

            int ticks = this.ticks;
            this.ticks = 0;


            if (ticks < 20) {
                boolean validClick = clientClickWindow.getCarriedItemStack() == null
                        || clientClickWindow.getCarriedItemStack().getType() == ItemTypes.AIR
                        || this.lastSlot == clientClickWindow.getSlot();

                if (validClick) {
                    return;
                }

                if (clientClickWindow.getWindowClickType() ==
                        WrapperPlayClientClickWindow.WindowClickType.QUICK_CRAFT) {
                    return;
                }

                long delta = (now - this.lastClick);

                this.lastClick = now;
                this.lastSlot = clientClickWindow.getSlot();

                if (delta <= 10L
                        && clientClickWindow.getButton() == 0
                        && clientClickWindow.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE) {
                    return;
                }

                if (this.firstClick) {
                    int ticksSinceOpen = getData().getActionProcessor().getLastInventoryOpenTimer()
                            .getDelta();

                    if (ticksSinceOpen < 10) {
                        int offset = Math.abs(ticksSinceOpen - this.lastTick);

                        boolean possible = offset == 1 && ticksSinceOpen < 4 && this.lastTick < 4
                                && ticksSinceOpen > 1;

                        if (ticksSinceOpen == this.lastTick || possible) {

                            this.buffer += (ticksSinceOpen >= 4 || reduceBuffer ? .50 : 1);

                            if (this.buffer >= 4) {
                                this.fail("ticksSinceOpen="+ticksSinceOpen,
                                        "lastTick="+this.lastTick,
                                        "possible="+possible,
                                        "buffer="+this.buffer,
                                        "offset="+offset);
                            }
                        } else {
                            this.buffer -= this.buffer > 0 ? .5 : 0;
                        }

                        this.lastTick = ticksSinceOpen;
                    }

                    this.firstClick = false;
                }
            }
        }
    }
}
