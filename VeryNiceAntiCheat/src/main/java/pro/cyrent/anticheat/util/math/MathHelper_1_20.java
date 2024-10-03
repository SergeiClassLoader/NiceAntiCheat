package pro.cyrent.anticheat.util.math;

import pro.cyrent.anticheat.util.vec.Vec3;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MathHelper_1_20 {
    public static <T> T make(Supplier<T> p_137538_) {
        return p_137538_.get();
    }

    public static <T> T make(T p_137470_, Consumer<T> p_137471_) {
        p_137471_.accept(p_137470_);
        return p_137470_;
    }

    private static final long UUID_VERSION = 61440L;
    private static final long UUID_VERSION_TYPE_4 = 16384L;
    private static final long UUID_VARIANT = -4611686018427387904L;
    private static final long UUID_VARIANT_2 = Long.MIN_VALUE;
    public static final float PI = (float) Math.PI;
    public static final float HALF_PI = ((float) Math.PI / 2F);
    public static final float TWO_PI = ((float) Math.PI * 2F);
    public static final float DEG_TO_RAD = ((float) Math.PI / 180F);
    public static final float RAD_TO_DEG = (180F / (float) Math.PI);
    public static final float EPSILON = 1.0E-5F;
    public static final float SQRT_OF_TWO = sqrt(2.0F);
    private static final float SIN_SCALE = 10430.378F;

    private static final float[] SIN = make(new float[65536], (p_14077_) -> {
        for (int i = 0; i < p_14077_.length; ++i) {
            p_14077_[i] = (float) Math.sin((double) i * Math.PI * 2.0D / 65536.0D);
        }

    });
    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
    private static final double ONE_SIXTH = 0.16666666666666666D;
    private static final int FRAC_EXP = 8;
    private static final int LUT_SIZE = 257;
    private static final double FRAC_BIAS = java.lang.Double.longBitsToDouble(4805340802404319232L);
    private static final double[] ASIN_TAB = new double[257];
    private static final double[] COS_TAB = new double[257];
    private static final float radToIndex = roundToFloat(651.8986469044033D);
    private static final float[] SIN_TABLE_FAST = new float[4096];


    //1.20 get rot vector.
    public static Vec3 getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = cos(g);
        float i = sin(g);
        float j = cos(f);
        float k = sin(f);
        return new Vec3((double)(i * j), (double)(-k), (double)(h * j));
    }

    public static float sin(float p_14032_) {
        return SIN[(int) (p_14032_ * 10430.378F) & '\uffff'];
    }

    public static float cos(float p_14090_) {
        return SIN[(int) (p_14090_ * 10430.378F + 16384.0F) & '\uffff'];
    }

    public static float sqrt(float p_14117_) {
        return (float) Math.sqrt((double) p_14117_);
    }


    public static float roundToFloat(double d) {
        return (float) ((double) Math.round(d * 1.0E8D) / 1.0E8D);
    }

    public static float sinFixed(int fast, float pValue) {

        switch (fast) {
            case 0: {
                return SIN[(int) (pValue * 10430.378F) & 65535];
            }

            case 1: {
                return SIN_TABLE_FAST[(int) (pValue * radToIndex) & 4095];
            }

            case 2: {
                return SIN_TABLE_FAST[(int) (pValue * radToIndex) & 4095];
            }
        }


        return SIN[(int) (pValue * 10430.378F) & 65535];
    }

    public static float cosFixed(int fast, float pValue) {

        switch (fast) {
            case 0: {
                return SIN[(int) (pValue * 10430.378F + 16384.0F) & 65535];
            }

            case 1: {
                return SIN_TABLE_FAST[(int) (pValue * radToIndex + 1024.0F) & 4095];
            }

            case 2: {
                return SIN_TABLE_FAST[(int) (pValue * radToIndex + 1024.0F) & 4095];
            }
        }

        return SIN[(int) (pValue * 10430.378F + 16384.0F) & 65535];
    }
}
