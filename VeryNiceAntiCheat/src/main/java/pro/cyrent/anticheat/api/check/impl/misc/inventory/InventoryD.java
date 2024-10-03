package pro.cyrent.anticheat.api.check.impl.misc.inventory;

import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import pro.cyrent.anticheat.api.check.*;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

@CheckInformation(
        name = "Inventory",
        subName = "D",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.INVENTORY,
        description = "Detects refills that click with less than 10ms delay",
        state = CheckState.RELEASE)
public class InventoryD extends Check {

    private long lastClick;
    private int lastSlot;
    private int waitTicks;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            WrapperPlayClientClickWindow wrapperPlayClientClickWindow;
            try {
                wrapperPlayClientClickWindow = new WrapperPlayClientClickWindow(event.getPacketReceiveEvent());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }


            if (wrapperPlayClientClickWindow.getWindowId() == 0 &&
                    wrapperPlayClientClickWindow.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE) {

                int slot = wrapperPlayClientClickWindow.getSlot();
                long now = System.currentTimeMillis();

                int button = wrapperPlayClientClickWindow.getButton();
                long delta = Math.abs(now - this.lastClick);

                if (wrapperPlayClientClickWindow.getCarriedItemStack().getType() != ItemTypes.POTION
                        || wrapperPlayClientClickWindow.getCarriedItemStack().getType() != ItemTypes.SPLASH_POTION) {
                    this.waitTicks = 20;
                    return;
                }

                if (this.lastSlot != slot) {

                    if (delta < 10
                            && this.waitTicks-- < 1) {
                        if (++this.threshold > 4.0) {
                            this.fail(
                                    "slot="+slot,
                                    "lastSlot="+this.lastSlot,
                                    "time(ms)="+delta,
                                    "waitTicks="+this.waitTicks,
                                    "item="+wrapperPlayClientClickWindow.getCarriedItemStack().getType(),
                                    "button="+button);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .5);
                    }
                } else {
                    this.waitTicks = 50;
                }


                this.lastSlot = slot;
                this.lastClick = now;
            }
        }
    }
}