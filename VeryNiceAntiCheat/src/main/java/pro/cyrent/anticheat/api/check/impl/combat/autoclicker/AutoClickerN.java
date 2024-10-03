package pro.cyrent.anticheat.api.check.impl.combat.autoclicker;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CheckInformation(
        name = "AutoClicker",
        subName = "N",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Detects consistent gridded like click patterns",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.ALPHA)
public class AutoClickerN extends Check {

    private double threshold;
    private int movements;
    private final List<Integer> preSamples = new CopyOnWriteArrayList<>();
    private final List<Double> deviationSamples = new CopyOnWriteArrayList<>();

    private int lastSize;

    private Double lastGrid;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {
            ++this.movements;
            this.lastSize = this.deviationSamples.size();
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ANIMATION) {
            if (getData().generalCancel()
                    || getData().getLastBlockBreakTimer().getDelta() < 15
                    || getData().getLastBlockPlaceTimer().getDelta() < 4
                    || getData().getLastBlockPlaceCancelTimer().getDelta() < 4
                    || getData().getActionProcessor().isDigging()) {
                this.preSamples.clear();
                return;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 60)
                return;

            if (this.movements < 8) {

                this.preSamples.add(this.movements);

                if (this.preSamples.size() >= 20) {
                    double standardDeviation = StreamUtil.getStandardDeviation(this.preSamples);
                    this.deviationSamples.add(standardDeviation);
                    this.preSamples.clear();
                }

                if (this.deviationSamples.size() >= 2) {

                    double grid = StreamUtil.getGridDouble(this.deviationSamples);

                    if (this.lastGrid != null) {

                        double gridOffset = grid - this.lastGrid;

                        int sizeChange = Math.abs(this.deviationSamples.size() - this.lastSize);

                        if (sizeChange > 0) {
                            if (grid < this.lastGrid && Math.abs(gridOffset) < .03 && grid < .07) {

                                if (++this.threshold > 4.25) {
                                    this.fail(
                                            "grid="+grid,
                                            "lastGrid="+this.lastGrid,
                                            "gridOffset="+Math.abs(gridOffset),
                                            "threshold="+this.threshold);
                                }
                            } else {
                                this.threshold -= Math.min(this.threshold, .1);
                            }
                        }
                    }

                    this.lastGrid = grid;
                }

                if (this.deviationSamples.size() > 5) {
                    this.lastGrid = null;
                    this.deviationSamples.clear();
                }
            }

            this.movements = 0;
        }
    }
}
