package pro.cyrent.anticheat.api.check.impl.combat.wtap;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.evicting.EvictingList;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

import java.util.Deque;

@CheckInformation(
        name = "WTap",
        subName = "D",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.W_TAP,
        description = "Detects if a player sends synced sprints with velocity",
        punishmentVL = 8,
        state = CheckState.PRE_RELEASE)
public class WTapD extends Check {

    private double threshold;
    private final Deque<Integer> velocitySprintData = new EvictingList<>(20);
    private int lastStartSent;

    // detects sprint reset velocity

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {

            int velocityTicks = getData().getHorizontalProcessor().getVelocityTicks();
            boolean velocity = velocityTicks <= 6;

            if (getData().getLastFireTickTimer().getDelta() < 20
                    || getData().getBlockProcessor().getLastWebUpdateTick() < 7) {
                return;
            }

            if (velocity && !getData().generalCancel() && !getData().getActionProcessor().isTeleportingV2() && !getData().getCollisionProcessor().isWebInside()) {
                if (this.lastStartSent == 0) {
                    this.velocitySprintData.add(velocityTicks);

                    if (this.velocitySprintData.size() > 18) {
                        double std = StreamUtil.getStandardDeviation(this.velocitySprintData);

                        if (std <= .5) {
                            if (++this.threshold > 4.5) {
                                this.fail("std="+std);
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, .125);
                        }
                    }
                }
            }
            this.lastStartSent++;
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event.getPacketReceiveEvent());

            if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING) {
                this.lastStartSent = 0;
            }
        }
    }
}
