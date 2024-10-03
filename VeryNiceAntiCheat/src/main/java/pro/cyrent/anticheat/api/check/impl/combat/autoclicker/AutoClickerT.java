package pro.cyrent.anticheat.api.check.impl.combat.autoclicker;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import java.util.*;

@CheckInformation(
        name = "AutoClicker",
        subName = "T",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Detects invalid long term kurtosis ranges",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.PRE_BETA)
public class AutoClickerT extends Check {

    private double threshold;
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

                if (this.clickData.size() >= 500) {
                    double kurtosis = StreamUtil.getKurtosis(this.clickData);

                    double max = 20.0D;
                    double min = 0.0D;

                    if (kurtosis > max || kurtosis < min) {
                        if (++this.threshold > 3.0) {
                            this.fail(
                                    "kurtosis="+kurtosis,
                                    "max="+max,
                                    "min="+min);
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

