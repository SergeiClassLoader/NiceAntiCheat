package pro.cyrent.anticheat.util.minecraftmaths;

import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.math.MathHelper_1_20;
import pro.cyrent.anticheat.util.vec.Vec3;

public class MinecraftMath {
    private static final float[] SIN_TABLE_FAST = new float[4096], SIN_TABLE_FAST_NEW = new float[4096];
    private static final float[] SIN_TABLE = new float[65536];
    private static final float radToIndex = roundToFloat(651.8986469044033D);

    public static float sin(int type, float value) {
        switch(type) {
            case 0:
            default: {
                return SIN_TABLE[(int) (value * 10430.378F) & 65535];
            }
            case 1: {
                return SIN_TABLE_FAST[(int) (value * 651.8986F) & 4095];
            }
            case 2: {
                return SIN_TABLE_FAST_NEW[(int)(value * radToIndex) & 4095];
            }
        }
    }

    public static double clamp(double num, double min, double max) {
        if (num < min) {
            return min;
        }
        return Math.min(num, max);
    }

    public static float cos(int type, float value) {
        switch (type) {
            case 0:
            default:
                return SIN_TABLE[(int) (value * 10430.378F + 16384.0F) & 65535];
            case 1:
                return SIN_TABLE_FAST[(int) ((value + ((float) Math.PI / 2F)) * 651.8986F) & 4095];
            case 2:
                return SIN_TABLE_FAST_NEW[(int)(value * radToIndex + 1024.0F) & 4095];
        }
    }

    public static float toFloat(float var0) {
        return (float) Math.sqrt(var0);
    }

    static {
        for (int i = 0; i < 65536; ++i)
        {
            SIN_TABLE[i] = (float)Math.sin((double)i * Math.PI * 2.0D / 65536.0D);
        }

        for (int j = 0; j < 4096; ++j)
        {
            SIN_TABLE_FAST[j] = (float)Math.sin(((float)j + 0.5F) / 4096.0F * ((float)Math.PI * 2F));
        }

        for (int l = 0; l < 360; l += 90)
        {
            SIN_TABLE_FAST[(int)((float)l * 11.377778F) & 4095] = (float)Math.sin((float)l * 0.017453292F);
        }

        for (int j = 0; j < SIN_TABLE_FAST_NEW.length; ++j)
        {
            SIN_TABLE_FAST_NEW[j] = roundToFloat(Math.sin((double)j * Math.PI * 2.0D / 4096.0D));
        }
    }

    private static float roundToFloat(double d)
    {
        return (float)((double)Math.round(d * 1.0E8D) / 1.0E8D);
    }


    public static Vec3 getVectorForRotation(PlayerData playerData, float yaw, float pitch, int fastMath) {

        float f = MinecraftMath.cos(fastMath, -yaw * 0.017453292F - (float) Math.PI);
        float f1 = MinecraftMath.sin(fastMath, -yaw * 0.017453292F - (float) Math.PI);

        float f2 = -MinecraftMath.cos(fastMath, -pitch * 0.017453292F);
        float f3 = MinecraftMath.sin(fastMath, -pitch * 0.017453292F);

        // 1.20 math!!!!
        if (playerData.getProtocolVersion() >= 393) {
            return MathHelper_1_20.getRotationVector(pitch, yaw);
        }

        return new Vec3(f1 * f2, f3, f * f2);
    }
}