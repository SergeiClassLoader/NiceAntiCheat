package pro.cyrent.anticheat.util.block;


import lombok.AllArgsConstructor;
import lombok.Getter;

import org.bukkit.Material;
import pro.cyrent.anticheat.util.nms.BoundingBox;

@Getter @AllArgsConstructor
public class PreScannedBoxes {
    private final BoundingBox boundingBox;
    private final Material material;
    private final Object axisAlignedBB;
}
