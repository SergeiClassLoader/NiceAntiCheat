package pro.cyrent.anticheat.util.event;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import lombok.Getter;

@Getter
public class EventTimer {
    private int tick, positionTick;
    private final int max;
    private final PlayerData user;
    private int serverTick;
    private boolean set;

    public EventTimer(int max, PlayerData user) {
        this.tick = 0;
        this.max = max;
        this.user = user;
        this.serverTick = 0;
        this.positionTick = 0;
    }


    public int getPositionDelta() {
        return (this.user.getMovementProcessor().getPositionTicks() - this.positionTick);
    }

    public int getDelta() {
        return (this.user.getMovementProcessor().getTick() - tick);
    }

    public int getServerDelta() {
        return Anticheat.INSTANCE.getTaskManager().getTick() - this.serverTick;
    }

    public boolean hasNotPassed(int ctick) {
        int connectedTick = this.user.getMovementProcessor().getTick();

        return ((connectedTick - tick) <= ctick);
    }

    public boolean hasNotPassedServerTick() {
        return this.set && ((Anticheat.INSTANCE.getTaskManager().getTick() - this.serverTick) < this.max);
    }

    public boolean hasNotPassedNoPing(int cTick) {
        return ((this.user.getMovementProcessor().getTick() - tick) <= cTick);
    }

    public boolean hasNotPassed() {
        return ((this.user.getMovementProcessor().getTick() - tick) <=
                (this.max));
    }

    public boolean hasNotPassedNoPing() {
        return ((this.user.getMovementProcessor().getTick() - tick) <= this.max);
    }

    public boolean passed() {
        return ((this.user.getMovementProcessor().getTick() - tick) >=
                (this.max));
    }

    public boolean passed(int cTick) {
        return ((this.user.getMovementProcessor().getTick() - tick) >=
                (cTick));
    }

    public boolean passedNoPing() {
        return ((this.user.getMovementProcessor().getTick() - tick) >= this.max);
    }

    public boolean passedNoPing(int cTick) {
        return ((this.user.getMovementProcessor().getTick() - tick) >= cTick);
    }

    public void resetServer() {
        this.serverTick = Anticheat.INSTANCE.getTaskManager().getTick();
        this.set = true;
    }

    public void resetBoth() {
        this.reset();
        this.resetServer();
        this.set = true;
    }

    public void reset() {
        this.tick = this.user.getMovementProcessor().getTick();
        this.positionTick = this.user.getMovementProcessor().getPositionTicks();
        this.set = true;
    }

    public void skip() {
        this.tick += 20;
        this.positionTick += 20;
        this.serverTick += 20;
    }

    public void fullReset() {
        this.tick = -1;
    }
}