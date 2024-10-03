package pro.cyrent.anticheat.api.check.impl.combat.autoclicker;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import pro.cyrent.anticheat.api.check.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CheckInformation(
        name = "AutoClicker",
        subName = "D",
        checkNameEnum = CheckName.AUTO_CLICKER,
        checkType = CheckType.COMBAT,
        description = "Detects if the players click pattern contains no outliers.",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.RELEASE)
public class AutoClickerD extends Check {

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

                if (clickSamples.size() >= 1000) {

                    int outliers = (int) clickSamples.stream().filter(delay -> delay > 3).count();

                    if (outliers < 7) {
                        if (++this.threshold > 2) {
                            this.fail("outlier="+outliers,
                                    "maxOutlier=6-e7");
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, 1.2);
                    }

                    this.clickSamples.clear();
                }
            }

            this.movements = 0;
        }
    }
}