package pro.cyrent.anticheat.api.check.impl.combat.aim;


import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "L",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishmentVL = 8,
        description = "Detects invalid modulo offsets in the players aim",
        state = CheckState.PRE_RELEASE)
public class AimL extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasRotationChanged()) {
                return;
            }

            if (this.getData().getMovementProcessor().getDeltaXZ() < .03
                    || this.getData().generalCancel()
                    || getData().getActionProcessor().isTeleportingV3()
                    || getData().getSensitivityProcessor().getSensitivityCycles() < 50) {
                this.threshold -= Math.min(this.threshold, .01);
                return;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3) {

                float sensitivityValue = getData().getSensitivityProcessor().getSensitivityValue();
                long sensitivity = getData().getSensitivityProcessor().getSensitivity();

                if (sensitivity == -1 || sensitivityValue == -1) return;

                float yawDelta = Math.abs(getData().getMovementProcessor().getDeltaYaw());
                float pitchDelta = Math.abs(getData().getMovementProcessor().getDeltaPitch());

                if (yawDelta > 8.0 || pitchDelta > 8.0) {
                    return;
                }

                float f = sensitivityValue * .6F + .2F;
                float f1 = f * f * f * 1.2F;

                float deltaX = yawDelta / f1;
                float deltaY = pitchDelta / f1;

                float offsetX = Math.abs(Math.round(deltaX) - deltaX);
                float offsetY = Math.abs(Math.round(deltaY) - deltaY);

                boolean invalid = (((offsetX > 0.15F && offsetY < 0.0001F)
                        || (offsetY > 0.08F && offsetX < 0.0001F)))
                        && yawDelta > 0.75 && pitchDelta > .75;

                if (invalid) {
                    if (++this.threshold > 10.0) {
                        this.fail("offsetX=" + offsetX,
                                "offsetY=" + offsetY,
                                "sensitivity=" + sensitivity,
                                "sensitivity-value=" + sensitivityValue,
                                "deltaPitch=" + pitchDelta,
                                "deltaYaw=" + yawDelta);
                    }
                } else {
                    // can be 0.05 if you feel risky!!! (mines 0.05 but i think it can false on that so? idk)
                    this.threshold -= Math.min(this.threshold, .125);
                }
            }
        }
    }
}