package pro.cyrent.anticheat.util.ray.blocks;

import pro.cyrent.anticheat.util.ray.*;
import pro.cyrent.anticheat.util.ray.ComplexCollisionBox;
import pro.cyrent.anticheat.util.ray.SimpleCollisionBox;

public class CouldronBounding extends ComplexCollisionBox {

    public CouldronBounding() {
        this.add(new SimpleCollisionBox(0,0,0,1, 0.3125,1));
        double thickness = 0.125;
        this.add(new SimpleCollisionBox(0, 0.3125, 0, thickness, 1, 1));
        this.add(new SimpleCollisionBox(1-thickness, 0.3125, 0, 1, 1, 1));
        this.add(new SimpleCollisionBox(0, 0.3125, 0, 1, 1, thickness));
        this.add(new SimpleCollisionBox(0, 0.3125, 1-thickness, 1, 1, 1));
    }
}
