package pro.cyrent.anticheat.api.check.impl.combat.autoclicker;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import java.util.Deque;
import java.util.LinkedList;

@CheckInformation(
        name = "AutoClicker",
        subName = "U",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Detects invalid click pattern variances",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.PRE_BETA)
public class AutoClickerU extends Check {

    private double threshold, lastVariance;
    private int movements;

    private final Deque<Integer> clickData = new LinkedList<>();

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
                if (this.movements == 0) return;

                this.clickData.addLast(this.movements);

                if (this.clickData.size() >= 100) {
                    double variance = StreamUtil.getVariance(this.clickData);
                    double delta = Math.abs(variance - this.lastVariance);

                    if (variance < 1.0) {
                        if (delta < 0.01) {
                            if (++this.threshold > 3.0) {
                                this.fail(
                                        "variance="+variance,
                                        "delta="+delta);
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, 1.2);
                        }
                    }

                    this.lastVariance = variance;
                    this.clickData.clear();
                }
            }

            this.movements = 0;
        }
    }
}
