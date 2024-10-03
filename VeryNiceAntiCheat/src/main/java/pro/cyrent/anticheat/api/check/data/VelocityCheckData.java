package pro.cyrent.anticheat.api.check.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VelocityCheckData {
    private final double smallest;
    private final int velocityTick;
    private final int listSize;
    private final double ratio;
    private final double maxOffset;
}
