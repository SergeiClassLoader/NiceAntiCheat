package pro.cyrent.anticheat.impl.processor.world;

import org.bukkit.block.Block;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.block.BlockResult;
import pro.cyrent.anticheat.util.block.BlockUtil;
import pro.cyrent.anticheat.util.block.box.BoundingBox;
import pro.cyrent.anticheat.util.block.collide.CollideEntry;
import pro.cyrent.anticheat.util.event.EventTimer;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.util.NumberConversions;
import pro.cyrent.anticheat.util.location.FlyingLocation;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.Set;

@Deprecated
@Getter
public class CollisionProcessor extends Event {
    private final PlayerData data;
    private int liquidFullyTicks;
    private int entityUpdateTicks;
    private int liquidTicks;
    private boolean web, webInside, webFullCheck;
    private int mountTicks;
    private int webTicks;
    private int movingTicks;
    private boolean waterFully, lavaFully, water, lava;

    private boolean serverGround, lastServerGround;

    private int nearBoatTicks;

    private int movingUpFix, nearCartTicks;

    private final EventTimer lastEnderDragonNearTimer;

    private final EventTimer lastUnloadedChunkTimer;

    private boolean chunkLoaded;

    @Setter
    private boolean boat, dragon, cart;


    private double entityYLocation;

    private int flowingTicks;


    private int blockX;
    private int blockY;
    private int blockZ;

    private int webFullTicks;

    private Material currentMaterial;

    public CollisionProcessor(PlayerData user) {
        this.data = user;
        this.lastUnloadedChunkTimer = new EventTimer(20, user);
        this.lastEnderDragonNearTimer = new EventTimer(20, user);
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.isMovement()) {
                WrapperPlayClientPlayerFlying flying =
                        new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());
                try {
                    this.checkEntities();
                } catch (Exception ignored) {
                }

                if (this.getData().getPlayer().getVehicle() != null) {
                    this.mountTicks += this.mountTicks < 20 ? 1 : 0;
                } else {
                    this.mountTicks -= this.mountTicks > 0 ? 1 : 0;
                }

                World world = getData().getPlayer().getWorld();

                if (world == null || getData().getMovementProcessor().getTo() == null) {
                    return;
                }

                if (flying.hasPositionChanged()) {

                    Location location = getData()
                            .getMovementProcessor().getTo().toLocation(world);

                    if (location == null) {
                        return;
                    }

                    boolean checkChunk = getData().getGhostBlockProcessor().getLastInvalidTick() < 7
                            || getData().getActionProcessor().isTeleportingV3()
                            || getData().getLastTeleport().getDelta() < 100
                            || getData().getMovementProcessor().getTick() < 100;

                    if (checkChunk) {
                        this.chunkLoaded = BlockUtil.isChunkLoaded(location);
                    }

                    if (!this.chunkLoaded) {
                        this.lastUnloadedChunkTimer.reset();
                    }

                }

                boolean badVector = Math.abs(this.getData().getMovementProcessor()
                        .getTo().toVector().length()
                        - this.getData().getMovementProcessor().getFrom().toVector().length()) >= 1;

                this.getData().setLastBoundingBox(getData().getBoundingBox());
                this.getData().setBoundingBox(new BoundingBox((badVector ?
                        this.getData().getMovementProcessor().getTo().toVector()
                        : this.getData().getMovementProcessor().getFrom().toVector()),
                        this.getData().getMovementProcessor().getTo().toVector())
                        .grow(0.3F, 0, 0.3F)
                        .add(0, 0, 0, 0, 1.84F, 0));


                this.blockX = NumberConversions.floor(getData().getMovementProcessor().getTo().getPosX());
                this.blockY = NumberConversions.floor(getData().getMovementProcessor().getTo().getPosY());
                this.blockZ = NumberConversions.floor(getData().getMovementProcessor().getTo().getPosZ());

                ++this.flowingTicks;

                // Water & lava flow checker
                if (world.isChunkLoaded(this.blockX >> 4, this.blockZ >> 4) && (this.lava || this.water)) {
                    Block block = getData().getPlayer().getLocation().getBlock();

                    if (block != null && block.getType() != null && (block.getType() == Material.WATER
                            || block.getType() == Material.STATIONARY_WATER
                            || block.getType() == Material.LAVA
                            || block.getType() == Material.STATIONARY_LAVA)) {
                        byte data = block.getData();

                        if (data != 0) {
                            this.flowingTicks = 0;
                        }
                    }
                }


