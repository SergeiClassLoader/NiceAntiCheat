package pro.cyrent.anticheat.impl.processor.world;


import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import jafama.StrictFastMath;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import org.bukkit.World;

import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.block.BlockUtil;
import pro.cyrent.anticheat.util.block.PreScannedBoxes;
import pro.cyrent.anticheat.util.block.wrap.BlockResultNew;
import pro.cyrent.anticheat.util.block.wrap.BoxMaterial;
import pro.cyrent.anticheat.util.event.EventTimer;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.nms.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.bukkit.Material.*;

@Getter
@Setter
public class CollisionWorldProcessor extends Event {
    private final PlayerData data;
    private boolean ground, lastGround, lastBlockAbove, blockAbove, collidingHorizontal, skull;
    private BoundingBox playerBoundingBox = null;

    private final EventTimer serverGroundTimer, collidingHorizontallyTimer, blockAboveTimer;

    private int groundTicks;
    private int stairTicks;
    private int slabTicks;
    private int liquidTicks;
    private int collideHorizontalTicks;
    private int iceTicks;
    private int blockAboveTicks;
    private int slimeTicks;
    private int climbableTicks;
    private int snowTicks;
    private int lillyPadTicks;
    private int carpetTicks;
    private int mountTicks;
    private int halfBlockTicks;
    private int movingTicks;
    private int anvilTicks;
    private int soulSandTicks;
    private int enderPortalTicks;
    private int pistionTicks;
    private int wallTicks;
    private int cauldronTicks;
    private int hopperTicks;
    private int serverGroundTicks;
    private int serverAirTicks;

    private boolean ice = false;
    private boolean slime = false;
    private boolean climbing = false;
    private boolean anvil = false;
    private boolean hopper = false;
    private boolean door = false;
    private boolean cauldron = false;
    private boolean wall = false;
    private boolean piston = false;
    private boolean enderPortal = false;
    private boolean soulSand = false;
    private boolean webFullCheck = false;
    private boolean carpet = false;
    private boolean lillyPad = false;
    private boolean stair = false;
    private boolean halfBlock = false;
    private boolean bed = false;
    private boolean snowHasIncompleteLayer = false;
    private boolean liquid = false;
    private boolean water = false;
    private boolean lava = false;
    private boolean snow = false;
    private boolean netherPortal = false;

    private final EventTimer halfBlockTimer;

