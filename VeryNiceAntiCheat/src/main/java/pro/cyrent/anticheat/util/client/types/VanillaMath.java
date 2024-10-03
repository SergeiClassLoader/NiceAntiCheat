package pro.cyrent.anticheat.util.client.types;

import pro.cyrent.anticheat.util.client.ClientMath;
import pro.cyrent.anticheat.util.math.MathHelper;

public class VanillaMath implements ClientMath {
    @Override
    public float sin(float value) {
        return MathHelper.sin(value);
    }

    @Override
    public float cos(float value) {
        return MathHelper.cos(value);
    }

    public static float sqrt(float f) {
        return (float) Math.sqrt(f);
    }
}