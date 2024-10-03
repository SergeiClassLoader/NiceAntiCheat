package pro.cyrent.anticheat.util.location;

import com.github.retrooper.packetevents.util.Vector3d;
import pro.cyrent.anticheat.util.block.box.BoundingBox;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

@Getter @Setter
public class FlyingLocation {
    private int tick;
    private double posX;
    private double posY;
    private double posZ;
    private float yaw;
    private float pitch;
    private boolean onGround;

    // DON'T FUCKING STORE THE WORLD PER PLAYER IT CAUSES A MEMORY LEAK!!!!!!! (I
    private String world;
    private long timeStamp;

    public FlyingLocation() {
    }

    public void setFlyingLocation(FlyingLocation flyingLocation) {
        this.posX = flyingLocation.posX;
        this.posY = flyingLocation.posY;
        this.posZ = flyingLocation.posZ;
        this.yaw = flyingLocation.yaw;
        this.pitch = flyingLocation.pitch;
        this.world = flyingLocation.getWorld();
        this.timeStamp = System.currentTimeMillis();
    }

    public FlyingLocation(Location flyingLocation) {
        this.posX = flyingLocation.getX();
        this.posY = flyingLocation.getY();
        this.posZ = flyingLocation.getZ();
        this.yaw = flyingLocation.getYaw();
        this.pitch = flyingLocation.getPitch();
        this.world = flyingLocation.getWorld().getName();
        this.timeStamp = System.currentTimeMillis();
    }
    public FlyingLocation(double d0, double d1, double d2) {
        this.posX = d0;
        this.posY = d1;
        this.posZ = d2;
        this.timeStamp = System.currentTimeMillis();
    }

    public FlyingLocation(int tick, double posX, double posY, double posZ) {
        this.tick = tick;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.timeStamp = System.currentTimeMillis();
    }

    public FlyingLocation(int tick, double posX, double posY, double posZ, float yaw, float pitch) {
        this.tick = tick;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.timeStamp = System.currentTimeMillis();
    }

    public FlyingLocation(String world,double posX, double posY, double posZ) {
        this.world = world;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.timeStamp = System.currentTimeMillis();
    }

    public FlyingLocation(String world, int tick, double posX, double posY, double posZ) {
        this.world = world;
        this.tick = tick;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.timeStamp = System.currentTimeMillis();
    }

    public FlyingLocation(String world, double posX, double posY, double posZ, float yaw, float pitch) {
        this.world = world;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.timeStamp = System.currentTimeMillis();
    }

    public FlyingLocation(double posX, double posY, double posZ, float yaw, float pitch) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.timeStamp = System.currentTimeMillis();
    }

    public FlyingLocation(String world, int tick, double posX, double posY, double posZ, float yaw, float pitch,
                          boolean onGround) {
        this.world = world;
        this.tick = tick;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.timeStamp = System.currentTimeMillis();
    }

    public Vector toVector() {
        return new Vector(this.posX, this.posY, this.posZ);
    }


    public Location toLocation(World world) {
        return new Location(world, this.posX, this.posY, this.posZ, this.yaw, this.pitch);
    }

    public FlyingLocation add(double x, double y, double z) {
        return new FlyingLocation(this.tick, this.posX + x, this.posY + y,
                this.posZ + z, this.yaw, this.pitch);
    }

    public FlyingLocation clone() {
        return new FlyingLocation(
                this.world,
                this.tick,
                this.posX,
                this.posY,
                this.posZ,
                this.yaw,
                this.pitch,
                this.onGround
        );
    }

    public double distanceSquaredXZ(FlyingLocation o) {
        if (o.getWorld() != null && getWorld() != null && o.getWorld() == getWorld()) {
            return NumberConversions.square(this.getPosX() - o.getPosX())
                    + NumberConversions.square(this.getPosZ() - o.getPosZ());
        }
        return 0.0;
    }

    public BoundingBox toCollisionBox() {
        return toBB(0.3, 1.8)
                .expand(0.001, 0.001, 0.001);
    }

    public BoundingBox toBB(double width, double height) {
        return new BoundingBox(
                new Vector(this.posX - width, this.posY, this.posZ - width),
                new Vector(this.posX + width, this.posY + height, this.posZ + width));
    }

    public BoundingBox toBB(double width, double depth, double height) {
        return new BoundingBox(
                new Vector(this.posX - width, this.posY - depth, this.posZ - width),
                new Vector(this.posX + width, this.posY + height, this.posZ + width));
    }

    public double distanceSquaredXZ(Vector3d o) {
        if (getWorld() != null) {
            return NumberConversions.square(this.getPosX() - o.getX())
                    + NumberConversions.square(this.getPosZ() - o.getZ());
        }
        return 0.0;
    }

    public double distanceXZ(Vector3d o) {
        if (getWorld() != null) {
            return this.getPosX() - o.getX()
                    + this.getPosZ() - o.getZ();
        }
        return 0.0;
    }

    public double distanceXZ(Location o) {
        if (getWorld() != null && o.getWorld() != null && o.getWorld().getName()
                .equalsIgnoreCase(getWorld())) {
            return this.getPosX() - o.getX()
                    + this.getPosZ() - o.getZ();
        }
        return Double.MAX_VALUE;
    }


    public double deltaYAbs(FlyingLocation o) {
        if (o.getWorld() != null && getWorld() != null && o.getWorld() == getWorld()) {
            return Math.abs(o.getPosY() - this.posY);
        }
        return 0.0;
    }

    public String toString() {
        return "[" + this.posX + ", " + this.posY + ", " + this.posZ + "]";
    }
}