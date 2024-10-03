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
        subName = "E",
        checkNameEnum = CheckName.AUTO_CLICKER,
        checkType = CheckType.COMBAT,
        description = "Compares Kurtosis and Skewness data of the click pattern",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.BETA)
public class AutoClickerE extends Check {

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

                if (clickSamples.size() >= 100) {
                    double skewness = StreamUtil.getSkewness(this.clickSamples);
                    double kurtosis = StreamUtil.getKurtosis(this.clickSamples);

                    double kurtSkewDelta = Math.abs(kurtosis - skewness);

                    if (kurtSkewDelta > 8.0 && kurtosis < 5) {
                        if (++this.threshold > 3) {
                            this.fail(
                                    "delta=" + kurtSkewDelta);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .3);
                    }


                    this.clickSamples.clear();
                }
            }

            this.movements = 0;
        }
    }
}