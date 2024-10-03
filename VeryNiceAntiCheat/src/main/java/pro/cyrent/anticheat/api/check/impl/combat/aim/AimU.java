package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "U",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishable = false,
        experimental = true,
        description = "Finds a collection of duplicate +/- motions in the players pitch",
        state = CheckState.BETA)
public class AimU extends Check {

    private double threshold;
    private double lastDeltaPitch;

    private double upDuplicate;
    private double downDuplicate;
    private double completeDuplicate;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasRotationChanged()) {
                return;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 4) {

                if (this.getData().getMovementProcessor().getDeltaXZ() < .085
                        || getData().getCinematicProcessor().isCinematic()
                        || getData().getSensitivityProcessor().sensitivityCycles < 50
                        || this.getData().generalCancel()) {
                    return;
                }

                if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 2) {

                    double pitchDelta = getData().getMovementProcessor().getDeltaPitch();
                    double lastPitchDelta = getData().getMovementProcessor().getLastDeltaPitch();
                    double pitchAccel = Math.abs(getData().getMovementProcessor().getPitchAccel());

                    double deltaYawAbs = getData().getMovementProcessor().getDeltaYawAbs();
                    double lastDeltaYawAbs = getData().getMovementProcessor().getLastDeltaYawAbs();

                    if (((Math.abs((Math.abs(pitchDelta) - Math.abs(lastPitchDelta))) < 1E-12)
                            || Math.abs((Math.abs(pitchDelta) - pitchAccel)) < 1E-12)
                            && deltaYawAbs > 1.5F && lastDeltaYawAbs > 1.5F) {

                        if (lastPitchDelta < 0 && pitchDelta > 0
                                || pitchAccel < 0 && pitchDelta > 0) {
                            this.downDuplicate++;
                        } else if (lastPitchDelta > 0 && pitchDelta < 0
                                || pitchAccel > 0 && pitchDelta < 0) {
                            this.upDuplicate++;
                        }

                        if (this.downDuplicate > 30 || this.upDuplicate > 30) {
                            if (++this.threshold > 4.5) {
                                this.fail("downDuplicates="+this.downDuplicate);
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, .0125);
                        }

                    } else {
                        this.downDuplicate -= Math.min(this.downDuplicate, .05);
                        this.upDuplicate -= Math.min(this.upDuplicate, .05);
                        this.completeDuplicate -= Math.min(this.completeDuplicate, .05);
                    }
                }
            }
        }
    }
}