package pro.cyrent.anticheat.util.block;

import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.block.box.BoundingBox;
import pro.cyrent.anticheat.util.math.Pair;
import pro.cyrent.anticheat.util.service.CollisionBox;
import pro.cyrent.anticheat.util.service.CollisionData;
import pro.cyrent.anticheat.util.service.SimpleCollisionBox;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.enums.Half;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.*;

public class BlockUtil {
    public static Map<Material, BoundingBox> collisionBoundingBoxes;

    public BlockUtil() {
        collisionBoundingBoxes = new HashMap<>();
        setupCollisionBB();
    }


    public static boolean connectsToGlassPane(WrappedBlockState target) {
        CollisionBox targetBox = CollisionData.getData(target.getType()).getBox();

        if (targetBox == null)
            return false;

        if (targetBox.isFullBlock())
            return true;

        if (BlockTags.GLASS_PANES.contains(target.getType()))
            return true;

        return BlockTags.GLASS_BLOCKS.contains(target.getType());
    }

    public static Pair<SimpleCollisionBox, Boolean> func_176306_h(PlayerData data, ClientVersion version, WrappedBlockState block, int x, int y, int z) {
        List<Pair<SimpleCollisionBox, Boolean>> possibleValues = new ArrayList<>();

        BlockFace facing = block.getFacing();
        Half half = block.getHalf();

        float f = 0.5F;
        float f1 = 1.0F;

        if (half == Half.TOP) {
            f = 0.0F;
            f1 = 0.5F;
        }

        float f2 = 0.0F;
        float f3 = 1.0F;
        float f4 = 0.0F;
        float f5 = 0.5F;
        boolean flag1 = true;

        if (facing == BlockFace.EAST) {
            WrappedBlockState target = data.getWorldProcessor().getBlock(x + 1, y, z);

            f2 = 0.5F;
            f5 = 1.0F;

            if (BlockTags.STAIRS.contains(target.getType())
                    && target.getHalf() == block.getHalf()) {
                BlockFace targetFacing = target.getFacing();

                if (targetFacing == BlockFace.NORTH && !isSameStair(block, target)) {
                    f5 = 0.5F;
                    flag1 = false;
                } else if (targetFacing == BlockFace.SOUTH && !isSameStair(block, target)) {
                    f4 = 0.5F;
                    flag1 = false;
                }
            }
        } else if (facing == BlockFace.WEST) {
            WrappedBlockState target = data.getWorldProcessor().getBlock(x - 1, y, z);

            f3 = 0.5F;
            f5 = 1.0F;

            if (BlockTags.STAIRS.contains(target.getType())) {
                BlockFace targetFacing = target.getFacing();

                if (targetFacing == BlockFace.NORTH && !isSameStair(block, target)) {
                    f5 = 0.5F;
                    flag1 = false;
                } else if (targetFacing == BlockFace.SOUTH && !isSameStair(block, target)) {
                    f4 = 0.5F;
                    flag1 = false;
                }
            }
        } else if (facing == BlockFace.SOUTH) {
            WrappedBlockState target = data.getWorldProcessor().getBlock(x, y, z + 1);

            f4 = 0.5F;
            f5 = 1.0F;

            if (BlockTags.STAIRS.contains(target.getType()) && target.getHalf() == block.getHalf()) {
                BlockFace targetFacing = target.getFacing();

                if (targetFacing == BlockFace.WEST && !isSameStair(block, target)) {
                    f3 = 0.5F;
                    flag1 = false;
                } else if (targetFacing == BlockFace.EAST && !isSameStair(block, target)) {
                    f2 = 0.5F;
                    flag1 = false;
                }
            }
        } else if (facing == BlockFace.NORTH) {
            WrappedBlockState target = data.getWorldProcessor().getBlock(x, y, z - 1);

            if (BlockTags.STAIRS.contains(target.getType()) && target.getHalf() == block.getHalf()) {
                BlockFace targetFacing = target.getFacing();

                if (targetFacing == BlockFace.WEST && !isSameStair(block, target)) {
                    f3 = 0.5F;
                    flag1 = false;
                } else if (targetFacing == BlockFace.EAST && !isSameStair(block, target)) {
                    f2 = 0.5F;
                    flag1 = false;
                }
            }
        }

        return new Pair<>(new SimpleCollisionBox(f2, f, f4, f3, f1, f5), flag1);
    }

