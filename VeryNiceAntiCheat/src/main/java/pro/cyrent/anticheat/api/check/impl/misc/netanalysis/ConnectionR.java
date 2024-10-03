package pro.cyrent.anticheat.api.check.impl.misc.netanalysis;

import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.evicting.EvictingList;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

import java.util.Deque;
import java.util.List;

@CheckInformation(
        name = "Connection",
        subName = "R",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.NET_ANALYSIS,
        description = "Finds invalid connection changes based on their sampled pings",
        punishable = false,
        experimental = true,
        state = CheckState.BETA)
public class ConnectionR extends Check {

    private boolean sentFlying;

    private final Deque<Integer> pingSamples = new EvictingList<>(100);
    private final Deque<Integer> combatPingSamples = new EvictingList<>(100);
    private double buffer;
    private int fastFlyingTicks;

    private double buffer2;

    private double lastAvgCombat, lastAvgPing;

    private int ticksSinceAttack = 20;

    // Detects high lag backtracks.

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
            boolean attacking = this.ticksSinceAttack <= 5;
            boolean velocity = getData().getVelocityProcessor().getServerVelocityTicks() < 150;
            boolean teleporting = getData().getActionProcessor().isTeleportingV3()
                    || getData().getActionProcessor().isTeleporting();

            int transactionPing = (int) getData().getTransactionProcessor().getTransactionPing();

            if (!attacking) {
                this.pingSamples.add(transactionPing);
            } else {
                this.combatPingSamples.add(transactionPing);
            }

            if (attacking && velocity && !teleporting
                    && getData().getMovementProcessor().getTick() > 160
                    && !getData().generalCancel()) {

                if (this.combatPingSamples.size() < 40 || this.pingSamples.size() < 40) {
                    return;
                }

                double averageCombatPing = StreamUtil.getAverage(this.combatPingSamples);
                double averagePing = StreamUtil.getAverage(this.pingSamples);

                if (averageCombatPing >= (averagePing + 150) || averagePing >= (averageCombatPing + 150)) {

                    boolean valid = averageCombatPing > this.lastAvgCombat || averagePing > this.lastAvgPing;

                    if (this.fastFlyingTicks == 0 && valid) {
                        if ((this.buffer += 0.1) > 15) {
                            this.buffer = 9;

                            if (++this.buffer2 > 7) {
                                this.buffer2 = 4;
                                this.fail("buffer="+this.buffer,
                                        "buffer-2="+this.buffer2,
                                        "avgPing="+averagePing,
                                        "avgCombatPing"+averageCombatPing);

                                getData().getBackTrackProcessor().runCheckR();
                            }
                        }
                    } else {
                        this.buffer -= Math.min(this.buffer, .005);
                    }
                } else {
                    this.buffer -= Math.min(this.buffer, .009);
                }

                this.lastAvgCombat = averageCombatPing;
                this.lastAvgPing = averagePing;
            } else {
                this.buffer -= Math.min(this.buffer, .012);
            }
        }

        this.buffer2 -= Math.min(this.buffer2, 0.00125);
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
                            && getData().getCombatProcessor().getLastPlayerAttacked() != null) {
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
