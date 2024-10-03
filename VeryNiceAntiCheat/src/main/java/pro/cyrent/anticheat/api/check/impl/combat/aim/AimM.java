package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "M",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishable = false,
        experimental = true,
        description = "Detects if a player's constantly moves quick/has head snap type movements",
        state = CheckState.BETA)
public class AimM extends Check {

    private float lastYawDelta, lastYawAccel, lastLastYawAccel;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (!event.isMovement()) return;

        WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

        if (!flying.hasRotationChanged() || getData().generalCancel()
                || getData().getActionProcessor().getTeleportTicks() <= 10) {
            return;
        }

        if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3) {

            float yawDelta = getData().getMovementProcessor().getDeltaYawAbs();
            float yawAccel = getData().getMovementProcessor().getYawAccel();
            float accelDelta = Math.abs(yawAccel - this.lastYawAccel);
            float gamer = Math.abs(accelDelta - this.lastLastYawAccel);

            boolean invalidAccel = accelDelta < 2.0 && gamer > 20;
            boolean invalidBoth = accelDelta > 20.0 && gamer < 2.0;

            boolean invalid = invalidAccel || invalidBoth;

            if (invalid) {
                if (++this.threshold > 12) {
                    this.fail("invalidAccel=" + invalidAccel,
                            "invalidBoth=" + invalidBoth);
                }
            } else {
                this.threshold -= Math.min(this.threshold, 0.08);
            }

            this.lastLastYawAccel = accelDelta;
            this.lastYawAccel = yawAccel;
            this.lastYawDelta = yawDelta;
        }
    }
}