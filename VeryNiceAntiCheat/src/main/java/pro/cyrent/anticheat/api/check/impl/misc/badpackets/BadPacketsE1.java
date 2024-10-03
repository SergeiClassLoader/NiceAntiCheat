package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSteerVehicle;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "BadPackets",
        subName = "E1",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects if a player sends impossible vehicle packets.",
        punishable = false,
        state = CheckState.PRE_BETA)
public class BadPacketsE1 extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {
                WrapperPlayClientSteerVehicle steerVehicle =
                        new WrapperPlayClientSteerVehicle(event.getPacketReceiveEvent());

                float forward = Math.abs(steerVehicle.getForward());
                float side = Math.abs(steerVehicle.getSideways());

                float max = 0.98F;
                boolean invalid = side > max || forward > max;

                if (invalid) {
                    if (++this.threshold > 5.0) {
                        this.fail("Sending impossible vehicle packets to the server.",
                                "forward="+forward,
                                "sideways="+side,
                                "max="+max);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .005);
                }
            }
        }
    }
}