    public CollisionWorldProcessor(PlayerData user) {
        this.data = user;
        this.serverGroundTimer = new EventTimer(20, user);
        this.collidingHorizontallyTimer = new EventTimer(20, user);
        this.blockAboveTimer = new EventTimer(20, user);
        this.halfBlockTimer = new EventTimer(20, user);
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            if (flying.hasPositionChanged()) {
                double deltaXZ = getData().getMovementProcessor().getDeltaXZ();

                Vector vector = new Vector(flying.getLocation().getX(), flying.getLocation().getY(),
                        flying.getLocation().getZ());

                double expand = 0.6F;

                if (deltaXZ >= 0.65F && (getData().getMovementProcessor().isPositionGround()
                        || getData().getMovementProcessor().getTo().isOnGround()
                        || getData().getCollisionProcessor().isServerGround()
                        || getData().getCollisionWorldProcessor().isGround())) {
                    expand = 1.2F;
                }

                this.playerBoundingBox = new BoundingBox(vector, vector)
                        .expand(expand, 0, expand)
                        // .expand(0.05, 0, 0.05)
                        .expandMax(0, 3F, 0)
                        .expandMin(0, -.5, 0);

            }

            if (this.playerBoundingBox != null) {
                this.handleNewBlockProcessing();
            }

        }
    }

    public void handleNewBlockProcessing() {
        BoundingBox boundingBox = this.playerBoundingBox;

        World w = getData().getPlayer().getWorld();

        List<PreScannedBoxes> resultBoxes = Anticheat.INSTANCE.getInstanceManager().getInstance()
                .getCollidingBoxes(w, boundingBox, getData());

        final BlockResultNew blockCollisionResult = new BlockResultNew();
        final List<BoxMaterial> blocks = new ArrayList<>();

        for (PreScannedBoxes preScannedBoxes : resultBoxes) {
            BoundingBox box = preScannedBoxes.getBoundingBox();

            Location location = box.getMinimum().toLocation(w);

            Material material = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(
                    w,
                    StrictFastMath.floor(location.getX()),
                    StrictFastMath.floor(location.getY()),
                    StrictFastMath.floor(location.getZ())
            );

            // fall back to a location that isn't floored for the material if the current one is null
            if (material == null) {
                material = preScannedBoxes.getMaterial();
            }

            // null check for the final time
            if (material == null) continue;

            if (material != Material.AIR) {
                final BoundingBox clonedAbove = boundingBox.clone()
                        .add(0, .75, 0, 0, .35f, 0);

                final BoundingBox verticalBox = box.clone().shrink(0.05, 0, 0.05);
                final BoundingBox groundBox = box.clone().expand(0, .4, 0);

                BoundingBox horizontalCollide = boundingBox.clone();
                horizontalCollide.expandMin(0, .5, 0);
                horizontalCollide.expandMax(0, -3F, 0);


                if (boundingBox.clone().expandMin(0, 0.5, 0).collidesHorizontallyNoReflection(box)
                        && material != Material.SIGN_POST
                        && material != Material.REDSTONE
                        && material != Material.RAILS
                        && material != Material.GOLD_PLATE
                        && material != Material.IRON_PLATE
                        && material != Material.STONE_PLATE
                        && material != Material.WOOD_PLATE
                        && material != Material.ACTIVATOR_RAIL
                        && material != Material.DETECTOR_RAIL
                        && material != Material.POWERED_RAIL
                        && material != Material.SIGN && material != Material.WALL_SIGN) {
                    blockCollisionResult.setCollidingHorizontally(true);
                    //  horizontalCollide.draw(Bukkit.getOnlinePlayers());
                }

                //       box.clone().draw(Bukkit.getOnlinePlayers());
                //     this.playerBoundingBox.clone().draw(Bukkit.getOnlinePlayers());

                if (boundingBox.clone().expandMin(0.0, 0.5, 0.0).collidesVerticallyNoReflection(groundBox)) {
                    if (blockCollisionResult.isCollidingHorizontally()) {
                        blockCollisionResult.setServerGround(this.locationBasedGround());
                    } else {

                        boolean isFenceGate = BlockUtil.isFenceGate(material);

                        if (isFenceGate) {
                            Block block = BlockUtil.getBlock(location);

                            if (block != null && (block.getData() & 0x4) == 0) {
                                isFenceGate = false;
                            }
                        }

                        if (material != REDSTONE_WIRE
                                && !isFenceGate
                                && material != WATER
                                && material != LAVA
                                && material != STATIONARY_WATER
                                && material != SIGN
                                && material != SIGN_POST
                                && material != TORCH
                                && material != REDSTONE_TORCH_OFF
                                && material != REDSTONE_TORCH_ON
                                && material != STATIONARY_LAVA
                                && material != TRIPWIRE
                                && material != STRING
                                && material != TRIPWIRE_HOOK) {
                            blockCollisionResult.setServerGround(true);
                        }
                    }
                }


                if (verticalBox.collidesVerticallyNoReflection(clonedAbove)) {
                    blockCollisionResult.setBlockAbove(true);
                    this.blockAboveTimer.resetBoth();
                    this.blockAboveTicks = 20;
                }

                //Bukkit.broadcastMessage(""+material);
                blocks.add(new BoxMaterial(box, material));
            }

        }

        this.lastGround = this.ground;
        this.lastBlockAbove = this.blockAbove;

        this.ground = blockCollisionResult.isServerGround();
        this.blockAbove = blockCollisionResult.isBlockAbove();
        this.collidingHorizontal = blockCollisionResult.isCollidingHorizontally();

        if (this.ground) {
            this.serverGroundTimer.resetBoth();
        }

        if (this.collidingHorizontal) {
            this.collidingHorizontallyTimer.resetBoth();
        }

        if (this.blockAbove) {
            this.blockAboveTimer.resetBoth();
        }

        this.handleBlocks(blocks);
    }

    public boolean locationBasedGround() {
        double max = 0.5;

        if (Math.abs(getData().getMovementProcessor().getDeltaY()) > .005D) {
            max = 0.3;
        }

        boolean ground = false;

        Location location = getData().getMovementProcessor().getTo().toLocation(getData().getPlayer().getWorld());

        ground:
        {
            for (double x = -max; x <= max; x += max) {
                for (double z = -max; z <= max; z += max) {
                    for (double y = 0; y < .5; y += 0.1) {

                        Location currentLocation = location.clone().add(x, -y, z);
                        Material material = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(
                                getData().getPlayer().getWorld(),
                                currentLocation.getX(), currentLocation.getY(), currentLocation.getZ()
                        );

                        boolean isFenceGate = BlockUtil.isFenceGate(material);

                        if (isFenceGate) {
                            Block block = BlockUtil.getBlock(currentLocation);

                            if (block != null && (block.getData() & 0x4) == 0) {
                                isFenceGate = false;
                            }
                        }

                        if (material != Material.AIR
                                && material != REDSTONE_WIRE
                                && material != TRIPWIRE
                                && material != SIGN
                                && material != SIGN_POST
                                && material != TORCH
                                && material != REDSTONE_TORCH_OFF
                                && material != REDSTONE_TORCH_ON
                                && material != STRING
                                && isFenceGate
                                && material != TRIPWIRE_HOOK
                                && material != Material.LAVA
                                && material != Material.STATIONARY_LAVA
                                && material != Material.WATER
                                && material != Material.STATIONARY_WATER) {
                            ground = true;
                            break ground;
                        }
                    }
                }
            }
        }

        return ground;
    }

    public void handleBlocks(List<BoxMaterial> blocks) {

        boolean ice = false;
        boolean slime = false;
        boolean climbing = false;
        boolean anvil = false;
        boolean hopper = false;
        boolean door = false;
        boolean cauldron = false;
        boolean wall = false;
        boolean piston = false;
        boolean enderPortal = false;
        boolean soulSand = false;
        boolean webFullCheck = false;
        boolean carpet = false;
        boolean lillyPad = false;
        boolean stair = false;
        boolean halfBlock = false;
        boolean bed = false;
        boolean snowHasIncompleteLayer = false;
        boolean liquid = false;
        boolean skull = false;
        boolean snow = false;
        boolean water = false;
        boolean lava = false;
        boolean portal = false;

        for (BoxMaterial box : blocks) {

            switch (box.getMaterial()) {

                case ANVIL: {
                    anvil = true;
                    break;
                }
                case HOPPER: {
                    hopper = true;
                    break;
                }

                case ACACIA_DOOR:
                case BIRCH_DOOR:
                case IRON_DOOR:
                case JUNGLE_DOOR:
                case SPRUCE_DOOR:
                case TRAP_DOOR:
                case WOOD_DOOR:
                case WOODEN_DOOR:
                case IRON_TRAPDOOR:
                case DARK_OAK_DOOR: {
                    door = true;
                    break;
                }
                case CAULDRON: {
                    cauldron = true;
                    break;
                }

                case COBBLE_WALL: {
                    wall = true;
                    break;
                }


                case PISTON_BASE:
                case PISTON_EXTENSION:
                case PISTON_MOVING_PIECE:
                case PISTON_STICKY_BASE: {
                    piston = true;
                    break;
                }

                case ENDER_PORTAL:
                case ENDER_PORTAL_FRAME: {
                    enderPortal = true;
                    break;
                }

                case SOUL_SAND: {
                    soulSand = true;
                    break;
                }

                case WEB: {
                    webFullCheck = true;
                    break;
                }

                case CARPET: {
                    carpet = true;
                    break;
                }

                case WATER_LILY: {
                    lillyPad = true;
                    break;
                }

                case SNOW:
                case SNOW_BLOCK: {

                    // detects if the snow block has incomplete layers "dips"
                    if (box.getMaterial() == Material.SNOW && (box.getBoundingBox().getMaximum().getY() % 1) != .875) {
                        snowHasIncompleteLayer = true;
                        //      Bukkit.broadcastMessage("snow layer");
                    }

                    // Bukkit.broadcastMessage("snow");

                    snow = true;
                    break;
                }

                case VINE:
                case LADDER: {
                    climbing = true;
                    break;
                }

                case SLIME_BLOCK: {
                    slime = true;
                    break;
                }

                case ICE:
                case PACKED_ICE: {
                    ice = true;
                    break;
                }

                case LAVA:
                case STATIONARY_LAVA:
                case STATIONARY_WATER:
                case WATER: {
                    liquid = true;

                    break;
                }

                case SANDSTONE_STAIRS:
                case SMOOTH_STAIRS:
                case SPRUCE_WOOD_STAIRS:
                case ACACIA_STAIRS:
                case BIRCH_WOOD_STAIRS:
                case BRICK_STAIRS:
                case COBBLESTONE_STAIRS:
                case DARK_OAK_STAIRS:
                case JUNGLE_WOOD_STAIRS:
                case NETHER_BRICK_STAIRS:
                case QUARTZ_STAIRS:
                case RED_SANDSTONE_STAIRS:
                case WOOD_STAIRS: {
                    stair = true;
                    break;
                }
            }

            switch (box.getMaterial()) {
                case BED_BLOCK:
                case BED: {
                    bed = true;
                    break;
                }

                case SKULL: {
                    skull = true;
                    break;
                }
            }

            switch (box.getMaterial()) {
                case BREWING_STAND:
                case CHEST:
                case TRAPPED_CHEST:
                case ENDER_CHEST:
                case ENCHANTMENT_TABLE:
                case CARPET:
                case HOPPER:
                case CAULDRON:
                case IRON_BARDING:
                case FENCE:
                case FENCE_GATE:
                case ACACIA_FENCE:
                case BIRCH_FENCE:
                case ACACIA_FENCE_GATE:
                case DARK_OAK_FENCE:
                case IRON_FENCE:
                case STONE_SLAB2:
                case DOUBLE_STONE_SLAB2:
                case SOUL_SAND:
                case JUNGLE_FENCE:
                case BIRCH_FENCE_GATE:
                case COBBLE_WALL:
                case DAYLIGHT_DETECTOR:
                case DAYLIGHT_DETECTOR_INVERTED:
                case DARK_OAK_FENCE_GATE:
                case JUNGLE_FENCE_GATE:
                case NETHER_FENCE:
                case BED_BLOCK:
                case BED:
                case CAKE:
                case CAKE_BLOCK:
                case FLOWER_POT:
                case SKULL:
                case TRAP_DOOR:
                case IRON_TRAPDOOR:
                case SPRUCE_FENCE:
                case SPRUCE_FENCE_GATE:
                case REDSTONE_COMPARATOR:
                case REDSTONE_COMPARATOR_ON:
                case REDSTONE_COMPARATOR_OFF:
                case STAINED_GLASS_PANE:
                case SANDSTONE_STAIRS:
                case COCOA:
                case DIODE:
                case DIODE_BLOCK_OFF:
                case DIODE_BLOCK_ON:
                case SMOOTH_STAIRS:
                case SPRUCE_WOOD_STAIRS:
                case ACACIA_STAIRS:
                case CACTUS:
                case BIRCH_WOOD_STAIRS:
                case BRICK_STAIRS:
                case COBBLESTONE_STAIRS:
                case DARK_OAK_STAIRS:
                case JUNGLE_WOOD_STAIRS:
                case STEP:
                case DOUBLE_STEP:
                case WOOD_DOUBLE_STEP:
                case WOOD_STEP:
                case NETHER_BRICK_STAIRS:
                case QUARTZ_STAIRS:
                case RED_SANDSTONE_STAIRS:
                case WOOD_STAIRS: {
                    halfBlock = true;
                    break;
                }

                case WATER:
                case STATIONARY_WATER: {
                    water = true;
                    break;
                }

                case LAVA:
                case STATIONARY_LAVA: {
                    lava = true;
                    break;
                }
            }

            if (box.getMaterial().name().toLowerCase(Locale.ROOT).contains("slab")
                    || box.getMaterial().name().toLowerCase(Locale.ROOT).contains("step")) {
                halfBlock = true;
            }

            if (box.getMaterial() == Material.PORTAL) {
                portal = true;
            }
        }

        this.netherPortal = portal;


        this.water = water;
        this.lava = lava;
        this.skull = skull;
        this.bed = bed;
        this.halfBlock = halfBlock;
        this.snow = snow || snowHasIncompleteLayer;
        this.stair = stair;
        this.liquid = liquid;
        this.ice = ice;
        this.slime = slime;
        this.climbing = climbing;
        this.snowHasIncompleteLayer = snowHasIncompleteLayer;
        this.lillyPad = lillyPad;
        this.carpet = carpet;
        this.webFullCheck = webFullCheck;
        this.soulSand = soulSand;
        this.enderPortal = enderPortal;
        this.piston = piston;
        this.door = door;
        this.wall = wall;
        this.hopper = hopper;
        this.anvil = anvil;
        this.cauldron = cauldron;

        if (carpet) {
            this.carpetTicks = 20;
        } else {
            this.carpetTicks -= (this.carpetTicks > 0 ? 1 : 0);
        }

        if (climbing) {
            this.climbableTicks = 20;
        } else {
            this.climbableTicks -= (this.climbableTicks > 0 ? 1 : 0);
        }

        if (snow) {
            this.snowTicks = 20;
        } else {
            this.snowTicks -= (this.snowTicks > 0 ? 1 : 0);
        }

        if (lillyPad) {
            this.lillyPadTicks = 20;
        } else {
            this.lillyPadTicks -= (this.lillyPadTicks > 0 ? 1 : 0);
        }


        if (slime) {
            this.slimeTicks = 20;
        } else {
            this.slimeTicks -= (this.slimeTicks > 0 ? 1 : 0);
        }

        if (this.blockAbove) {
            this.blockAboveTicks = 20;
        } else {
            this.blockAboveTicks -= (this.blockAboveTicks > 0 ? 1 : 0);
        }

        if (this.ground) {
            this.groundTicks = 20;
        } else {
            this.groundTicks -= this.groundTicks > 0 ? 1 : 0;
        }

        if (halfBlock) {
            this.halfBlockTimer.reset();
            this.stairTicks = 20;
        } else {
            this.stairTicks -= this.stairTicks > 0 ? 1 : 0;
        }

        if (halfBlock) {
            this.halfBlockTimer.reset();
            this.slabTicks = 20;
        } else {
            this.slabTicks -= this.slabTicks > 0 ? 1 : 0;
        }

        if (liquid) {
            this.liquidTicks = 20;
        } else {
            this.liquidTicks -= this.liquidTicks > 0 ? 1 : 0;
        }

        if (this.collidingHorizontal) {
            this.collideHorizontalTicks = 20;
        } else {
            this.collideHorizontalTicks -= this.collideHorizontalTicks > 0 ? 1 : 0;
        }

        if (ice) {
            this.iceTicks = 20;
        } else {
            this.iceTicks -= this.iceTicks > 0 ? 1 : 0;
        }

        if (this.ground) {
            if (this.serverGroundTicks < 20) this.serverGroundTicks++;
            this.serverAirTicks = 0;
        } else {
            this.serverGroundTicks = 0;
            if (this.serverAirTicks < 20) this.serverAirTicks++;
        }

        if (anvil) {
            this.anvilTicks = 20;
        } else {
            this.anvilTicks -= this.anvilTicks > 0 ? 1 : 0;
        }

        if (hopper) {
            this.hopperTicks = 20;
        } else {
            this.hopperTicks -= this.hopperTicks > 0 ? 1 : 0;
        }

        if (cauldron) {
            this.cauldronTicks = 20;
        } else {
            this.cauldronTicks -= this.cauldronTicks > 0 ? 1 : 0;
        }

        if (wall) {
            this.wallTicks = 20;
        } else {
            this.wallTicks -= this.wallTicks > 0 ? 1 : 0;
        }

        if (piston) {
            this.pistionTicks = 20;
        } else {
            this.pistionTicks -= this.pistionTicks > 0 ? 1 : 0;
        }

        if (enderPortal) {
            this.enderPortalTicks = 20;
        } else {
            this.enderPortalTicks -= this.enderPortalTicks > 0 ? 1 : 0;
        }

        if (soulSand) {
            this.soulSandTicks = 20;
        } else {
            this.soulSandTicks -= this.soulSandTicks > 0 ? 1 : 0;
        }

        if (halfBlock) {
            this.halfBlockTimer.reset();
            this.halfBlockTicks = 20;
        } else {
            this.halfBlockTicks -= this.halfBlockTicks > 0 ? 1 : 0;
        }
    }
}
