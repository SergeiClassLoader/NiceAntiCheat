package pro.cyrent.anticheat.util.thread;

import lombok.Getter;

import java.util.concurrent.ExecutorService;

@Getter
public class Thread {
    private final ExecutorService executorService;
    public int count;

    public Thread(ExecutorService executorService) {
        this.executorService = executorService;
        this.count++;
    }

    public void execute(Runnable runnable) {
        this.executorService.execute(runnable);
    }
}