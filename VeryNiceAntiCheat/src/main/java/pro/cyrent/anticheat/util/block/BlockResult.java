package pro.cyrent.anticheat.util.block;

import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.block.box.BoundingBox;
import pro.cyrent.anticheat.util.block.collide.CollideEntry;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Step;
import org.bukkit.material.WoodenStep;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.bukkit.Material.*;

@Getter
public final class BlockResult {

    public boolean serverGround, lastServerGround, door;

    public boolean liquid;
    public boolean stair;
    public boolean slab;
    public boolean ice;
    public boolean slime;
    public boolean climbable;
    public boolean snow, snowHasIncompleteLayer;
    public boolean lillyPad;
    public boolean carpet;
    public boolean bed;
    public boolean anvil;
    public boolean web, webInside, webFullCheck;
    public boolean halfBlock;
    public boolean movingUp;
    public boolean soulSand;
    public boolean enderPortal;
    public boolean piston;
    public boolean cauldron;
    public boolean hopper;
    public boolean collideHorizontal;
    public boolean blockAbove;
    public boolean wall;
    public boolean waterFully;
    private boolean lavaFully;

    public boolean water, lava, serverPosGround;

    public boolean skull;
    public Material material;

    private double lastBoundingBoxY;

    private final Map<String, Set<CollideEntry>> collidedBlocksCache = new HashMap<>();

    private Set<CollideEntry> getCachedCollidedBlocks(PlayerData user, BoundingBox boundingBox) {

        String cacheKey = boundingBox.toString();

        // Check if the result is already cached
        if (collidedBlocksCache.containsKey(cacheKey)) {
            return collidedBlocksCache.get(cacheKey);
        }

        Set<CollideEntry> collidedBlocks = new HashSet<>();

        for (CollideEntry collideEntry : boundingBox.getCollidedBlocks(user)) {
            if (collidedBlocks.size() > 30) {
                break;
            }
            collidedBlocks.add(collideEntry);
        }

        // Cache the result
        collidedBlocksCache.put(cacheKey, collidedBlocks);

        return collidedBlocks;
    }

    public void checkBlockAbove(PlayerData user) {

        boolean skull = false;

        for (CollideEntry collideEntry : new BoundingBox(
                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY()
                        + user.getPlayer().getEyeHeight(true),
                user.getMovementProcessor().getTo().getPosZ(),

                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY() + 2.2F,
                user.getMovementProcessor().getTo().getPosZ())
                .expand(0.3001F, .0, 0.3001F).addXYZ(0, 0, 0).getCollidedBlocks(user)) {
            if (collideEntry.getBlock() == Material.SKULL) {
                skull = true;
                break;
            }
        }


        this.skull = skull;


        boolean aboveGay = false;

        for (CollideEntry collideEntry : new BoundingBox(
                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY()
                        + user.getPlayer().getEyeHeight(true),
                user.getMovementProcessor().getTo().getPosZ(),

                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY() + 1.9F,
                user.getMovementProcessor().getTo().getPosZ())
                .expand(.306D, .0, .306D)
                .addXYZ(0, 0.625, 0).getCollidedBlocks(user)) {
            if (collideEntry.getBlock() == Material.SNOW
                    || collideEntry.getBlock() == Material.REDSTONE_COMPARATOR_OFF
                    || collideEntry.getBlock() == Material.REDSTONE_COMPARATOR
                    || collideEntry.getBlock() == Material.SKULL
                    || collideEntry.getBlock() == Material.IRON_TRAPDOOR
                    || collideEntry.getBlock() == Material.TRAP_DOOR
                    || collideEntry.getBlock() == Material.FLOWER_POT
                    || collideEntry.getBlock() == Material.DIODE_BLOCK_ON
                    || collideEntry.getBlock() == Material.DIODE_BLOCK_OFF
                    || collideEntry.getBlock() == Material.DIODE) {
                aboveGay = true;
                break;
            }
        }


        boolean aboveGay2 = false;

        for (CollideEntry collideEntry : new BoundingBox(
                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY()
                        + user.getPlayer().getEyeHeight(true),
                user.getMovementProcessor().getTo().getPosZ(),

                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY() + 1.9F,
                user.getMovementProcessor().getTo().getPosZ())
                .expand(.306D, .0, .306D)
                .addXYZ(0, 0.625, 0).getCollidedBlocks(user)) {
            if (collideEntry.getBlock() != Material.AIR) {
                aboveGay2 = true;
                break;
            }
        }

        boolean aboveGay3 = false;

        for (CollideEntry collideEntry : new BoundingBox(
                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY() + user.getPlayer().getEyeHeight(true),
                user.getMovementProcessor().getTo().getPosZ(),
                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY() + 1.9F,
                user.getMovementProcessor().getTo().getPosZ())
                .expand(0.306D, 0, 0.306D)
                .getCollidedBlocks(user)) {
            if (collideEntry.getBlock() != Material.AIR) {
                aboveGay3 = true;
                break;
            }
        }

        boolean above = false;

        for (CollideEntry collideEntry : new BoundingBox(
                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY() + 1.62F,
                user.getMovementProcessor().getTo().getPosZ(),
                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY() + 1.62F,
                user.getMovementProcessor().getTo().getPosZ())
                .expand(0.306D, 0, 0.306D)
                .addXYZ(0, 0.625, 0)
                .getCollidedBlocks(user)) {
            if (collideEntry.getBlock() != Material.AIR) {
                above = true;
                break;
            }
        }

        this.blockAbove = above;

        if (aboveGay || aboveGay2 || aboveGay3 || skull) {
            this.blockAbove = true;
        }
    }

