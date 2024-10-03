package pro.cyrent.anticheat.api.check.impl.combat.autoclicker;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import pro.cyrent.anticheat.api.check.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CheckInformation(
        name = "AutoClicker",
        subName = "M",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AUTO_CLICKER,
        description = "Detects the similarity between the players current click patterns, and their last",
        punishmentVL = 10,
        punishable = false,
        state = CheckState.ALPHA)
public class AutoClickerM extends Check {

    private double threshold, lastSimilarity;
    private int movements;

    private List<Integer> clickSamplesCached = new CopyOnWriteArrayList<>();
    private final List<Integer> clickSamples = new CopyOnWriteArrayList<>();

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {
            ++this.movements;
        }

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ANIMATION) {
            if (getData().generalCancel()
                    || getData().getLastBlockBreakTimer().getDelta() < 15
                    || getData().getLastBlockPlaceTimer().getDelta() < 4
                    || getData().getLastBlockPlaceCancelTimer().getDelta() < 4
                    || getData().getActionProcessor().isDigging()) {
                this.clickSamples.clear();
                return;
            }

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() > 3)
                return;

            if (this.movements < 8) {

                this.clickSamples.add(this.movements);

                if (this.clickSamples.size() >= 100) {

                    double similarity = calculateSimilarity(this.clickSamples, this.clickSamplesCached);

                    double delta = Math.abs(similarity - this.lastSimilarity);

                    if (delta < .015 && similarity < .80) {
                        if (++this.threshold > 5) {
                            this.fail("delta="+delta,
                                    "similarity="+similarity);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, 0.75);
                    }

                    this.lastSimilarity = similarity;
                    this.clickSamplesCached = this.clickSamples;
                    this.clickSamples.clear();
                }
            }

            this.movements = 0;
        }
    }


    public double calculateSimilarity(List<Integer> currentList, List<Integer> lastList) {
        if (currentList.size() != lastList.size()) {
            return Double.MAX_VALUE;
        }

        int totalComparisons = currentList.size() * lastList.size();
        double totalDifference = 0.0;

        // Calculate total difference between all pairwise comparisons
        for (double current : currentList) {
            for (double last : lastList) {
                totalDifference += Math.abs(current - last);
            }
        }

        // Normalize to a similarity score between 0 and 1
        double maxPossibleDifference = totalComparisons * getMaxValue(lastList);
        double similarity = 1.0 - (totalDifference / maxPossibleDifference);

        return similarity;
    }

    private double getMaxValue(List<Integer> list) {
        double max = Double.MIN_VALUE;

        for (double value : list) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
}