package pro.cyrent.anticheat.api.check.impl.movement.scaffold;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import org.bukkit.Location;

@CheckInformation(
        name = "Scaffold",
        subName = "C",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.SCAFFOLD,
        description = "Detects if the player scaffolds downwards",
        punishmentVL = 3,
        state = CheckState.RELEASE)
public class ScaffoldC extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isPlace()) {
            if (getData().generalCancel()) return;

            WrapperPlayClientPlayerBlockPlacement placement = new WrapperPlayClientPlayerBlockPlacement(event.getPacketReceiveEvent());

            if (placement.getBlockPosition() == null) return;

            if (getData().generalCancel()
                    || getData().isBedrock() || getData().getActionProcessor().isTeleportingV2()
                    || getData().getActionProcessor().isTeleportingReal()) {
                return;
            }

            if (placement.getFace() == BlockFace.DOWN && getData().getMovementProcessor().getTo().isOnGround()) {
                double offsetY = getData().getMovementProcessor().getTo().getPosY() -
                        placement.getBlockPosition().getY();

                if (offsetY >= 1) {
                    this.fail("Player is scaffolding downwards (impossible)");
                }
            }
        }
    }
}
