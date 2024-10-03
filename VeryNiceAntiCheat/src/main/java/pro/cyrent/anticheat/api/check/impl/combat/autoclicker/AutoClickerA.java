package pro.cyrent.anticheat.api.check.impl.combat.autoclicker;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import pro.cyrent.anticheat.api.check.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CheckInformation(
        name = "AutoClicker",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Detects if the player clicks too quickly",
        punishmentVL = 10,
        state = CheckState.RELEASE)
public class AutoClickerA extends Check {

    private double threshold;
    private long lastSent = System.currentTimeMillis();

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ANIMATION) {

                if (getData().generalCancel()
                        || getData().getLastBlockPlaceTimer().hasNotPassed(2)
                        || getData().getLastBlockPlaceCancelTimer().hasNotPassed(2)
                        || getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() < 3
                        || getData().getActionProcessor().isDigging()
                        || getData().getClickProcessor().getClicks().size() < 20
                        || getData().getMovementProcessor().getSkippedPackets() > 5
                        || getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 3
                        || getData().getLastBlockBreakTimer().hasNotPassed(9)) {
                    return;
                }

                double cps = getData().getClickProcessor().getCps();

                if (getData().getClickProcessor().getCps() >= Anticheat.INSTANCE.getChecksValues().getAutoClickerACPS()) {
                    if (++this.threshold > 4.5) {
                        this.threshold = 0;

                        if (Anticheat.INSTANCE.getChecksValues().isAutoClickerCancelHits()) {
                            getData().getCombatProcessor().setCancelTimedAttack(true);
                        }

                        if (Anticheat.INSTANCE.getChecksValues().isAlertAutoClickerA() &&
                                (System.currentTimeMillis() - this.lastSent) >= 2000L) {
                            String msg = Anticheat.INSTANCE.getChecksValues().getAutoClickerAMessage()
                                    .replace("%MAXCPS%",
                                            String.valueOf(Anticheat.INSTANCE.getChecksValues().getAutoClickerACPS()))
                                    .replace("%CPS%", String.valueOf(Math.round(getData().getClickProcessor().getCps())));
                            getData().getPlayer().sendMessage("");
                            getData().getPlayer().sendMessage(msg);
                            getData().getPlayer().sendMessage("");
                            this.lastSent = System.currentTimeMillis();
                        }

                        if (Anticheat.INSTANCE.getChecksValues().isAutoClickerAAlert()) {
                            this.fail(
                                    "cps=" + cps,
                                    "maxCps=" + Anticheat.INSTANCE.getChecksValues().getAutoClickerACPS());
                        }
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .003);
                }
            }
        }
    }
}