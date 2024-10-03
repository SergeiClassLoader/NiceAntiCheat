package pro.cyrent.anticheat.util.task;

import pro.cyrent.anticheat.api.user.PlayerData;
import lombok.Getter;

public class TransactionTickHolder {

    private final PlayerData data;

    public TransactionTickHolder(final PlayerData data) {
        this.data = data;

        tickHandler = new TransactionTickHandler(data);
    }

    @Getter
    private TransactionTickHandler tickHandler;

    public int transactionTick;

    // reach check post
    public void confirmFunction(final TransactionPacketAction action) {
        tickHandler.addToConfirm(action, tickHandler.getTick());
    }

    public void confirmFunctionVel(final TransactionPacketAction action) {
        tickHandler.addToConfirmVel(action, tickHandler.getTickVel());
    }

    // reach check pre
    public void confirmFunctionAndTick(final TransactionPacketAction action) {
        tickHandler.addToConfirm(action, tickHandler.getTick());
        tickHandler.push(true);
    }

    public void confirmFunctionAndTickVelocity(final TransactionPacketAction action) {
        tickHandler.addToConfirmVel(action, tickHandler.getTickVel());
        tickHandler.pushVel(true);
    }

    public void handlePacketAtPre(final long timestamp, final short packet) {
        tickHandler.checkReceive(timestamp, packet);
    }

    public void handlePacketAtPreVel(final long timestamp, final short packet) {
        tickHandler.checkVelocity(packet);
    }


    public void confirmFunctionLast(final TransactionPacketAction action) {
        final boolean push = tickHandler.getActions(tickHandler.getLastTick()) == null
                || tickHandler.hasReceived(tickHandler.getLastTick())
                || elapsed(tickHandler.whenSent(tickHandler.getLastTick()), 5L);

        if (push) {
            tickHandler.addToConfirm(action, tickHandler.getTick());
            tickHandler.push(true);
        } else {
            tickHandler.addToConfirm(action, tickHandler.getLastTick());
        }
    }

    public void pushTick() {
        tickHandler.push(true);
    }

    public void pushVelTick() {
        tickHandler.pushVel(true);
    }

    private static boolean elapsed(long from, long required) {
        return System.currentTimeMillis() - from > required;
    }
}