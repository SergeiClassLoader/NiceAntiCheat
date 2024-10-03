package pro.cyrent.anticheat.api.check.impl.movement.noslow;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "NoSlowDown",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.NO_SLOWDOWN,
        experimental = true,
        punishable = false,
        description = "Detects if the player moves incorrectly while using items (food & bows)",
        state = CheckState.BETA)
public class NoSlowA extends Check {

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            if (getData().getNoSlowDownProcessor().getBuffer() > 7) {
                this.fail("Cloud Check");
            }
        }
    }
}
