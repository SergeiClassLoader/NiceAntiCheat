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
        subName = "B",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Detects if the player clicks too consistently (data size of 100)",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.RELEASE)
public class AutoClickerB extends Check {

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

                    double std = StreamUtil.getStandardDeviation(this.clickData);

                    if (std <= 0.45) {
                        if (++this.threshold > 3) {
                            this.fail( "std="+std);
                        }

                    } else {
                        this.threshold -= Math.min(this.threshold, .5);
                    }


                    this.clickData.clear();
                }
            }

            this.movements = 0;
        }
    }
}