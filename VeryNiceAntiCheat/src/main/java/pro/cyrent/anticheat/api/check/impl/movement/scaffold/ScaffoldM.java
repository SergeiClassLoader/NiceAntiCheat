package pro.cyrent.anticheat.api.check.impl.movement.scaffold;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;

@CheckInformation(
        name = "Scaffold",
        subName = "M",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.SCAFFOLD,
        description = "Detects consistent diagonal bridging",
        punishable = false,
        experimental = true,
        state = CheckState.PRE_ALPHA)
public class ScaffoldM extends Check {

    private int lastFaceValue, lastLastFaceValue;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isPlace()) {
            WrapperPlayClientPlayerBlockPlacement placement = new WrapperPlayClientPlayerBlockPlacement(event.getPacketReceiveEvent());

            if (getData().generalCancel()
                    || getData().getVelocityProcessor().getVelocityTicksConfirmed() < 20
                    || getData().getVelocityProcessor().getServerVelocityTicks() < 20
                    || getData().isBedrock()) {
                this.threshold = 0;
                return;
            }

            if (getData().getScaffoldProcessor().getScaffoldTimer().getDelta() < 3
                    || getData().getScaffoldProcessor().getScaffoldTimerJump().getDelta() < 3) {

                short value = placement.getFace().getFaceValue();
                double deltaXZ = getData().getMovementProcessor().getDeltaXZ();

                if (value == this.lastLastFaceValue && value != this.lastFaceValue && deltaXZ > 0.18) {
                    if (++this.threshold > 5.0) {
                        this.fail("face="+value,
                                "lastFace="+this.lastFaceValue,
                                "lastFastFace="+this.lastLastFaceValue);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, 1.5);
                }

                this.lastLastFaceValue = this.lastFaceValue;
                this.lastFaceValue = value;
            }
        }
    }
}
