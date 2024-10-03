package pro.cyrent.anticheat.api.check.impl.movement.scaffold;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CheckInformation(
        name = "Scaffold",
        subName = "K",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.SCAFFOLD,
        punishable = false,
        description = "Detects odd patterns in the players movements while scaffolding",
        state = CheckState.ALPHA)
public class ScaffoldK extends Check {

    private double threshold, lastDelta;
    private int exemptTicks;
    private final List<Double> movementSamples = new CopyOnWriteArrayList<>();

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            if (getData().generalCancel()
                    || getData().isBedrock()) return;

            this.exemptTicks++;

            if (getData().getPotionProcessor().isSpeedPotion() && getData().getPotionProcessor().getSpeedPotionTicks() < 1
                    || !getData().getPotionProcessor().isSpeedPotion() && getData().getPotionProcessor().getSpeedPotionTicks() > 0
                    || getData().getPotionProcessor().getSpeedPotionAmplifier() > 3
                    || this.getData().getActionProcessor().getWalkSpeed() != 0.1F) {
                this.threshold = 0;
                this.exemptTicks = 0;
                return;
            }

            if (this.exemptTicks < 20) {
                this.threshold = 0;
                return;
            }

            if (getData().getVelocityProcessor().getServerVelocityTicks() < 20
                    || getData().getVelocityProcessor().getVelocityTicksConfirmed() < 20) {
                return;
            }

            if (getData().getActionProcessor().isSneaking()) return;

            if (getData().getScaffoldProcessor().getScaffoldTimerJump().getDelta() < 4) {
                double deltaXZ = getData().getMovementProcessor().getDeltaXZ();
                double lastDeltaXZ = getData().getMovementProcessor().getLastDeltaXZ();

                double delta = Math.abs(deltaXZ - lastDeltaXZ);

                double deltaDelta = Math.abs(delta - this.lastDelta);

                if (deltaXZ >= .1 && delta < 1) {

                    this.movementSamples.add(deltaDelta);

                    if (this.movementSamples.size() == 40) {
                        double std = StreamUtil.getStandardDeviation(this.movementSamples);

                        if (std < .004) {
                            if (++this.threshold > 3) {
                                this.fail(
                                        "std="+std);
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, .2);
                        }

                        this.movementSamples.clear();
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .02);
                }

                this.lastDelta = delta;
            }
        }
    }
}
