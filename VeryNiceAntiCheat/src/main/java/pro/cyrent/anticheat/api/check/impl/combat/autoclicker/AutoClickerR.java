package pro.cyrent.anticheat.api.check.impl.combat.autoclicker;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CheckInformation(
        name = "AutoClicker",
        subName = "R",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Detects invalid click patterns",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.PRE_BETA)
public class AutoClickerR extends Check {

    private double threshold, lastEntropy, lastCoefficient, lastCorrelation;
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

                if (this.clickData.size() >= 40) {

                    double serialCorrelation = StreamUtil.calculateSerialCorrelation(this.clickData);
                    double entropy = StreamUtil.getEntropy(this.clickData);
                    double giniCoefficient = StreamUtil.giniCoefficient(this.clickData);
                    double cps = StreamUtil.getCPS(this.clickData);

                    double entropyOffset = Math.abs(entropy - this.lastEntropy);
                    double coefficientOffset = Math.abs(giniCoefficient - this.lastCoefficient);
                    double correlationOffset = Math.abs(serialCorrelation - this.lastCorrelation);

                    boolean isCps = cps >= 9.25 && getData().getClickProcessor().getCps() >= 9.25;

                    if (isInvalidClickData(giniCoefficient, entropy, serialCorrelation,
                            entropyOffset, coefficientOffset, correlationOffset) && isCps) {

                        if (++this.threshold > 6.5) {
                            this.fail("entropy="+entropy,
                                    "correlation="+serialCorrelation,
                                    "coefficient="+giniCoefficient,
                                    "entropy-offset="+entropyOffset,
                                    "coefficient-offset="+coefficientOffset,
                                    "correlation-offset="+correlationOffset);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .075);
                    }

                    this.lastCoefficient = giniCoefficient;
                    this.lastCorrelation = serialCorrelation;
                    this.lastEntropy = entropy;
                    this.clickData.clear();
                }
            }

            this.movements = 0;
        }
    }

    // This will detect average invalid click data based off lots of shit.
    private boolean isInvalidClickData(double giniCoefficient, double entropy, double correlation,
                                       double entropyOffset, double coefficientOffset, double correlationOffset) {
        return giniCoefficient < 0.028D && entropy < .77 && correlation < 0.5D
                || coefficientOffset < 0.02D && entropyOffset < 0.02D && correlationOffset < 0.02D
                || entropyOffset < 0.037D && entropy < 1.5D && correlation < 0.5D
                || coefficientOffset < 0.006D && entropy < 1.1D && correlation < 0.3D
                || correlationOffset < 0.01D && entropy < 1.1D && correlation < 0.25D;
    }
}
