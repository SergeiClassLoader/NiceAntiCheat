package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "I",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        description = "Detects if a players yaw rotation equals their last",
        punishmentVL = 10,
        state = CheckState.PRE_BETA)
public class AimI extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasRotationChanged()) {
                this.threshold -= Math.min(this.threshold, .5);
                return;
            }

            boolean exempt = getData().getActionProcessor().isTeleportingV2();

            if (exempt || getData().generalCancel()
                    || getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 3) return;

            float lastYaw = getData().getMovementProcessor().getLastDeltaYawAbs();
            float yaw = getData().getMovementProcessor().getDeltaYawAbs();

            if (yaw < 1.0F || lastYaw < 1.0F) {
                this.threshold -= Math.min(this.threshold, .05);
                return;
            }

            if (yaw == lastYaw && yaw > 3.5F) {
                if (++this.threshold > 20) {
                    this.fail("yaw="+yaw,
                            "lastYaw="+yaw,
                            "sensitivity="+getData().getSensitivityProcessor().getSensitivity());
                }
            } else {
                this.threshold -= Math.min(this.threshold, 1.5);
            }
        }
    }
}
