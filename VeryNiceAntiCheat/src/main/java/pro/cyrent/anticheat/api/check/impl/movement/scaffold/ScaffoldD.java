package pro.cyrent.anticheat.api.check.impl.movement.scaffold;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.block.box.HydroBB;
import pro.cyrent.anticheat.util.block.box.HydroMovingPosition;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.vec.Vec3;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import org.bukkit.Bukkit;
import org.bukkit.util.Vector;

import java.util.Arrays;

@CheckInformation(
        name = "Scaffold",
        subName = "D",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.SCAFFOLD,
        description = "Detects if the player places blocks too far away",
        punishable = false,
        state = CheckState.PRE_BETA)
public class ScaffoldD extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isPlace()) {
            if (getData().generalCancel()) return;

            WrapperPlayClientPlayerBlockPlacement placement = new WrapperPlayClientPlayerBlockPlacement(event.getPacketReceiveEvent());

            if (placement.getBlockPosition() == null) return;

            if (getData().getBlockAgainst() != null) {

                if (getData().getScaffoldProcessor().getScaffoldTimerJump().getDelta() < 3
                        || getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() < 3) {

                    FlyingLocation to = getData().getMovementProcessor().getTo();
                    FlyingLocation from = getData().getMovementProcessor().getFrom();

                    double min = Double.MAX_VALUE;

                    for (float d : Arrays.asList(1.62f - 0.08f, 1.62f)) {

                        for (FlyingLocation location : Arrays.asList(to, from)) {

                            if (location == null) return;

                            HydroBB hydroBB = new HydroBB(new Vector(placement.getBlockPosition().getX(),
                                    placement.getBlockPosition().getY(), placement.getBlockPosition().getZ())
                                    .toBlockVector());

                            Vec3 eyeRot = getVectorForRotation(location.getYaw(), location.getPitch(), 0);

                            Vec3 eyes = new Vec3(location.getPosX(), location.getPosY() + d, location.getPosZ());

                            Vec3 scaledEyeDir = eyes.addVector(
                                    eyeRot.xCoord * 6.0, eyeRot.yCoord * 6.0, eyeRot.zCoord * 6.0
                            );

                            if (scaledEyeDir == null) {
                                return;
                            }

                            HydroMovingPosition objectMouseOver = hydroBB.calculateIntercept(
                                    eyes, scaledEyeDir
                            );

                            // check for bb intercept
                            if (objectMouseOver != null) {

                                if (objectMouseOver.hitVec == null) return;

                                // there is interception
                                // get the distance
                                // if smaller, use this one
                                double calculated = objectMouseOver.hitVec.distanceTo(eyes);

                                if (calculated < min) {
                                    min = calculated;
                                }
                            }
                        }
                    }

                    if (min != Double.MAX_VALUE) {
                        if (min >= 4.6) {
                            if (++this.threshold > 3) {
                                this.fail("Placing blocks while not looking at the currently placed block");
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, .25);
                        }
                    }
                }
            }
        }
    }

    public static Vec3 getVectorForRotation(float yaw, float pitch, int fastMath) {
        float f = cos(fastMath,-yaw * 0.017453292F - (float) Math.PI);
        float f1 = sin(fastMath, -yaw * 0.017453292F - (float) Math.PI);
        float f2 = -cos(fastMath, -pitch * 0.017453292F);
        float f3 = sin(fastMath, -pitch * 0.017453292F);

        return new Vec3(f1 * f2, f3, f * f2);
    }

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
}
