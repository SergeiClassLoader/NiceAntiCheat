package pro.cyrent.anticheat.api.check.impl.combat.autoclicker;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.evicting.EvictingList;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import java.util.Deque;

@CheckInformation(
        name = "AutoClicker",
        subName = "P",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Detects not enough skewness in the click pattern",
        punishmentVL = 10,
        punishable = false,
        experimental = true,
        state = CheckState.BETA)
public class AutoClickerP extends Check {

    private double threshold, lastStd, lastSkew;
    private int movements;
    private final Deque<Double> clicks = new EvictingList<>(100);

    private int invalidTicks;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {
            ++this.movements;
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ANIMATION) {

            if (getData().generalCancel()
                    || getData().getLastBlockBreakTimer().getDelta() < 20
                    || getData().getLastBlockPlaceTimer().getDelta() < 10
                    || getData().getMovementProcessor().getSkippedPackets() > 5
                    || getData().getLastBlockBreakTimer().hasNotPassed(9)
                    || getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 20
                    || getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() < 10
                    || getData().getActionProcessor().isDigging()) {
                return;
            }

            if (this.movements < 8) {
                double outlier = getData().getClickProcessor().getOutlier();

                double skewness = getData().getClickProcessor().getSkewness();
                double std = getData().getClickProcessor().getStdDev();
                double mean = getData().getClickProcessor().getMean();
                double mode = getData().getClickProcessor().getMean();

                if (this.clicks.size() > 90 && mean < 2.0 && mode < 2.0) {

                    double stdDelta = Math.abs(std - this.lastStd);
                    double skewDelta = Math.abs(skewness - this.lastSkew);

                    if (stdDelta < 0.001 && skewDelta < .02 && outlier < 75) {

                        if (++this.invalidTicks > 24) {
                            this.invalidTicks = 0;

                            if (++this.threshold > 4.5) {
                                this.fail(
                                        "std-delta="+stdDelta,
                                        "skew-delta="+skewDelta,
                                        "std="+std,
                                        "skew="+skewness,
                                        "mode="+mode,
                                        "mean="+mean,
                                        "outlier="+outlier);
                                this.threshold = 4.5;
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, .006);
                        }
                    } else {
                        this.invalidTicks = 0;
                    }

                    this.lastSkew = skewness;
                    this.lastStd = std;
                }

                this.clicks.add(outlier);
            }

            this.movements = 0;
        }
    }
}
