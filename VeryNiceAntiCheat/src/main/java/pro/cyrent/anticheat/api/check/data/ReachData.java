package pro.cyrent.anticheat.api.check.data;

import pro.cyrent.anticheat.api.user.PlayerData;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class ReachData {
    private final double distance, distanceNo003;
    private final boolean validHitbox;
    private final boolean attack, interact;
    private PlayerData playerData;
}
