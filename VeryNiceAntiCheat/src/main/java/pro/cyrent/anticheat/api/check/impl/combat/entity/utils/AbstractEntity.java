package pro.cyrent.anticheat.api.check.impl.combat.entity.utils;


import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.location.FlyingLocation;

public abstract class AbstractEntity {

    public abstract void createEntity(PlayerData user, PlayerData forcedUser, EntityData.EntityType entityType, EntityData entityData);

    public abstract void removeEntity(PlayerData user, PlayerData forcedUser);

    public abstract void tickEntity(PlayerData user, PlayerData forcedUser, FlyingLocation customLocation, boolean onGround);
}
