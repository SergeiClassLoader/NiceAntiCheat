package pro.cyrent.anticheat.api.check.impl.misc.badpackets;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.util.Vector;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "BadPackets",
        subName = "J",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Checks for VClip/invalid packet order while moving",
        punishmentVL = 10,
        experimental = true,
        punishable = false,
        state = CheckState.BETA)
public class BadPacketsJ extends Check {

    private Vector lastVector = null;
    private boolean emptyFlying;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {
                WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

                if (!flying.hasPositionChanged()) {
                    this.emptyFlying = true;
                }

                if (flying.hasPositionChanged()) {

                    Vector currentVector = new Vector(flying.getLocation().getX(),
                            flying.getLocation().getY(), flying.getLocation().getZ());

                    if (this.emptyFlying) {
                        this.emptyFlying = false;

                        if (exempt()
                                || getData().getVelocityProcessor().getServerVelocityTicks() < 20
                                || getData().getVelocityProcessor().getVelocityTicksConfirmed() < 20
                                || getData().isBedrock()) {
                            this.lastVector = null;
                            return;
                        }

                        if (this.lastVector != null) {
                            double distance = currentVector.distanceSquared(this.lastVector);

                            if (distance > 1.5) {
                                if (++this.threshold > 1) {
                                    this.fail("Distance="+distance,
                                            "Threshold="+this.threshold);
                                }
                            } else {
                                this.threshold -= Math.min(this.threshold, .005);
                            }
                        }
                    }


                    this.lastVector = currentVector;
                }
            }
        }

        if (event.getPacketSendEvent() != null) {
            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
                this.emptyFlying = false;
            }
        }
    }

    private boolean exempt() {
        return getData().generalCancel() || getData().getActionProcessor().isTeleportingV2()
                || getData().getCollisionProcessor().getNearBoatTicks() > 0
                || getData().getLastTeleport().getDelta() < 10;
    }
}
