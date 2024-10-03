package pro.cyrent.anticheat.util.block.box;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.block.BlockUtil;
import pro.cyrent.anticheat.util.block.collide.CollideEntry;
import pro.cyrent.anticheat.util.block.wrap.WrappedBlock;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.math.MathHelper;
import pro.cyrent.anticheat.util.math.MathUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("ALL")
public class BoundingBox {

    @Getter
    public double minX, minY, minZ, maxX, maxY, maxZ;

    public BoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public BoundingBox(Vector min, Vector max) {
        this.minX = (float) Math.min(min.getX(), max.getX());
        this.minY = (float) Math.min(min.getY(), max.getY());
        this.minZ = (float) Math.min(min.getZ(), max.getZ());
        this.maxX = (float) Math.max(min.getX(), max.getX());
        this.maxY = (float) Math.max(min.getY(), max.getY());
        this.maxZ = (float) Math.max(min.getZ(), max.getZ());
    }

    public BoundingBox add(double x, double y, double z) {
        double newMinX = minX + x;
        double newMaxX = maxX + x;
        double newMinY = minY + y;
        double newMaxY = maxY + y;
        double newMinZ = minZ + z;
        double newMaxZ = maxZ + z;

        return new BoundingBox(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }

    public BoundingBox add(Vector vector) {
        double x = (double) vector.getX(), y = (double) vector.getY(), z = (double) vector.getZ();

        double newMinX = minX + x;
        double newMaxX = maxX + x;
        double newMinY = minY + y;
        double newMaxY = maxY + y;
        double newMinZ = minZ + z;
        double newMaxZ = maxZ + z;

        return new BoundingBox(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }

    public BoundingBox set(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;

        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        return this;
    }

    public BoundingBox expandMax(double x, double y, double z) {
        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;
        return this;
    }

    public BoundingBox expandMin(double x, double y, double z) {
        this.minX += x;
        this.minY += y;
        this.minZ += z;
        return this;
    }

    public BoundingBox expand(double x, double y, double z) {
        this.minX -= x;
        this.minY -= y;
        this.minZ -= z;
        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;
        return this;
    }

    public BoundingBox addXYZ(double x, double y, double z) {
        this.minX += x;
        this.minY += y;
        this.minZ += z;
        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;
        return this;
    }

    public BoundingBox grow(double x, double y, double z) {
        this.minX -= x;
        this.minY -= y;
        this.minZ -= z;

        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;

        return this;
    }
    public BoundingBox shrink(double x, double y, double z) {
        double newMinX = minX + x;
        double newMaxX = maxX - x;
        double newMinY = minY + y;
        double newMaxY = maxY - y;
        double newMinZ = minZ + z;
        double newMaxZ = maxZ - z;

        return new BoundingBox(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }

    public BoundingBox add(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new BoundingBox(this.minX + minX, this.minY + minY, this.minZ + minZ, this.maxX + maxX, this.maxY + maxY, this.maxZ + maxZ);
    }

    public BoundingBox subtract(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new BoundingBox(this.minX - minX, this.minY - minY, this.minZ - minZ, this.maxX - maxX, this.maxY - maxY, this.maxZ - maxZ);
    }

    public BoundingBox subtractY(double minY) {
        return new BoundingBox(this.minX, this.minY - minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public boolean intersectsWithBox(Vector vector) {
        return (vector.getX() > this.minX && vector.getX() < this.maxX) && ((vector.getY() > this.minY && vector.getY() < this.maxY) && (vector.getZ() > this.minZ && vector.getZ() < this.maxZ));
    }

   /* public AxisAlignedBB toGay() {
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }*/


    public BoundingBox addY(double y) {
        return new BoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY + y, this.maxZ);
    }

    public BoundingBox addMinY(float y) {
        return new BoundingBox(this.minX, this.minY + y, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public BoundingBox addMinY(double y) {
        return new BoundingBox(this.minX, this.minY + y, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public Set<CollideEntry> getCollidedBlocks(PlayerData user) {

        Set<CollideEntry> toReturn = new HashSet<>();

        int total = 0;

        int minX = MathUtil.floor(this.minX);
        int maxX = MathUtil.floor(this.maxX + 1);
        int minY = MathUtil.floor(this.minY);
        int maxY = MathUtil.floor(this.maxY + 1);
        int minZ = MathUtil.floor(this.minZ);
        int maxZ = MathUtil.floor(this.maxZ + 1);
        World world = user.getPlayer().getWorld();

        if (world == null) return toReturn;

        if (!user.getCollisionProcessor().isChunkLoaded()) {
            return toReturn;
        }

        Location loc = new Location(world, 0, 0, 0);
        BoundingBox boundingBox = new BoundingBox(0, 0, 0, 0, 0, 0);

        block:
        {
            for (double x = minX; x < maxX; x++) {
                for (double z = minZ; z < maxZ; z++) {
                    for (double y = minY - 1; y < maxY; y++) {

                        if (total++ > 30) break block;

                        loc.setX(x);
                        loc.setY(y);
                        loc.setZ(z);

                        boundingBox.set(minX, minY, minZ, maxX, maxY, maxZ);
                        boundingBox.grow(.3f, .35, .3f);

                        toReturn.add(new CollideEntry(Anticheat.INSTANCE.getInstanceManager()
                                .getInstance().getType(world, x, y, z), this, boundingBox));
                    }
                }
            }
        }

        return toReturn;
    }

    public Vector getMinimum() {
        return new Vector(minX, minY, minZ);
    }

    public Vector getMaximum() {
        return new Vector(maxX, maxY, maxZ);
    }

    public List<Block> getAllBlocks(Player player) {
        Location min = new Location(player.getWorld(), MathUtil.floor(minX), MathUtil.floor(minY), MathUtil.floor(minZ));
        Location max = new Location(player.getWorld(), MathUtil.floor(maxX), MathUtil.floor(maxY), MathUtil.floor(maxZ));
        List<Block> all = new CopyOnWriteArrayList<>();
        for (float x = (float) min.getX(); x < max.getX(); x++) {
            for (float y = (float) min.getY(); y < max.getY(); y++) {
                for (float z = (float) min.getZ(); z < max.getZ(); z++) {

                    Block block = BlockUtil.getBlock(new Location(player.getWorld(), x, y, z));

                    assert block != null;
                    if (!block.getType().equals(Material.AIR)) {
                        all.add(block);
                    }
                }
            }
        }
        return all;
    }

    public List<WrappedBlock> getBlocks(PlayerData data) {
        List<WrappedBlock> blocks = new ArrayList<>();
        for (int x = MathHelper.floor_double(this.minX); x <= MathHelper.floor_double(this.maxX); x++) {
            for (int y = MathHelper.floor_double(this.minY); y <= MathHelper.floor_double(this.maxY); y++) {
                for (int z = MathHelper.floor_double(this.minZ); z <= MathHelper.floor_double(this.maxZ); z++) {

               //     WrappedBlockState block = data.getWorldProcessor().getBlock(x, y, z);
                //    if (!block.getType().isAir()) {
               //         blocks.add(new WrappedBlock(block.getType(), x, y, z));
               //     }
                }
            }
        }
        return blocks;
    }

    public boolean collides(Vector vector) {
        return (vector.getX() >= this.minX && vector.getX() <= this.maxX) && ((vector.getY() >= this.minY && vector.getY() <= this.maxY) && (vector.getZ() >= this.minZ && vector.getZ() <= this.maxZ));
    }

    public boolean collidesHorizontally(Vector vector) {
        return (vector.getX() >= this.minX && vector.getX() <= this.maxX) && ((vector.getY() > this.minY && vector.getY() < this.maxY) && (vector.getZ() >= this.minZ && vector.getZ() <= this.maxZ));
    }


    public boolean b(BoundingBox var1) {
        if (var1.minX > this.maxX && var1.minX < this.minX) {
            if (var1.minZ > this.maxZ && var1.minZ < this.maxZ) {
                return var1.minY > this.maxY && var1.minY < this.maxY;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean collidesHorizontally(BoundingBox other) {
        BoundingBox otherBox = (BoundingBox) other;

        return otherBox.maxX >= this.minX && otherBox.minX <= this.maxX
                && otherBox.maxZ >= this.minZ && otherBox.minZ <= this.maxZ;
    }

    public boolean collidesVertically(Vector vector) {
        return (vector.getX() > this.minX && vector.getX() < this.maxX) && ((vector.getY() >= this.minY && vector.getY() <= this.maxY) && (vector.getZ() > this.minZ && vector.getZ() < this.maxZ));
    }


    public double rayTrace(Vector start, Vector direction, double maxDistance) {

        double startX = start.getX();
        double startY = start.getY();
        double startZ = start.getZ();

        Vector dir = direction.clone();

        double dirX = dir.getX();
        double dirY = dir.getY();
        double dirZ = dir.getZ();

        dir.setX(dirX == -0.0D ? 0.0D : dirX);
        dir.setY(dirY == -0.0D ? 0.0D : dirY);
        dir.setZ(dirZ == -0.0D ? 0.0D : dirZ);

        dirX = dir.getX();
        dirY = dir.getY();
        dirZ = dir.getZ();

        double divX = 1.0D / dirX;
        double divY = 1.0D / dirY;
        double divZ = 1.0D / dirZ;

        double tMin;
        double tMax;

        if (dirX >= 0.0D) {

            tMin = (getMinX() - startX) * divX;
            tMax = (getMaxX() - startX) * divX;

        } else {

            tMin = (getMaxX() - startX) * divX;
            tMax = (getMinX() - startX) * divX;
        }

        double tyMin;
        double tyMax;

        if (dirY >= 0.0D) {

            tyMin = (getMinY() - startY) * divY;
            tyMax = (getMaxY() - startY) * divY;

        } else {

            tyMin = (getMaxY() - startY) * divY;
            tyMax = (getMinY() - startY) * divY;
        }

        if (tMin <= tyMax && tMax >= tyMin) {

            if (tyMin > tMin) {
                tMin = tyMin;
            }

            if (tyMax < tMax) {
                tMax = tyMax;
            }

            double tzMin;
            double tzMax;

            if (dirZ >= 0.0D) {

                tzMin = (getMinZ() - startZ) * divZ;
                tzMax = (getMaxZ() - startZ) * divZ;

            } else {

                tzMin = (getMaxZ() - startZ) * divZ;
                tzMax = (getMinZ() - startZ) * divZ;

            }

            if (tMin <= tzMax && tMax >= tzMin) {

                if (tzMin > tMin) tMin = tzMin;

                if (tzMax < tMax) tMax = tzMax;

                return tMax < 0.0D || tMin >= maxDistance ? -1D : tMin;
            }
        }

        return -1D;
    }

    public double getEyeDistance(float pitch, FlyingLocation pos) {
        double nearestX = clamp(pos.getPosX(), minX, maxX);
        double nearestZ = clamp(pos.getPosZ(), minZ, maxZ);

        double distX = pos.getPosX() - nearestX;
        double distZ = pos.getPosZ() - nearestZ;

        double dist = MathUtil.hypot(distX, distZ);

        if (Math.abs(pitch) != 90) {
            dist /= Math.cos(Math.toRadians(pitch));
        }

        return dist;
    }

    public BoundingBox expandSpecial(double factor) {
        // Calculate expansion values for each dimension
        double expandX = (maxX - minX) * factor;
        double expandZ = (maxZ - minZ) * factor;

        // Expand the bounding box
        minX -= expandX;
        maxX += expandX;
        minZ -= expandZ;
        maxZ += expandZ;

        return this;
    }

    public boolean isColliding(BoundingBox playerBoundingBox) {
        // Check if any corner of the player's bounding box lies within the expanded bounding box of the block
        return collides(playerBoundingBox.getMinimum()) || collides(playerBoundingBox.getMaximum());
    }

    public double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public BoundingBox clone() {
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public String toString() {
        return "[" + minX + ", " + minY + ", " + minZ + ", " + maxX + ", " + maxY + ", " + maxZ + "]";
    }
}