    public void checkNewServerGround(PlayerData playerData) {


        BoundingBox boundingBox = playerData.getBoundingBox();
        Set<CollideEntry> collidedBlocks = getCachedCollidedBlocks(playerData, boundingBox);

        boolean serverG = false;
        for (CollideEntry collideEntry : collidedBlocks) {
            if (collideEntry.getBlock() != Material.AIR &&
                    collideEntry.getBlock() != STATIONARY_WATER &&
                    collideEntry.getBlock() != Material.STATIONARY_LAVA &&
                    collideEntry.getBlock() != Material.WATER &&
                    collideEntry.getBlock() != Material.LAVA &&
                    collideEntry.getBlock() != Material.WEB) {
                serverG = true;
                break;
            }
        }

        this.lastServerGround = this.serverGround;
        this.serverGround = serverG;
    }

    public void checkWeb(PlayerData user) {

        BoundingBox boundingBox = new BoundingBox(
                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY() + 1.2F,
                user.getMovementProcessor().getTo().getPosZ(),
                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY() + 1.64F,
                user.getMovementProcessor().getTo().getPosZ())
                .expand(0.3F, 0, 0.3F);
        Set<CollideEntry> collidedBlocks = getCachedCollidedBlocks(user, boundingBox);

        boolean web = false;

        for (CollideEntry collideEntry : collidedBlocks) {
            if (collideEntry.getBlock() == Material.WEB) {
                web = true;
                break;
            }
        }

        this.web = web;
    }

    public void checkWebFull(PlayerData user) {
        BoundingBox boundingBox = new BoundingBox(
                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY(),
                user.getMovementProcessor().getTo().getPosZ(),
                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY() + 1.98F,
                user.getMovementProcessor().getTo().getPosZ())
                .expand(0.5F, 0, 0.5F);
        Set<CollideEntry> collidedBlocks = getCachedCollidedBlocks(user, boundingBox);

        boolean web = false;
        for (CollideEntry collideEntry : collidedBlocks) {
            if (collideEntry.getBlock() == Material.WEB) {
                web = true;
                break;
            }
        }

        this.webFullCheck = web;
    }

    public void checkWebInside(PlayerData user) {
        BoundingBox boundingBox = new BoundingBox(
                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY() + 1.2F,
                user.getMovementProcessor().getTo().getPosZ(),
                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY() + 1.64F,
                user.getMovementProcessor().getTo().getPosZ())
                .expand(0.29F, 0, 0.29F);
        Set<CollideEntry> collidedBlocks = getCachedCollidedBlocks(user, boundingBox);

        boolean web = false;
        for (CollideEntry collideEntry : collidedBlocks) {
            if (collideEntry.getBlock() == Material.WEB) {
                web = true;
                break;
            }
        }

        this.webInside = web;
    }

