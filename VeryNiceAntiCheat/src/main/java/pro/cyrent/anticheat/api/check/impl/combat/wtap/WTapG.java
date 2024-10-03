package pro.cyrent.anticheat.api.check.impl.combat.wtap;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CheckInformation(
        name = "WTap",
        subName = "G",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.W_TAP,
        description = "Detects invalid sprint packets",
        punishmentVL = 10,
        punishable = false,
        experimental = true,
        state = CheckState.PRE_BETA)
public class WTapG extends Check {

    private double threshold;
    private long lastStartSprint;
    private boolean update = false;

    private double lastStd;

    private final List<Long> sprintResetList = new CopyOnWriteArrayList<>();

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {

            if (getData().generalCancel()) {
                return;
            }

            if (this.update) {
                this.update = false;

                if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 20
                        && getData().getVelocityProcessor().getVelocityTicksConfirmed() < 120) {

                    this.sprintResetList.add(this.lastStartSprint);

                    if (this.sprintResetList.size() >= 10) {
                        double std = StreamUtil.getStandardDeviation(this.sprintResetList);

                        double offset = Math.abs(std - this.lastStd);

                        if (std < 700 || offset < 20) {
                            if (++this.threshold > 3.5) {
                                this.fail(
                                        "offset="+offset,
                                        "std="+std);
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, .2);
                        }

                        this.lastStd = std;
                        this.sprintResetList.clear();
                    }
                }
            }
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event.getPacketReceiveEvent());

            if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING) {
                this.lastStartSprint = System.currentTimeMillis();
                this.update = true;
            }
        }
    }
}
