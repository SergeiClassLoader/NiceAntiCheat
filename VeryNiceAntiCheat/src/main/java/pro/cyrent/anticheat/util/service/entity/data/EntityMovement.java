package pro.cyrent.anticheat.util.service.entity.data;

import lombok.Data;

@Data
public class EntityMovement {
    private final double x, y, z;
    private final Type type;

    enum Type {
        RELATIVE,
        ABSOLUTE,
        NONE;
    }
}