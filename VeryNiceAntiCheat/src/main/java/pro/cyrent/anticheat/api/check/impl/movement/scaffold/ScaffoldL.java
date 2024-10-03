package pro.cyrent.anticheat.api.check.impl.movement.scaffold;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CheckInformation(
        name = "Scaffold",
        subName = "L",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.SCAFFOLD,
        description = "Detects if the player has odd consistent movements",
        punishmentVL = 30,
        state = CheckState.ALPHA)
public class ScaffoldL extends Check {

    private double threshold, lastDeviation;
    private int places;
    private int lastPlace;

    private final List<Double> movementSamples = new CopyOnWriteArrayList<>();

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            if (getData().generalCancel()
                    || getData().isBedrock()) return;

            if (this.places > 0 && getData().getPlayer().isSneaking()) {
                this.places = 0;
                return;
            }

            if (getData().generalCancel()
                    || getData().getVelocityProcessor().getVelocityTicksConfirmed() < 10) {
                this.threshold = 0;
                this.places = 0;
                return;
            }

            if (!getData().getMovementProcessor().getTo().isOnGround()
                    || !getData().getMovementProcessor().getFrom().isOnGround()) {
                this.places = 0;
                return;
            }

            if (getData().getScaffoldProcessor().getScaffoldTimer().getDelta() < 2) {

                double deltaXZ = getData().getMovementProcessor().getDeltaXZ();

                if (deltaXZ > 0.118 || deltaXZ < 0.06) {
                    return;
                }

                ++this.places;

                this.movementSamples.add(deltaXZ);

                if (this.movementSamples.size() >= 10 && this.places > 12) {
                    double deviation = StreamUtil.getDeviation(this.movementSamples);

                    double devDelta = Math.abs(deviation - this.lastDeviation);

                    float pitch = getData().getMovementProcessor().getTo().getPitch();

                    if (deviation < 0.03 && this.lastDeviation < 0.03 && devDelta < 0.005
                            && pitch > 77.0 && pitch < 80.0
                            && this.places > this.lastPlace) {
                        if (++this.threshold > 3.5) {
                            this.fail("Potentially using Vape V4 Scaffold (GodBridge Mode?)");
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .25);
                    }

                    this.lastDeviation = deviation;
                    this.movementSamples.clear();
                }

                this.lastPlace = this.places;
            }
        }
    }
}
