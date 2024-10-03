package pro.cyrent.anticheat.impl.processor.connection.tracker;


import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.util.block.box.HydroBB;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class NewTrackedEntity {

    public static final float WIDTH = 0.6F;
    public static final float HEIGHT = 1.8F;
    public static final float EXPAND = WIDTH / 2.0F;

    public final int entityID;

    public double serverPosX;
    public double serverPosY;
    public double serverPosZ;

    public FlyingLocation nextReach;

    public boolean confirming = false;
    public boolean teleport = false;

    public int trackedLocations;
    public int teleports;

    public boolean reallyUsingPrePost;

    public final List<PossiblePosition> positions = new CopyOnWriteArrayList<>();
    public final List<PossiblePosition> startPositions = new CopyOnWriteArrayList<>();

    public void initial(FlyingLocation initial) {
        PossiblePosition position = new PossiblePosition();

        int posX = Anticheat.INSTANCE.getServerVersion() == 18 ?
                (int) initial.getPosX() : (int) initial.getPosX() * 32;

        int posY = Anticheat.INSTANCE.getServerVersion() == 18 ?
                (int) initial.getPosY() : (int) initial.getPosY() * 32;

        int posZ = Anticheat.INSTANCE.getServerVersion() == 18 ?
                (int) initial.getPosZ() : (int) initial.getPosZ() * 32;

        this.serverPosX = posX;
        this.serverPosY = posY;
        this.serverPosZ = posZ;

        position.setPosition(posX, posY, posZ);

        this.positions.add(position);
        this.startPositions.add(position);
    }

    public void update() {
        for (PossiblePosition position : positions) {
            position.onLivingUpdate();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PossiblePosition {
        private int increment;

        private double otherPlayerMPX;
        private double otherPlayerMPY;
        private double otherPlayerMPZ;
        private double otherPlayerMPYaw;

        private double rotationYaw;
        private double rotationPitch;
        private double otherPlayerMPPitch;

        public double posX;
        public double posY;
        public double posZ;

        private HydroBB entityBoundingBox;

        public boolean skip;

        public void onLivingUpdate() {
            if (increment > 0) {

                double d0 = posX + (otherPlayerMPX - posX) / (double) increment;
                double d1 = posY + (otherPlayerMPY - posY) / (double) increment;
                double d2 = posZ + (otherPlayerMPZ - posZ) / (double) increment;

                --increment;

                setPosition(d0, d1, d2);
            } else {
                setPosition(posX, posY, posZ);
            }
        }

        public void setPositionAndRotation2(double x, double y, double z, double yaw, double pitch) {
            otherPlayerMPX = x;
            otherPlayerMPY = y;
            otherPlayerMPZ = z;
            otherPlayerMPYaw = yaw;
            otherPlayerMPPitch = pitch;
            increment = 3;
        }

        public void setPosition(double x, double y, double z) {
            posX = x;
            posY = y;
            posZ = z;

            float f = 0.6F / 2.0F;
            float f1 = 1.8F;

            entityBoundingBox = new HydroBB(x - (double) f, y, z - (double) f, x + (double) f, y + (double) f1, z + (double) f);
        }

        public PossiblePosition clone() {
            return new PossiblePosition(
                    increment,

                    otherPlayerMPX,
                    otherPlayerMPY,
                    otherPlayerMPZ,
                    otherPlayerMPYaw,

                    rotationYaw,
                    rotationPitch,

                    otherPlayerMPPitch,
                    posX,
                    posY,
                    posZ,

                    entityBoundingBox,

                    skip);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PossiblePosition that = (PossiblePosition) o;
            return hashCode() == that.hashCode();
        }

        @Override
        public int hashCode() {
            return Objects.hash(increment,
                    roundToPlace(otherPlayerMPX, 2),
                    roundToPlace(otherPlayerMPY, 2),
                    roundToPlace(otherPlayerMPZ, 2),
                    roundToPlace(posX, 2),
                    roundToPlace(posY, 2),
                    roundToPlace(posZ, 2));
        }

        private double roundToPlace(double value, int places) {
            double multiplier = Math.pow(10, places);

            return Math.round(value * multiplier) / multiplier;
        }
    }
}