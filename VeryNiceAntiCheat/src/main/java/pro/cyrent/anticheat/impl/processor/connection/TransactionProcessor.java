package pro.cyrent.anticheat.impl.processor.connection;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.impl.misc.timer.TimerA;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.impl.processor.connection.backtrack.ConnectionProcessor;
import pro.cyrent.anticheat.util.cache.CachedSizeConcurrentLinkedQueue;
import pro.cyrent.anticheat.util.evicting.EvictingList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


@Getter
@Setter
public class TransactionProcessor extends Event {
    private final PlayerData data;

    // queues for transactions & keep alives.
    private final Queue<TransactionEntry> transactionQueue = new ConcurrentLinkedQueue<>();
    private final Deque<KeepAliveEntry> keepAliveQueue = new EvictingList<>(100);

    private final Map<Short, Long> queuedTransactionsMap = new ConcurrentHashMap<>();

    private long lastKeepAliveTimeStamp;

    // tasks for pre & post
    private final Map<Short, List<Runnable>> tasks = new ConcurrentHashMap<>();

    //Timer A queue & timer info.
    public final Queue<TimerTransactionEntry> timerTransactionEntryLinkedDeque = new CachedSizeConcurrentLinkedQueue<>();
    public long lastPlayerNanoTime, lastPlayerClock;

    // keep alive ping
    private int keepAlivePing = 0;

    // transaction ping ticks
    private int pingTicks;

    // last movement confirmation difference tick
    private int lastConfirmationTick;

    private int flyingTick, realFlyingTick;

    // transaction shit
    public long transactionPing, lastTransactionPing, transactionPingDrop, postTransactionPing;
    private long lastTransactionTimeStamp;


    //verboses:
    public double lastSentTransaction;
    public double fixAbuse;
    public double pingFixThreshold;

    public TransactionProcessor(PlayerData user) {
        this.data = user;
        this.lastTransactionTimeStamp = System.currentTimeMillis();
        this.lastKeepAliveTimeStamp = System.currentTimeMillis();
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {

            if (event.isMovement()) {
                this.realFlyingTick = getData().getServerTick();
                this.lastConfirmationTick++;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {

                WrapperPlayClientWindowConfirmation wrapper =
                        new WrapperPlayClientWindowConfirmation(event.getPacketReceiveEvent());

                short id = wrapper.getActionId();

                this.queuedTransactionsMap.remove(id);

                if (wrapper.getWindowId() == 0) {
                    TimerA timerA = (TimerA) getData().getCheckManager().forClass(TimerA.class);

                    TimerTransactionEntry entry;

                    for (TimerTransactionEntry timerTransactionEntry : this.timerTransactionEntryLinkedDeque) {
                        if (timerTransactionEntry.getAction() == id) {

                            int loop = 0;
                            long nanoTime = System.nanoTime();

                            do {
                                entry = this.timerTransactionEntryLinkedDeque.poll();
                                if (entry == null || loop++ > 3000) break;

                                this.lastPlayerNanoTime = (nanoTime - entry.getNanotime());
                                this.lastPlayerClock = entry.getNanotime();

                                if (timerA != null && timerA.isEnabled()) {
                                    timerA.handleTransaction(timerTransactionEntry);
                                }

                                // timerD.handleTransaction(timerTransactionEntry);
                            } while (entry.getAction() != id);
                            break;
                        }
                    }
                }

                if (this.tasks.containsKey(id)) {
                    List<Runnable> taskList = this.tasks.remove(id);
                    for (Runnable task : taskList) {
                        task.run();
                    }
                }

                if (id < 0 || id > 10000) {
                    // Make sure the queue is not empty.
                    if (!this.transactionQueue.isEmpty()) {
                        // Get the peek as we don't want to remove it unless its confirmed.
                        TransactionEntry entry = this.transactionQueue.peek();

                        // Check the entry isn't null and matched the id.
                        if (entry != null) {

                            if (entry.getAction() == id) {

                                // Poll the queue.
                                TransactionEntry poll = this.transactionQueue.poll();

                                // Check the poll isn't null.
                                if (poll != null) {
                                    // Set the keep alive ping.
                                    this.lastTransactionPing = this.transactionPing;
                                    this.transactionPing = (int) (event.getTimestamp() - poll.timestamp);

                                    if (id > 100) {
                                        this.postTransactionPing = (int) (event.getTimestamp() - poll.timestamp);
                                    }

                                    this.lastTransactionTimeStamp = System.currentTimeMillis();
                                    this.transactionPingDrop = Math.abs(this.lastTransactionPing - this.transactionPing);
                                    this.pingTicks = (int) ((this.transactionPing / 50.0) + 1);
                                    this.lastConfirmationTick = 0;
                                    this.flyingTick = getData().getServerTick();
                                }
                            } else if (!getData().generalCancel()
                                    && Anticheat.INSTANCE.getConnectionValues().isOutOfOrder()) {
                                getData().kickPlayer("Sending transactions ids out of order. " +
                                        "id=" + id + ", " + "peek=" + entry.getAction());

                            }
                        }
                    }
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
                WrapperPlayClientKeepAlive keepAlive = new WrapperPlayClientKeepAlive(event.getPacketReceiveEvent());

                // Keep alives are ran on different thread then transactions so the id's can go out of order.
                if (!this.keepAliveQueue.isEmpty()) {

                    for (KeepAliveEntry entry : this.keepAliveQueue) {

                        if (keepAlive.getId() == entry.id) {
                            // The IDs match, remove the ID from the queue
                            this.keepAlivePing = (int) (event.getTimestamp() - entry.timestamp);
                            this.keepAliveQueue.remove(entry);

                            // THIS IS A BUNGEECORD BUG WHERE IT WILL NOT SEND KEEP ALIVES AGAIN IF ONE IS CANCELED!
                            // store this to get the timestamp for when a player cheats because
                            // the keep alive stops getting sent if the ids go out of range.
                            this.lastKeepAliveTimeStamp = System.currentTimeMillis();
                            break;
                        }
                    }
                }
            }
        }

        // Server Packets.
        if (event.getPacketSendEvent() != null) {

            // Server Keep Alive.
            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.KEEP_ALIVE) {
                WrapperPlayServerKeepAlive keepAlive = new WrapperPlayServerKeepAlive(event.getPacketSendEvent());
                this.keepAliveQueue.add(new KeepAliveEntry(keepAlive.getId(), System.currentTimeMillis()));
            }

            // Server transactions.
            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION) {
                WrapperPlayServerWindowConfirmation confirmation =
                        new WrapperPlayServerWindowConfirmation(event.getPacketSendEvent());

                short id = confirmation.getActionId();

                // Ignore inventory IDs.
                if (id < 0 || id > 10000) {
                    this.transactionQueue.add(new TransactionEntry(id,
                            event.getTimestamp()));
                    this.queuedTransactionsMap.put(id, event.getTimestamp());
                }

                if (id < -10000) {
                    // Add the timer a deque when less than 0 because i don't trust the post ids.
                    this.timerTransactionEntryLinkedDeque.add(new
                            TimerTransactionEntry(id, event.getTimestamp(),
                            System.nanoTime()));
                }
            }
        }
    }

