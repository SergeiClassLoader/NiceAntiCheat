package pro.cyrent.anticheat.api.check.impl.combat.killaura;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.api.check.data.ReachData;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.block.BlockUtil;
import pro.cyrent.anticheat.util.block.box.HydroBB;
import pro.cyrent.anticheat.util.blockiterator.BlockIterator;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.math.VectorUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import pro.cyrent.anticheat.api.check.*;

import java.util.Arrays;
import java.util.Locale;

@CheckInformation(
        name = "KillAura",
        subName = "J",
        checkType = CheckType.COMBAT,
        description = "Wall attack detection",
        checkNameEnum = CheckName.KILL_AURA,
        experimental = true,
        punishable = false,
        state = CheckState.BETA)
public class KillAuraJ extends Check {

    private double threshold;

    @Override
    public void onReach(ReachData data) {

        if (!data.isValidHitbox() || !data.isAttack()) return;

        Block block = getCurrentBlock();

        if (getData().getActionProcessor().isDigging() || getData().getLastBlockBreakTimer().getDelta() < 20) {
            this.threshold = 0;
            return;
        }

        if (block.getType() != Material.AIR) {

            if (invalidBlock(block.getType())) {
                return;
            }

            Player entity = getData().getCombatProcessor().getLastPlayerAttacked();

            if (entity == null) return;

            PlayerData target = Anticheat.INSTANCE.getUserManager().getUser(entity);

            if (target == null) return;

            Location blockLocation = new Location(block.getWorld(), block.getX(), block.getY(), block.getZ());
            Vector vector = new Vector(block.getX(), block.getY(), block.getZ());

            FlyingLocation to = getData().getMovementProcessor().getTo();

            double min = Double.MAX_VALUE;

            for (double d : Arrays.asList((double) (1.62f - 0.08f), (double) (1.62f))) {

                HydroBB hydroBB = new HydroBB(vector.toBlockVector());

                hydroBB.expand(-0.4, -0.4, -0.4);

                Vector eyes = new Vector(to.getPosX(), to.getPosY() + d, to.getPosZ());

                Vector best = VectorUtils.cutBoxToVector(eyes, hydroBB);

                min = Math.min(min, eyes.distance(best));
            }

            if (min != Double.MAX_VALUE) {
                double blockDistanceBukkit = blockLocation.distance(getData().getMovementProcessor()
                        .getTo().toLocation(getData().getPlayer().getWorld()));

                double targetDistanceFromBlock = blockLocation.distance(target.getMovementProcessor()
                        .getTo().toLocation(getData().getPlayer().getWorld()));

                if (data.getDistance() > 1.75 && data.getDistance() > min
                        && data.getDistance() > blockDistanceBukkit
                        && targetDistanceFromBlock > 2.0) {
                    if (++this.threshold > 3) {
                        this.fail("playerDistance="+data.getDistance(),
                                "blockDistance="+min,
                                "targetBlockDistance="+targetDistanceFromBlock);
                    }
                }
            } else {
                this.threshold -= Math.min(this.threshold, .5);
            }
        }
    }

    public Block getCurrentBlock() {
        BlockIterator iterator = new BlockIterator(getData().getPlayer(), 3); // Change 100 to your desired range
        Block block = null;

        while (iterator.hasNext()) {
            block = iterator.next();
            if (block.getType().isSolid()) {
                break;
            }
        }

        return block;
    }

    boolean invalidBlock(Material material) {
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
            case WATER:
            case LAVA:
            case STATIONARY_LAVA:
            case STATIONARY_WATER:
            case SIGN:
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
            case REDSTONE_TORCH_ON:
            case REDSTONE:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_COMPARATOR_OFF:
            case REDSTONE_COMPARATOR_ON:
            case REDSTONE_WIRE:
            case STONE_BUTTON:
            case WOOD_BUTTON:
            case LEVER:
            case FENCE:
            case FENCE_GATE:
            case ACACIA_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case SPRUCE_FENCE_GATE:
            case HOPPER:
            case WEB:
            case SNOW:
            case RED_MUSHROOM:
            case ENDER_PORTAL:
            case BED:
            case BED_BLOCK:
            case SKULL:
            case DRAGON_EGG:
            case ITEM_FRAME:
            case BANNER:
            case STANDING_BANNER:
            case ARMOR_STAND:
            case PAINTING:
            case TRIPWIRE:
            case STRING:
            case TRIPWIRE_HOOK:
            case DIODE:
            case DIODE_BLOCK_ON:
            case DIODE_BLOCK_OFF:
            case CARPET:
            case CAKE:
            case CAKE_BLOCK:
            case DEAD_BUSH:
            case CACTUS:
            case WALL_SIGN: {
                return true;
            }
        }

        if (material.name().toLowerCase(Locale.ROOT).contains("door")) {
            return true;
        }

        return BlockUtil.isStair(material) || BlockUtil.isSlab(material);
    }
}
