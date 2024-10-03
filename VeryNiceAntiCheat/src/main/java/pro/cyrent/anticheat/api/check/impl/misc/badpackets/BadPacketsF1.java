package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "BadPackets",
        subName = "F1",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if a player sens multiple flying packets without a position.",
        punishable = false,
        experimental = true,
        enabled = false,
        state = CheckState.PRE_BETA)
public class BadPacketsF1 extends Check {

    private int ticks;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {
                WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

                if (flying.hasPositionChanged()
                        || getData().getProtocolVersion() > 47
                        || getData().generalCancel()
                        || getData().getActionProcessor().getLastVehicleTimer().getDelta() < 10) {
                    this.ticks = 0;
                } else {
                    this.ticks++;
                }

               // Bukkit.broadcastMessage(""+this.ticks);

                if (this.ticks > 20) {
                    this.fail("Sending multiple flying packets without a position.",
                            "ticks="+this.ticks);
                    this.ticks = 0;
                }
            }
        }
    }
}
