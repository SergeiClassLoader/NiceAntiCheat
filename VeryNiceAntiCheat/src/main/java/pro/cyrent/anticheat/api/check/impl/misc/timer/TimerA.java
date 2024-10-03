package pro.cyrent.anticheat.api.check.impl.misc.timer;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.impl.processor.connection.TransactionProcessor;

import java.util.concurrent.TimeUnit;

@CheckInformation(
        name = "Timer",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.TIMER,
        description = "Detects if the players game speeds up",
        state = CheckState.RELEASE)
public class TimerA extends Check {

    private final long addBalance = TimeUnit.MILLISECONDS.toNanos(50L);

    public long timerDClockMovement = (long) (System.nanoTime() - 6e10);
    public boolean movement;
    public long timerDBalance;
    public long lol;

    private long lastSex = System.currentTimeMillis();
    private long sex = -50;

    private double bufferFLying;
    private double bufferTransaction;
    private int lastServerFail;
    private boolean waitingTeleport;

    /**
     * I want extra food money for this ;)
     */

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketSendEvent() != null) {
            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION) {
                this.lastServerFail++;
            }

            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
                this.timerDBalance -= this.addBalance;
                this.sex -= 50L;
                this.waitingTeleport = true;
            }
        }

        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {
                WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

                if (flying.hasRotationChanged() && flying.hasPositionChanged()) {
                    this.waitingTeleport = false;
                }

                this.preHandleTransaction(event);
                this.preHandleFLying(event);
            }
        }
    }

    private void preHandleFLying(PacketEvent event) {
        long delta = event.getTimestamp() - this.lastSex;
        this.lastSex = event.getTimestamp();

        if (this.lastSex != -1L) {
            this.sex += 50L;
            this.sex -= delta;

            if (this.getData().getMovementProcessor().getTick() > 20) {
                this.handlePostSex();
            }
        }
    }

    private void handlePostSex() {

        if (this.sex > 50L && !this.waitingTeleport) {

            if (getData().getLastTeleport().isSet()
                    && getData().getLastTeleport().getDelta() < 10
                    || exempt()
                    || (getData().getHorizontalProcessor().getVelocitySimulator()
                    .getLastVelocitySimulatedTimer().isSet()
                    && getData().getHorizontalProcessor().getVelocitySimulator()
                    .getLastVelocitySimulatedTimer().getDelta() < 7)) {
                this.sex = 0L;
                return;
            }

            if (this.lastServerFail < 1200) {

                if (!this.exempt() && this.bufferFLying++ > 4) {

                    // this will cause a disabler but whatever
                    if (getData().getTransactionProcessor().getTransactionPing() > 800
                            || getData().getTransactionProcessor().getTransactionPing() == 0
                            || getData().getTransactionProcessor().getKeepAlivePing() == 0
                            || getData().getTransactionProcessor().getPostTransactionPing() > 800
                            || getData().getTransactionProcessor().getKeepAlivePing() > 800) {
                        getData().setBack();
                        getData().sendDevAlert("Timer A", "High ping while flagging",
                                "transaction-ping="+getData().getTransactionProcessor().getTransactionPing(),
                                "transaction-ping-post="+getData().getTransactionProcessor().getTransactionPing(),
                                "keep-alive="+getData().getTransactionProcessor().getTransactionPing());
                        this.bufferFLying = 2;

                        return;
                    }


                    this.bufferFLying = 3;

                    this.fail("real-balance=" + this.sex,
                            "flying-balance=" + this.timerDBalance,
                            "lastFail=" + this.lastServerFail,
                            "lol=" + this.lol,
                            "bufferFly=" + this.bufferFLying,
                            "bufferTrans=" + this.bufferTransaction,
                            "movement=" + this.movement);
                }
            }

            this.sex = 0L;
        } else {
            this.bufferFLying -= this.bufferFLying > 0 ? 0.001 : 0;
        }
    }

    private void postHandleTransaction(double timerSpeed) {

        if (getData().getLastTeleport().isSet()
                && getData().getLastTeleport().getDelta() < 3
                || (getData().getHorizontalProcessor().getVelocitySimulator()
                .getLastVelocitySimulatedTimer().isSet()
                && getData().getHorizontalProcessor().getVelocitySimulator()
                .getLastVelocitySimulatedTimer().getDelta() < 7)) {
            this.timerDBalance -= this.addBalance;
            return;
        }

        if (this.timerDBalance > System.nanoTime() && timerSpeed >= 1.0 && !exempt()
                && getData().getMovementProcessor().getTick() > 20) {
            this.timerDBalance -= this.addBalance;

            if (!this.waitingTeleport && this.bufferTransaction++ > 4) {
                this.bufferTransaction = 3;

                if (this.sex < -500L) {
                    this.sex = -350L;
                }

                this.lastServerFail = 0;
            }
        } else {
            this.bufferTransaction -= this.bufferTransaction > 0 ? 0.001 : 0;
        }
    }

    private boolean exempt() {
        return getData().generalCancel();
    }

    private void preHandleTransaction(PacketEvent event) {
        this.timerDBalance += this.addBalance;

        long lastPacket = event.getTimestamp() - this.lol;
        double timerSpeed = Math.min(50D / (lastPacket < 1 ? 20L : lastPacket), 20D);
        this.lol = event.getTimestamp();

        this.postHandleTransaction(timerSpeed);
        this.timerDBalance = Math.max(this.timerDBalance, this.timerDClockMovement - ((long) 120e6));
        this.movement = true;
    }

    public void handleTransaction(TransactionProcessor.TimerTransactionEntry entry) {

        if (entry == null) {
            return;
        }

        if (this.movement) {
            this.timerDClockMovement = entry.getNanotime();
            //getData().getProcessorManager().getTransactionProcessor().getLastPlayerClock();
            this.movement = false;
        }
    }
}
