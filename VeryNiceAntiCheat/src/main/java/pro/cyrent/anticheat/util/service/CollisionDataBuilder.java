package pro.cyrent.anticheat.util.service;

import pro.cyrent.anticheat.api.user.PlayerData;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;

public interface CollisionDataBuilder {

    CollisionBox fetch(PlayerData data, ClientVersion version, WrappedBlockState block, int x, int y, int z);
}