package pro.cyrent.anticheat.util.block.collide;

import pro.cyrent.anticheat.util.block.box.BoundingBox;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CollideEntryV2 {
    private final WrappedBlockState block;
    private final BoundingBox boundingBox;
    private final BoundingBox blockBox;
}