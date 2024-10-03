package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "H",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        description = "Detects if the players snaps their head while fighting",
        punishmentVL = 3,
        state = CheckState.RELEASE)
public class AimH extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            boolean exempt = getData().getActionProcessor().isTeleportingV2();

            if (exempt || getData().generalCancel()) return;

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 2
                    || this.getData().getMovementProcessor().getDeltaXZ() < .085) return;

            double deltaYaw = getData().getMovementProcessor().getDeltaYawAbs();

            if (flying.hasRotationChanged()) {

                double mouseYaw = getData().getSensitivityProcessor().getMouseX();
                double fix = Math.abs(deltaYaw - mouseYaw);
                double snap = Math.abs(deltaYaw - fix);

                if (deltaYaw > 2000) {

                    if ((snap > 3000 || snap < 1.01 && snap > 0.98) && fix > 4000) {
                        this.fail("snap="+snap,
                                "six="+fix,
                                "deltaYaw="+deltaYaw,
                                "mouseX="+mouseYaw);
                    }
                }
            }
        }
    }
}
