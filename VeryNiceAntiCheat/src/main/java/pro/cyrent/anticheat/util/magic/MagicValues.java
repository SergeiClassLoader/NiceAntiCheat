package pro.cyrent.anticheat.util.magic;

import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.processor.prediction.horizontal.MoveFlyingResult;
import pro.cyrent.anticheat.util.math.MathHelper_1_20;
import pro.cyrent.anticheat.util.math.Point;
import pro.cyrent.anticheat.util.minecraftmaths.MinecraftMath;

public class MagicValues {
    public static final boolean[] TRUE_FALSE = new boolean[]{true, false};
    public static final boolean[] TRUE = new boolean[]{true};
    public static final boolean[] FALSE = new boolean[]{false};

    public static final double ZERO_THREE = 0.03D;
    public static final double VERTICAL_MULTIPLIER = 0.9800000190734863D;
    public static final double VERTICAL_SUBTRACTED = 0.08D;

    public static final double MIN_VERTICAL_1_8 = 0.005D;
    public static final double MIN_VERTICAL_1_9 = 0.003D;


    // This uses both moving flying's for other prediction engines.
    public static MoveFlyingResult moveFlyingResult(PlayerData playerData, float moveForward, float moveStrafe, float rotationYaw,
                                                    float friction, int fast, int version) {

        //1.13+
        if (version > 404) {
            moveFlying(rotationYaw, moveForward, moveStrafe, friction, fast);
        }

        //1.7 - 1.12.2
        return moveFlying(playerData,
                moveForward, moveStrafe, friction,
                fast,
                rotationYaw
        );
    }


    // 1.17+ move flying method directly from source (taken from 1.20, but still works for 1.17)
    public static MoveFlyingResult moveFlying(float rotationYaw, float moveForward,
                                              float moveStrafe, float friction, int fast) {
        Point point = new Point(moveStrafe, 0.D, moveForward);

        final double d0 = point.lengthSquared();
        if (d0 < 1.0E-7D) {
            return new MoveFlyingResult(0, 0);
        } else {
            Point vector3d = (d0 > 1.0D ? point.normalize() : point).scale(friction);

            float f = MathHelper_1_20.sin(rotationYaw * 0.017453292F);
            float f1 = MathHelper_1_20.cos(rotationYaw * 0.017453292F);

            return new MoveFlyingResult(vector3d.getX() * (double) f1 - vector3d.getZ() * (double) f,
                    vector3d.getZ() * (double) f1 + vector3d.getX() * (double) f);
        }
    }


    // 1.7 - 1.16.5 move flying method.
    public static MoveFlyingResult moveFlying(PlayerData playerData, final float moveForward, final float moveStrafe,
                                              final float friction,
                                              final int fast, float yaw) {
        float diagonal = moveStrafe * moveStrafe + moveForward * moveForward;

        float moveFlyingFactorX = 0.0F;
        float moveFlyingFactorZ = 0.0F;

        if (diagonal >= 1.0E-4F) {
            diagonal = MinecraftMath.toFloat(diagonal);

            if (diagonal < 1.0F) {
                diagonal = 1.0F;
            }

            diagonal = friction / diagonal;

            final float strafe = moveStrafe * diagonal;
            final float forward = moveForward * diagonal;

            final float rotationYaw = yaw;

            // 1.9 - 1.16.5
            if (playerData.getProtocolVersion() > 47
                    && playerData.getProtocolVersion() < 755) {

                float f1 = MinecraftMath.sin(fast,rotationYaw * 0.017453292F);
                float f2 = MinecraftMath.cos(fast,rotationYaw * 0.017453292F);

                final float factorX = strafe * f2 - forward * f1;
                final float factorZ = forward * f2 + strafe * f1;

                moveFlyingFactorX = factorX;
                moveFlyingFactorZ = factorZ;
            } else {

                // 1.7 - 1.8.9
                final float f1 = MinecraftMath.sin(fast, rotationYaw * (float) Math.PI / 180.0F);
                final float f2 = MinecraftMath.cos(fast, rotationYaw * (float) Math.PI / 180.0F);

                final float factorX = strafe * f2 - forward * f1;
                final float factorZ = forward * f2 + strafe * f1;

                moveFlyingFactorX = factorX;
                moveFlyingFactorZ = factorZ;
            }
        }

        return new MoveFlyingResult(moveFlyingFactorX, moveFlyingFactorZ);
    }
}
