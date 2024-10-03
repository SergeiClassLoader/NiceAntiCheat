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
        subName = "F",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Detects invalid delta changes in the click variance",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.BETA)
public class AutoClickerF extends Check {

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
                    int max = StreamUtil.getMaximumInt(this.clickSamples);
                    int min = StreamUtil.getMinimumInt(this.clickSamples);

                    int delta = Math.abs(max - min);

                    double variance = StreamUtil.getVariance(this.clickSamples);
                    double varianceFixed = delta / variance;

                    // Ik ik your like wtf this much threshold and lowers that much
                    // some how it still gets autoclickers lmao

                    if (varianceFixed < 0.0125) {
                        if (++this.threshold > 9) {
                            this.fail("variance="+variance,
                                    "varDelta="+varianceFixed,
                                    "delta="+delta);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, 1.75);
                    }

                    this.clickSamples.clear();
                }
            }

            this.movements = 0;
        }
    }
}