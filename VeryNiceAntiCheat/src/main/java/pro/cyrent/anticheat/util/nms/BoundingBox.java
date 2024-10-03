package pro.cyrent.anticheat.util.nms;

import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;

import lombok.Getter;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.math.MathHelper;
import pro.cyrent.anticheat.util.math.Step;

import java.util.Collection;


@SuppressWarnings("ConstantConditions")
public class BoundingBox {

    @Getter
    public double minX, minY, minZ, maxX, maxY, maxZ;

    public BoundingBox() {

    }

    public BoundingBox(Vector vector, double width, double height, double depth) {
        this.minX =  new Vector(vector.getX() - width, vector.getY() - depth, vector.getZ() - width).getX();
        this.maxX =  new Vector(vector.getX() + width, vector.getY() + height, vector.getZ() + width).getX();
    }

    public BoundingBox(FlyingLocation vector, double width, double height, double depth) {
        this.minX =  new Vector(vector.getPosX() - width, vector.getPosY() - depth, vector.getPosZ() - width).getX();
        this.maxX =  new Vector(vector.getPosX() + width, vector.getPosY() + height, vector.getPosZ() + width).getX();
    }

    public double calculateYOffset(BoundingBox other, double offsetY) {
        if (other.maxX > this.minX && other.minX < this.maxX && other.maxZ > this.minZ && other.minZ < this.maxZ) {
            if (offsetY > 0.0D && other.maxY <= this.minY) {
                double d1 = this.minY - other.maxY;

                if (d1 < offsetY) {
                    offsetY = d1;
                }
            } else if (offsetY < 0.0D && other.minY >= this.maxY) {
                double d0 = this.maxY - other.minY;

                if (d0 > offsetY) {
                    offsetY = d0;
                }
            }

            return offsetY;
        } else {
            return offsetY;
        }
    }

    public BoundingBox(pro.cyrent.anticheat.util.location.Vector vector, double width, double height, double depth) {
        this.minX =  new Vector(vector.getX() - width, vector.getY() - depth, vector.getZ() - width).getX();
        this.maxX =  new Vector(vector.getX() + width, vector.getY() + height, vector.getZ() + width).getX();
    }

    public BoundingBox(Vector data) {
        this.minX =  (data.getX() - 0.4D);
        this.minY =  data.getY();
        this.minZ =  (data.getZ() - 0.4D);
        this.maxX =  (data.getX() + 0.4D);
        this.maxY =  (data.getY() + 1.9D);
        this.maxZ =  (data.getZ() + 0.4D);
    }


    public BoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public BoundingBox(Vector min, Vector max) {
        this.minX = Math.min(min.getX(), max.getX());
        this.minY = Math.min(min.getY(), max.getY());
        this.minZ = Math.min(min.getZ(), max.getZ());
        this.maxX = Math.max(min.getX(), max.getX());
        this.maxY = Math.max(min.getY(), max.getY());
        this.maxZ = Math.max(min.getZ(), max.getZ());
    }

