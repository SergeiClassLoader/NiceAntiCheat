package pro.cyrent.anticheat.api.database.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class ClientData {
    private final String license;
    private final String currentDate;

    private final String valid;
    private final String serverIP;
}
