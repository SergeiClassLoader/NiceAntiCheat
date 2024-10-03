package pro.cyrent.anticheat.util.task.type;

import pro.cyrent.anticheat.util.task.Task;

public interface PostTask extends Task {

    void handlePostTick();
}
