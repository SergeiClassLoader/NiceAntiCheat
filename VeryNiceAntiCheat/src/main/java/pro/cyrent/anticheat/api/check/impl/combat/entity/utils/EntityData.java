
package pro.cyrent.anticheat.api.check.impl.combat.entity.utils;

import pro.cyrent.anticheat.util.location.FlyingLocation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class EntityData {
    private FlyingLocation spawnLocation, postLocation;
    private float health;
    private boolean onGround;
    private boolean invisible;
    private EntityType entityType;
    private double offsetX, offsetY, offsetZ;

    public enum EntityType {
        BASIC,
        ADVANCED,
    }
}
