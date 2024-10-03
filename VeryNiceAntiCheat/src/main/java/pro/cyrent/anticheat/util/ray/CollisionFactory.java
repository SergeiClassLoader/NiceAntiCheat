package pro.cyrent.anticheat.util.ray;

import org.bukkit.block.Block;

public interface CollisionFactory {
    CollisionBox fetch(int version, Block block);
}