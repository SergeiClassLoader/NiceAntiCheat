package pro.cyrent.anticheat.util.version;


import pro.cyrent.anticheat.api.user.PlayerData;

public interface IVersion {

    int getClientVersion(PlayerData user);

    void onLoad();
}