    public static Pair<SimpleCollisionBox, Boolean> func_176304_i(PlayerData data, ClientVersion version, WrappedBlockState block, int x, int y, int z) {
        List<Pair<SimpleCollisionBox, Boolean>> possibleValues = new ArrayList<>();

        BlockFace facing = block.getFacing();

        float f = 0.5F;
        float f1 = 1.0F;

        if (block.getHalf() == Half.TOP) {
            f = 0.0F;
            f1 = 0.5F;
        }

        float f2 = 0.0F;
        float f3 = 0.5F;
        float f4 = 0.5F;
        float f5 = 1.0F;
        boolean flag1 = false;

        if (facing == BlockFace.EAST) {
            WrappedBlockState target = data.getWorldProcessor().getBlock(x + 1, y, z);

            if (BlockTags.STAIRS.contains(target.getType()) && target.getHalf() == block.getHalf()) {
                BlockFace targetFacing = target.getFacing();

                if (targetFacing == BlockFace.NORTH && !isSameStair(block, target)) {
                    f4 = 0.0F;
                    f5 = 0.5F;
                    flag1 = true;
                } else if (targetFacing == BlockFace.SOUTH && !isSameStair(block, target)) {
                    f4 = 0.5F;
                    f5 = 1.0F;
                    flag1 = true;
                }
            }
        } else if (facing == BlockFace.WEST) {
            WrappedBlockState target = data.getWorldProcessor().getBlock(x - 1, y, z);

            if (BlockTags.STAIRS.contains(target.getType()) && target.getHalf() == block.getHalf()) {
                f2 = 0.5F;
                f3 = 1.0F;

                BlockFace targetFacing = target.getFacing();

                if (targetFacing == BlockFace.NORTH && !isSameStair(block, target)) {
                    f4 = 0.0F;
                    f5 = 0.5F;
                    flag1 = true;
                } else if (targetFacing == BlockFace.SOUTH && !isSameStair(block, target)) {
                    f4 = 0.5F;
                    f5 = 1.0F;
                    flag1 = true;
                }
            }

        } else if (facing == BlockFace.SOUTH) {
            WrappedBlockState target = data.getWorldProcessor().getBlock(x, y, z + 1);

            if (BlockTags.STAIRS.contains(target.getType()) && target.getHalf() == block.getHalf()) {
                f4 = 0.0F;
                f5 = 0.5F;

                BlockFace targetFacing = target.getFacing();

                if (targetFacing == BlockFace.WEST && !isSameStair(block, target)) {
                    flag1 = true;
                } else if (targetFacing == BlockFace.EAST && !isSameStair(block, target)) {
                    f2 = 0.5F;
                    f3 = 1.0F;
                    flag1 = true;
                }
            }
        } else if (facing == BlockFace.NORTH) {
            WrappedBlockState target = data.getWorldProcessor().getBlock(x, y, z - 1);

            if (BlockTags.STAIRS.contains(target.getType()) && target.getHalf() == block.getHalf()) {
                BlockFace targetFacing = target.getFacing();

                if (targetFacing == BlockFace.WEST && !isSameStair(block, target)) {
                    flag1 = true;
                } else if (targetFacing == BlockFace.EAST && !isSameStair(block, target)) {
                    f2 = 0.5F;
                    f3 = 1.0F;
                    flag1 = true;
                }
            }
        }

        if (flag1) {
            return new Pair<>(new SimpleCollisionBox(f2, f, f4, f3, f1, f5), flag1);
        }

        return new Pair<>(null, flag1);
    }

    public static boolean isSameStair(WrappedBlockState block, WrappedBlockState target) {
        return BlockTags.STAIRS.contains(target.getType())
                && target.getHalf() == block.getHalf()
                && target.getFacing() == block.getFacing();
    }

