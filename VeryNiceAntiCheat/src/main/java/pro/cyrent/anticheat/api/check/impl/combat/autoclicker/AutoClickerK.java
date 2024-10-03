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
        subName = "K",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Detects jumping in the click pattern",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.ALPHA)
public class AutoClickerK extends Check {

    private double threshold;
    private int movements;
    private final List<Integer> clickSamples = new CopyOnWriteArrayList<>();

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {
            ++this.movements;
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ANIMATION) {
            if (getData().generalCancel()
                    || getData().getLastBlockBreakTimer().getDelta() < 15
                    || getData().getLastBlockPlaceTimer().getDelta() < 4
                    || getData().getLastBlockPlaceCancelTimer().getDelta() < 4
                    || getData().getActionProcessor().isDigging()) {
                this.clickSamples.clear();
                return;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 60)
                return;

            if (this.movements < 8) {

                this.clickSamples.add(this.movements);

                if (this.clickSamples.size() >= 40) {
                    double mean = StreamUtil.mean(this.clickSamples);
                    double median = StreamUtil.getMedian(this.clickSamples);
                    int distinct = StreamUtil.getDistinct(this.clickSamples);
                    double kurtosis = StreamUtil.getKurtosis(this.clickSamples);

                    if (mean < 2 && median < 3 && distinct > 2 && distinct < 6
                            && (kurtosis > 35.0 || kurtosis < -2.0)) {
                        if (++this.threshold > 5.0) {
                            this.fail("kurtosis="+kurtosis,
                                    "distinct="+distinct,
                                    "mean="+mean,
                                    "median="+median);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .75);
                    }

                    this.clickSamples.clear();
                }
            }

            this.movements = 0;
        }
    }
}