                if (getData().getBoundingBox() != null) {

                    Set<CollideEntry> collideEntries =
                            getData().getBoundingBox().getCollidedBlocks(this.getData());

                    if (collideEntries != null) {

                        final BlockResult blockResult = new BlockResult();

                        blockResult.checkWeb(getData());
                        blockResult.checkWebFull(getData());
                        blockResult.checkWebInside(getData());
                        blockResult.checkWaterFull(getData());
                        blockResult.checkLavaFull(getData());
                        blockResult.checkWater(getData());
                        blockResult.checkLava(getData());
                        blockResult.checkNewServerGround(getData());

                        this.processTicks(blockResult);

                        collideEntries.clear();
                    }
                }
            }
        }
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")

    void checkEntities() {

        if (getData() == null || getData().generalCancel() || getData().getPlayer() == null || getData().getPlayer().getWorld() == null
                || getData().getLastWorldChange() == null || getData().getLastWorldChange().getDelta() < 3) {
            return;
        }

        World world = getData().getPlayer().getWorld();

        if (getData().getMovementProcessor().getTo() == null) return;

        FlyingLocation location = getData().getMovementProcessor().getTo();

        Entity[] entities = Anticheat.INSTANCE.getInstanceManager().getInstance().getEntities(
                world,
                (int) location.getPosX(),
                (int) location.getPosZ()
        );

        if (entities == null || entities.length < 1) return;

        boolean boat = false;
        boolean cart = false;
        boolean dragon = false;

        for (int i = 0; i < entities.length; i++) {
            Entity entity = entities[i];

            // Check if entity is null or we have reached the end of the array
            if (entity == null) break;

            if (entity.getLocation() == null) continue;

            double distance = location.distanceXZ(entity.getLocation());

            double yDistance = Math.abs(this.entityYLocation - location.getPosY());
            boolean isChanging = Math.abs(entity.getLocation().getY() - this.entityYLocation) > 0;

            if (entity.equals(getData().getPlayer()) || distance > 2.5 || yDistance > 3 && !isChanging) {
                continue;
            }

            String entityName = entity.getType().name();

            switch (entityName) {
                case "BOAT": {
                    boat = true;
                    this.nearBoatTicks = 5;
                    break;
                }

                case "MINECART": {
                    cart = true;
                    this.nearCartTicks = 5;
                    break;
                }

                case "ENDER_DRAGON": {
                    dragon = true;
                    this.lastEnderDragonNearTimer.reset();
                    break;
                }
            }

            this.entityYLocation = entity.getLocation().getY();
        }

        this.boat = boat;
        this.dragon = dragon;
        this.cart = cart;

        if (this.cart) {
            this.nearCartTicks = 5;
        } else {
            this.nearCartTicks -= Math.min(this.nearCartTicks, 1);
        }

        if (this.boat) {
            this.nearBoatTicks = 5;
        } else {
            this.nearBoatTicks -= Math.min(this.nearBoatTicks, 1);
        }
    }


    private void processTicks(BlockResult blockResult) {

        this.currentMaterial = blockResult.material;

        this.waterFully = blockResult.waterFully;
        this.lavaFully = blockResult.isLavaFully();

        if (getData().getMovementProcessor().getDeltaY() <= 0.0
                || getData().getMovementProcessor().getFrom().getPosY() >=
                getData().getMovementProcessor().getTo().getPosY() ) {
            this.movingUpFix = 0;
        } else {
            if (this.movingUpFix < 20) {
                this.movingUpFix++;
            }
        }

        if (blockResult.isLavaFully() || blockResult.isWaterFully()) {
            this.liquidFullyTicks += this.liquidFullyTicks < 20 ? 1 : 0;
        } else {
            this.liquidFullyTicks -= this.liquidFullyTicks > 0 ? 1 : 0;
        }

        if (blockResult.isMovingUp()) {
            this.movingTicks += this.movingTicks < 50 ? 10 : 0;
        } else {
            this.movingTicks -= this.movingTicks > 0 ? 1 : 0;
        }

        this.web = blockResult.web;
        this.webInside = blockResult.webInside;
        this.webFullCheck = blockResult.webFullCheck;

        if (blockResult.isWeb()) {
            this.webTicks += (this.webTicks < 20 ? 1 : 0);
        } else {
            this.webTicks -= (this.webTicks > 0 ? 1 : 0);
        }

        if (blockResult.isLiquid()) {
            this.liquidTicks += this.liquidTicks < 5 ? 5 : 0;
        } else {
            this.liquidTicks -= this.liquidTicks > 0 ? 1 : 0;
        }

        if (blockResult.webFullCheck) {
            this.webFullTicks += (this.webFullTicks < 20 ? 1 : 0);
        } else {
            this.webFullTicks -= (this.webFullTicks > 0 ? 1 : 0);
        }


        this.lastServerGround = blockResult.lastServerGround;
        this.serverGround = blockResult.serverGround;


        this.lava = blockResult.isLava();
        this.water = blockResult.isWater();

    }
}
