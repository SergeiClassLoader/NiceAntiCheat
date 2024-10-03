package pro.cyrent.anticheat.api.database;

import pro.cyrent.anticheat.api.database.api.InputData;

import java.util.List;

public interface DatabaseInterface {
    void addViolation(InputData inputData);
    void initManager();
    void shutdown();
    List<InputData> getLogs(String playerName);
    boolean isSetup();
    void dropReset();
}
