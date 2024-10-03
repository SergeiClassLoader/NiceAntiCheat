package pro.cyrent.anticheat.api.check.impl.combat.autoclicker;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import pro.cyrent.anticheat.api.check.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CheckInformation(
        name = "AutoClicker",
        subName = "I",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Detects consistent block placements (fastplace)",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.RELEASE)
public class AutoClickerI extends Check {

    private double threshold;
    private int movements;
    private final List<Integer> clickSamples = new CopyOnWriteArrayList<>();

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {
            ++this.movements;
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {

            if (getData().generalCancel()
                    || getData().getLastBlockBreakTimer().getDelta() < 15
                    || getData().getActionProcessor().isDigging()) {
                this.clickSamples.clear();
                return;
            }

            if (getData().getLastBlockPlaceTimer().getDelta() > 10
                    || getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() > 10) {
                return;
            }

            if (this.movements < 20) {

                this.clickSamples.add(this.movements);

                if (clickSamples.size() >= 40) {
                    double deviation = StreamUtil.getDeviation(this.clickSamples);

                    double n = (0.325 - deviation) * 2.0 + 0.675;

                    if (n > .5) {
                        if (++this.threshold > 1.5) {
                            this.fail("dev="+deviation,
                                    "calc="+n);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .35);
                    }

                    this.clickSamples.clear();
                }
            }

            this.movements = 0;
        }
    }
}