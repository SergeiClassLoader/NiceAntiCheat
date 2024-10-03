package pro.cyrent.anticheat.api.check.impl.misc.badpackets;


import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;

@CheckInformation(
        name = "BadPackets",
        subName = "K1",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.BAD_PACKETS,
        description = "Detects sending multiple sprint/sneaks in a row without changing states",
        punishable = false,
        experimental = true,
        state = CheckState.PRE_BETA)
public class BadPacketsK1 extends Check {

    private double threshold;
    private int stopSneaking, startSneaking;
    private int stopSprinting, startSprinting;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
                WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event.getPacketReceiveEvent());

                if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SNEAKING) {
                    this.startSneaking++;
                    this.stopSneaking = 0;
                }

                if (action.getAction() == WrapperPlayClientEntityAction.Action.STOP_SNEAKING) {
                    this.stopSneaking++;
                    this.startSneaking = 0;
                }

                if (action.getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING) {
                    this.startSprinting++;
                    this.stopSprinting = 0;
                }

                if (action.getAction() == WrapperPlayClientEntityAction.Action.STOP_SPRINTING) {
                    this.stopSprinting++;
                    this.startSprinting = 0;
                }

                if (!getData().generalCancel()) {

                    boolean invalid = this.startSprinting > 2 || this.stopSprinting > 2
                            || this.startSneaking > 2 || this.stopSneaking > 2;

                    if (invalid) {
                        if (++this.threshold > 2.5) {
                            this.fail("stop-sneaks-sent=" + this.stopSneaking,
                                    "start-sneaks-sent=" + this.startSneaking,
                                    "stop-sprint-sent=" + this.stopSprinting,
                                    "start-sprint-sent=" + this.startSprinting);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .005);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .005);
                }
            }
        }
    }
}
