package pro.cyrent.anticheat.util.block.wrap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import pro.cyrent.anticheat.util.nms.BoundingBox;

@Getter @AllArgsConstructor
public class BoxMaterial {
    private BoundingBox boundingBox;
    private Material material;
}
