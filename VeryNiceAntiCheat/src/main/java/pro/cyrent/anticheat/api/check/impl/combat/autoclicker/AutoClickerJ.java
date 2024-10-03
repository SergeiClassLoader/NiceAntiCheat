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
        subName = "J",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Extra Randomization Check",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.BETA)
public class AutoClickerJ extends Check {

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
                    int max = StreamUtil.getMaximumInt(this.clickSamples);
                    int min = StreamUtil.getMinimumInt(this.clickSamples);

                    double std = StreamUtil.getStandardDeviation(this.clickSamples);
                    double skewness = StreamUtil.getSkewness(this.clickSamples);
                    double kurtosis = StreamUtil.getKurtosis(this.clickSamples);

                    double kurtSkewDelta = Math.abs(kurtosis - skewness);
                    double stdSkewDelta = Math.abs(std - skewness);

                    if (min == 1 && max < 4) {
                        if (kurtSkewDelta > 2.0 && stdSkewDelta > 1.5) {
                            if (++this.threshold > 2) {
                                this.fail("kurtosis=" + kurtosis,
                                        "skewness=" + skewness,
                                        "std=" + std,
                                        "ksDelta=" + kurtSkewDelta,
                                        "skDelta=" + stdSkewDelta,
                                        "min=" + min,
                                        "max=" + max);
                            }
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .25);
                    }
                } else {
                    this.threshold = 0;
                }

                this.clickSamples.clear();
            }
        }

        this.movements = 0;
    }
}