package pro.cyrent.anticheat.api.check.impl.movement.scaffold;


import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;


@CheckInformation(
        name = "Scaffold",
        subName = "I",
        checkType = CheckType.MOVEMENT,
        checkNameEnum = CheckName.SCAFFOLD,
        description = "Detects invalid block direction placements",
        punishable = false,
        state = CheckState.PRE_ALPHA)
public class ScaffoldI extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isPlace()) {
            WrapperPlayClientPlayerBlockPlacement placement =
                    new WrapperPlayClientPlayerBlockPlacement(event.getPacketReceiveEvent());

            if (placement.getBlockPosition() == null) return;

            final Location blockLocation = new Location(getData().getPlayer().getWorld(),
                    placement.getBlockPosition().getX(),
                    placement.getBlockPosition().getY(),
                    placement.getBlockPosition().getZ());

            final int face = placement.getFace().getFaceValue();

            Material block = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(getData().getPlayer().getWorld(),
                    placement.getBlockPosition().getX(),
                    placement.getBlockPosition().getY(),
                    placement.getBlockPosition().getZ());

            if (block == null
                    || getData().getBlockAgainst() == null
                    || getData().getBlockAgainst().getType() == null
                    || getData().getBlockAgainst().getType().name().contains("door")
                    || getData().getBlockAgainst().getType().name().contains("slab")
                    || getData().getBlockAgainst().getType().name().contains("stair")
                    || getData().getBlockAgainst().getType().name().contains("fence")
                    || !getData().getBlockAgainst().getType().isBlock()
                    || getData().getPlayer().getItemInHand().getType().name().contains("slab")
                    || getData().getPlayer().getItemInHand().getType().name().contains("stair")
                    || getData().getPlayer().getItemInHand().getType().name().contains("fence")
                    || getData().getPlayer().getItemInHand().getType().name().contains("wall")
                    || getData().getPlayer().getItemInHand() == null
                    || getData().getPlayer().getItemInHand().getType() == null
                    || !getData().getPlayer().getItemInHand().getType().isBlock()) {
                return;
            }

            boolean halfBlock = false;

            switch (block) {
                case THIN_GLASS:
                case IRON_FENCE:
                case FENCE_GATE:
                case WALL_SIGN:
                case FENCE:
                case SIGN_POST:
                case SIGN:
                case NETHER_FENCE:
                case STONE_SLAB2:
                case DOUBLE_STONE_SLAB2:
                case COBBLE_WALL: {
                    halfBlock = true;
                    break;
                }
            }

            if (block.isSolid() && !halfBlock && getData().getScaffoldProcessor().getScaffoldTimerJump().getDelta() < 7) {
                final double x = getData().getMovementProcessor().getTo().getPosX();
                final double y = getData().getMovementProcessor().getTo().getPosY();
                final double z = getData().getMovementProcessor().getTo().getPosZ();

                if (y - blockLocation.getY() > 0.45) {
                    final Location location = new Location(getData().getPlayer().getWorld(),
                            x, y + getData().getPlayer().getEyeHeight(), z);

                    if (!this.interactedCorrectly(blockLocation, location, face)) {
                        if (++this.threshold > 3) {
                            this.fail("Invalid direction face to the block");
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .1);
                    }
                }
            }
        }
    }


    private boolean interactedCorrectly(final Location blockLoc, final Location playerLoc, final int face) {
        switch (face) {
            case 0: {
                final double limit = blockLoc.getY() - 0.03;
                return playerLoc.getY() < limit;
            }
            case 1: {
                return true;
            }
            case 2: {
                final double limit = blockLoc.getZ() + 0.03;
                return playerLoc.getZ() < limit;
            }
            case 3: {
                final double limit = blockLoc.getZ() - 0.03;
                return playerLoc.getZ() > limit;
            }
            case 4: {
                final double limit = blockLoc.getX() + 0.03;
                return limit > playerLoc.getX();
            }
            case 5: {
                final double limit = blockLoc.getX() - 0.03;
                return playerLoc.getX() > limit;
            }
            default: {
                return true;
            }
        }
    }
}
