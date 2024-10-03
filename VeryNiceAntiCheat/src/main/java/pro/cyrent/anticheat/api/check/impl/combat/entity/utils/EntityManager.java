package pro.cyrent.anticheat.api.check.impl.combat.entity.utils;

import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManager {

    @Getter
    private final Map<UUID, CustomEntity> entityMap = new ConcurrentHashMap<>();

    public CustomEntity getEntity(int id) {

        for (CustomEntity customEntity : this.entityMap.values()) {
            if (customEntity.getEntityID() != id || !customEntity.isActive()) continue;

            return customEntity;
        }

        return null;
    }

    public void removeAll(PlayerData user, PlayerData forcedUser) {

        if (user == null || user.getEntityHelper1_8() == null
                || user.getEntityHelper1_8().getCustomEntities().size() < 1
                || user.getCurrentSpawnedEntites() == null) return;

        user.getEntityHelper1_8().getCustomEntities().forEach(customEntity -> {

            if (customEntity == null || customEntity.getCreationData() == null
                    || customEntity.getCreationData().getEntityType() == null) return;

            customEntity.removeEntity(user, forcedUser);

            user.getCurrentSpawnedEntites().stream().filter(integer -> integer ==
                    customEntity.getEntityID()).forEach(integer ->
                    user.getCurrentSpawnedEntites().remove(integer));

            this.entityMap.remove(customEntity.getUuid());
            user.getEntityHelper1_8().getCustomEntities().remove(customEntity);
        });
    }

    public void remove(PlayerData user, PlayerData forcedUser, EntityData.EntityType entityType) {

        if (user.getEntityHelper1_8().getCustomEntities().size() == 0) return;

        StreamUtil.filter(user.getEntityHelper1_8().getCustomEntities(), customEntity ->
                customEntity.getCreationData().getEntityType() != null &&
                customEntity.getCreationData().getEntityType() == entityType).forEach(customEntity -> {
            customEntity.removeEntity(user, forcedUser);

            user.getCurrentSpawnedEntites().stream().filter(integer -> integer ==
                    customEntity.getEntityID()).forEach(integer ->
                    user.getCurrentSpawnedEntites().remove(integer));

            this.entityMap.remove(customEntity.getUuid());
            user.getEntityHelper1_8().getCustomEntities().remove(customEntity);
        });
    }

    public CustomEntity getEntity(UUID uuid) {
        return this.entityMap.get(uuid);
    }

    public CustomEntity createEntity(EntityData.EntityType entityType, PlayerData user, PlayerData forcedUser, EntityData entityData) {
        UUID randomUUID = UUID.randomUUID();

        CustomEntity customEntity = new EntityHook18(user, forcedUser, randomUUID, entityData);

        customEntity.createEntity(user, forcedUser, entityType, entityData);

        this.entityMap.put(randomUUID, customEntity);
        return customEntity;
    }
}
