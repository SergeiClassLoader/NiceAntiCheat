package pro.cyrent.anticheat.util.ray.blocks;

import pro.cyrent.anticheat.util.ray.*;
import org.bukkit.block.Block;
import pro.cyrent.anticheat.util.ray.CollisionBox;
import pro.cyrent.anticheat.util.ray.CollisionFactory;
import pro.cyrent.anticheat.util.ray.SimpleCollisionBox;

public class TrapDoorHandler implements CollisionFactory {
    @Override
    public CollisionBox fetch(int version, Block block) {
        byte data = block.getState().getData().getData();
        float var2 = 0.1875F;

        if ((data & 4) != 0) {
            if ((data & 3) == 0) {
                return new SimpleCollisionBox(0.0F, 0.0F, 1.0F - var2, 1.0F, 1.0F, 1.0F);
            }

            if ((data & 3) == 1) {
                return new SimpleCollisionBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, var2);
            }

            if ((data & 3) == 2) {
                return new SimpleCollisionBox(1.0F - var2, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
            }

            if ((data & 3) == 3) {
                return new SimpleCollisionBox(0.0F, 0.0F, 0.0F, var2, 1.0F, 1.0F);
            }
        } else {
            if ((data & 8) != 0) {
                return new SimpleCollisionBox(0.0F, 1.0F - var2, 0.0F, 1.0F, 1.0F, 1.0F);
            } else {
                return new SimpleCollisionBox(0.0F, 0.0F, 0.0F, 1.0F, var2, 1.0F);
            }
        }
        return null;
    }
}
