package pro.cyrent.anticheat.util.task;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.map.ConcurrentEvictingMap;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import lombok.Data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class TransactionTickHandler {

    private final PlayerData data;

    private final Map<Short, Long> dataMap = new ConcurrentHashMap<>();
    private final Map<Short, List<TransactionPacketAction>> actionMap = new ConcurrentEvictingMap<>(300);
    private final Map<Short, List<TransactionPacketAction>> actionMap2 = new ConcurrentEvictingMap<>(300);
    private final Map<Short, Long> receiveMap = new ConcurrentEvictingMap<>(50);

    private short ticks = Short.MAX_VALUE, ticksVel = 12000;
    private short lastTicks;

    public void checkReceive(final long timestamp, final short id) {

        this.getFunction(id, true).ifPresent(function -> {

            getData().getReachProcessor().getEntityUpdateTransactionTimer().reset();
            function.forEach(TransactionPacketAction::handle);
        });

        this.onConsume(id);

    }

    public void checkVelocity(short id) {
        this.getFunctionVel(id, true).ifPresent(function -> function.forEach(TransactionPacketAction::handle));
    }

    public void addToConfirm(final TransactionPacketAction packetAction, final short ticks) {

        final List<TransactionPacketAction> actions = this.actionMap.get(ticks);

        if (actions != null) {
            actions.add(packetAction);
        } else {
            this.actionMap.put(ticks, new ArrayList<>(Collections.singleton(packetAction)));
        }
    }

    public void addToConfirmVel(final TransactionPacketAction packetAction, final short ticks) {

        final List<TransactionPacketAction> actions = this.actionMap2.get(ticks);

        if (actions != null) {
            actions.add(packetAction);
        } else {
            this.actionMap2.put(ticks, new ArrayList<>(Collections.singleton(packetAction)));
        }
    }

    public List<TransactionPacketAction> getActions(final short tick) {
        return this.actionMap.get(tick);
    }

    public boolean hasReceived(final short tick) {
        return receiveMap.containsKey(tick);
    }

    public void onConsume(final short id) {
        data.getTickHolder().transactionTick = id;
    }

    private Optional<List<TransactionPacketAction>> getFunction(final short identification, final boolean remove) {
        final List<TransactionPacketAction> runnable = actionMap.get(identification);

        if (runnable == null) return Optional.empty();
        else if (remove) actionMap.remove(identification);

        return Optional.of(runnable);
    }

    private Optional<List<TransactionPacketAction>> getFunctionVel(final short identification, final boolean remove) {
        final List<TransactionPacketAction> runnable = actionMap2.get(identification);

        if (runnable == null) return Optional.empty();
        else if (remove) actionMap2.remove(identification);

        return Optional.of(runnable);
    }

    public void push(final boolean flush) {
        /*
         * This is not really randomization, but it is still going to be a little unpredictable for the client
         * since it cannot really know the server ticks to be able to accurately assume the action number that is
         * about to be sent to them. If we wanted we could use a secure random just to make things a little more difficult.
         */

        this.onPush(flush, this.ticks);

        this.lastTicks = ticks;
        this.ticks = this.getNextTick();

    }

    public void pushVel(final boolean flush) {

        /*
         * This is not really randomization, but it is still going to be a little unpredictable for the client
         * since it cannot really know the server ticks to be able to accurately assume the action number that is
         * about to be sent to them. If we wanted we could use a secure random just to make things a little more difficult.
         */


        this.onPushVel(flush, this.ticksVel);

        this.ticksVel = getNextTickVel();
    }

    public void onPush(final boolean push, final short id) {
        getData().getTransactionProcessor().sendPacket(new
                WrapperPlayServerWindowConfirmation(0, id, false), getData().getPlayer());
    }

    public void onPushVel(final boolean push, final short id) {
        getData().getTransactionProcessor().sendPacket(new
                WrapperPlayServerWindowConfirmation(0, id, false), getData().getPlayer());
    }

    public long whenSent(final short tick) {
        return dataMap.containsKey(tick) ? dataMap.get(tick) : -1;
    }

    public short getTickVel() {
        return ticksVel;
    }

    public short getNextTickVel() {

        short tick = ticksVel;

        if (tick >= (12000 + 5212)) {
            tick = 12000;
        }

        tick++;

        return tick;
    }

    public short getTick() {
        return ticks;
    }

    public short getNextTick() {

        short tick = ticks;

        if (tick <= (Short.MAX_VALUE - 5000)) {
            tick = Short.MAX_VALUE;
        }

        tick--;

        return tick;
    }


    public short getLastTick() {
        return lastTicks;
    }
}