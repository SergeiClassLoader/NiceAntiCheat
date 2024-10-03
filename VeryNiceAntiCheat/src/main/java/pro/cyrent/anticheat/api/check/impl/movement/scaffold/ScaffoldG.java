package pro.cyrent.anticheat.api.check.impl.movement.scaffold;


import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.block.box.HydroBB;
import pro.cyrent.anticheat.util.block.box.HydroMovingPosition;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.math.VectorUtils;
import pro.cyrent.anticheat.util.vec.Vec3;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.nio.Buffer;
import java.util.Arrays;

@CheckInformation(
        name = "Scaffold",
        subName = "G",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.SCAFFOLD,
        description = "Detects if the player places too many blocks in a second",
        experimental = true,
        punishable = false,
        state = CheckState.PRE_ALPHA)
public class ScaffoldG extends Check {

    private double threshold;
    private int blocksPlaced;
    private long lastTiming = System.currentTimeMillis();

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            if (getData().getScaffoldProcessor().getScaffoldTimerJump().getDelta() < 7) {

                if (getData().getBlockProcessor().getLastConfirmedBlockPlaceTimer().getDelta() < 2) {
                    this.blocksPlaced++;
                }

                if ((System.currentTimeMillis() - this.lastTiming) > 1000L) {
                    this.lastTiming = System.currentTimeMillis();
                    this.blocksPlaced = 0;
                }

                if (this.blocksPlaced >= 10) {
                    if (++this.threshold > 2.5) {
                        this.fail(
                                "blocksPlaced=" + this.blocksPlaced);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .05);
                }
            }
        }
    }
}
