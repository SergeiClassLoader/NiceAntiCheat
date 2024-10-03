package pro.cyrent.anticheat.api.check.impl.movement.scaffold;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

@CheckInformation(
        name = "Scaffold",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.SCAFFOLD,
        description = "Detects if the player isn't looking at the block while placing it",
        punishmentVL = 10,
        state = CheckState.PRE_BETA)
public class ScaffoldA extends Check {

    private double threshold;
    private boolean lineOfSight;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isPlace()) {
            if (getData().generalCancel()
                    || getData().isBedrock()) return;

            if (getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() < 3
                    || getData().getScaffoldProcessor().getScaffoldTimerJump().getDelta() < 3) {

                Block block = getTargetBlock(getData().getPlayer(), 5);

                if (block != null && block.getType() != Material.AIR) {
                    this.lineOfSight = true;
                } else {
                    this.lineOfSight = false;
                }

                if (!this.lineOfSight) {

                    if (++this.threshold > 16) {
                        this.fail("No line of sight while placing blocks");
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .7);
                }
            } else {
                this.threshold -= Math.min(this.threshold, .1);
            }
        }
    }

    public final Block getTargetBlock(Player player, int range) {
        BlockIterator iter = new BlockIterator(player, range);
        Block lastBlock = iter.next();

        while (iter.hasNext()) {
            lastBlock = iter.next();

            if (lastBlock.getType() == Material.AIR) {
                continue;
            }
            break;
        }
        return lastBlock;
    }
}