    public void checkHorizontalNew(PlayerData user) {
        // Adjust these values based on your specific needs
        float playerWidth = 0.8F;  // Width
        float playerHeight = 1.9F; // Height

        float boxSizeX = playerWidth;
        float boxSizeY = playerHeight;
        float boxSizeZ = playerWidth;

        // Adjust the player's position for the bounding box
        double playerPosX = user.getMovementProcessor().getTo().getPosX();
        double playerPosY = user.getMovementProcessor().getTo().getPosY();
        double playerPosZ = user.getMovementProcessor().getTo().getPosZ();

        // Create the bounding box
        BoundingBox boundingBox = new BoundingBox(
                playerPosX - boxSizeX / 2,
                playerPosY + 1.58F,
                playerPosZ - boxSizeZ / 2,
                playerPosX + boxSizeX / 2,
                playerPosY + boxSizeY,
                playerPosZ + boxSizeZ / 2
        );

        boundingBox.expandSpecial(1.7);

        boolean horizontal = false;

        loop:
        {
            Set<CollideEntry> collidedBlocks = getCachedCollidedBlocks(user, boundingBox);

            for (CollideEntry collideEntry : collidedBlocks) {
                if (collideEntry.getBlock() != AIR
                        && collideEntry.getBlock() != LAVA
                        && collideEntry.getBlock() != STATIONARY_LAVA
                        && collideEntry.getBlock() != WATER
                        && collideEntry.getBlock() != LONG_GRASS
                        && collideEntry.getBlock() != RAILS
                        && collideEntry.getBlock() != TORCH
                        && collideEntry.getBlock() != LEVER
                        && collideEntry.getBlock() != YELLOW_FLOWER
                        && collideEntry.getBlock() != DEAD_BUSH
                        && collideEntry.getBlock() != SAPLING
                        && collideEntry.getBlock() != STONE_BUTTON
                        && collideEntry.getBlock() != WOOD_BUTTON
                        && collideEntry.getBlock() != WOOD_PLATE
                        && collideEntry.getBlock() != GOLD_PLATE
                        && collideEntry.getBlock() != IRON_PLATE
                        && collideEntry.getBlock() != STONE_PLATE
                        && collideEntry.getBlock() != SIGN
                        && collideEntry.getBlock() != SIGN_POST
                        && collideEntry.getBlock() != PAINTING
                        && collideEntry.getBlock() != SUGAR_CANE
                        && collideEntry.getBlock() != SUGAR_CANE_BLOCK
                        && collideEntry.getBlock() != BANNER
                        && collideEntry.getBlock() != STANDING_BANNER
                        && collideEntry.getBlock() != ITEM_FRAME
                        && collideEntry.getBlock() != STATIONARY_WATER) {
                    horizontal = true;
                    break loop;
                }
            }
        }

        //Bukkit.broadcastMessage(""+horizontal);

        this.collideHorizontal = horizontal;
    }


    public void checkWater(PlayerData user) {
        boolean water = false;

        BoundingBox boundingBox = new BoundingBox(
                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY(),
                user.getMovementProcessor().getTo().getPosZ(),
                user.getMovementProcessor().getTo().getPosX(),
                user.getMovementProcessor().getTo().getPosY(),
                user.getMovementProcessor().getTo().getPosZ())
                .subtractY(-1F)
                .addY(0.03F)
                .shrink(0.01F, 0, 0.01F);
        Set<CollideEntry> collidedBlocks = boundingBox.getCollidedBlocks(user);

        for (CollideEntry collideEntry : collidedBlocks) {
            if (collideEntry.getBlock() == STATIONARY_WATER) {
                water = true;
                break;
            }
        }

        this.water = water;
    }

