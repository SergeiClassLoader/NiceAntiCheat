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
        subName = "C",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Detects if the players click pattern matches their previous",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.RELEASE)
public class AutoClickerC extends Check {

    private double threshold, lastStandardDeviation;
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
                    || getData().getLastBlockPlaceCancelTimer().getDelta() < 4
                    || getData().getActionProcessor().isDigging()) {
                this.clickData.clear();
                return;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 60) return;

            if (this.movements < 8) {

                this.clickData.add(this.movements);

                if (this.clickData.size() >= 100) {

                    double standardDeviation = StreamUtil.getStandardDeviation(this.clickData);

                    double difference = Math.abs(standardDeviation - this.lastStandardDeviation);

                    if (difference <= 0.01) {
                        if (++this.threshold > 3) {

                            this.fail("differance=" + difference,
                                    "std=" + standardDeviation);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .5);
                    }

                    this.lastStandardDeviation = standardDeviation;
                    this.clickData.clear();
                }
            }

            this.movements = 0;
        }
    }
}