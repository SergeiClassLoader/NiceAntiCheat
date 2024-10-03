package pro.cyrent.anticheat.util.math;

import pro.cyrent.anticheat.util.block.box.HydroBB;
import pro.cyrent.anticheat.util.vec.Vec3;
import org.bukkit.util.Vector;

public class VectorUtils {
    public static Vector cutBoxToVector(Vector vectorToCutTo, Vector min, Vector max) {
        HydroBB box = new HydroBB(min, max).sort();
        return cutBoxToVector(vectorToCutTo, box);
    }

    public static Vector cutBoxToVector(Vector vectorCutTo, HydroBB box) {
        return new Vector(clamp(vectorCutTo.getX(), box.minX, box.maxX),
                clamp(vectorCutTo.getY(), box.minY, box.maxY),
                clamp(vectorCutTo.getZ(), box.minZ, box.maxZ));
    }

    public static Vec3 cutBoxToVector(Vec3 vectorCutTo, HydroBB box) {
        return new Vec3(clamp(vectorCutTo.getXCoord(), box.minX, box.maxX),
                clamp(vectorCutTo.getYCoord(), box.minY, box.maxY),
                clamp(vectorCutTo.getZCoord(), box.minZ, box.maxZ));
    }

    public static double clamp(double d, double d2, double d3) {
        if (d < d2) {
            return d2;
        }
        return Math.min(d, d3);
    }
}