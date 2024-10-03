package pro.cyrent.anticheat.util.ray;

import java.util.List;

public interface CollisionBox {
    boolean isCollided(CollisionBox other);
    CollisionBox copy();
    CollisionBox offset(double x, double y, double z);
    void downCast(List<SimpleCollisionBox> list);
    boolean isNull();
}