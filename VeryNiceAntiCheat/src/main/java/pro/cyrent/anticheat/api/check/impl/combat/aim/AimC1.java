package pro.cyrent.anticheat.api.check.impl.combat.aim;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.Bukkit;

@CheckInformation(
        name = "Aim",
        subName = "C1",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        description = "Detects very low precise divisor changes",
        punishmentVL = 12,
        punishable = false,
        experimental = true,
        state = CheckState.DEV)
public class AimC1 extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasRotationChanged() || getData().generalCancel()
                    || getData().getActionProcessor().isTeleportingV2()) return;

            final float mcpSensitivity = this.getData().getSensitivityProcessor().getSensitivityValue();

            if (mcpSensitivity < 0.01
                    || getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 10) {
                this.threshold -= Math.min(this.threshold, .005);
                return;
            }

            float f = (float) mcpSensitivity * 0.6f + 0.2f;
            float realGCD = f * f * f * 8.0F;

            // only gets smallest delta pitch/yaw motion based on sensitivity.
            float value = (float) (realGCD * 0.15D) * this.getVar(false);

            float deltaPitchAbs = getData().getMovementProcessor().getDeltaPitchAbs();

            float fromPitch = Math.abs(getData().getMovementProcessor().getFrom().getPitch());
            float pitch = Math.abs(getData().getMovementProcessor().getTo().getPitch());

            // predict next motion based on last delta yaw
            float rotationYaw = (float) ((double) fromPitch
                    + (double) value);

            float offset = Math.abs(pitch - rotationYaw);
            float deltaOffset = Math.abs(deltaPitchAbs - offset);
            float valueOffset = Math.abs(deltaOffset - value);

            float gcdOffset = mcpSensitivity % deltaPitchAbs;

            if (offset != 0 && offset < 1E-5 && valueOffset < 1E-12 && gcdOffset < 1E-5) {
                if (++this.threshold > 1.25) {
                    this.fail(
                            "offset=" + offset,
                            "value-offset=" + valueOffset,
                            "gcd-divisor-offset=" + gcdOffset,
                            "currentPitch=" + pitch,
                            "delta-pitch=" + deltaPitchAbs);
                }
            }
        } else {
            this.threshold -= Math.min(this.threshold, .005);
        }
    }

    private float getVar(boolean x) {
        final float mcpSensitivity = this.getData().getSensitivityProcessor().getSensitivityValue();
        float delta = x ? getData().getMovementProcessor().getDeltaYawAbs()
                : getData().getMovementProcessor().getDeltaPitchAbs();
        float f = mcpSensitivity * 0.6f + .2f;
        float calc = f * f * f * 8;
        return (delta / (calc * .15f));
    }
}