package pro.cyrent.anticheat.api.check.impl.combat.entity.utils;

import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class CustomEntity extends AbstractEntity {

    private final PlayerData user;
    private final PlayerData forcedUser;
    private final UUID uuid;

    @Setter
    private EntityData creationData;

    @Setter
    private boolean active = true;

    @Setter
    private int lastUpdateTick;

    public CustomEntity(PlayerData user, PlayerData forcedUser, UUID uuid, EntityData creationData) {
        this.user = user;
        this.uuid = uuid;
        this.forcedUser = forcedUser;
        this.creationData = creationData;
    }

    @Setter
    private int entityID;

    @Setter
    private FlyingLocation lastReportedLocation;

    @Override
    public void createEntity(PlayerData user, PlayerData forcedUser, EntityData.EntityType entityType, EntityData entityData) {

    }

    @Override
    public void removeEntity(PlayerData user, PlayerData forcedUser) {

    }

    @Override
    public void tickEntity(PlayerData user, PlayerData forcedUser, FlyingLocation customLocation, boolean onGround) {

    }
}
