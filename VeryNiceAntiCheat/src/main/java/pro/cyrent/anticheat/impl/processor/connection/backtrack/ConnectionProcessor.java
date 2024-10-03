package pro.cyrent.anticheat.impl.processor.connection.backtrack;

import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.evicting.EvictingList;
import pro.cyrent.anticheat.util.stream.StreamUtil;

import lombok.Getter;
import lombok.Setter;


import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public class ConnectionProcessor extends Event {
    private final PlayerData data;

    private final Deque<Integer> pingSamplesCombat = new EvictingList<>(100);
    private final Deque<Integer> pingSamples = new EvictingList<>(100);

    private final List<Integer> transactionPing = new CopyOnWriteArrayList<>();
    private final List<Integer> keepAlivePing = new CopyOnWriteArrayList<>();

    private final Deque<Integer> flyingSamplesCombat = new EvictingList<>(100);
    private final Deque<Integer> flyingSamples = new EvictingList<>(100);

    private int averageCombatPing, averageNonCombatPing;

    private int averageCombatFlying, averageFlying;

    private double averageTransactionPing, averageKeepAlivePing;

    private int combatPingDifference;

    private int flyingCombatDifference;

    private boolean connectionSet = false;
    private boolean flyingSet = false;

    private int pingBeforeVelocity;
    private int pingAfterVelocity;

    public ConnectionProcessor(PlayerData user) {
        this.data = user;
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {

                int ping = (int) getData().getTransactionProcessor().getTransactionPing();
                int kping = (int) getData().getTransactionProcessor().getKeepAlivePing();
                int skippedPackets = getData().getMovementProcessor().getSkippedPackets();
                boolean combat = getData().getCombatProcessor().getLastUseEntityTimer().getDelta() <= 200;

                this.transactionPing.add(ping);
                this.keepAlivePing.add(kping);

                // But demon why don't you just use an evicting list??? well because if you do and the list is full,
                // it's going to keep adding them, always giving the average ping, I only want to get it every 100 samples
                // This ensures there's not a massive false positive range.

                if (this.keepAlivePing.size() >= 5) {
                    this.averageKeepAlivePing = StreamUtil.getAverage(this.keepAlivePing);
                    this.keepAlivePing.clear();
                }


                if (this.transactionPing.size() >= 100) {
                    this.averageTransactionPing = StreamUtil.getAverage(this.transactionPing);
                    this.transactionPing.clear();
                }


                //sample ping when they are in combat.

                if (combat) {
                    if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 4) {
                        this.flyingSamplesCombat.add(skippedPackets);
                    } else {
                        this.flyingSamples.add(skippedPackets);
                    }
                }

                boolean velocity = getData().getVelocityProcessor().getVelocityTicksConfirmed() < 6;

                if (combat) {

                    if (!velocity) {
                        this.pingAfterVelocity = (int) getData().getTransactionProcessor().getTransactionPing();
                    } else {
                        this.pingBeforeVelocity = (int) getData().getTransactionProcessor().getTransactionPing();
                    }

                    if (getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 10) {
                        this.pingSamplesCombat.add(ping);
                    } else {
                        this.pingSamples.add(ping);
                    }
                }

                boolean size = this.pingSamples.size() > 80 && this.pingSamplesCombat.size() > 80;

                if (!this.pingSamples.isEmpty() && !this.pingSamplesCombat.isEmpty() && combat) {

                    if (size) {

                        if (!this.connectionSet) {
                            this.connectionSet = true;
                        }

                        double averageCombatPing = StreamUtil.getAverage(this.pingSamplesCombat);
                        double averagePingNoCombat = StreamUtil.getAverage(this.pingSamples);

                        this.combatPingDifference = (int) Math.abs(averageCombatPing - averagePingNoCombat);

                        this.averageCombatPing = (int) averageCombatPing;
                        this.averageNonCombatPing = (int) averagePingNoCombat;
                    }
                }

                if (!this.flyingSamples.isEmpty() && !this.flyingSamplesCombat.isEmpty() && combat) {

                    if (this.flyingSamples.size() > 80 && this.flyingSamplesCombat.size() > 80) {

                        if (!this.flyingSet) {
                            this.flyingSet = true;
                        }

                        double averageCombatPing = StreamUtil.getAverage(this.flyingSamplesCombat);
                        double averagePingNoCombat = StreamUtil.getAverage(this.flyingSamples);

                        this.flyingCombatDifference = (int) Math.abs(averageCombatPing - averagePingNoCombat);

                        this.averageCombatFlying = (int) averageCombatPing;
                        this.averageFlying = (int) averagePingNoCombat;
                    }
                }
            }
        }
    }
}