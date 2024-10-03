package pro.cyrent.anticheat.util.block.wrap;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class BlockResultNew {
    private boolean collidingHorizontally, serverGround, blockAbove;
}
