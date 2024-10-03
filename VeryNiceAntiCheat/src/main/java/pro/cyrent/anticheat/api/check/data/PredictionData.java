package pro.cyrent.anticheat.api.check.data;

import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.processor.prediction.horizontal.HorizontalProcessor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class PredictionData {
    private final double offset;
    private final boolean invalid;
    private final PlayerData playerData;
    private final HorizontalProcessor.BruteForcedData data;
    private final int velocityTick;
    private final double pastMotion, deltaXZ;
    private boolean fastMath;
    private double max;
}
