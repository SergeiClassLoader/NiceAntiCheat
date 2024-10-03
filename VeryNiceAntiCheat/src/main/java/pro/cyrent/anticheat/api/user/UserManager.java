package pro.cyrent.anticheat.api.user;

import pro.cyrent.anticheat.Anticheat;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class UserManager {
    private final Map<UUID, PlayerData> userMap = new ConcurrentHashMap<>();

    public void addUser(Player player) {
        UUID uuid = player.getUniqueId();
        userMap.computeIfAbsent(uuid, key -> new PlayerData(player));
    }

    public PlayerData getUser(Player player) {
        return this.userMap.get(player.getUniqueId());
    }

    public PlayerData getUser(UUID player) {
        return this.userMap.get(player);
    }

    public void removeUser(Player player) {
        PlayerData playerData = Anticheat.INSTANCE.getUserManager().getUser(player);

        if (playerData != null) {
            Anticheat.INSTANCE.getThreadManager().shutdownThread(playerData);
            this.userMap.remove(player.getUniqueId());
        }
    }

    public void removeUserID(UUID player) {
        PlayerData playerData = Anticheat.INSTANCE.getUserManager().getUser(player);

        if (playerData != null) {
            Anticheat.INSTANCE.getThreadManager().shutdownThread(playerData);
            this.userMap.remove(player);
        }
    }
}