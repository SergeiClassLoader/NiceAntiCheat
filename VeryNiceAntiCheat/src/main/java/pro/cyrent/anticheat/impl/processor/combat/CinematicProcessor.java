package pro.cyrent.anticheat.impl.processor.combat;

import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.event.EventTimer;
import pro.cyrent.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CinematicProcessor extends Event {
    private final PlayerData data;

    private double lastYawAcelleration, lastPitchAcelleration;
    private double lastX, lastY, lastLastY, lastLastYaw;
    private int ticks;
    private boolean cinematic;
    private boolean inTick;

    private final EventTimer lastValid;

    private boolean specialGCD = false;

    public CinematicProcessor(PlayerData user) {
        this.data = user;
        this.lastValid = new EventTimer(80, user);
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {

                WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

                if (!flying.hasRotationChanged()) return;

                float yawAcelleration = Math.abs(this.getData().getMovementProcessor().getDeltaYaw());
                float pitchAcelleration = Math.abs(this.getData().getMovementProcessor().getDeltaPitch());

                // They are not rotating
               if (yawAcelleration < 0.002 || pitchAcelleration < 0.002) return;

                // Deltas between the current acelleration and last
                double x = Math.abs(yawAcelleration - this.lastYawAcelleration);
                double y = Math.abs(pitchAcelleration - this.lastPitchAcelleration);

                // Deltas between last X & Y
                double deltaX = Math.abs(x - this.lastX);
                double deltaY = Math.abs(y - this.lastY);

                // Pitch delta change
                double pitchChangeAcelleration = Math.abs(this.lastLastY - deltaY);
                double yawChangeAcelleration = Math.abs(this.lastLastYaw - deltaX);
                this.inTick = false;

                boolean yawCheck = !MathUtil.isScientificNotation(yawAcelleration) && yawAcelleration > 0.08
                        && yawChangeAcelleration > 0 && yawChangeAcelleration < 0.0855;

                //new one
                boolean pitchCheckExtra = pitchAcelleration > 0.08
                        && !MathUtil.isScientificNotation(pitchChangeAcelleration)
                        && pitchChangeAcelleration > 0 && pitchChangeAcelleration < 0.5;

                //old one
                boolean oldPitchCheck = (pitchAcelleration > .08 && pitchChangeAcelleration > 0
                        && !MathUtil.isScientificNotation(pitchChangeAcelleration)
                        && pitchChangeAcelleration < .0855);

                //fixes aim f.
                this.specialGCD = Math.abs(yawAcelleration - yawChangeAcelleration) > 20.0;

                // we have to check something different for pitch due to it being a little harder to check for being smooth
                if (x < .04 || y < .04 || oldPitchCheck) {

                    // check if the GCD is valid
                    if (this.isInvalidGCD()) {
                        this.ticks += (this.ticks < 20 ? 1 : 0);
                        this.lastValid.reset();
                    }
                } else {
                    this.ticks -= this.ticks > 0 ? 1 : 0;
                }

                this.lastLastYaw = deltaX;
                this.lastLastY = deltaY;
                this.lastX = x;
                this.lastY = y;

                this.lastYawAcelleration = yawAcelleration;
                this.lastPitchAcelleration = pitchAcelleration;

                // set the cinematic boolean if they are using
                this.cinematic = (this.ticks > 0 || this.lastValid.hasNotPassed(20))
                        && this.getData().getMovementProcessor().getTick() > 10;
            }
        }
    }

    private boolean isInvalidGCD() {
        return this.getData().getSensitivityProcessor().getPitchGCD() < 131072L;
    }
}
