package pro.cyrent.anticheat.util.thread;

import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.bukkit.RunUtils;
import pro.cyrent.anticheat.util.math.MathUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
public class ThreadManager {
    private final int threads = Runtime.getRuntime().availableProcessors();
    private final int maxThreads = 15;
    private final List<Thread> userThreads = new CopyOnWriteArrayList<>();

    public void update() {
        //
    }

    public void shutdownThread(PlayerData user) {
        user.getThread().count--;

        if (user.getThread().getCount() < 1) {
            user.getThread().getExecutorService().shutdownNow();
            this.userThreads.remove(user.getThread());
        }
    }

    public Thread generate() {

        int size = this.userThreads.size();

        if (size > (maxThreads - 1)) {
            Thread randomThread = this.getUserThreads()
                    .stream()
                    .min(Comparator.comparing(Thread::getCount))
                    .orElse(MathUtil.randomElement(this.getUserThreads()));

            if (randomThread == null) return null;

            randomThread.count++;
            return randomThread;
        } else {
            Thread thread = new Thread(
                    Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                            .setNameFormat("Lumos Player Thread " + this.userThreads.size() + 1)
                            .setUncaughtExceptionHandler((t, e) -> RunUtils.task(e::printStackTrace))
                            .build())
            );

            this.userThreads.add(thread);
            return thread;
        }
    }


    public ExecutorService generateServiceScheduledCored() {
        return Executors.newFixedThreadPool(this.threads);
    }

    public ScheduledExecutorService generateServiceScheduled() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}