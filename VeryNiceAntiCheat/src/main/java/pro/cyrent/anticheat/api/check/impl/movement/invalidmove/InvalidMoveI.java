package pro.cyrent.anticheat.api.check.impl.movement.invalidmove;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "InvalidMove", checkNameEnum = CheckName.INVALID_MOVE,
        subName = "I",
        checkType = CheckType.MOVEMENT,
        experimental = true,
        punishable = false,
        description = "Detects if the player moves too quickly while sneaking",
        state = CheckState.ALPHA)
public class InvalidMoveI extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasPositionChanged()) return;

            if (getData().getLastTeleport().getDelta() < 19
                    || getData().generalCancel()
                    || getData().isBedrock()
                    || getData().getActionProcessor().isTeleportingV2()) {
                return;
            }

            if (getData().getMovementPredictionProcessor().isCanFlagSneak()) {
                if (++this.threshold > 15) {
                    this.fail("threshold=" + threshold);
                    getData().getPlayer().setSneaking(false);
                    this.threshold /= 2;
                }
            } else {
                this.threshold -= Math.min(this.threshold, 1);
            }
        }
    }
}
