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
        subName = "Q",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Detects constant spike type click patterns",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.BETA)
public class AutoClickerQ extends Check {

    private double threshold, lastOutlier;
    private int movements, lastMovements;
    private final List<Integer> clickPattern = new CopyOnWriteArrayList<>();

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

            boolean cps = getData().getClickProcessor().getCps() > 9.5;

            if (this.movements < 8 && this.lastMovements < 8 && cps) {

                int delta = Math.abs(this.movements - this.lastMovements);

                this.clickPattern.add(delta);

                if (this.clickPattern.size() >= 40) {
                    double std = StreamUtil.getStandardDeviation(this.clickPattern);
                    double avg = StreamUtil.getAverage(this.clickPattern);
                    double variance = StreamUtil.getVariance(this.clickPattern);

                    if (std < 0.55 && avg <= .5 && variance < 10.0) {
                        if (++this.threshold > 4.0) {
                            this.threshold = 3.0;
                            this.fail(
                                    "std="+std,
                                    "avg="+avg,
                                    "variance="+variance);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, 0.06);
                    }

                    this.clickPattern.clear();
                }
            }

            this.lastMovements = this.movements;
            this.movements = 0;
        }
    }
}