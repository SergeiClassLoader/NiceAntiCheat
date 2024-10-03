package pro.cyrent.anticheat.api.check.impl.combat.aimanalysis;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInformation(
        name = "Analysis",
        subName = "E",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.AIM_ANALYSIS,
        punishable = false,
        experimental = true,
        enabled = false,
        description = "Detects if a player looks at the bot too much while in combat",
        state = CheckState.ALPHA)
public class AnalysisE extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() == null) return;

        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (!flying.hasRotationChanged() || !getData().isBotRunning()) return;

            if (getData().getMovementProcessor().getDeltaXZ() > 0.07
                    && getData().getMovementProcessor().getDeltaXZ() < 1) {

                if (getData().isLookingAtBot()) {
                    if (++this.threshold > 75) {
                        this.fail("Cloud Check");
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .5);
                }
            }
        }
    }
}