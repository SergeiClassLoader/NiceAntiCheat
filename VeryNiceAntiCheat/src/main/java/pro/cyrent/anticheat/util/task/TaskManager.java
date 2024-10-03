package pro.cyrent.anticheat.util.task;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.Check;
import pro.cyrent.anticheat.api.user.PlayerData;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;
import pro.cyrent.anticheat.impl.processor.connection.RangedTransactionGenerator;

import java.util.ArrayList;
import java.util.List;



@Getter
public class TaskManager extends BukkitRunnable {

    private final List<Task> tasks = new ArrayList<>();

    private boolean running;

    private final boolean lastPost = false;
    private int tick;

    private short id;

    private final RangedTransactionGenerator preGen =
            new RangedTransactionGenerator(Short.MIN_VALUE, (short) -10000);

    public void start() {
        this.running = true;
        this.runTaskTimerAsynchronously(Anticheat.INSTANCE.getPlugin(), 0L, 0L);
    }

    public Object confirmation(short id) {
        return Anticheat.INSTANCE.getInstanceManager().getInstance().createTransaction(id);
    }

    public Object getNextConfirmation() {

        if (this.tick % 20 == 0) {
            this.preGen.generateNextTransaction(false, false);
            this.id = (short) Math.abs(this.preGen.currentTransaction);
        } else {
            this.preGen.generateNextTransaction(true, false);
            this.id = this.preGen.currentTransaction;
        }

        short currentId = this.id;
        return confirmation(currentId);
    }

    public Object getNextPostConfirmation() {
        this.preGen.generateNextTransaction(false, false);
        this.id = (short) Math.abs(this.preGen.currentTransaction);

        short currentId = this.id;
        return confirmation(currentId);
    }

    public Object createKeepAlive() {
        long id = System.nanoTime() / 1000000L;
        return Anticheat.INSTANCE.getInstanceManager().getInstance().createKeepAlive(id);
    }

