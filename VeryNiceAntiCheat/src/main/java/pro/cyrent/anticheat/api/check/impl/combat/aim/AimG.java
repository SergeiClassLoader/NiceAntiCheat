package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "G",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        description = "Detects if the players aim values don't match minecraft",
        punishmentVL = 15,
        state = CheckState.RELEASE)
public class AimG extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            boolean exempt = getData().getActionProcessor().isTeleportingV2();

            if (exempt || getData().generalCancel() || !flying.hasRotationChanged()) return;

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 2) return;

            double deltaYaw = getData().getMovementProcessor().getDeltaYawAbs();
            double deltaPitch = getData().getMovementProcessor().getDeltaPitchAbs();

            if (deltaYaw < 1.0F || deltaPitch < 1.0F) return;

            if (getData().getCinematicProcessor().isCinematic()) {
                this.threshold = 0;
                return;
            }

            double divisorX = getData().getSensitivityProcessor().getDivisorX();
            double divisorY = getData().getSensitivityProcessor().getDivisorY();

            double divisorMax = Math.max(divisorX, divisorY);

            double roundedDivisor = Math.round(divisorMax);

            if (divisorMax < 0.0078125F || roundedDivisor > 0 && divisorMax == roundedDivisor) {
                if (++this.threshold > 25) {
                    this.fail("divisorX="+divisorX,
                            "divisorY="+divisorY,
                            "divisorMax="+divisorMax,
                            "divisorRound="+roundedDivisor);
                }
            } else {
                this.threshold -= Math.min(this.threshold, 2);
            }
        }
    }
}
