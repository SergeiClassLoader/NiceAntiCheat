package pro.cyrent.anticheat.api.check.impl.misc.ghosthand;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.block.BlockUtil;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.material.Bed;
import pro.cyrent.anticheat.api.check.*;

import java.util.LinkedList;
import java.util.List;

@CheckInformation(
        name = "GhostHand",
        checkType = CheckType.MISC,
        checkNameEnum = CheckName.GHOST_HAND,
        description = "Detects if the player breaks a bed/dragon egg through walls",
        punishable = false,
        experimental = true,
        state = CheckState.RELEASE)
public class GhostHandA extends Check {

    private final org.bukkit.block.BlockFace[] blockFaces = {
            org.bukkit.block.BlockFace.EAST,
            org.bukkit.block.BlockFace.WEST,
            org.bukkit.block.BlockFace.NORTH,
            org.bukkit.block.BlockFace.SOUTH
    };

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() == null) return;

        if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event.getPacketReceiveEvent());

            if (digging.getBlockPosition() == null) return;

            double x = digging.getBlockPosition().getX();
            double y = digging.getBlockPosition().getY();
            double z = digging.getBlockPosition().getZ();

            // xyz to location
            Location location = new Location(getData().getPlayer().getWorld(), x, y, z);

            // get the blocks material
            Material material = Anticheat.INSTANCE.getInstanceManager().getInstance()
                    .getType(getData().getPlayer().getWorld(), location.getBlockX(),
                    location.getBlockY(), location.getBlockZ());

            // is it valid
            if (this.isBlockValid(material)) {

                // get the eye location
                Location eyeLocation = new Location(getData().getPlayer().getWorld(), x,
                        y + getData().getPlayer().getEyeHeight(), z);

                // did they incorrectly break the block based of the eye locations found face / direction
                if (!this.incorrectFace(location, eyeLocation, digging.getBlockFace())) {

                    // check for invalid faces of blocks
                    Block current = location.add(0, 1, 0).getBlock();

                    for (org.bukkit.block.BlockFace blockFace : this.blockFaces) {
                        Block a = current.getRelative(blockFace);
                        Block b = current.getRelative(blockFace).getRelative(org.bukkit.block.BlockFace.UP);

                        Material bX = Anticheat.INSTANCE.getInstanceManager().getInstance()
                                .getType(getData().getPlayer().getWorld(), a.getX(), a.getY(), a.getZ());

                        Material bZ = Anticheat.INSTANCE.getInstanceManager().getInstance()
                                .getType(getData().getPlayer().getWorld(), b.getX(), b.getY(), b.getZ());

                        if (bX != Material.AIR && bZ != Material.AIR) {
                            return;
                        }
                    }

                    // check if target block is the head block of a bed
                    if (material == Material.BED_BLOCK) {
                        Block block = new Location(getData().getPlayer().getWorld(), x, y, z).getBlock();
                        Bed bed = (Bed) block.getState().getData();

                        Location toCheck;

                        // checks for head of bed block
                        if (bed.isHeadOfBed()) {
                            toCheck = block.getRelative(bed.getFacing().getOppositeFace()).getLocation();
                        } else {
                            toCheck = block.getRelative(bed.getFacing()).getLocation();
                        }

                        if (StreamUtil.anyMatch(this.hasBlocksAround(getData(),
                                        toCheck.getX(), toCheck.getY(), toCheck.getZ()),
                                this::isValidNearBlock)) {
                            return;
                        }
                    }

                    // check if blocks above
                    Material blockAbove = Anticheat.INSTANCE.getInstanceManager().getInstance()
                            .getType(getData().getPlayer().getWorld(), x, y + 1, z);

                    if (this.isValidNearBlock(blockAbove)) {
                        return;
                    }

                    // check if air is near the block
                    if (this.isIgnoredBlocks(getData(), x, y, z, location)) {
                        return;
                    }

                    // check for any dupe blocks

                    int sex = 0;

                    for (Material blocks : this.getNearByBlocks(getData(),
                            new Location(getData().getPlayer().getWorld(), x, y, z))) {

                        switch (blocks) {
                            case BED_BLOCK:
                            case DRAGON_EGG: {
                                sex++;
                                break;
                            }
                        }
                    }

                    if (sex > 2) return;

                    this.fail("face="+digging.getBlockFace().name(),
                            "block="+material.name());
                }
            }
        }
    }

    boolean isIgnoredBlocks(PlayerData user, double x, double y, double z, Location location) {

        return StreamUtil.anyMatch(this.getSurroundingBlocks(user, location), this::invalidNormalBlock)
                || StreamUtil.anyMatch(this.hasBlocksAround(user, x, y, z), this::isValidNearBlock);
    }

    List<Material> hasBlocksAround(PlayerData user, double x, double y, double z) {
        List<Material> blocks = new LinkedList<>();

        Material x1 = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(user.getPlayer().getWorld(), x + 1, y, z);
        Material x2 = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(user.getPlayer().getWorld(), x + 1, y, z);
        Material x3 = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(user.getPlayer().getWorld(), x - 1, y, z);
        Material x4 = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(user.getPlayer().getWorld(), x + 1, y, z);

        Material z1 = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(user.getPlayer().getWorld(), x, y, z + 1);
        Material z2 = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(user.getPlayer().getWorld(), x, y, z + 1);
        Material z3 = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(user.getPlayer().getWorld(), x, y, z - 1);
        Material z4 = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(user.getPlayer().getWorld(), x, y, z + 1);

        blocks.add(x1);
        blocks.add(x2);
        blocks.add(x3);
        blocks.add(x4);

        blocks.add(z1);
        blocks.add(z2);
        blocks.add(z3);
        blocks.add(z4);

        return blocks;
    }

    boolean isBlockValid(Material material) {

        switch (material) {
            case BED_BLOCK:
            case BED:
            case DRAGON_EGG: {
                return true;
            }
        }

        return false;
    }

    boolean incorrectFace(Location blockLoc, Location playerLoc, BlockFace face) {
        switch (face) {
            case UP: {
                return true;
            }

            case DOWN: {
                double limit = blockLoc.getY() - 0.03;
                return playerLoc.getY() < limit;
            }

            case WEST: {
                double limit = blockLoc.getX() + 0.03;
                return limit > playerLoc.getX();
            }

            case EAST: {
                double limit = blockLoc.getX() + 1 - 0.03;

                return playerLoc.getX() > limit;
            }

            case NORTH: {
                double limit = blockLoc.getZ() + 0.03;
                return playerLoc.getZ() < limit;
            }

            case SOUTH: {
                double limit = blockLoc.getZ() + 1 - 0.03;

                return playerLoc.getZ() > limit;
            }

            default:
                return true;
        }
    }

    List<Material> getSurroundingBlocks(PlayerData user, Location cloned) {
        List<Material> blocks = new LinkedList<>();

        double locationX = cloned.getX();
        double locationY = cloned.getY();
        double locationZ = cloned.getZ();

        double expand = .3;

        for (double x = -expand; x <= expand; x++) {
            for (double z = -expand; z <= expand; z++) {

                cloned.setX(locationX + x);
                cloned.setZ(locationZ + z);

                cloned.setY(locationY + 1D);
                Material blockAbove = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(cloned.getWorld(),
                        cloned.getBlockX(),
                        cloned.getBlockY(), cloned.getBlockZ());

                cloned.setY(locationY);
                Material blockCenter = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(cloned.getWorld()
                        , cloned.getBlockX(),
                        cloned.getBlockY(), cloned.getBlockZ());

                cloned.setY(locationY - 1D);
                Material blockBelow = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(cloned.getWorld(),
                        cloned.getBlockX(),
                        cloned.getBlockY(), cloned.getBlockZ());

                if (blockAbove == null || blockCenter == null || blockBelow == null) break;

                blocks.add(blockAbove);
                blocks.add(blockCenter);
                blocks.add(blockBelow);
            }
        }

        return blocks;
    }

    boolean isValidNearBlock(Material material) {
        return !material.isBlock() || !material.isSolid() || BlockUtil.isFence(material)
                || this.invalidNormalBlock(material);
    }

    boolean invalidNormalBlock(Material material) {
        switch (material) {
            case COBBLE_WALL:
            case SIGN_POST:
            case WALL_BANNER:
            case ANVIL:
            case SAPLING:
            case FLOWER_POT:
            case YELLOW_FLOWER:
            case BROWN_MUSHROOM:
            case HUGE_MUSHROOM_1:
            case HUGE_MUSHROOM_2:
            case TORCH:
            case DAYLIGHT_DETECTOR:
            case DAYLIGHT_DETECTOR_INVERTED:
            case REDSTONE_COMPARATOR:
            case RAILS:
            case ACTIVATOR_RAIL:
            case POWERED_RAIL:
            case DETECTOR_RAIL:
            case CHEST:
            case GOLD_PLATE:
            case IRON_PLATE:
            case WOOD_PLATE:
            case STONE_PLATE:
            case ENDER_CHEST:
            case TRAPPED_CHEST:
            case LADDER:
            case VINE:
            case PISTON_BASE:
            case PISTON_EXTENSION:
            case PISTON_STICKY_BASE:
            case PISTON_MOVING_PIECE:
            case ENCHANTMENT_TABLE:
            case ENDER_PORTAL_FRAME:
            case LONG_GRASS:
            case THIN_GLASS:
            case STAINED_GLASS_PANE:
            case STAINED_GLASS:
            case CARPET:
            case CAKE:
            case CAKE_BLOCK:
            case DEAD_BUSH:
            case CACTUS:
            case WALL_SIGN: {
                return true;
            }
        }

        return BlockUtil.isStair(material) || BlockUtil.isSlab(material);
    }

    List<Material> getNearByBlocks(PlayerData user, Location location) {
        List<Material> materials = new LinkedList<>();

        World world = user.getPlayer().getWorld();
        int radius = 2;

        for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                materials.add(Anticheat.INSTANCE.getInstanceManager().getInstance()
                        .getType(world, x, location.getY(), z));
            }
        }

        return materials;
    }
}