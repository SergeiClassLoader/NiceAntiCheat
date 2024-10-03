package pro.cyrent.anticheat.api.database.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BanData {
    private final String username;
    private final String currentDate;

    private final String banReason;
}
