package pro.cyrent.anticheat.util.mcp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Vector {

    private double x, y, z;

    public double distance(final Vector other) {
        final double deltaX = other.getX() - x;
        final double deltaY = other.getY() - y;
        final double deltaZ = other.getZ() - z;

        return Math.sqrt((deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ));
    }

    public double distanceSqrt(final Vector other) {
        final double deltaX = other.getX() - x;
        final double deltaY = other.getY() - y;
        final double deltaZ = other.getZ() - z;

        return (deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ);
    }

    public BlockPos toBlockPos() {
        return new BlockPos(
                floor_double(x),

                floor_double(y),

                floor_double(z));
    }

    public static int floor_double(double value) {
        int i = (int) value;
        return value < (double) i ? i - 1 : i;
    }
}