    public void sendPacket(PacketWrapper<?> packet, Player player) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public void sendPacket(Object packet, Player player) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, (PacketWrapper<?>) packet);
    }

    public void sendPacket(PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(getData().getPlayer(), packet);
    }

    public void sendPacket(Object packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(getData().getPlayer(), (PacketWrapper<?>) packet);
    }

    public void sendPacketSilently(Object packet, Player player) {
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, (PacketWrapper<?>) packet);
    }

    public void confirmPre(Runnable runnable) {
        short current = Anticheat.INSTANCE.getTaskManager().getPreGen().currentTransaction;

        if (!this.tasks.containsKey(current)) {
            Object pre = Anticheat.INSTANCE.getTaskManager().getNextConfirmation();
            current = Anticheat.INSTANCE.getTaskManager().getId();
            Anticheat.INSTANCE.getInstanceManager().getInstance().sendPacket(pre, getData());
        }

        this.tasks.computeIfAbsent(current, k -> new ArrayList<>()).add(runnable);
    }

    public void confirmPost(Runnable runnable) {
        short current = (short) Math.abs(Anticheat.INSTANCE.getTaskManager().getId());

        if (!this.tasks.containsKey(current)) {
            Object post = Anticheat.INSTANCE.getTaskManager().getNextPostConfirmation();
            current = (short) Math.abs(Anticheat.INSTANCE.getTaskManager().getId());
            Anticheat.INSTANCE.getInstanceManager().getInstance().sendPacket(post, getData());
        }

        this.tasks.computeIfAbsent(current, k -> new ArrayList<>()).add(runnable);
    }

    public void confirmPrePost(Runnable runnable) {
        this.confirmPre(runnable);
        this.confirmPost(runnable);
    }

    @Getter @AllArgsConstructor
    public static final class TimerTransactionEntry {
        private final short action;
        private final long timestamp, nanotime;
    }

    @Getter @AllArgsConstructor
    public static final class TransactionEntry {
        private final short action;
        private final long timestamp;
    }

    @Getter @AllArgsConstructor
    public static final class KeepAliveEntry {
        private final long id;
        private final long timestamp;
    }
}