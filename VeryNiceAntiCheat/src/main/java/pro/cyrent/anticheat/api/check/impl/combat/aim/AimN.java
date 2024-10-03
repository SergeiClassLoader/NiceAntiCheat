package pro.cyrent.anticheat.api.check.impl.combat.aim;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Aim",
        subName = "N",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishable = false,
        punishmentVL = 40,
        description = "Detects unrealistic randomization but over time (pitch)",
        state = CheckState.BETA)
public class AimN extends Check {

    private double threshold;
    private boolean aimIsReady;
    private double verboseUp;
    private int upTick, downTick;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            boolean ground = !getData().getMovementProcessor().getTo().isOnGround()
                    || !getData().getMovementProcessor().getFrom().isOnGround();

            if (ground) {
                return;
            }

            int tick = getData().getMovementProcessor().getTick();

            if ((tick - this.verboseUp) > 100) {
                this.threshold = 0;
                this.verboseUp = tick;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3) {

                double deltaYaw = getData().getMovementProcessor().getDeltaYaw();
                double deltaPitch = getData().getMovementProcessor().getDeltaPitch();

                double absDeltaPitch = Math.abs(deltaPitch);

                if (deltaPitch != 0.0 && absDeltaPitch > 1.0 && absDeltaPitch < 90 && Math.abs(deltaYaw) > 1.20) {
                    if (deltaPitch > 0.0) {
                        this.upTick = tick;
                    } else if (deltaPitch < 0.0) {
                        this.downTick = tick;
                    }

                    int up = (tick - this.upTick);
                    int down = (tick - this.downTick);

                    if (up == 1 && down == 0) {
                        if (!this.aimIsReady) {
                            this.aimIsReady = true;
                        } else {
                            if (this.threshold > 0) this.threshold--;
                        }
                    } else if (down == 1 && up == 0) {
                        if (this.aimIsReady) {
                            this.aimIsReady = false;

                            if (this.threshold < 47) this.threshold++;
                            this.verboseUp = tick;
                        }
                    }
                }

                if (this.threshold > 45) {
                    this.threshold = 44;
                    this.fail("Cloud Check");
                }
            }
        }
    }
}