    @Override
    public void run() {

        if (!this.running) return;

        long now = System.currentTimeMillis();

        ++this.tick;

        Object keepAliveObj = null;

        if (this.tick % 5 == 0) {
            keepAliveObj = this.createKeepAlive();
        }

        Object confirmation = getNextConfirmation();

        short id = this.id;

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            PlayerData data = Anticheat.INSTANCE.getUserManager().getUser(player.getUniqueId());

            if (data == null) continue;

            //pre

            if (data.getReachProcessor() != null) {
                data.getReachProcessor().getPreMap().add(id);
            }

            if (data.getTransactionProcessor() != null) {

                Anticheat.INSTANCE.getInstanceManager().getInstance().sendPacket(confirmation, data);

                if (keepAliveObj != null) {
                    Anticheat.INSTANCE.getInstanceManager().getInstance().sendPacket(keepAliveObj, data);
                }
            }

            if (data.getSetBackProcessor() != null) {
                data.getSetBackProcessor().lastInvalidTick -=
                        Math.min(data.getSetBackProcessor().lastInvalidTick, 2);
            }


            data.setRegenServerTick(data.getRegenServerTick() + 1);
            data.setSatiatedServerTick(data.getSatiatedServerTick() + 1);
            data.setTicksSinceBow(data.getTicksSinceBow() + 1);
            data.setTicksSinceEat(data.getTicksSinceEat() + 1);
            data.setLastServerTick(data.getServerTick());

            data.getNoSlowDownProcessor().setLastTicksSinceSlotChange(
                    data.getNoSlowDownProcessor().getTickSinceSlotChange());

            data.getNoSlowDownProcessor().setTickSinceSlotChange(
                    data.getNoSlowDownProcessor().getTickSinceSlotChange() + 1);
            data.setServerTick(this.tick);

            for (Check info : data.getCachedChecks()) {
                info.onServerTick(data);
            }

            if (data.getVelocityProcessor().getCombatStopTicks() > 0) {
                data.getVelocityProcessor().setCombatStopTicks(data.getVelocityProcessor()
                        .getCombatStopTicks() - 1);
            }

            if (data.getTransactionProcessor() != null) {
                int delta = Math.abs(data.getServerTick() - data.getTransactionProcessor().getFlyingTick());
                int deltaFlying = Math.abs(data.getServerTick() - data.getTransactionProcessor().getRealFlyingTick());

                if (delta > 4) {
                    data.setSetBackTicks(40);
                } else {
                    if (data.getSetBackTicks() > 0) {
                        data.setSetBackTicks(data.getSetBackTicks() - 1);
                    }
                }
                data.setIgnoreSetback(data.getSetBackTicks() > 0);
            }

            if (data.getCombatProcessor() != null) {

                if (data.getCombatProcessor().isCancelTimedAttack()) {
                    data.getCombatProcessor().setCancelTime(data.getCombatProcessor().getCancelTime() + 1);
                } else {
                    data.getCombatProcessor().setCancelTime(0);
                }

                long time = now - data.getCombatProcessor().getLastFailLag();

                if (time < (2000 + data.getTransactionProcessor().getTransactionPing())) {
                    if (data.getCombatProcessor().getFixLagTicks() <= 20) {
                        data.getCombatProcessor().setFixLagTicks(data.getCombatProcessor().getFixLagTicks() + 1);
                    }
                } else {
                    if (data.getCombatProcessor().getFixLagTicks() > 0) {
                        data.getCombatProcessor().setFixLagTicks(data.getCombatProcessor().getFixLagTicks() - 1);
                    }
                }

                //     data.getCombatProcessor().setCancelMovements(data.getCombatProcessor().getFixLagTicks() >= 15);

                if (data.getCombatProcessor().getCancelTime() > 40
                        && data.getCombatProcessor().isCancelTimedAttack()) {
                    data.getCombatProcessor().setCancelTimedAttack(false);
                }
            }

            data.setLastTransaction(data.getLastTransaction() + 1);
            data.setLastFlying(data.getLastFlying() + 1);

            // KICKS HERE BECAUSE PE 2.0 CAN BREAK!

            if (Anticheat.INSTANCE.getConnectionValues().isLongDistance()
                    && this.tick % 3 == 0) {
                long distance = now - data.getTransactionProcessor().getLastTransactionTimeStamp();
                long distanceKeepAlive = now - data.getTransactionProcessor().getLastKeepAliveTimeStamp();

                if (distance >= 60000L || distanceKeepAlive >= 60000L) {
                    data.kickPlayer("Too long of a time with no connection response, transaction="
                            + distance + ", keepAlive="+distanceKeepAlive);
                }
            }

            if (Anticheat.INSTANCE.getConnectionValues().isPingKick()) {
                if (data.getTransactionProcessor().transactionPing >= 700
                        || data.getTransactionProcessor().postTransactionPing >= 700
                        || data.getTransactionProcessor().getKeepAlivePing() >= 700
                        || data.getTransactionProcessor().getKeepAlivePing() == 0
                        && data.getTransactionProcessor().transactionPing == 0
                        || data.getTransactionProcessor().transactionPingDrop > 700) {

                    data.getTransactionProcessor().pingFixThreshold++;

                    if (data.getTransactionProcessor().pingFixThreshold >
                            Anticheat.INSTANCE.getConnectionValues().getConnectionPingThreshold()) {
                        data.kickPlayer("Too high of ping for a long period, " +
                                "pingPost=" + data.getTransactionProcessor().postTransactionPing
                                + ", ping=" + data.getTransactionProcessor().transactionPing +
                                ", drop=" + data.getTransactionProcessor().transactionPingDrop
                                + ", keep-alive=" + data.getTransactionProcessor().getKeepAlivePing());
                    }
                } else {
                    data.getTransactionProcessor().pingFixThreshold -= Math.min(data.getTransactionProcessor()
                            .pingFixThreshold, 5);
                }
            }

            if (data.getTransactionProcessor().getLastConfirmationTick() >
                    Anticheat.INSTANCE.getConnectionValues().getConfirmTick()) {
                data.kickPlayer("Not sending back connection confirmations while moving");
            }

            int transactionQueue = data.getTransactionProcessor().getTransactionQueue().size();
            int overall = Anticheat.INSTANCE.getConnectionValues().getOverallSize();

            if (Anticheat.INSTANCE.getConnectionValues().isUseOverall()) {
                if (transactionQueue >= overall) {
                    if (++data.getTransactionProcessor().fixAbuse
                            > Anticheat.INSTANCE.getConnectionValues().getFixAbuseThreshold()) {
                        data.kickPlayer("Overall high transaction queue size, overallSize=" + transactionQueue
                                + " | maxOverall=" + Anticheat.INSTANCE.getConnectionValues().getOverallSize());
                    }
                } else {
                    data.getTransactionProcessor().fixAbuse -=
                            Math.min(data.getTransactionProcessor().fixAbuse, 20.0);
                }
            }

            if (data.getTransactionProcessor().getTransactionQueue().size() >
                    Anticheat.INSTANCE.getConnectionValues().getQueuedSizeCombat()
                    && (data.getCombatProcessor().getLastUseEntityTimer().getDelta() <= 10
                    || data.getVelocityProcessor().getServerVelocityTicks() < 10)) {
                data.getTransactionProcessor().lastSentTransaction++;

                if (data.getTransactionProcessor().lastSentTransaction >
                        Anticheat.INSTANCE.getConnectionValues().getLastSentTransactionThreshold()) {
                    data.kickPlayer("Too many transactions in the queue while in combat. " +
                            "queue="+data.getTransactionProcessor().getTransactionQueue().size());
                }
            } else {
                data.getTransactionProcessor().lastSentTransaction -=
                        Math.min(data.getTransactionProcessor().getLastSentTransaction(), 1);
            }
        }
    }

    public void stop() {
        if (!running) return;

        running = false;
        this.cancel();
    }
}