package pro.cyrent.anticheat.api.database.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class InputData {
    private final String UUID, playerName, checkName, checkType;
    private final double violation;
    private final boolean experimental;

    private final int ping;

    private final String checkData;

    private final String currentDate, loginDate, loginTime, banWave, license;
}