    public BoundingBox(pro.cyrent.anticheat.util.location.Vector min, Vector max) {
        this.minX =  Math.min(min.getX(), max.getX());
        this.minY =  Math.min(min.getY(), max.getY());
        this.minZ =  Math.min(min.getZ(), max.getZ());
        this.maxX =  Math.max(min.getX(), max.getX());
        this.maxY =  Math.max(min.getY(), max.getY());
        this.maxZ =  Math.max(min.getZ(), max.getZ());
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

    public BoundingBox addXZ(double x, double z) {
        double newMinX = minX + x;
        double newMaxX = maxX + x;
        double newMinZ = minZ + z;
        double newMaxZ = maxZ + z;

        return new BoundingBox(newMinX, this.minY, newMinZ, newMaxX, this.maxY, newMaxZ);
    }

    public BoundingBox add(Vector vector) {
        double x =  vector.getX(), y =  vector.getY(), z =  vector.getZ();

        double newMinX = minX + x;
        double newMaxX = maxX + x;
        double newMinY = minY + y;
        double newMaxY = maxY + y;
        double newMinZ = minZ + z;
        double newMaxZ = maxZ + z;

        return new BoundingBox(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
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

    public BoundingBox expandXZ(double x, double z) {
        this.minX -= x;
        this.minZ -= z;
        this.maxX += x;
        this.maxZ += z;
        return this;
    }

    public double getEyeDistance(double pitch, FlyingLocation pos) {
        double nearestX = clamp(pos.getPosX(), minX, maxX);
        double nearestZ = clamp(pos.getPosZ(), minZ, maxZ);

        double distX = pos.getPosX() - nearestX;
        double distZ = pos.getPosZ() - nearestZ;

        double dist = Math.hypot(distX, distZ);

        if (Math.abs(pitch) != 90) {
            dist /= Math.cos(Math.toRadians(pitch));
        }

        return dist;
    }

    public double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public BoundingBox grow(double x, double y, double z) {
        double newMinX = minX - x;
        double newMaxX = maxX + x;
        double newMinY = minY - y;
        double newMaxY = maxY + y;
        double newMinZ = minZ - z;
        double newMaxZ = maxZ + z;

        return new BoundingBox(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }

    public BoundingBox growXZ(double x, double z) {
        double newMinX = minX - x;
        double newMaxX = maxX + x;
        double newMinZ = minZ - z;
        double newMaxZ = maxZ + z;

        return new BoundingBox(newMinX, this.minY, newMinZ, newMaxX, this.maxY, newMaxZ);
    }

    public BoundingBox growFuck(double x, double y, double z) {
        double newMinX = minX - x;
        double newMaxX = maxX + x;
        double newMinZ = minZ - z;
        double newMaxZ = maxZ + z;

        return new BoundingBox(newMinX, minY, newMinZ, newMaxX, maxY + y, newMaxZ);
    }

    public BoundingBox addCoord(double x, double y, double z) {
        if (x < 0.0D) {
            minX += x;
        } else if (x > 0.0D) {
            maxX += x;
        }

        if (y < 0.0D) {
            minY += y;
        } else if (y > 0.0D) {
            maxY += y;
        }

        if (z < 0.0D) {
            minZ += z;
        } else if (z > 0.0D) {
            maxZ += z;
        }

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

    public BoundingBox addY(double y) {
        return new BoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY + y, this.maxZ);
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


    public Vector getMinimum() {
        return new Vector(minX, minY, minZ);
    }

    public Vector getMaximum() {
        return new Vector(maxX, maxY, maxZ);
    }

    public boolean intersectsWithBox(BoundingBox otherBox) {
        return otherBox.maxX > this.minX && otherBox.minX < this.maxX && otherBox.maxY > this.minY && otherBox.minY < this.maxY && otherBox.maxZ > this.minZ && otherBox.minZ < this.maxZ;
    }


    public boolean collides(Vector vector) {
        return (vector.getX() >= this.minX && vector.getX() <= this.maxX) && ((vector.getY() >= this.minY && vector.getY() <= this.maxY) && (vector.getZ() >= this.minZ && vector.getZ() <= this.maxZ));
    }

    public boolean collides(Object other) {
        if (other instanceof BoundingBox) {
            BoundingBox otherBox = (BoundingBox) other;
            return otherBox.maxX >= this.minX && otherBox.minX <= this.maxX && otherBox.maxY >= this.minY && otherBox.minY <= this.maxY && otherBox.maxZ >= this.minZ && otherBox.minZ <= this.maxZ;
        }

        return false;
    }

    public boolean collidesNoReflection(BoundingBox otherBox) {
        return otherBox.maxX >= this.minX && otherBox.minX <= this.maxX
                && otherBox.maxY >= this.minY && otherBox.minY <= this.maxY
                && otherBox.maxZ >= this.minZ && otherBox.minZ <= this.maxZ;
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

    public boolean collidesHorizontally(Object other) {
        if (other instanceof BoundingBox) {
            BoundingBox otherBox = (BoundingBox) other;
            return otherBox.maxX >= this.minX && otherBox.minX <= this.maxX
                    && otherBox.maxY > this.minY && otherBox.minY < this.maxY
                    && otherBox.maxZ >= this.minZ && otherBox.minZ <= this.maxZ;
        }

        return false;
    }

    public boolean collidesHorizontallyNoReflection(BoundingBox otherBox) {
        return otherBox.maxX >= this.minX && otherBox.minX <= this.maxX
                && otherBox.maxY > this.minY && otherBox.minY < this.maxY
                && otherBox.maxZ >= this.minZ && otherBox.minZ <= this.maxZ;
    }

    public boolean collidesHorizontally(Object other, double addX, double addY, double addZ) {
        BoundingBox otherBox = (BoundingBox) other;

        return otherBox.maxX >= this.minX && otherBox.minX <= this.maxX
                && otherBox.maxZ >= this.minZ && otherBox.minZ <= this.maxZ;
    }

    public boolean collidesVertically(Vector vector) {
        return (vector.getX() > this.minX && vector.getX() < this.maxX) && ((vector.getY() >= this.minY && vector.getY() <= this.maxY) && (vector.getZ() > this.minZ && vector.getZ() < this.maxZ));
    }

    public boolean collidesVertically(Object other) {
        if (other instanceof BoundingBox) {
            BoundingBox otherBox = (BoundingBox) other;
            return otherBox.maxX > this.minX && otherBox.minX < this.maxX && otherBox.maxY >= this.minY && otherBox.minY <= this.maxY && otherBox.maxZ > this.minZ && otherBox.minZ < this.maxZ;
        }

        return false;
    }

    public boolean collidesVerticallyNoReflection(BoundingBox otherBox) {
        return otherBox.maxX > this.minX && otherBox.minX < this.maxX && otherBox.maxY >= this.minY
                && otherBox.minY <= this.maxY && otherBox.maxZ > this.minZ && otherBox.minZ < this.maxZ;
    }

    public double distance(FlyingLocation location) {
        double dx = Math.min(Math.abs(location.getPosX() - this.minX), Math.abs(location.getPosX() - this.maxX));
        double dz = Math.min(Math.abs(location.getPosZ() - this.minZ), Math.abs(location.getPosZ() - this.maxZ));

        return dx * dx + dz * dz;
    }

    public BoundingBox expandSpecial(double factor) {
        // Calculate expansion values for each dimension
        double expandX = (maxX - minX) * factor;
        double expandZ = (maxZ - minZ) * factor;

        double expandY = (maxY - minY) * factor;

        // Expand the bounding box
        minX -= expandX;
        maxX += expandX;

        minY -= expandY;
        maxY += expandY;

        minZ -= expandZ;
        maxZ += expandZ;

        return this;
    }

    public double distanceXZ(double d, double d2) {
        if (this.containsXZ(d, d2)) {
            return 0.0;
        }
        double d3 = Math.min(Math.pow(d - this.minX, 2.0), Math.pow(d - this.maxX, 2.0));
        double d4 = Math.min(Math.pow(d2 - this.minZ, 2.0), Math.pow(d2 - this.maxZ, 2.0));
        return MathHelper.sqrt_double(d3 + d4);
    }

    public boolean containsXZ(double d, double d2) {
        return this.minX <= d && this.minZ >= d && this.maxX <= d2 && this.maxZ >= d2;
    }

    public BoundingBox clone() {
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public String toString() {
        return "[" + minX + ", " + minY + ", " + minZ + ", " + maxX + ", " + maxY + ", " + maxZ + "]";
    }

    public void draw(Collection<? extends Player> players) {
        BoundingBox box = clone().expand(0.025, 0.025, 0.025);
        this.drawCuboid(box, players);
    }

    private void drawCuboid(BoundingBox box, Collection<? extends Player> players) {
        Step.GenericStepper<Float> x = Step.step((float) box.minX, 0.241f, (float) box.maxX);
        Step.GenericStepper<Float> y = Step.step((float) box.minY, 0.241f, (float) box.maxY);
        Step.GenericStepper<Float> z = Step.step((float) box.minZ, 0.241f, (float) box.maxZ);
        for (float fx : x) {
            for (float fy : y) {
                for (float fz : z) {
                    int check = 0;
                    if (x.first() || x.last()) check++;
                    if (y.first() || y.last()) check++;
                    if (z.first() || z.last()) check++;
                    if (check >= 2) {

                        Particle particleType = new Particle(
                                ParticleTypes.FLAME
                        );

                        WrapperPlayServerParticle particle = new WrapperPlayServerParticle(
                                particleType, false, new Vector3d(fx, fy, fz),
                                new Vector3f(0, 0, 0),
                                0, 1
                        );

                        for (Player p : players) {
                            PlayerData user = Anticheat.INSTANCE.getUserManager().getUser(p);
                            user.getTransactionProcessor().sendPacket(particle, user.getPlayer());
                        }
                    }
                }
            }
        }
    }
}
