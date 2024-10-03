package pro.cyrent.anticheat.api.check.impl.combat.aim;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "Aim",
        subName = "Y",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM,
        punishable = false,
        description = "Finds high aggressive yaw rotations",
        state = CheckState.PRE_BETA)
public class AimY extends Check {

    private double threshold;
    private double lastMCP;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 2) {
                long sensitivity = getData().getSensitivityProcessor().getSensitivity();

                double deltaXZ = getData().getMovementProcessor().getDeltaXZ();

                if (deltaXZ < .085) {
                    return;
                }

                float yawAccel = Math.abs(getData().getMovementProcessor().getYawAccel());
                float deltaPitch = Math.abs(getData().getMovementProcessor().getDeltaPitch());
                float deltaYaw = Math.abs(getData().getMovementProcessor().getDeltaYaw());

                double mcp = getData().getSensitivityProcessor().getSensitivityValue();

                if (deltaPitch != 0.0f && sensitivity == -1 && yawAccel <= 30 && deltaYaw > 2.f) {

                    if (mcp == this.lastMCP) {

                        if (++this.threshold > 70) {
                            this.fail(
                                    "mcp="+mcp,
                                    "lastMCP="+lastMCP,
                                    "deltaPitch="+deltaPitch,
                                    "sensitivity="+sensitivity,
                                    "deltaYaw="+deltaYaw,
                                    "yawAccel="+yawAccel);
                        }
                    } else {
                        this.threshold = 0;
                    }
                } else {
                    this.threshold = 0;
                }

                this.lastMCP = mcp;
            }
        }
    }
}
