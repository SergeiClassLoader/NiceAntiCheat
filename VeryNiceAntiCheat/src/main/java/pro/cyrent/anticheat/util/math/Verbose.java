package pro.cyrent.anticheat.util.math;

import lombok.Getter;
import lombok.Setter;
import pro.cyrent.anticheat.util.time.TimeUtils;

@Setter
@Getter
public class Verbose {

    private int verbose;
    private long lastFlagTime;

    public boolean flag(int amount) {
        lastFlagTime = System.currentTimeMillis();
        return (verbose++) > amount;
    }

    public boolean flag(int amount, long reset) {
        if (!TimeUtils.passed(lastFlagTime, reset)) {
            lastFlagTime = System.currentTimeMillis();
            return (verbose++) > amount;
        }
        verbose = 0;
        lastFlagTime = System.currentTimeMillis();
        return false;
    }

    public boolean flagCurrentMS(int amount, long reset, long ms) {
        if (!TimeUtils.passed(lastFlagTime, reset, ms)) {
            lastFlagTime = ms;
            return (verbose++) > amount;
        }
        verbose = 0;
        lastFlagTime = ms;
        return false;
    }

    public boolean flagPrecise(int amount, long reset) {
        if (!TimeUtils.passed(lastFlagTime, reset)) {
            lastFlagTime = System.currentTimeMillis();
            return (++verbose) >= amount;
        }
        verbose = 0;
        lastFlagTime = System.currentTimeMillis();
        return false;
    }

    public boolean flagPrecise(int amount, long reset, long millis) {
        if (!TimeUtils.passed(lastFlagTime, reset)) {
            lastFlagTime = millis;
            return (++verbose) >= amount;
        }
        verbose = 0;
        lastFlagTime = millis;
        return false;
    }

    public boolean flag(int amount, int cap, long reset) {
        if (!TimeUtils.passed(lastFlagTime, reset)) {
            lastFlagTime = System.currentTimeMillis();
            if (verbose <= cap) verbose++;
            return verbose > amount;
        }
        verbose = 0;
        lastFlagTime = System.currentTimeMillis();
        return false;
    }

    public int getVerbose() {
        return verbose;
    }

    public void setVerbose(int verbose) {
        this.verbose = verbose;
    }

    public void takeaway() {
        verbose = verbose > 0 ? verbose - 1 : 0;
    }

    public void takeaway(int amount) {
        verbose = verbose > 0 ? verbose - amount : 0;
    }

    public boolean flag(int amount, long reset, int toAdd) {
        if (!TimeUtils.elapsed(lastFlagTime, reset)) {
            lastFlagTime = System.currentTimeMillis();
            return (verbose += toAdd) > amount;
        }
        verbose = 0;
        lastFlagTime = System.currentTimeMillis();
        return false;
    }
}
