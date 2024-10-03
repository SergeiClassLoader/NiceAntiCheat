package pro.cyrent.anticheat.api.check.impl.combat.killaura;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.avaje.ebeaninternal.server.transaction.BulkEventListenerMap;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSpectate;
import org.bukkit.Bukkit;

@CheckInformation(
        name = "KillAura",
        subName = "O",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.KILL_AURA,
        description = "Detects if a players sends entity actions while attacking",
        punishmentVL = 3,
        state = CheckState.RELEASE)
public class KillAuraO extends Check {

    private boolean entityAction = false;
    private double threshold;
    private String lastAction = "null";

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {

            if (event.isMovement()) {
                this.entityAction = false;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
                WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event.getPacketReceiveEvent());

                if (getData().generalCancel()
                        || getData().getMovementProcessor().getSkippedPackets() > 0
                        || getData().getMovementProcessor().getLastFlyingPauseTimer().getDelta() < 10
                        || getData().getProtocolVersion() > 47
                        || getData().getActionProcessor().isTeleportingV2()) {
                    this.entityAction = false;
                    return;
                }

                if (action.getAction() == WrapperPlayClientEntityAction.Action.START_FLYING_WITH_ELYTRA
                        || action.getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING
                        || action.getAction() == WrapperPlayClientEntityAction.Action.STOP_SPRINTING
                        || action.getAction() == WrapperPlayClientEntityAction.Action.STOP_JUMPING_WITH_HORSE
                        || action.getAction() == WrapperPlayClientEntityAction.Action.START_JUMPING_WITH_HORSE) {
                    this.entityAction = false;
                    return;
                }


                this.lastAction = action.getAction().name();
                this.entityAction = true;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {

                if (this.entityAction) {
                    if (++this.threshold > 2.5) {
                        this.fail("action="+this.lastAction);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, 0.05);
                }
            }
        }
    }
}