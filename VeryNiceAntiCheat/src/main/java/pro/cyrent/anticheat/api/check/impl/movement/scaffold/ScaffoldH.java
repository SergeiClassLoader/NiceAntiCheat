package pro.cyrent.anticheat.api.check.impl.movement.scaffold;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "Scaffold",
        subName = "H",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.SCAFFOLD,
        description = "Detects if the player never sneaks while scaffolding (and jumping)",
        state = CheckState.PRE_RELEASE)
public class ScaffoldH extends Check {

    private double threshold;
    private int places;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            if (getData().generalCancel()) return;

            if (this.places > 0 && getData().getPlayer().isSneaking()) {
                this.places = 0;
                return;
            }

            if (getData().generalCancel()
                    || getData().getActionProcessor().isTeleportingV3()
                    || getData().isBedrock()) {
                this.threshold = 0;
                this.places = 0;
                return;
            }

            if (getData().getVelocityProcessor().getServerVelocityTicks() < 20
                    || getData().getVelocityProcessor().getVelocityTicksConfirmed() < 20) {
                this.threshold = 0;
                this.places = 0;
                return;
            }

            if (getData().getMovementProcessor().getBlockJumpTimer().getDelta() < 3) {
                this.threshold = 0;
                this.places = 0;
                return;
            }

            if (getData().getPlayer().getItemInHand() == null
                    || getData().getPlayer().getItemInHand().getType() == null
                    || !getData().getPlayer().getItemInHand().getType().isBlock()) {
                this.threshold = 0;
                this.places = 0;
                return;
            }

            if (getData().getScaffoldProcessor().getScaffoldTimerJump().getDelta() < 3
                    && !getData().getMovementProcessor().getTo().isOnGround()) {

                if (++this.places > 25) {
                    if (++this.threshold > 3) {
                        this.fail("Placing blocks too long without slowing down. (never sneaking)");
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, 0.01);
                }
            }
        }
    }
}
