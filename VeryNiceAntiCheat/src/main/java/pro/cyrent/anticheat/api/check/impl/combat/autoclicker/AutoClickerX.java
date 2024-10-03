package pro.cyrent.anticheat.api.check.impl.combat.autoclicker;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CheckInformation(
        name = "AutoClicker",
        subName = "X",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Detects a various amount of auto-clicker patterns",
        punishmentVL = 10,
        punishable = false,
        experimental = true,
        state = CheckState.DEV)
public class AutoClickerX extends Check {

    private double lastDev, lastEntropy, lastVariance, variance;

    private double globalCheatPercent;

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

            if (this.movements < 10) {

                this.clickData.add(this.movements);

                if (this.clickData.size() >= 100) {
                    double dev = StreamUtil.getDeviation(this.clickData);
                    double cps = StreamUtil.getCPS(this.clickData);
                    double entropy = StreamUtil.getEntropy(this.clickData);
                    double skewness = StreamUtil.getSkewness(this.clickData);
                    double variance = StreamUtil.getVariance(this.clickData);

                    int distinct = StreamUtil.getDistinct(this.clickData);

                    this.handleInvalidClickData(dev, entropy, distinct, skewness, cps, variance);

                    this.lastVariance = this.variance;
                    this.variance = variance;
                    this.lastEntropy = entropy;
                    this.lastDev = dev;
                    this.clickData.clear();
                }
            }

            this.movements = 0;
        }
    }

    private void handleInvalidClickData(double dev, double entropy, int distinct,
                                      double skewness, double cps, double variance) {

        double cheatingPercent = 0;

        double deviationOffset = Math.abs(dev - this.lastDev);
        double entropyOffset = Math.abs(entropy - this.lastEntropy);
        double varOffset = Math.abs(variance - this.lastVariance);

        if (deviationOffset <= .1D) {
            cheatingPercent += 5;
        }

        if (dev < .70D) {
            cheatingPercent += 5;
        }

        if (entropyOffset <= .15D) {
            cheatingPercent += 5;
        }

        if (skewness > 0.0D && skewness < 0.20D) {
            cheatingPercent += 5;
        }

        if (distinct < 4) {
            cheatingPercent += 5;
        }

        if (varOffset < .3D) {
            cheatingPercent += 5;
        }

        if (cps > 13) {
            cheatingPercent += 10;
        } else {
            cheatingPercent *= 0.5;
        }

        this.globalCheatPercent += cheatingPercent;


        if (this.globalCheatPercent >= 70.0) {
            this.fail(
                    "var="+variance,
                    "varOffset="+varOffset,
                    "cps="+cps,
                    "distinct="+distinct,
                    "dev="+dev,
                    "devOffset="+deviationOffset,
                    "entropy="+entropy,
                    "entropyOffset="+entropyOffset,
                    "skewness="+skewness,
                    "percent="+this.globalCheatPercent);
        }

        if (this.globalCheatPercent >= 100) {
            this.globalCheatPercent = 100;
        }

        this.globalCheatPercent -= Math.min(this.globalCheatPercent, 20);
    }
}