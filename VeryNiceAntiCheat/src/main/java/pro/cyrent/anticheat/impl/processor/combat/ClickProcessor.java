package pro.cyrent.anticheat.impl.processor.combat;

import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.evicting.EvictingList;
import pro.cyrent.anticheat.util.math.Tuple;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Getter
@Setter
public class ClickProcessor extends Event {
    private final PlayerData data;

    private int movements;
    private final Deque<Integer> clicks = new EvictingList<>(40);

    private double kurtosis;
    private double mean;
    private double mode;
    private double skewness;
    private double cps;
    private double stdDev;
    private double variance;

    //outliers
    private Tuple<List<Long>, List<Long>> outlierTuple = new Tuple<>(new ArrayList<>(), new ArrayList<>());
    private double outlier;
    private int min, max;
    private int lowestOutlier;
    private int highestOutlier;

    public ClickProcessor(PlayerData user) {
        this.data = user;
    }

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {
            this.movements++;
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ANIMATION) {

            if (getData().generalCancel() || getData().getLastBlockPlaceTimer().getDelta() < 7
                    || getData().getActionProcessor().isDigging()
                    || getData().getLastBlockBreakTimer().getDelta() < 10
                    || getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() < 7
                    || getData().getLastBlockPlaceCancelTimer().getDelta() < 7) return;

            if (this.movements < 15) {

                this.clicks.add(this.movements);

                if (this.clicks.size() >= 20) {
                    this.kurtosis = StreamUtil.getKurtosis(this.clicks);
                    this.mean = StreamUtil.getMedian(this.clicks);
                    this.mode = StreamUtil.getModeV2(this.clicks);
                    this.skewness = StreamUtil.getSkewness(this.clicks);
                    this.cps = StreamUtil.getCPS(this.clicks);
                    this.stdDev = StreamUtil.getStandardDeviation(this.clicks);
                    this.variance = this.stdDev / clicks.size();

                    Tuple outlierTuple = StreamUtil.getOutliers(this.clicks);

                    this.min = Integer.MAX_VALUE;
                    this.max = Integer.MIN_VALUE;

                    int total = 0;
                    for (Integer integer : this.clicks) {
                        total += integer;

                        if (total > 20) {
                            this.min = Math.min(integer, this.min);
                            this.max = Math.max(integer, this.max);
                        }
                    }

                    if (outlierTuple != null) {
                        this.outlierTuple = outlierTuple;
                        this.outlier = (this.lowestOutlier = (this.outlierTuple.one.size())) +
                                (this.highestOutlier = (this.outlierTuple.two.size()));
                    }
                }
            }

            this.movements = 0;
        }
    }
}
