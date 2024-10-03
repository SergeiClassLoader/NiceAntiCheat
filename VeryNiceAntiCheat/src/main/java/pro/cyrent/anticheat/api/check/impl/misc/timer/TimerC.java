package pro.cyrent.anticheat.api.check.impl.misc.timer;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CheckInformation(
        name = "Timer",
        subName = "C",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.TIMER,
        description = "Finds and analyzes improper game speed changes",
        experimental = true,
        state = CheckState.PRE_ALPHA)
public class TimerC extends Check {

    private Long lastFlying = null;
    private final long time = 50L;
    private long flyingBalance, lastBalance;

    private Double offset = null;
    private double lastAverageBalance;

    private double slowSpeedCount;

    private final List<Long> balanceList = new CopyOnWriteArrayList<>();

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            long now = System.currentTimeMillis();

            if (getData().getProtocolVersion() > 47) return;

            if (getData().getActionProcessor().isTeleportingV3() && (this.flyingBalance > 250
                    || this.flyingBalance < -250L)) {
                this.flyingBalance = 0;
            }

            // Only run if the flying packet has been sent at least twice.
            if (this.lastFlying != null) {

                // get the flying delta
                long flyingDelta = (now - this.lastFlying);
                long balanceDelta = (this.time - flyingDelta);

                this.lastBalance = this.flyingBalance;

                // add the delta to the current balance
                this.flyingBalance += balanceDelta;

                // add the balance to a list, so we can get the average
                this.balanceList.add(this.flyingBalance);

                // 20 is one second so its good enough
                if (this.balanceList.size() >= 20) {
                    double average = StreamUtil.getAverage(this.balanceList);

                    // We compare the average to their last average to see if their game movement is going down.
                    this.offset = average - this.lastAverageBalance;

                    // Check if their average balance offset is going negative (1 tick of time)
                    if (this.offset <= 0 && average < -250) {

                        // up the slow speed counter
                        this.slowSpeedCount++;
                    } else {
                        // lower this counter if its not happening.
                        this.slowSpeedCount = 0;
                    }

                    // reset the balance in case of false positives when their slow count is 0.
                    if ((this.flyingBalance <= -250L || this.flyingBalance >= 250) && this.slowSpeedCount == 0) {
                        this.flyingBalance = 0;
                    }

                    // Get the last balance for comparison
                    this.lastAverageBalance = average;

                    // clear the list, so we can check next time to see if its still happening
                    this.balanceList.clear();
                }

                // make sure the offset was set.
                if (this.offset != null) {

                    long difference = Math.abs(this.flyingBalance - this.lastBalance);

                    // Check if their difference in balance is greater than 50 milliseconds,
                    // and their current balance under 250ms while it's going down, while in combat.
                    // In theory this should detect tick-base but knowing my luck it will false flag the whole server!
                    if (difference > 50L && (this.flyingBalance + 50) < this.lastBalance && this.flyingBalance < -250L
                            && getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 7
                            && getData().getActionProcessor().getServerPositionTicks() > 10
                            && !getData().generalCancel()
                            && !getData().getActionProcessor().isTeleportingV3()) {

                        // Check if their slow speed count is more than 20 times, and their offset is below 0.
                        // This means their game has slowed down 20 times, but sped up at the same time.
                        // Legit players can't usually do this without some real specific stuff happening
                        if (this.slowSpeedCount >= 20 && this.offset < -50) {
                            if (++this.threshold > 3.25) {
                                this.fail("Slow Count="+this.slowSpeedCount,
                                        "Offset="+this.offset,
                                        "Flying Balance="+this.flyingBalance,
                                        "Threshold="+this.threshold);
                            }
                        } else {
                            this.threshold -= Math.min(this.threshold, 0.05);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, 0.05);
                    }
                }
            }

            this.lastFlying = now;
        }
    }
}