    public void checkWaterFull(PlayerData user) {
        // Adjust these values based on your specific needs
        float playerWidth = 0.8F;  // Width
        float playerHeight = 1.9F; // Height

        float boxSizeX = playerWidth;
        float boxSizeY = playerHeight;
        float boxSizeZ = playerWidth;

        // Adjust the player's position for the bounding box
        double playerPosX = user.getMovementProcessor().getTo().getPosX();
        double playerPosY = user.getMovementProcessor().getTo().getPosY();
        double playerPosZ = user.getMovementProcessor().getTo().getPosZ();

        // Create the bounding box
        BoundingBox boundingBox = new BoundingBox(
                playerPosX - boxSizeX / 2,
                playerPosY + 1.0,
                playerPosZ - boxSizeZ / 2,
                playerPosX + boxSizeX / 2,
                playerPosY + boxSizeY,
                playerPosZ + boxSizeZ / 2
        );

        boundingBox.expand(0.005, 0, 0.005);

        boolean water = false;

        loop:
        {
            Set<CollideEntry> collidedBlocks = boundingBox.getCollidedBlocks(user);

            for (CollideEntry collideEntry : collidedBlocks) {
                if (collideEntry.getBlock() == Material.WATER || collideEntry.getBlock() == Material.STATIONARY_WATER) {
                    water = true;
                    break loop;
                }
            }
        }

        this.waterFully = water;
    }

    public void checkLavaFull(PlayerData user) {
        // Adjust these values based on your specific needs
        float playerWidth = 0.8F;  // Width
        float playerHeight = 1.9F; // Height

        float boxSizeX = playerWidth;
        float boxSizeY = playerHeight;
        float boxSizeZ = playerWidth;

        // Adjust the player's position for the bounding box
        double playerPosX = user.getMovementProcessor().getTo().getPosX();
        double playerPosY = user.getMovementProcessor().getTo().getPosY();
        double playerPosZ = user.getMovementProcessor().getTo().getPosZ();

        // Create the bounding box
        BoundingBox boundingBox = new BoundingBox(
                playerPosX - boxSizeX / 2,
                playerPosY + 1.0,
                playerPosZ - boxSizeZ / 2,
                playerPosX + boxSizeX / 2,
                playerPosY + boxSizeY,
                playerPosZ + boxSizeZ / 2
        );

        boundingBox.expand(0.005, 0, 0.005);

        boolean lava = false;

        loop:
        {
            Set<CollideEntry> collidedBlocks = boundingBox.getCollidedBlocks(user);

            for (CollideEntry collideEntry : collidedBlocks) {
                if (collideEntry.getBlock() == Material.LAVA || collideEntry.getBlock() == Material.STATIONARY_LAVA) {
                    lava = true;
                    break loop;
                }
            }
        }

        this.lavaFully = lava;
    }

    public void checkLava(PlayerData user) {
        FlyingLocation to = user.getMovementProcessor().getTo();
        boolean lava = false;

        BoundingBox boundingBox = new BoundingBox(
                (float) to.getPosX(),
                (float) to.getPosY(),
                (float) to.getPosZ(),
                (float) to.getPosX(),
                (float) to.getPosY(),
                (float) to.getPosZ())
                .subtractY(-1F)
                .addY(0.03F)
                .shrink(0.01F, 0, 0.01F);
        Set<CollideEntry> collidedBlocks = boundingBox.getCollidedBlocks(user);

        for (CollideEntry collideEntry : collidedBlocks) {
            if (collideEntry.getBlock() == Material.STATIONARY_LAVA) {
                lava = true;
                break;
            }
        }

        this.lava = lava;
    }


    public void checkNewServerPosGround(PlayerData playerData) {
        boolean ground = false;

        BoundingBox boundingBox = new BoundingBox(
                playerData.getMovementProcessor().getTo().getPosX(),
                playerData.getMovementProcessor().getTo().getPosY(),
                playerData.getMovementProcessor().getTo().getPosZ(),
                playerData.getMovementProcessor().getTo().getPosX(),
                playerData.getMovementProcessor().getTo().getPosY(),
                playerData.getMovementProcessor().getTo().getPosZ())
                .expand(0.303, .303, 0.303);
        Set<CollideEntry> collidedBlocks = getCachedCollidedBlocks(playerData, boundingBox);

        for (CollideEntry collideEntry : collidedBlocks) {
            if (collideEntry.getBlock() != Material.AIR &&
                    collideEntry.getBlock() != STATIONARY_WATER &&
                    collideEntry.getBlock() != Material.STATIONARY_LAVA &&
                    collideEntry.getBlock() != Material.WATER &&
                    collideEntry.getBlock() != Material.LAVA &&
                    collideEntry.getBlock() != Material.WEB) {
                ground = true;
                break;
            }
        }

        this.serverPosGround = ground;
    }


