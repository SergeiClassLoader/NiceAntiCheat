package pro.cyrent.anticheat.api.check.impl.movement.scaffold;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import org.bukkit.Bukkit;

@CheckInformation(
        name = "Scaffold",
        subName = "N",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.SCAFFOLD,
        description = "Detects no slow down in the players movement while scaffolding",
        punishable = false,
        experimental = true,
        state = CheckState.PRE_ALPHA)
public class ScaffoldN extends Check {

    private int slowdown;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            if (getData().generalCancel()
                    || getData().getPlayer().getWalkSpeed() != 0.2F
                    || getData().getVelocityProcessor().getVelocityTicksConfirmed() < 20
                    || getData().getVelocityProcessor().getServerVelocityTicks() < 20
                    || getData().getPotionProcessor().getJumpPotionAmplifier() > 0
                    || getData().isBedrock()) {
                this.slowdown = 0;
                this.threshold -= Math.min(this.threshold, .045);
                return;
            }

            if (getData().getScaffoldProcessor().getScaffoldTimer().getDelta() < 3
                    || getData().getScaffoldProcessor().getScaffoldTimerJump().getDelta() < 3) {

                double deltaXZ = getData().getMovementProcessor().getDeltaXZ();

                double max = getData().getPotionProcessor().getSpeedPotionTicks() > 0 ?
                        .170 + (getData().getPotionProcessor().getSpeedPotionAmplifier() * 0.0425) : .170;

                if (deltaXZ >= max) {
                    this.slowdown++;
                } else {
                    this.slowdown = 0;
                }

                if (this.slowdown > 10) {
                    if (++this.threshold > 7.0) {
                        this.fail("deltaXZ="+deltaXZ,
                                "slowDown="+slowdown,
                                "max="+max);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .045);
                }
            }
        }
    }
}