    public static boolean canConnectToFence(WrappedBlockState block, WrappedBlockState target) {
        if (target.getType() == StateTypes.BARRIER || target.getType() == StateTypes.AIR)
            return false;

        boolean netherBrickFence = block.getType() == StateTypes.NETHER_BRICK_FENCE;

        if ((netherBrickFence && target.getType() == StateTypes.NETHER_BRICK_FENCE)
                || (!netherBrickFence && BlockTags.WOODEN_FENCES.contains(target.getType())))
            return true;

        if (BlockTags.FENCE_GATES.contains(target.getType()))
            return true;

        if (target.getType() == StateTypes.MELON
                || target.getType() == StateTypes.PUMPKIN
                || target.getType() == StateTypes.CARVED_PUMPKIN
                || target.getType() == StateTypes.JACK_O_LANTERN)
            return false;

        CollisionBox targetBox = CollisionData.getData(target.getType()).getBox();

        if (targetBox == null)
            return false;

        if (target.getType().isBlocking() && targetBox.isFullBlock())
            return true;

        return false;
    }

    public static boolean canConnectToWall(WrappedBlockState target) {
        if (target.getType() == StateTypes.BARRIER)
            return false;

        if (BlockTags.WALLS.contains(target.getType())
                || BlockTags.FENCE_GATES.contains(target.getType()))
            return true;

        if (target.getType() == StateTypes.MELON
                || target.getType() == StateTypes.PUMPKIN
                || target.getType() == StateTypes.CARVED_PUMPKIN
                || target.getType() == StateTypes.JACK_O_LANTERN)
            return false;

        CollisionBox targetBox = CollisionData.getData(target.getType()).getBox();

        if (targetBox == null)
            return false;

        return target.getType().isBlocking() && targetBox.isFullBlock();
    }

    public static boolean isFence(Material material) {

        switch (material) {
            case FENCE:
            case FENCE_GATE:
            case ACACIA_FENCE:
            case BIRCH_FENCE:
            case ACACIA_FENCE_GATE:
            case DARK_OAK_FENCE:
            case IRON_FENCE:
            case JUNGLE_FENCE:
            case BIRCH_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case NETHER_FENCE:
            case SPRUCE_FENCE:
            case SPRUCE_FENCE_GATE: {
                return true;
            }
        }

        return false;
    }

    public static boolean isCake(final Material material) {
        return material.toString().contains("CAKE");
    }

