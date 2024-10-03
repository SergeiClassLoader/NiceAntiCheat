package pro.cyrent.anticheat.impl.processor.combat;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.evicting.EvictingList;
import pro.cyrent.anticheat.util.evicting.EvictingQueue;
import pro.cyrent.anticheat.util.math.MathUtil;
import pro.cyrent.anticheat.util.optifine.MCSmoothing;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.Getter;
import lombok.Setter;

import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class SensitivityProcessor extends Event {
    private final PlayerData data;

    private double mouseX, mouseY;
    private int realMouseX, realMouseY;
    private long pitchGCD, yawGCD;
    private final double gcdOffset = Math.pow(2.0, 24.0);

    public int sensitivityCycles = 0;
    private final EvictingQueue<Float> sensitivityYSamples = new EvictingQueue<>(80);
    private final EvictingQueue<Float> sensitivityXSamples = new EvictingQueue<>(80);
    public float sensitivityValue, sensitivityNoReset;
    public long sensitivity;

    //cin
    private int lastRate;
    private int lastInvalidSensitivity;
    private int lastCinematic;
    private boolean invalidRate;
    private boolean invalidSensitivity;
    private boolean cinematic;


    @Getter
    private final Deque<Float> yawGcdList = new EvictingQueue<>(45),
            pitchGcdList = new EvictingQueue<>(45);

    //new
    public static final double MINIMUM_DIVISOR = ((Math.pow(0.2f, 3) * 8) * 0.15) - 1e-3;

    public float sensitivityX, sensitivityY;
    public double sensitivityValueX, sensitivityCyclesX;

    public double divisorX;
    public double divisorY;

    private int optifineTicks;

    private boolean setSensitivity = false;

    private int averageDPI, dpi;

    private final MCSmoothing yawSmoothing = new MCSmoothing();
    private final MCSmoothing pitchSmoothing = new MCSmoothing();


    private final Set<Integer> candidates = new HashSet<>();

    private long sensitivityNew, calcSensitivity;

    private double mcpSensitivity;


    public SensitivityProcessor(PlayerData user) {
        this.data = user;
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {

                WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

                processMouse();

                if (flying.hasRotationChanged()) {

                    this.pitchGCD = MathUtil.gcd(
                            (long) (getData().getMovementProcessor().getDeltaPitchAbs()
                                    * this.gcdOffset),
                            (long) (Math.abs(getData().getMovementProcessor().getLastDeltaPitchAbs())
                                    * this.gcdOffset)
                    );

                    this.yawGCD = MathUtil.gcd(
                            (long) (getData().getMovementProcessor().getDeltaYawAbs()
                                    * this.gcdOffset),
                            (long) (getData().getMovementProcessor().getLastDeltaYawAbs()
                                    * this.gcdOffset)
                    );

                    this.yawGcdList.add((float) this.yawGCD);
                    this.pitchGcdList.add((float) this.pitchGCD);

                    //    this.processSensitivity();
                    this.processCinematic();
                    this.findSensitivity();
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.CLIENT_SETTINGS) {
                this.sensitivityCycles = 0;
            }
        }
    }

    private void processMouse() {

        double yawGCD = this.yawGCD
                / this.gcdOffset;

        double pitchGCD = this.pitchGCD
                / this.gcdOffset;

        this.divisorX = yawGCD;

        this.divisorY = pitchGCD;

        this.mouseX = Math.abs((getData().getMovementProcessor().getTo().getYaw()
                - getData().getMovementProcessor().getFrom().getYaw()) / yawGCD);

        this.mouseY = Math.abs((getData().getMovementProcessor().getTo().getPitch()
                - getData().getMovementProcessor().getFrom().getPitch()) / pitchGCD);

        float deltaPitch = getData().getMovementProcessor().getTo().getPitch()
                - getData().getMovementProcessor().getFrom().getPitch();
        float deltaYaw = getData().getMovementProcessor().getTo().getYaw()
                - getData().getMovementProcessor().getFrom().getYaw();

        this.realMouseX = (int) MathUtil.roundFast(deltaYaw / yawGCD);
        this.realMouseY = (int) MathUtil.roundFast(Math.abs(deltaPitch) / yawGCD);
    }

    private void findSensitivity() {

        float deltaPitch = this.getData().getMovementProcessor().getDeltaPitch();
        float deltaYaw = this.getData().getMovementProcessor().getDeltaYaw();

        if (deltaPitch == 0F || Math.abs(deltaYaw) == 360.0F || deltaYaw == 0.0F || Math.abs(this.getData()
                .getMovementProcessor()
                .getTo().getPitch()) == 90F) return;

        this.sensitivityXSamples.add(deltaYaw);
        this.sensitivityYSamples.add(deltaPitch);

        if (this.sensitivityYSamples.size() < 79 || this.sensitivityXSamples.size() < 79) return;

        float gcdY = MathUtil.sensGcd(this.sensitivityYSamples);
        float gcdX = MathUtil.sensGcd(this.sensitivityXSamples);

        if (MathUtil.hasNotation(gcdY) || MathUtil.hasNotation(gcdX)) return;

        //yaw
        double f2 = Math.exp(Math.log(gcdX / .15 / 8.0) / 3);

        double sensitivityTwo = ((f2 - .2) / .6) * 200;

        if (sensitivityTwo > 99.9D && sensitivityTwo < 100D) sensitivityTwo += .1D;

        long sensitivityX = (long) sensitivityTwo;

        if (sensitivityX >= 0L && sensitivityX <= 200L) {
            this.sensitivityX = MathUtil.sensitityValues.get(sensitivityX);
            this.sensitivityValueX = MathUtil.sensitityValues.get(sensitivityX);
            this.sensitivityCyclesX++;
            //     this.setSensitivity = true;
        } else {
            this.sensitivityValueX = -1F;
            //    this.setSensitivity = false;
        }

        //Pitch
        double f1 = Math.exp(Math.log(gcdY / .15 / 8) / 3);

        double sensitivityOne = ((f1 - .2) / .6) * 200;

        if (sensitivityOne > 99.9D && sensitivityOne < 100D) sensitivityOne += .1D;

        //Bukkit.broadcastMessage(""+sensitivityOne + " "+f1);

        long sensitivity = (long) sensitivityOne;

        if (sensitivity >= 0L && sensitivity <= 200L) {
            this.sensitivity = sensitivity;
            this.sensitivityY = MathUtil.sensitityValues.get(sensitivity);
            this.sensitivityValue = MathUtil.sensitityValues.get(sensitivity);
            this.sensitivityNoReset = MathUtil.sensitityValues.get(sensitivity);
            this.sensitivityCycles++;

            int dpiX = (int) Math.round(mouseX / this.sensitivity);
            int dpiY = (int) Math.round(mouseY / this.sensitivity);

            // Take an average of X and Y DPI
            this.averageDPI = (dpiX + dpiY) / 2;

            double movementX = deltaYaw / this.sensitivity;
            double movementY = deltaPitch / this.sensitivity;

            // DPI is proportional to movement
            int dpiX1 = (int) Math.round(movementX);
            int dpiY1 = (int) Math.round(movementY);

            //  this.setSensitivity = true;
            this.dpi = (dpiX1 + dpiY1) / 2;
        } else {
            this.sensitivity = -1;
            this.sensitivityValue = -1F;
            //  this.setSensitivity = false;
        }
    }

    public static float percentToSens(int percent) {
        return percent * .0070422534f;
    }

    private void processCinematic() {
        final int now = Anticheat.INSTANCE.getTaskManager().getTick();

        float deltaYaw = getData().getMovementProcessor().getDeltaYaw();
        float lastDeltaYaw = getData().getMovementProcessor().getLastDeltaYaw();

        float deltaPitch = getData().getMovementProcessor().getDeltaPitch();
        float lastDeltaPitch = getData().getMovementProcessor().getLastDeltaPitch();

        final float differenceYaw = Math.abs(deltaYaw - lastDeltaYaw);
        final float differencePitch = Math.abs(deltaPitch - lastDeltaPitch);
        final float joltYaw = Math.abs(differenceYaw - deltaYaw);
        final float joltPitch = Math.abs(differencePitch - deltaPitch);
        if (joltYaw > 1.0 && joltPitch > 1.0) {
            this.lastRate = now;
        }
        if (deltaPitch < 20.0f && this.sensitivityValue < 0) {
            this.lastInvalidSensitivity = now;
        }
        if (this.invalidRate && this.invalidSensitivity) {
            this.lastCinematic = now;
        }
        this.invalidRate = (now - this.lastRate > 3);
        this.invalidSensitivity = (now - this.lastInvalidSensitivity < 3);
        this.cinematic = (now - this.lastCinematic < 8);
    }

    public float getExpiermentalDeltaX() {
        float deltaPitch = getData().getMovementProcessor().getDeltaYaw();
        float sens = this.sensitivityX;
        float f = sens * 0.6f + .2f;
        float calc = f * f * f * 8;
        float result = deltaPitch / (calc * .15f);

        return result;
    }

    public float getExpiermentalDeltaY() {
        float deltaPitch = getData().getMovementProcessor().getDeltaPitch();
        float sens = this.sensitivityY;
        float f = sens * 0.6f + .2f;
        float calc = f * f * f * 8;
        float result = deltaPitch / (calc * .15f);

        return result;
    }
}