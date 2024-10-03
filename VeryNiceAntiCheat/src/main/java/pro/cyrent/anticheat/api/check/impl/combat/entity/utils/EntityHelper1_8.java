package pro.cyrent.anticheat.api.check.impl.combat.entity.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityPlayer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class EntityHelper1_8 {

    public EntityPlayer entityPlayer;
    public int entityID;

    public int entityZombieID;
    public double zombieStartYaw;
    public double movementIncrease;
    public double lastZombieSpawn;
    public boolean zombieAttacked;
    public int zombieAttackTimes, validRaycastTicks;
    public long resetTime = 5;
    public int targetEntity;
    public CustomEntity lastEntityBot;

    public int raycastVisableCheckTicks, visableRaycastCheckTicks,
            raycastTicks, raycastVisableTicks, moveUpTicks;
    public boolean raycastCheckEntity;

    public double visableStartYaw, visableIncresement;
    public boolean visableReverse;

    private final List<CustomEntity> customEntities = new CopyOnWriteArrayList<>();

    @Getter @AllArgsConstructor
    public static final class RaycastBot {
        private final Entity entityZombie;
        private final double offset;
        private final int count;
        private final double y, x, z;
    }

    public static class RaycastEntity {

        @Getter @Setter
        private Entity entityBlock;

        @Getter @Setter
        private Direction direction;

        @Getter @Setter
        private int entityID;

        public RaycastEntity(Entity entityBlock, Direction direction) {
            this.entityBlock = entityBlock;
            this.direction = direction;
            this.entityID = entityBlock.getId();
        }

        public enum Direction {
            RIGHT, LEFT
        }
    }
}