    public static boolean isAir(final Material material) {
        final String string = material.toString();
        switch (string) {
            case "AIR":
            case "CAVE_AIR":
            case "VOID_AIR": {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public static Material getBlockTypeASync(final World world, final int x, final int y, final int z) {
        if (world.isChunkLoaded(x >> 4, z >> 4)) {
            return world.getBlockAt(x, y, z).getType();
        }
        return Material.SPONGE;
    }

    public static boolean isSolid(final Material material) {
        return material.isSolid();
    }

    public static boolean isSign(final Material material) {
        return material.toString().contains("SIGN");
    }

    public static boolean isRepeater(final Material material) {
        return material.toString().equals("REPEATER") || material.toString().equals("DIODE");
    }

    public static boolean isFlowerPot(final Material material) {
        return material.toString().equals("FLOWER_POT");
    }

    public static boolean isConduit(final Material material) {
        return material.toString().equals("CONDUIT");
    }

    public static boolean isBamboo(final Material material) {
        return material.toString().contains("BAMBOO");
    }

    public static boolean isLava(final Material material) {
        return material.toString().contains("LAVA");
    }

    public static boolean isPressurePlate(final Material material) {
        return material.toString().contains("PLATE");
    }

    public static boolean isPane(final Block block) {
        return block.getType().toString().contains("PANE");
    }

    public static boolean isRail(final Material material) {
        return material.toString().contains("RAIL");
    }

    public static boolean isSolidGlass(final Material material) {
        return material.toString().contains("GLASS") && !material.toString().contains("PANE");
    }

    public static boolean isPane(final Material material) {
        return material.toString().contains("PANE");
    }

    public static boolean isPowderSnow(final Material material) {
        return material.toString().equals("POWDER_SNOW");
    }

    public static boolean isSeaGrass(final Material material) {
        return material.toString().contains("GRASS") && material.toString().contains("SEA");
    }

    public static boolean isKelp(final Material material) {
        return material.toString().contains("KELP");
    }

    public static boolean isAmethyst(final Material material) {
        return material.toString().contains("AMETHYST");
    }

    public static boolean isDripstone(final Material material) {
        return material.toString().contains("DRIPSTONE");
    }

    public static boolean isShulkerBox(final Material material) {
        return material.toString().contains("SHULKER");
    }

    public static boolean isEndRod(final Material material) {
        return material.toString().equals("END_ROD");
    }

    public static boolean isCactus(final Material material) {
        return material.toString().equalsIgnoreCase("CACTUS");
    }

    public static boolean isCauldron(final Material material) {
        return material.toString().equals("CAULDRON");
    }

    public static boolean isFenceGate(final Material material) {
        return material.toString().contains("GATE");
    }

    public static boolean isGrowable(final Material material) {
        return material.toString().contains("GROWABLE");
    }

    public static boolean isBerryBush(final Material material) {
        return material.toString().equalsIgnoreCase("SWEET_BERRY_BUSH");
    }

    public static boolean isPiston(final Material material) {
        return material.toString().contains("PISTON");
    }

    public static boolean isDoor(final Material material) {
        return material.toString().contains("DOOR");
    }

    public static boolean isHopper(final Material material) {
        return material.toString().equals("HOPPER");
    }

    public static boolean isChain(final Block block) {
        return block.getType().toString().contains("CHAIN");
    }

    public static boolean isChain(final Material material) {
        return material.toString().equals("CHAIN");
    }

    public static boolean isClimbable(final Block block) {
        return block.getType().toString().contains("LADDER") || block.getType().toString().contains("VINE") || block.getType().toString().equalsIgnoreCase("SCAFFOLDING");
    }

    public static boolean isClimbable(final Material material) {
        return material.toString().equals("LADDER") || material.toString().contains("VINE") || material.toString().equals("SCAFFOLDING");
    }

    public static boolean isSnow(final Material material) {
        return material.toString().equals("SNOW");
    }

    public static boolean isLiquid(final Block block) {
        return block.getType().toString().contains("LAVA") || block.getType().toString().contains("WATER") || block.getType().toString().contains("BUBBLE");
    }

    public static boolean isLiquid(final Material material) {
        return material.toString().contains("LAVA") || material.toString().contains("WATER") || material.toString().contains("BUBBLE");
    }

    public static boolean isSeaPickle(final Block block) {
        return block.getType().toString().contains("PICKLE");
    }

    public static boolean isSeaPickle(final Material material) {
        return material.toString().equals("SEA_PICKLE");
    }

    public static boolean isTurtleEgg(final Block block) {
        return block.getType().toString().contains("TURTLE") && block.getType().toString().contains("EGG");
    }

    public static boolean isTurtleEgg(final Material material) {
        return material.toString().equals("TURTLE_EGG");
    }

    public static boolean isLectern(final Block block) {
        return block.getType().toString().contains("LECTERN");
    }

    public static boolean isLectern(final Material material) {
        return material.toString().equals("LECTERN");
    }

    public static boolean isWeb(final Block block) {
        return block.getType().toString().contains("WEB");
    }

    public static boolean isWeb(final Material material) {
        return material.toString().equals("WEB") || material.toString().equals("COBWEB");
    }

    public static boolean isAnvil(final Block block) {
        return block.getType().toString().contains("ANVIL");
    }

    public static boolean isAnvil(final Material material) {
        return material.toString().equals("ANVIL") || material.toString().equals("DAMAGED_ANVIL") || material.toString().equals("CHIPPED_ANVIL");
    }

    public static boolean isSlime(final Block block) {
        return block.getType().toString().contains("SLIME");
    }

    public static boolean isSlime(final Material material) {
        return material.toString().equals("SLIME_BLOCK");
    }

    public static boolean isBoat(final Block block) {
        return block.getType().toString().contains("BOAT");
    }

    public static boolean isFarmland(final Block block) {
        return block.getType().toString().contains("FARMLAND");
    }

    public static boolean isFarmland(final Material material) {
        return material.toString().contains("FARMLAND");
    }

    public static boolean isWall(final Material material) {
        return material.toString().contains("WALL") && !material.toString().contains("SIGN");
    }

    public static boolean isSweetBerries(final Block block) {
        return block.getType().toString().contains("SWEET");
    }

    public static boolean isGlassBottle(final Material material) {
        return material.toString().equals("GLASS_BOTTLE");
    }

    public static boolean isSweetBerries(final Material material) {
        return material.toString().equals("SWEET_BERRY_BUSH");
    }

    public static boolean isLilyPad(final Block block) {
        return block.getType().toString().contains("LILY");
    }

    public static boolean isLilyPad(final Material material) {
        return material.toString().equals("LILY_PAD") || material.toString().equals("WATER_LILY");
    }

    public static boolean isBed(final Material material) {
        return material.toString().contains("BED") && !material.toString().equals("BEDROCK");
    }

    public static boolean isPortalFrame(final Block block) {
        return block.getType().toString().contains("FRAME");
    }

    public static boolean isPortalFrame(final Material material) {
        return material.toString().equals("END_PORTAL_FRAME") || material.toString().equals("ENDER_PORTAL_FRAME");
    }

    public static boolean isFence(final Block block) {
        return block.getType().toString().contains("FENCE");
    }


    public static boolean isDaylightSensor(final Block block) {
        return block.getType().toString().contains("DAYLIGHT");
    }

    public static boolean isDaylightSensor(final Material material) {
        return material.toString().equals("DAYLIGHT_DETECTOR");
    }

    public static boolean isStair(final Block block) {
        return block.getType().toString().contains("STAIR");
    }

    public static boolean isStair(final Material material) {
        return material.toString().contains("STAIR");
    }

    public static boolean isSlab(final Block block) {
        return block.getType().toString().contains("SLAB") || block.getType().toString().contains("STEP");
    }

    public static boolean isSlab(final Material material) {
        return material.toString().contains("SLAB") || material.toString().contains("STEP");
    }

    public static boolean isTrapdoor(final Material material) {
        return material.toString().contains("TRAP") && material.toString().contains("DOOR");
    }

    public static boolean isSkull(final Block block) {
        return block.getType().toString().contains("SKULL") || block.getType().toString().contains("HEAD");
    }

    public static boolean isSkull(final Material material) {
        return material.toString().contains("SKULL") || material.toString().contains("HEAD");
    }

    public static boolean isHoney(final Block block) {
        return block.getType().toString().contains("HONEY");
    }

    public static boolean isHoney(final Material material) {
        return material.toString().equals("HONEY_BLOCK");
    }

    public static boolean isBubbleColumn(final Block block) {
        return block.getType().toString().contains("BUBBLE");
    }

    public static boolean isBubbleColumn(final Material material) {
        return material.toString().equals("BUBBLE_COLUMN");
    }

    public static boolean isScaffolding(final Block block) {
        return block.getType().toString().equals("SCAFFOLDING");
    }

    public static boolean isScaffolding(final Material material) {
        return material.toString().equals("SCAFFOLDING");
    }

    public static boolean isCampfire(final Block block) {
        return block.getType().toString().contains("CAMPFIRE");
    }

    public static boolean isCampfire(final Material material) {
        return material.toString().equals("CAMPFIRE") || material.toString().equals("SOUL_CAMPFIRE");
    }

    public static boolean isBrewingStand(final Block block) {
        return block.getType().toString().contains("BREWING");
    }

    public static boolean isBrewingStand(final Material material) {
        return material.toString().equals("BREWING_STAND");
    }

    public static boolean isFrostedIce(final Material material) {
        return material.toString().equals("FROSTED_ICE");
    }

    public static boolean isCarpet(final Block block) {
        return block.getType().toString().contains("CARPET");
    }

    public static boolean isCarpet(final Material material) {
        return material.toString().contains("CARPET");
    }

    public static boolean isIce(final Material material) {
        return material.toString().equals("ICE") || material.toString().equals("PACKED_ICE") || material.toString().equals("BLUE_ICE") || material.toString().equals("FROSTED_ICE");
    }

    public static boolean isString(final Block block) {
        return block.getType().toString().contains("WIRE") || block.getType().toString().contains("STRING");
    }

    public static boolean isBell(final Block block) {
        return block.getType().toString().contains("BELL");
    }

    public static boolean isBell(final Material material) {
        return material.toString().equals("BELL");
    }

    public static boolean isSoulSand(final Block block) {
        return block.getType().toString().contains("SOUL");
    }

    public static boolean isSoulSandOnly(final Material material) {
        return material.toString().equalsIgnoreCase("SOUL_SAND");
    }

    public static boolean isSoulSand(final Material material) {
        return material.toString().equals("SOUL_SAND") || material.toString().equalsIgnoreCase("SOUL_SOIL");
    }

    public static boolean isChest(final Material material) {
        return material.toString().contains("CHEST") || material.toString().equalsIgnoreCase("CHEST");
    }

    public static boolean isDeepSlate(final Material material) {
        return material.toString().contains("DEEPSLATE");
    }

    public static boolean isPath(final Material material) {
        return material.toString().contains("PATH");
    }

    public static boolean isSand(final Material material) {
        return material.toString().contains("SAND");
    }

    public static boolean isGravel(final Material material) {
        return material.toString().contains("GRAVEL");
    }

    public static boolean isLantern(final Material material) {
        return material.toString().contains("LANTERN");
    }

    public static boolean isStripped(final Material material) {
        return material.toString().contains("STRIPPED");
    }

    public static boolean isEnchantmentTable(final Material material) {
        return material.toString().contains("ENCHANT");
    }

    public static List<Material> getNearbyBlocksAsync(final World world,
                                                      final int blockX, final int blockY,
                                                      final int blockZ, final int radius) {
        final List<Material> nearby = new LinkedList<Material>();
        for (int x = blockX - radius; x <= blockX + radius; ++x) {
            for (int y = blockY - radius; y <= blockY + radius + 1; ++y) {
                for (int z = blockZ - radius; z <= blockZ + radius; ++z) {
                    if (world.isChunkLoaded(x >> 4, z >> 4)) {
                        nearby.add(world.getBlockAt(x, y, z).getType());
                    }
                    else {
                        nearby.add(Material.SPONGE);
                    }
                }
            }
        }
        return nearby;
    }

    public static boolean isChunkLoaded(Location location) {
        return (location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4));
    }

    public static Block getBlock(Location location) {
        if (location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return location.getBlock();
        } else {
            return null;
        }
    }

    private void setupCollisionBB() {
        collisionBoundingBoxes.put(Material.getMaterial("FIRE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("STONE_PLATE"), new BoundingBox((float) 0.0625, (float) 0.0, (float) 0.0625, (float) 0.9375, (float) 0.0625, (float) 0.9375));
        collisionBoundingBoxes.put(Material.getMaterial("GRAVEL"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("COBBLESTONE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("NETHER_BRICK"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("PUMPKIN"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("CARROT"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 0.25, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("TNT"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("SOUL_SAND"), new BoundingBox(0f, 0f,0f, 1f, 0.875f, 1f));
        collisionBoundingBoxes.put(Material.getMaterial("SAND"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("WOOD_PLATE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("SIGN_POST"), new BoundingBox((float) 0.25, (float) 0.0, (float) 0.25, (float) 0.75, (float) 1.0, (float) 0.75));
        collisionBoundingBoxes.put(Material.getMaterial("COCOA"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("DETECTOR_RAIL"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 0.125, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("HARD_CLAY"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("NETHERRACK"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("STONE_BUTTON"), new BoundingBox((float) 0.3125, (float) 0.0, (float) 0.375, (float) 0.6875, (float) 0.125, (float) 0.625));
        collisionBoundingBoxes.put(Material.getMaterial("CLAY"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("QUARTZ_BLOCK"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("HUGE_MUSHROOM_1"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("HUGE_MUSHROOM_2"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("LAVA"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("BEACON"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("GRASS"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("DEAD_BUSH"), new BoundingBox((float) 0.09999999403953552, (float) 0.0, (float) 0.09999999403953552, (float) 0.8999999761581421, (float) 0.800000011920929, (float) 0.8999999761581421));
        collisionBoundingBoxes.put(Material.getMaterial("GLOWSTONE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("ICE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("BRICK"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("REDSTONE_TORCH_ON"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("REDSTONE_TORCH_OFF"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("POWERED_RAIL"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 0.125, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("DISPENSER"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("JUKEBOX"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("EMERALD_BLOCK"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("STONE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("BOOKSHELF"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("MYCEL"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("OBSIDIAN"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("PORTAL"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("GOLD_PLATE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("COAL_BLOCK"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("GOLD_BLOCK"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("STAINED_CLAY"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("MOB_SPAWNER"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("BEDROCK"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("IRON_ORE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("REDSTONE_BLOCK"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("SIGN"), new BoundingBox((float) 0.25, (float) 0.0, (float) 0.25, (float) 0.75, (float) 1.0, (float) 0.75));
        collisionBoundingBoxes.put(Material.getMaterial("IRON_PLATE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("GOLD_ORE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("POTATO"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 0.25, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("MOSSY_COBBLESTONE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("RAILS"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 0.125, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("HAY_BLOCK"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("TORCH"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("CARPET"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 0.0625, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("DIRT"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("EMERALD_ORE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("REDSTONE_LAMP_ON"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("REDSTONE_LAMP_OFF"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("NETHER_WARTS"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 0.25, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("SPONGE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("WORKBENCH"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("SANDSTONE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("LAPIS_BLOCK"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("NOTE_BLOCK"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("WOOL"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("COMMAND"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("ENDER_STONE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("TRIPWIRE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 0.15625, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("SAPLING"), new BoundingBox((float) 0.09999999403953552, (float) 0.0, (float) 0.09999999403953552, (float) 0.8999999761581421, (float) 0.800000011920929, (float) 0.8999999761581421));
        collisionBoundingBoxes.put(Material.getMaterial("PACKED_ICE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("LAPIS_ORE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("SMOOTH_BRICK"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("RED_MUSHROOM"), new BoundingBox((float) 0.30000001192092896, (float) 0.0, (float) 0.30000001192092896, (float) 0.699999988079071, (float) 0.4000000059604645, (float) 0.699999988079071));
        collisionBoundingBoxes.put(Material.getMaterial("BROWN_MUSHROOM"), new BoundingBox((float) 0.30000001192092896, (float) 0.0, (float) 0.30000001192092896, (float) 0.699999988079071, (float) 0.4000000059604645, (float) 0.699999988079071));
        collisionBoundingBoxes.put(Material.getMaterial("DIAMOND_BLOCK"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("CROPS"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 0.25, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("IRON_BLOCK"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("MELON"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("DIAMOND_ORE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("LEVER"), new BoundingBox((float) 0.25, (float) 0.0, (float) 0.25, (float) 0.75, (float) 0.6000000238418579, (float) 0.75));
        collisionBoundingBoxes.put(Material.getMaterial("SUGAR_CANE"), new BoundingBox((float) 0.125, (float) 0.0, (float) 0.125, (float) 0.875, (float) 1.0, (float) 0.875));
        collisionBoundingBoxes.put(Material.getMaterial("COAL_ORE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("WATER_LILY"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 0.015625, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("QUARTZ_ORE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("GLASS"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("TRIPWIRE_HOOK"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("VINE"), new BoundingBox((float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0, (float) 1.0, (float) 1.0));
        collisionBoundingBoxes.put(Material.getMaterial("WEB"), new BoundingBox(0, 0, 0, 1, 1, 1));
        collisionBoundingBoxes.put(Material.getMaterial("WATER"), new BoundingBox(0, 0, 0, 0.9f, 0.9f, 0.9f));
        collisionBoundingBoxes.put(Material.getMaterial("STATIONARY_WATER"), new BoundingBox(0, 0, 0, 0.9f, 0.9f, 0.9f));
        collisionBoundingBoxes.put(Material.getMaterial("STATIONARY_LAVA"), new BoundingBox(0, 0, 0, 0.9f, 0.9f, 0.9f));
    }
}