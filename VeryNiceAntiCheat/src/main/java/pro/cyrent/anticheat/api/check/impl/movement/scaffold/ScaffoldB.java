package pro.cyrent.anticheat.api.check.impl.movement.scaffold;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

@CheckInformation(
        name = "Scaffold",
        subName = "B",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.SCAFFOLD,
        description = "Detects if the player never sneaks while scaffolding",
        punishmentVL = 30,
        state = CheckState.PRE_RELEASE)
public class ScaffoldB extends Check {

    private double threshold;
    private int places;

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

                if (getData().getMovementProcessor().getDeltaXZ() < .1) {
                    this.places -= Math.min(this.places, 2);
                    return;
                }

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
