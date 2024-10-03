package pro.cyrent.anticheat.api.check.impl.combat.aimanalysis;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

import java.util.LinkedList;
import java.util.List;

@CheckInformation(
        name = "Analysis",
        subName = "B",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM_ANALYSIS,
        punishable = false,
        experimental = true,
        description = "Detects invalid aim patterns with little outliers over time",
        state = CheckState.BETA)
public class AnalysisB extends Check {

    private double threshold;

    private double lastDev, lastAvg;

    private final List<Float> pitchChanges = new LinkedList<>();
    private final List<Double> pitchAverages = new LinkedList<>();

    @Override
    public void onPacket(PacketEvent event) {
      if (event.isMovement()) {

          WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

          if (!flying.hasRotationChanged()) {
              return;
          }

          if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 4) {
              float pitchDelta = getData().getMovementProcessor().getDeltaPitchAbs();
              float yawDelta = getData().getMovementProcessor().getDeltaYawAbs();
              float lastYawDelta = getData().getMovementProcessor().getLastDeltaYawAbs();

              if (yawDelta > 2.5F && lastYawDelta > 2.5F) {

                  this.pitchChanges.add(pitchDelta);

                  if (this.pitchChanges.size() >= 40) {
                      double average = StreamUtil.getAverage(this.pitchChanges);
                      this.pitchAverages.add(average);
                      this.pitchChanges.clear();
                  }

                  if (this.pitchAverages.size() >= 8) {

                      double std = StreamUtil.getStandardDeviation(this.pitchAverages);
                      double dev = StreamUtil.getDeviation(this.pitchAverages);
                      double avgAvg = StreamUtil.getAverage(this.pitchAverages);

                      boolean invalid = Math.abs(dev - this.lastDev) < 0.005
                              || Math.abs(avgAvg - this.lastAvg) < 0.005
                              || dev < .025 && std < .025;

                      if (invalid) {
                          if (++this.threshold > 7) {
                              this.fail("dev="+dev,
                                      "lastDev="+this.lastDev,
                                      "avg="+avgAvg,
                                      "lastAvg="+this.lastAvg);
                          }
                      } else {
                          this.threshold -= Math.min(this.threshold, 0.125);
                      }

                      this.lastDev = dev;
                      this.lastAvg = avgAvg;
                      this.pitchAverages.clear();
                  }
              }
          }
      }
    }
}