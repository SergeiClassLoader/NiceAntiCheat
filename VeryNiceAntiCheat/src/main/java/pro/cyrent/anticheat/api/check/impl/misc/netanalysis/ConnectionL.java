package pro.cyrent.anticheat.api.check.impl.misc.netanalysis;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckInformation(
        name = "Connection",
        subName = "L",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.NET_ANALYSIS,
        description = "Finds invalid connection changes based on their sampled ping",
        punishable = false,
        experimental = true,
        enabled = false,
        state = CheckState.BETA)
public class ConnectionL extends Check {

    private int fastFlyingTicks;
    private int pingBeforeCombat = -1;
    private int lastTimeSincePing;
    private int ticksSinceAttack = 20;

    private boolean sentFlying;
    private double buffer;

    private boolean combatLagged = false;

    private long lastFail = System.currentTimeMillis();

    @Override
    public void onServerTick(PlayerData playerData) {
        if (playerData.getMovementProcessor().getSkippedPackets() > 5) {
            this.fastFlyingTicks += (this.fastFlyingTicks < 20 ? 1 : 0);
        } else {
            this.fastFlyingTicks -= this.fastFlyingTicks > 0 ? 1 : 0;
        }
    }

    @Override
    public void onClientTransaction(PlayerData playerData, long timestamp) {

        if (this.sentFlying && getData().getMovementProcessor().getTick() > 100) {
            boolean attacking = this.ticksSinceAttack <= 3;

            boolean teleporting = getData().getActionProcessor().isTeleportingV3()
                    || getData().getActionProcessor().isTeleporting();

            int ping = (int) getData().getTransactionProcessor().getTransactionPing();

            boolean invalid = ping > (this.pingBeforeCombat + 80);
            boolean combatInvalid = !this.combatLagged && this.lastTimeSincePing < 300 && invalid;

            if (attacking && !teleporting
                    && getData().getMovementProcessor().getTick() > 160
                    && !getData().generalCancel()) {
                if (this.pingBeforeCombat != -1 && this.fastFlyingTicks == 0 && combatInvalid) {
                    if (++this.buffer > 5.0) {
                        this.buffer = 4;
                        this.fail(
                                "buffer="+this.buffer,
                                "ping-before="+this.pingBeforeCombat,
                                "ping-now="+ping,
                                "lastTimeSincePing="+this.lastTimeSincePing,
                                "attack-tick="+this.ticksSinceAttack);

                        getData().getBackTrackProcessor().runCheckR();
                    }

                    this.lastFail = System.currentTimeMillis();
                    this.combatLagged = true;
                }
            } else {
                this.combatLagged = false;
                this.pingBeforeCombat = ping;
                this.lastTimeSincePing = 0;
            }
        }

        if ((System.currentTimeMillis() - this.lastFail) >= 30000L) {
            this.buffer = 0;
        }

        this.buffer -= Math.min(this.buffer, 0.005);
        this.lastTimeSincePing++;
        this.sentFlying = false;
    }

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {
                this.ticksSinceAttack++;
                this.sentFlying = true;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interactEntity =
                        new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {

                    if (getData().getCombatProcessor().getLastPlayerAttack() <= 20
                            && getData().getCombatProcessor().getLastPlayerEntityAttacked() != null) {
                        this.ticksSinceAttack = 0;
                    }
                }
            }
        }

        if (event.getPacketSendEvent() != null) {
            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
                this.sentFlying = false;
            }
        }
    }
}