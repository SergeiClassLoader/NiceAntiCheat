package pro.cyrent.anticheat.api.check.impl.combat.autoclicker;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.Bukkit;

import java.util.Deque;
import java.util.LinkedList;

@CheckInformation(
        name = "AutoClicker",
        subName = "V",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Detects invalid variance & entropy click patterns",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.PRE_BETA)
public class AutoClickerV extends Check {

    private double threshold;
    private double lastVariance, lastLastVariance;
    private double lastEntropy, lastLastEntropy;
    private int movements;

    private final Deque<Integer> clickData = new LinkedList<>();

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        // All flying packets.
        if (event.isMovement()) {
            // Count up the movements every flying packet to get an accurate representation (in ticks)
            // So we can later use this to sample for our auto-clicker detection.
            ++this.movements;
        }

        // We check arm animation as it gives us better detail than the attack packet
        // The attack packet should only ever be used in auto-clicker checks for specific cases.
        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ANIMATION) {

            // Return the check for general things such as digging, placing, and server lag.
            if (getData().generalCancel()
                    || getData().getLastBlockBreakTimer().getDelta() < 15
                    || getData().getLastBlockPlaceTimer().getDelta() < 4
                    || getData().getActionProcessor().isDigging()
                    || getData().getLastBlockPlaceCancelTimer().getDelta() < 4) {
                this.clickData.clear();
                return;
            }

            // If not in combat then we will ignore this check.
            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 60) {
                return;
            }

            // Movement less than 8 to filter out low cps.
            if (this.movements < 8) {

                // Filter out 0 since, 0 can be replicated multiple times with higher cps.
                if (this.movements == 0) return;

                // Add the movements to sample later.
                this.clickData.addLast(this.movements);

                // Cap the sample size at 50, so we can detect quicker, and so we don't have to wait a year!
                if (this.clickData.size() >= 50) {

                    // Get the skewness for later so we don't have false positives at higher skewness ranges.
                    double skewness = Math.abs(StreamUtil.getSkewness(this.clickData));

                    // Ignore if the players skewness is way too high.
                    if (skewness > 0.1D) {
                        this.clickData.clear();
                        return;
                    }

                    // Grab the variance & entropy from the stream utility class.
                    double variance = StreamUtil.getVariance(this.clickData);
                    double entropy = StreamUtil.getEntropy(this.clickData);

                    // Detects if the current variance offsets are valid and the variance is high enough.
                    boolean validVariance = (Math.abs(variance - this.lastVariance) < 1.0
                            || Math.abs(variance - this.lastLastVariance) < 1.0) && variance > 11;

                    // Detects avg valid entropy level.
                    boolean validEntropy = entropy > .8 && entropy < 1.5;

                    // Entropy is on average around .8 - and 1.1, while a players variance on avg is above 10.
                    // This in theory should detect invalid/odd auto-clicker patterns.
                    boolean invalidVarEntropy = entropy < .45 && variance < 5;

                    // Sets to true, when the entropy offset range is invalid and way too small.
                    boolean invalidEntropyOffset = Math.abs(entropy - this.lastEntropy) < 0.02D
                            || Math.abs(entropy - this.lastLastEntropy) < 0.02D;

                    // Basic abnormal entropy range.
                    boolean invalid = invalidVarEntropy
                            // Invalid entropy offset ranges.
                            || validVariance && validEntropy && invalidEntropyOffset;

                    // Check if it's an invalid click pattern.
                    if (invalid) {
                        // Add a small buffer in case of false positives.
                        if (++this.threshold > 3.0) {
                            // Alert to staff
                            this.fail("entropy="+entropy,
                                    "last-entropy="+this.lastEntropy,
                                    "previous-entropy="+this.lastLastEntropy,
                                    "variance="+variance,
                                    "last-variance="+this.lastVariance,
                                    "previous-variance="+this.lastLastVariance,
                                    "skewness="+skewness);
                        }
                    } else {
                        // Lower the buffer when not invalid to prevent any false positives.
                        this.threshold -= Math.min(this.threshold, .4);
                    }


                    // Store data sets from previously finished sampling.
                    this.lastLastEntropy = this.lastEntropy;
                    this.lastLastVariance = this.lastVariance;
                    this.lastEntropy = entropy;
                    this.lastVariance = variance;

                    // Clear the click data once the sampling of 50 has completed.
                    this.clickData.clear();
                }
            }

            // Reset the flying movements to 0 once the player has sent an animation packet
            // We do this in ticks so the flying packet can't cause a lot of false positives
            // We also do this because milliseconds are very unreliable in auto-clicker checks.
            this.movements = 0;
        }
    }
}