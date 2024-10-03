package pro.cyrent.anticheat.impl.processor.connection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RangedTransactionGenerator {
    public final short min;
    public final short max;

    private long time;

    public short currentTransaction, lastTransaction;

    public RangedTransactionGenerator(short min, short max) {
        this.min = min;
        this.max = max;
        this.currentTransaction = this.min;
    }

    public short generateNextTransaction(boolean updateFirst, boolean reach) {

        this.lastTransaction = this.currentTransaction;

        // update
        if (updateFirst) {
            updateTransaction(reach);
        }

        // get the action
        short action = this.currentTransaction;

        if (!updateFirst) {
            updateTransaction(reach);
        }

        // return it to send it
        return action;
    }

    private void updateTransaction(boolean reach) {
        if (reach) {
            currentTransaction += 2;
        } else {
            currentTransaction += 1;
        }
        if (currentTransaction > max)
            currentTransaction = min;
    }
}