package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

import java.util.Deque;
import java.util.LinkedList;

@CheckInformation(
        name = "Aim",
        subName = "S",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishable = false,
        experimental = true,
        description = "Detects if the players yaw mimics their pitch",
        state = CheckState.PRE_RELEASE)
public class AimS extends Check {

    private double threshold;
    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasRotationChanged()) return;

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3) {
                double deltaYaw = getData().getMovementProcessor().getDeltaYaw();
                double deltaPitch = getData().getMovementProcessor().getDeltaPitch();

                boolean invalidAbs = Math.abs(Math.abs(deltaPitch) + Math.abs(deltaYaw)) == 0
                        || Math.abs(Math.abs(deltaPitch) - Math.abs(deltaYaw)) == 0;

                boolean invalid = Math.abs(deltaPitch - deltaYaw) == 0
                        || Math.abs(deltaYaw + deltaPitch) == 0;

                boolean movingHead = deltaPitch > 0 && deltaYaw > 0;

                if ((invalid || invalidAbs) && movingHead) {
                    if (++this.threshold > 5.0) {
                        this.fail(
                                "deltaYaw="+deltaYaw,
                                "deltaPitch="+deltaPitch);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .025);
                }
            }
        }
    }
}