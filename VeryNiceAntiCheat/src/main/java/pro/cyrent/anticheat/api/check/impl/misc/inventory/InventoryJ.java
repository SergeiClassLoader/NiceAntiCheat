package pro.cyrent.anticheat.api.check.impl.misc.inventory;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.evicting.EvictingList;
import pro.cyrent.anticheat.util.stream.StreamUtil;

@CheckInformation(
        name = "Inventory",
        subName = "J",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.INVENTORY,
        experimental = true,
        description = "Detects clicking too quickly in the inventory.",
        state = CheckState.PRE_RELEASE)
public class InventoryJ extends Check {

    private int movements;
    private double threshold;
    private final EvictingList<Integer> samples = new EvictingList<>(40);


    @Override
    public void onPacket(PacketEvent event) {

        if (getData().isBedrock()) return;

        if (event.getPacketReceiveEvent() == null) return;

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {

            WrapperPlayClientClickWindow clientClickWindow = new WrapperPlayClientClickWindow(event.getPacketReceiveEvent());

            if (getData().generalCancel() || clientClickWindow.getWindowId() != 0) {
                this.threshold -= Math.min(this.threshold, 0.005);
                return;
            }

            if (clientClickWindow.getWindowClickType() ==
                    WrapperPlayClientClickWindow.WindowClickType.QUICK_CRAFT) {
                return;
            }

            if (this.movements < 10) {
                this.samples.add(this.movements);
            }

            if (this.samples.isFull()) {
                double cps = StreamUtil.getCPS(this.samples);

                if (cps >= 20) {
                    if (++this.threshold > 3.0) {
                        this.fail("cps="+cps);
                        this.threshold = 0;
                        this.samples.clear();
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, 0.025);
                }
            }

            this.movements = 0;
        }

        if (event.isMovement()) {
            ++this.movements;
        }
    }
}