    public void process(CollideEntry collideEntry, PlayerData playerData) {

        Material material = collideEntry.getBlock();
        Class<? extends MaterialData> blockData = material.getData();

        double minY = collideEntry.getBoundingBox().minY;

        if (material.isSolid()) {
            //  this.serverGround = true;
            this.material = material;
        }


        switch (material) {
            case ANVIL -> {
                this.anvil = true;
            }
            case HOPPER -> {
                this.hopper = true;
            }
            case ACACIA_DOOR, BIRCH_DOOR, IRON_DOOR, JUNGLE_DOOR, SPRUCE_DOOR, TRAP_DOOR, WOOD_DOOR, WOODEN_DOOR, IRON_TRAPDOOR, DARK_OAK_DOOR -> {
                this.door = true;
            }
            case CAULDRON -> {
                this.cauldron = true;
            }
            case COBBLE_WALL -> {
                this.wall = true;
            }
            case PISTON_BASE, PISTON_EXTENSION, PISTON_MOVING_PIECE, PISTON_STICKY_BASE -> {
                this.piston = true;
            }
            case ENDER_PORTAL, ENDER_PORTAL_FRAME -> {
                this.enderPortal = true;
            }
            case SOUL_SAND -> {
                this.soulSand = true;
            }
            case WEB -> {
                this.webFullCheck = true;
            }
            case CARPET -> {
                this.carpet = true;
            }
            case WATER_LILY -> {
                this.lillyPad = true;
            }
            case SNOW, SNOW_BLOCK -> {

                // detects if the snow block has incomplete layers "dips"
                if (material == Material.SNOW && (collideEntry.getBoundingBox().getMaximum().getY() % 1) != .875) {
                    this.snowHasIncompleteLayer = true;
                }

                snow = true;
            }
            case VINE, LADDER -> {
                this.climbable = true;
            }
            case SLIME_BLOCK -> {
                this.slime = true;
            }
            case ICE, PACKED_ICE -> {
                this.ice = true;
            }
            case LAVA, STATIONARY_LAVA, STATIONARY_WATER, WATER -> {
                this.liquid = true;
            }
            case SANDSTONE_STAIRS, SMOOTH_STAIRS, SPRUCE_WOOD_STAIRS, ACACIA_STAIRS, BIRCH_WOOD_STAIRS, BRICK_STAIRS, COBBLESTONE_STAIRS, DARK_OAK_STAIRS, JUNGLE_WOOD_STAIRS, NETHER_BRICK_STAIRS, QUARTZ_STAIRS, RED_SANDSTONE_STAIRS, WOOD_STAIRS -> {
                this.stair = true;
                this.halfBlock = true;
            }
            case BREWING_STAND, CHEST, TRAPPED_CHEST, ENDER_CHEST, ENCHANTMENT_TABLE, IRON_BARDING, FENCE, FENCE_GATE, ACACIA_FENCE, BIRCH_FENCE, ACACIA_FENCE_GATE, DARK_OAK_FENCE, IRON_FENCE, JUNGLE_FENCE, BIRCH_FENCE_GATE, DARK_OAK_FENCE_GATE, JUNGLE_FENCE_GATE, NETHER_FENCE, SPRUCE_FENCE, SPRUCE_FENCE_GATE, STAINED_GLASS_PANE, BED_BLOCK, SKULL, BED -> {

                this.halfBlock = true;

            }
        }
        switch (material) {
            case BED_BLOCK, BED -> {
                this.bed = true;
            }
        }
        if (material == Material.STEP || blockData == Step.class || blockData == WoodenStep.class) {
            this.halfBlock = true;
            this.slab = true;
        }


        if (this.slab || this.stair) {
            this.halfBlock = true;
        }


        if (this.halfBlock) {
            double y = Math.abs(this.lastBoundingBoxY - minY);
            double round = y % 1;

            if ((round == .5 || round == 1.5) || (round > .4995 && round < .732)) {
                this.movingUp = true;
            }
        }

        this.lastBoundingBoxY = minY;
    }
}