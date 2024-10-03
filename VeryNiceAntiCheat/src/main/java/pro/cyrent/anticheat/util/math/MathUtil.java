package pro.cyrent.anticheat.util.math;

import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.client.ClientMath;
import pro.cyrent.anticheat.util.evicting.EvictingQueue;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.minecraftmaths.MinecraftMath;
import pro.cyrent.anticheat.util.vec.Vec3;
import jafama.StrictFastMath;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class MathUtil {

    public static final Map<Long, Float> sensitityValues = new HashMap<>();
    public static final Random random = new Random();
    private static final Enchantment depthStrider = Enchantment.getByName("DEPTH_STRIDER");
    public static SecureRandom SECURE_RANDOM = new SecureRandom();

    public static final double EXPANDER = Math.pow(2, 24);

    public static long roundFast(final double x) {
        final long bits = Double.doubleToRawLongBits(x);
        final int biasedExp = ((int) (bits >> 52)) & 0x7ff;
        // Shift to get rid of bits past comma except first one: will need to
        // 1-shift to the right to end up with correct magnitude.
        final int shift = (52 - 1 + Double.MAX_EXPONENT) - biasedExp;
        if ((shift & -64) == 0) {
            // shift in [0,63], so unbiased exp in [-12,51].
            long extendedMantissa = 0x0010000000000000L | (bits & 0x000fffffffffffffL);
            if (bits < 0) {
                extendedMantissa = -extendedMantissa;
            }
            // If value is positive and first bit past comma is 0, rounding
            // to lower integer, else to upper one, which is what "+1" and
            // then ">>1" do.
            return ((extendedMantissa >> shift) + 1L) >> 1;
        } else {
            // +-Infinity, NaN, or a mathematical integer.
            return (long) x;
        }
    }

    public static float sensGcd(EvictingQueue<Float> numbers) {
        float result = (float) numbers.toArray()[0];
        for (int i = 1; i < numbers.size(); i++) {
            result = sensGcd((float) numbers.toArray()[i], result);
        }

        return result;
    }

    public static float sensGcd(float a, float b) {
        if (a <= .0001) return b;

        int quotient = getIntQuotient(b, a);
        float remainder = ((b / a) - quotient) * a;
        if (Math.abs(remainder) < Math.max(a, b) * 1E-3F) remainder = 0;
        return sensGcd(remainder, a);
    }

    public static int getIntQuotient(float dividend, float divisor) {
        float ans = dividend / divisor;
        float error = Math.max(dividend, divisor) * 1E-3F;
        return (int) (ans + error);
    }


    static {
        MathUtil.sensitityValues.put(0L, 0.0F);
        MathUtil.sensitityValues.put(1L, 0.0070422534F);
        MathUtil.sensitityValues.put(2L, 0.014084507F);
        MathUtil.sensitityValues.put(3L, 0.018779343F);
        MathUtil.sensitityValues.put(4L, 0.02112676F);
        MathUtil.sensitityValues.put(5L, 0.028169014F);
        MathUtil.sensitityValues.put(6L, 0.0281690166F);
        MathUtil.sensitityValues.put(7L, 0.03521127F);
        MathUtil.sensitityValues.put(8L, 0.04225352F);
        MathUtil.sensitityValues.put(9L, 0.049295776F);
        MathUtil.sensitityValues.put(10L, 0.0492957736F);
        MathUtil.sensitityValues.put(11L, 0.056338027F);
        MathUtil.sensitityValues.put(12L, 0.06338028F);
        MathUtil.sensitityValues.put(13L, 0.065727696F);
        MathUtil.sensitityValues.put(14L, 0.07042254F);
        MathUtil.sensitityValues.put(15L, 0.07746479F);
        MathUtil.sensitityValues.put(16L, 0.08450704F);
        MathUtil.sensitityValues.put(17L, 0.089201875F);
        MathUtil.sensitityValues.put(18L, 0.09154929F);
        MathUtil.sensitityValues.put(19L, 0.09859155F);
        MathUtil.sensitityValues.put(20L, 0.10093897F);
        MathUtil.sensitityValues.put(21L, 0.1056338F);
        MathUtil.sensitityValues.put(22L, 0.112676054F);
        MathUtil.sensitityValues.put(23L, 0.11971831F);
        MathUtil.sensitityValues.put(24L, 0.12441315F);
        MathUtil.sensitityValues.put(25L, 0.12676056F);
        MathUtil.sensitityValues.put(26L, 0.13380282F);
        MathUtil.sensitityValues.put(27L, 0.13849765F);
        MathUtil.sensitityValues.put(28L, 0.14084508F);
        MathUtil.sensitityValues.put(29L, 0.14788732F);
        MathUtil.sensitityValues.put(30L, 0.15492958F);
        MathUtil.sensitityValues.put(31L, 0.15962441F);
        MathUtil.sensitityValues.put(32L, 0.16197184F);
        MathUtil.sensitityValues.put(33L, 0.16901408F);
        MathUtil.sensitityValues.put(34L, 0.1713615F);
        MathUtil.sensitityValues.put(35L, 0.17605634F);
        MathUtil.sensitityValues.put(36L, 0.18309858F);
        MathUtil.sensitityValues.put(37L, 0.18779343F);
        MathUtil.sensitityValues.put(38L, 0.19014084F);
        MathUtil.sensitityValues.put(39L, 0.1971831F);
        MathUtil.sensitityValues.put(40L, 0.20422535F);
        MathUtil.sensitityValues.put(41L, 0.20657277F);
        MathUtil.sensitityValues.put(42L, 0.2112676F);
        MathUtil.sensitityValues.put(43L, 0.21830986F);
        MathUtil.sensitityValues.put(44L, 0.22065727F);
        MathUtil.sensitityValues.put(45L, 0.22535211F);
        MathUtil.sensitityValues.put(46L, 0.23239437F);
        MathUtil.sensitityValues.put(47L, 0.23943663F);
        MathUtil.sensitityValues.put(48L, 0.24413146F);
        MathUtil.sensitityValues.put(49L, 0.24647887F);
        MathUtil.sensitityValues.put(50L, 0.2535211F);
        MathUtil.sensitityValues.put(51L, 0.25821596F);
        MathUtil.sensitityValues.put(52L, 0.26056337F);
        MathUtil.sensitityValues.put(53L, 0.26760563F);
        MathUtil.sensitityValues.put(54L, 0.2746479F);
        MathUtil.sensitityValues.put(55L, 0.2769953F);
        MathUtil.sensitityValues.put(56L, 0.28169015F);
        MathUtil.sensitityValues.put(57L, 0.28873238F);
        MathUtil.sensitityValues.put(58L, 0.29107982F);
        MathUtil.sensitityValues.put(59L, 0.29577464F);
        MathUtil.sensitityValues.put(60L, 0.3028169F);
        MathUtil.sensitityValues.put(61L, 0.30985916F);
        MathUtil.sensitityValues.put(62L, 0.31220657F);
        MathUtil.sensitityValues.put(63L, 0.31690142F);
        MathUtil.sensitityValues.put(64L, 0.32394367F);
        MathUtil.sensitityValues.put(65L, 0.32629108F);
        MathUtil.sensitityValues.put(66L, 0.3309859F);
        MathUtil.sensitityValues.put(67L, 0.33802816F);
        MathUtil.sensitityValues.put(68L, 0.3415493F);
        MathUtil.sensitityValues.put(69L, 0.34507042F);
        MathUtil.sensitityValues.put(70L, 0.35211268F);
        MathUtil.sensitityValues.put(71L, 0.35915494F);
        MathUtil.sensitityValues.put(72L, 0.36150235F);
        MathUtil.sensitityValues.put(73L, 0.36619717F);
        MathUtil.sensitityValues.put(74L, 0.37323943F);
        MathUtil.sensitityValues.put(75L, 0.37558687F);
        MathUtil.sensitityValues.put(76L, 0.3802817F);
        MathUtil.sensitityValues.put(77L, 0.38732395F);
        MathUtil.sensitityValues.put(78L, 0.3943662F);
        MathUtil.sensitityValues.put(79L, 0.3967136F);
        MathUtil.sensitityValues.put(80L, 0.40140846F);
        MathUtil.sensitityValues.put(81L, 0.4084507F);
        MathUtil.sensitityValues.put(82L, 0.41314554F);
        MathUtil.sensitityValues.put(83L, 0.41549295F);
        MathUtil.sensitityValues.put(84L, 0.4225352F);
        MathUtil.sensitityValues.put(85L, 0.42957747F);
        MathUtil.sensitityValues.put(86L, 0.43192488F);
        MathUtil.sensitityValues.put(87L, 0.43661973F);
        MathUtil.sensitityValues.put(88L, 0.44366196F);
        MathUtil.sensitityValues.put(89L, 0.4483568F);
        MathUtil.sensitityValues.put(90L, 0.45070422F);
        MathUtil.sensitityValues.put(91L, 0.45774648F);
        MathUtil.sensitityValues.put(92L, 0.46478873F);
        MathUtil.sensitityValues.put(93L, 0.46948355F);
        MathUtil.sensitityValues.put(94L, 0.471831F);
        MathUtil.sensitityValues.put(95L, 0.47887325F);
        MathUtil.sensitityValues.put(96L, 0.48122066F);
        MathUtil.sensitityValues.put(97L, 0.48591548F);
        MathUtil.sensitityValues.put(98L, 0.49295774F);
        MathUtil.sensitityValues.put(99L, 0.49530515F);
        MathUtil.sensitityValues.put(100L, 0.5F);
        MathUtil.sensitityValues.put(101L, 0.5070422F);
        MathUtil.sensitityValues.put(102L, 0.5140845F);
        MathUtil.sensitityValues.put(103L, 0.5164319F);
        MathUtil.sensitityValues.put(104L, 0.52112675F);
        MathUtil.sensitityValues.put(105L, 0.52816904F);
        MathUtil.sensitityValues.put(106L, 0.53051645F);
        MathUtil.sensitityValues.put(107L, 0.53521127F);
        MathUtil.sensitityValues.put(108L, 0.542253F);
        MathUtil.sensitityValues.put(109L, 0.5492958F);
        MathUtil.sensitityValues.put(110L, 0.5539906F);
        MathUtil.sensitityValues.put(111L, 0.5586854F);
        MathUtil.sensitityValues.put(112L, 0.5633803F);
        MathUtil.sensitityValues.put(113L, 0.5680751F);
        MathUtil.sensitityValues.put(114L, 0.57042253F);
        MathUtil.sensitityValues.put(115L, 0.57746476F);
        MathUtil.sensitityValues.put(116L, 0.58450705F);
        MathUtil.sensitityValues.put(117L, 0.58920187F);
        MathUtil.sensitityValues.put(118L, 0.5915493F);
        MathUtil.sensitityValues.put(119L, 0.59859157F);
        MathUtil.sensitityValues.put(120L, 0.600939F);
        MathUtil.sensitityValues.put(121L, 0.6056338F);
        MathUtil.sensitityValues.put(122L, 0.6126761F);
        MathUtil.sensitityValues.put(123L, 0.6197183F);
        MathUtil.sensitityValues.put(124L, 0.62441313F);
        MathUtil.sensitityValues.put(125L, 0.62676054F);
        MathUtil.sensitityValues.put(126L, 0.63380283F);
        MathUtil.sensitityValues.put(127L, 0.63615024F);
        MathUtil.sensitityValues.put(128L, 0.64084506F);
        MathUtil.sensitityValues.put(129L, 0.647887350F);
        MathUtil.sensitityValues.put(130L, 0.6549296F);
        MathUtil.sensitityValues.put(131L, 0.6596244F);
        MathUtil.sensitityValues.put(132L, 0.6619718F);
        MathUtil.sensitityValues.put(133L, 0.6690141F);
        MathUtil.sensitityValues.put(134L, 0.6737089F);
        MathUtil.sensitityValues.put(135L, 0.6760563F);
        MathUtil.sensitityValues.put(136L, 0.6830986F);
        MathUtil.sensitityValues.put(137L, 0.685446F);
        MathUtil.sensitityValues.put(138L, 0.69014084F);
        MathUtil.sensitityValues.put(139L, 0.6971831F);
        MathUtil.sensitityValues.put(140L, 0.70422536F);
        MathUtil.sensitityValues.put(141L, 0.7065728F);
        MathUtil.sensitityValues.put(142L, 0.7112676F);
        MathUtil.sensitityValues.put(143L, 0.7183099F);
        MathUtil.sensitityValues.put(144L, 0.7253521F);
        MathUtil.sensitityValues.put(145L, 0.7253521F);
        MathUtil.sensitityValues.put(146L, 0.73239434F);
        MathUtil.sensitityValues.put(147L, 0.7394366F);
        MathUtil.sensitityValues.put(148L, 0.74413145F);
        MathUtil.sensitityValues.put(149L, 0.74647886F);
        MathUtil.sensitityValues.put(150L, 0.75352114F);
        MathUtil.sensitityValues.put(151L, 0.75821596F);
        MathUtil.sensitityValues.put(152L, 0.7605634F);
        MathUtil.sensitityValues.put(153L, 0.76760566F);
        MathUtil.sensitityValues.put(154L, 0.7746479F);
        MathUtil.sensitityValues.put(155L, 0.7769953F);
        MathUtil.sensitityValues.put(156L, 0.7816901F);
        MathUtil.sensitityValues.put(157L, 0.7887324F);
        MathUtil.sensitityValues.put(158L, 0.7934272F);
        MathUtil.sensitityValues.put(159L, 0.79577464F);
        MathUtil.sensitityValues.put(160L, 0.8028169F);
        MathUtil.sensitityValues.put(161L, 0.80985916F);
        MathUtil.sensitityValues.put(162L, 0.81220657F);
        MathUtil.sensitityValues.put(163L, 0.8169014F);
        MathUtil.sensitityValues.put(164L, 0.8239437F);
        MathUtil.sensitityValues.put(165L, 0.8286385F);
        MathUtil.sensitityValues.put(166L, 0.8309859F);
        MathUtil.sensitityValues.put(167L, 0.8380282F);
        MathUtil.sensitityValues.put(168L, 0.842723F);
        MathUtil.sensitityValues.put(169L, 0.8450704F);
        MathUtil.sensitityValues.put(170L, 0.85211265F);
        MathUtil.sensitityValues.put(171L, 0.85915494F);
        MathUtil.sensitityValues.put(172L, 0.86150235F);
        MathUtil.sensitityValues.put(173L, 0.86619717F);
        MathUtil.sensitityValues.put(174L, 0.87323946F);
        MathUtil.sensitityValues.put(175L, 0.8779343F);
        MathUtil.sensitityValues.put(176L, 0.8802817F);
        MathUtil.sensitityValues.put(177L, 0.8873239F);
        MathUtil.sensitityValues.put(178L, 0.8943662F);
        MathUtil.sensitityValues.put(179L, 0.899061F);
        MathUtil.sensitityValues.put(180L, 0.90140843F);
        MathUtil.sensitityValues.put(181L, 0.9084507F);
        MathUtil.sensitityValues.put(182L, 0.91079813F);
        MathUtil.sensitityValues.put(183L, 0.91549295F);
        MathUtil.sensitityValues.put(184L, 0.92253524F);
        MathUtil.sensitityValues.put(185L, 0.92957747F);
        MathUtil.sensitityValues.put(186L, 0.9319249F);
        MathUtil.sensitityValues.put(187L, 0.9366197F);
        MathUtil.sensitityValues.put(188L, 0.943662F);
        MathUtil.sensitityValues.put(189L, 0.9460094F);
        MathUtil.sensitityValues.put(190L, 0.9507042F);
        MathUtil.sensitityValues.put(191L, 0.9577465F);
        MathUtil.sensitityValues.put(192L, 0.96478873F);
        MathUtil.sensitityValues.put(193L, 0.96948355F);
        MathUtil.sensitityValues.put(194L, 0.97183096F);
        MathUtil.sensitityValues.put(195L, 0.97887325F);
        MathUtil.sensitityValues.put(196L, 0.98122066F);
        MathUtil.sensitityValues.put(197L, 0.9859155F);
        MathUtil.sensitityValues.put(198L, 0.9929578F);
        MathUtil.sensitityValues.put(199L, 0.9953052F);
        MathUtil.sensitityValues.put(200L, 1.0F);
    }

    public static int getFirstDecimalDigit(float num) {
        // Get the absolute value of the number
        num = Math.abs(num);

        // Extract the integer part of the number
        int integerPart = (int) num;

        // Get the first decimal digit
        return (int) ((num - integerPart) * 10);
    }

    public static double movingFlyingV3(PlayerData user) {
        FlyingLocation to = user.getMovementProcessor().getTo(),
                from = user.getMovementProcessor().getFrom();


        float strafe = 1F, forward = 1F;
        float f = strafe * strafe + forward * forward;

        float friction;

        float var3 = (0.6F * 0.91F);

        // walkSpeed / 2 is the same as the nms move speed
        double attributeSpeed =
                user.getPlayer().getWalkSpeed() / 2;

        // Always add sprinting
        attributeSpeed += attributeSpeed * 0.30000001192092896;

        // apply the current speed potion multiplier to the move speed
        if (user.getPotionProcessor().isSpeedPotion()) {
            attributeSpeed += user.
                    getPotionProcessor().getSpeedPotionAmplifier()
                    * 0.20000000298023224D * attributeSpeed;
        }

        // apply the current slowness potion multiplier to the move speed
        if (user.getPotionProcessor().isSlownessPotion()) {
            attributeSpeed += user.getPotionProcessor()
                    .getSlownessAmplifier() *
                    -.15000000596046448D * attributeSpeed;
        }


        float var4 = 0.16277136F / (var3 * var3 * var3);

        float moveSpeed = (float) attributeSpeed;

        if (from.isOnGround()) {
            friction = moveSpeed * var4;
        } else {
            friction = 0.026F;
        }

        if (f >= 1.0E-4F) {
            f = (float) Math.sqrt(f);
            if (f < 1.0F) {
                f = 1.0F;
            }
            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;

            float f1;
            float f2;

            if (user.getProtocolVersion() > 47 && user.getProtocolVersion() <= 340) {
                f1 = MinecraftMath.sin(0, to.getYaw() * 0.017453292F);
                f2 = MinecraftMath.cos(0, to.getYaw() * 0.017453292F);
            } else {
                f1 = MinecraftMath.sin(0,to.getYaw() * (float) Math.PI / 180.0F);
                f2 = MinecraftMath.cos(0,to.getYaw() * (float) Math.PI / 180.0F);
            }

            float motionXAdd = (strafe * f2 - forward * f1);
            float motionZAdd = (forward * f2 + strafe * f1);
            return Math.hypot(motionXAdd, motionZAdd);
        }

        return 0;
    }


    public static long getDelta(long one, long two) {
        return Math.abs(one - two);
    }

    public static float rotation(float s, float a) {
        float f = (s * 0.6f + .2f);
        float f2 = f * f * f * 1.2f;
        return a - (a % f2);
    }

    public static float angleDistanceNonAbsolute(float alpha, float beta) {
        float distance = beta - alpha;

        distance = (distance + 180) % 360 - 180;

        return distance;
    }



    public static double clamp180(double theta) {
        theta %= 360.0;
        if (theta >= 180.0) {
            theta -= 360.0;
        }
        if (theta < -180.0) {
            theta += 360.0;
        }
        return theta;
    }

    public static Material match(String material) {
        return Material.getMaterial(material.replace("LEGACY_", ""));
    }

    public static double hypotSquared(final double... array) {
        double n = 0.0;
        while (0 < array.length) {
            n += Math.pow(array[0], 2.0);
            int n2 = 0;
            ++n2;
        }
        return n;
    }

    public static double hypot(final double... array) {
        return Math.sqrt(hypotSquared(array));
    }

    public static int lcd(final int n, final int n2) {
        return BigInteger.valueOf(n).gcd(BigInteger.valueOf(n2)).intValueExact();
    }

    public static int lcd(final long n, final int n2, final int n3) {
        return (n3 <= n) ? n2 : gcd(n, n3, n2 % n3);
    }

    public static int gcd(final long n, final int n2, final int n3) {
        return (n3 <= n) ? n2 : gcd(n, n3, n2 % n3);
    }

    public static int gcd(final int n, final int n2) {
        return BigInteger.valueOf(n).gcd(BigInteger.valueOf(n2)).intValueExact();
    }

    public static long gcd(long current, long previous) {
        return (previous <= 16384L) ? current : gcd(previous, current % previous);
    }



    public static int getDepthStriderLevel(PlayerData user) {

        if (depthStrider != null) {
            ItemStack boots = user.getPlayer().getInventory().getBoots();

            if (boots != null) {
                return boots.getEnchantmentLevel(depthStrider);
            }
        }

        return 0;
    }

    public static double getAverage(Collection<? extends Number> values) {
        return values.stream()
                .mapToDouble(Number::doubleValue)
                .average()
                .orElse(0D);
    }

    public static double sqrt(double number) {
        if (number == 0) {
            return 0;
        }

        double t;
        double squareRoot = number / 2;

        // exploit fix
        if (Double.isInfinite(squareRoot)) {
            return 0;
        }

        do {
            t = squareRoot;
            squareRoot = (t + (number / t)) / 2;
        } while ((t - squareRoot) != 0);

        return squareRoot;
    }


    public static double getStandardDeviation(Collection<? extends Number> values) {
        double average = getAverage(values);

        double variance = 0;

        for (Number number : values) {
            variance += FastMath.pow(number.doubleValue() - average, 2D);
        }

        return sqrt(variance / values.size());
    }

    public static double getAbsoluteDelta(double one, double two) {
        return Math.abs(Math.abs(one) - Math.abs(two));
    }
    public static double round(double value, int places) {

        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd2 = new BigDecimal(value);
        bd2 = bd2.setScale(places, RoundingMode.HALF_UP);
        return bd2.doubleValue();
    }

    public static double selectRandomNumber(List<Double> numbers) {
        Random random = new Random();
        int randomIndex = random.nextInt(numbers.size());
        return numbers.get(randomIndex);
    }

    public static boolean containsEValue(double number) {
        return String.valueOf(number).toLowerCase().contains("e");
    }

    public static int getRandomInteger(int maximum, int minimum) {
        return ((int) (Math.random() * (maximum - minimum))) + minimum;
    }

    public static double getRandomDouble(double maximum, double minimum) {
        return minimum + (maximum - minimum) * random.nextDouble();
    }

    public static float getRandomFloat(float maximum, float minimum) {
        return minimum + (maximum - minimum) * random.nextFloat();
    }
    public static Vec3 getVectorForRotation(float yaw, float pitch, ClientMath clientMath) {
        float f = clientMath.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = clientMath.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -clientMath.cos(-pitch * 0.017453292F);
        float f3 = clientMath.sin(-pitch * 0.017453292F);

        return new Vec3(f1 * f2, f3, f * f2);
    }

    public static double preciseRound(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static double preciseRound(double value, double precision) {
        double scale = Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static boolean isScientificNotation(final Float f) {
        return f.toString().contains("E");
    }

    public static boolean isScientificNotation(final Double f) {
        return f.toString().contains("E");
    }

    public static boolean isScientificNotation(Number num) {
        return num.doubleValue() < .001D;
    }

    public static boolean hasNotation(double value) {
        return !(value > 0.0001);
    }

    public static double getCollisionModulo(double value) {
        return Math.abs(value % 0.015625D);
    }

    public static boolean isValidCollision(double value) {
        return Math.abs(value % 0.015625D) < 1E-10;
    }

    public static float wrapAngleTo180_float(float value) {
        value %= 360F;

        if (value >= 180.0F)
            value -= 360.0F;

        if (value < -180.0F)
            value += 360.0F;

        return value;
    }

    public static double hypot(final double x, final double z) {
        return Math.sqrt(x * x + z * z);
    }

    public static double fixValue(double value) {
        return value < 0 ? 0 : value;
    }

    public static float[] getRotations(FlyingLocation one, FlyingLocation two) {
        double diffX = two.getPosX() - one.getPosX();
        double diffZ = two.getPosZ() - one.getPosZ();
        double diffY = two.getPosY() + 2.0 - 0.4 - (one.getPosY() + 2.0);
        double dist = StrictFastMath.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (StrictFastMath.atan2(diffZ, diffX) * 180.0 / 3.141592653589793) - 90.0f;
        float pitch = (float) (-StrictFastMath.atan2(diffY, dist) * 180.0 / 3.141592653589793);
        return new float[]{yaw, pitch};
    }

    public static double getAngleRotation(FlyingLocation loc1, FlyingLocation loc2) {
        if (loc1 == null || loc2 == null) return -1;

        Vector playerRotation = new Vector(loc1.getYaw(), loc1.getPitch(), 0.0f);
        loc1.setPosY(0);
        loc2.setPosY(0);

        float[] rot = getRotations(loc1, loc2);
        Vector expectedRotation = new Vector(rot[0], rot[1], 0);
        return yawTo180D(playerRotation.getX() - expectedRotation.getX());
    }

    public static double yawTo180D(double dub) {
        if ((dub %= 360.0) >= 180.0) {
            dub -= 360.0;
        }
        if (dub < -180.0) {
            dub += 360.0;
        }
        return dub;
    }


    public static int floor(double var0) {
        int var2 = (int) var0;
        return var0 < var2 ? var2 - 1 : var2;
    }


    public static Vector getDirection(FlyingLocation loc) {
        Vector vector = new Vector();
        double rotX = loc.getYaw();
        double rotY = loc.getPitch();
        double xz = Math.cos(Math.toRadians(rotY));

        vector.setY(-Math.sin(Math.toRadians(rotY)));
        vector.setX(-xz * Math.sin(Math.toRadians(rotX)));
        vector.setZ(xz * Math.cos(Math.toRadians(rotX)));

        return vector;
    }


    public static float getAngle(final Vector one, final Vector two) {
        final double dot = Math.min(Math.max(
                        (one.getX() * two.getX() + one.getY() * two.getY() + one.getZ() * two.getZ())
                                / (one.length() * two.length()),
                        -1.0),
                1.0);

        return (float) Math.acos(dot);
    }


    public static int getPotionEffectLevel(Player player, PotionEffectType pet) {
        for (PotionEffect pe : player.getActivePotionEffects()) {
            if (pe.getType().getName().equalsIgnoreCase(pet.getName())) {
                return pe.getAmplifier() + 1;
            }
        }
        return 0;
    }

    public static <E> E randomElement(final Collection<? extends E> collection) {
        if (collection.size() == 0) return null;
        int index = new Random().nextInt(collection.size());

        if (collection instanceof List) {
            return ((List<? extends E>) collection).get(index);
        } else {
            Iterator<? extends E> iter = collection.iterator();
            for (int i = 0; i < index; i++) iter.next();
            return iter.next();
        }
    }

    public double getReversedModulus(float div, float a, double remainder) {
        if (a < remainder)
            return (remainder - a);

        return (div + remainder - a);
    }

    public static double checkMax(double value, double max) {
        return Math.min(value, max);
    }

    public static double trim(int degree, double d) {

        String format = "#.";
        for (int i = 1; i <= degree; ++i) {
            format += "#";
        }

        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        symbols.setDecimalSeparator('.');
        DecimalFormat twoDForm = new DecimalFormat(format, symbols);

        String formattedValue = twoDForm.format(d);

        try {
            return Double.parseDouble(formattedValue);
        } catch (NumberFormatException e) {
            // Handle non-numeric inputs gracefully
            return 0.0; // Return a default value or handle the error case accordingly
        }
    }
}
