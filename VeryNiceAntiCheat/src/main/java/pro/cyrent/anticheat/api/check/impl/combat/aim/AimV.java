package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "V",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishable = false,
        experimental = true,
        description = "Collects fast head acceleration and finds aim-assist like patterns",
        state = CheckState.BETA)
public class AimV extends Check {

    private double threshold;
    private double lastDelta;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 4) {

                if (this.getData().getMovementProcessor().getDeltaXZ() < .085
                        || getData().getCinematicProcessor().isCinematic()
                        || getData().getSensitivityProcessor().sensitivityCycles < 50
                        || this.getData().generalCancel()) {
                    return;
                }

                if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 2) {

                    double pitchDelta = getData().getMovementProcessor().getDeltaPitch();
                    double pitchAccel = Math.abs(getData().getMovementProcessor().getPitchAccel());
                    double deltaYawAbs = getData().getMovementProcessor().getDeltaYawAbs();
                    double lastDeltaYawAbs = getData().getMovementProcessor().getLastDeltaYawAbs();

                    double delta = Math.abs(deltaYawAbs - lastDeltaYawAbs);

                    double offset = Math.abs(delta - this.lastDelta);

                    if (offset > 15.0 && this.lastDelta > 15.0 && delta > 15.0
                            && Math.abs(pitchDelta) < .15 && Math.abs(pitchAccel) < .15) {

                        if (++this.threshold > 1.75) {
                            this.fail(
                                    "offset="+offset,
                                    "accel="+Math.abs(pitchAccel),
                                    "delta="+delta,
                                    "deltaPitch="+Math.abs(pitchDelta),
                                    "lastDeltaPitch="+this.lastDelta,
                                    "threshold="+this.threshold);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .0125);
                    }

                    this.lastDelta = delta;
                }
            }
        }
    }
}