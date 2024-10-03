package pro.cyrent.anticheat.api.check.impl.combat.autoclicker;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.math.Tuple;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CheckInformation(
        name = "AutoClicker",
        subName = "W",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Detects specific high randomization flaws",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.PRE_BETA)
public class AutoClickerW extends Check {

    private double threshold;
    private int movements;
    private final List<Integer> clickData = new CopyOnWriteArrayList<>();

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
                    || getData().getActionProcessor().isDigging()
                    || getData().getLastBlockPlaceCancelTimer().getDelta() < 4) {
                this.clickData.clear();
                return;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 60) {
                return;
            }

            if (this.movements < 8) {

                this.clickData.add(this.movements);

                if (this.clickData.size() >= 100) {
                    Tuple<List<Double>, List<Double>> outlierData = StreamUtil.getOutliers(this.clickData);

                    double skewness = Math.abs(StreamUtil.getSkewness(this.clickData));
                    double std = StreamUtil.getStandardDeviation(this.clickData);
                    double kurtosis = StreamUtil.getKurtosis(this.clickData);
                    double cps = StreamUtil.getCPS(this.clickData);

                    int distinct = StreamUtil.getDistinct(this.clickData);
                    int one = outlierData.one.size();
                    int two = outlierData.two.size();

                    int outlier = one + two;

                    if (cps >= 10 && std < 0.8D && skewness > 1.0
                            && outlier > 70 && distinct < 6 && distinct > 2 && kurtosis < 0D) {
                        if (++this.threshold > 3.5) {
                            this.fail(
                                    "cps="+cps,
                                    "std="+std,
                                    "skewness="+skewness,
                                    "outlier="+outlier,
                                    "distinct="+distinct,
                                    "kurtosis="+kurtosis);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .3125);
                    }

                    this.clickData.clear();
                }
            }

            this.movements = 0;
        }
    }
}
