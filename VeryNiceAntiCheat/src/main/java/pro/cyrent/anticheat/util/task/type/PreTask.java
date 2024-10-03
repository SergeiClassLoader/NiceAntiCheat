package pro.cyrent.anticheat.util.task.type;

import pro.cyrent.anticheat.util.task.Task;

public interface PreTask extends Task {

    void handlePreTick();
}
