package pro.cyrent.anticheat.impl.processor.prediction.simulator;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.processor.prediction.horizontal.HorizontalProcessor;
import pro.cyrent.anticheat.util.event.EventTimer;
import pro.cyrent.anticheat.util.math.MathUtil;
import pro.cyrent.anticheat.util.math.Motion;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

@Getter
@Setter
public class VelocitySimulator extends Event {

    private final PlayerData data;

    private boolean simulation = false;
    private double predictedY;
    private int ticks;
    private final EventTimer simulationTimer, lastVelocitySimulatedTimer;
    private long lastInvalidLocation;
    private double lastVelocityOffset;
    private double frameTicks;
    private int velocityTicks, velocitySimulateTicks;
    private long lastRun;
    private double posYBeforeSimulation;
    private Vector start;
    private int invalids;
    private int totals;
    private Float currentYaw;
    private Location toLocation;
    private boolean projectile = false;

    public VelocitySimulator(PlayerData user) {
        this.data = user;
        this.simulationTimer = new EventTimer(20, user);
        this.lastVelocitySimulatedTimer = new EventTimer(20, user);
    }

    public void onVelocity(HorizontalProcessor.TransactionVelocityEntry velocityEntry) {

        if (this.getData().getMovementProcessor().getTo().isOnGround()
                && getData().getCollisionWorldProcessor().isGround()) {
            this.predictedY = velocityEntry.getY();
        }

        if (this.simulation) {
            this.invalids = 0;

            double x = velocityEntry.getX();
            double z = velocityEntry.getZ();

            this.posYBeforeSimulation = this.getData().getMovementProcessor().getTo().getPosY();
            this.lastVelocityOffset = (x * x) + (z * z);

            if (this.lastVelocityOffset > .0) {
                this.lastVelocityOffset /= 2.5;
            }

            this.velocitySimulateTicks = 0;
            this.velocityTicks = 0;
            this.ticks = 0;
            this.totals /= this.totals == 0 ? 1 : Math.min(this.totals, 2);

            this.triggerSimulator(false);
        }
    }

    public void onTick(long timestamp) {

        if (this.simulation) {

            if (this.ticks++ > 320 || this.getData().getPlayer().getHealth() < 3) {
                this.simulation = false;
                //     this.currentYaw = null;
                this.frameTicks = 0;
                this.ticks = 0;
            }

            if (this.ticks > 20
                    && getData().getCollisionWorldProcessor().isGround()
                    && this.getData().getMovementProcessor().getTo().isOnGround()) {
                this.simulation = false;
                this.frameTicks = 0;
                //     this.currentYaw = null;
                this.ticks = 0;
            }

            this.handleNextVertical();
            this.velocitySimulateTicks++;
        }
    }

    private void handleNextVertical() {
        World world = getData().getPlayer().getWorld();

        if (world == null) return;

        if (this.velocityTicks++ > 1) {
            this.velocityTicks = 0;

            loop:
            {
                if (this.predictedY < 0) {
                    for (double yPosToCheck = 0; yPosToCheck < Math.abs(this.predictedY); yPosToCheck += 0.1) {
                        Material material = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(
                                world, this.getData().getMovementProcessor().getTo().getPosX(),
                                this.getData().getMovementProcessor().getTo().getPosY() + yPosToCheck,
                                this.getData().getMovementProcessor().getTo().getPosZ()
                        );

                        if (material != Material.AIR) {
                            break loop;
                        }
                    }

                    Material currentBelow = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(
                            world, this.getData().getMovementProcessor().getTo().getPosX(),
                            this.getData().getMovementProcessor().getTo().getPosY() - .1f,
                            this.getData().getMovementProcessor().getTo().getPosZ()
                    );

                    if (currentBelow != Material.AIR) {
                        break loop;
                    }
                }

                if (!this.getData().getCollisionProcessor().isChunkLoaded()) {
                    if (this.getData().getMovementProcessor().getTo().getPosY() > 0.0D) {
                        this.predictedY = -0.1D;
                    } else {
                        this.predictedY = 0.0D;
                    }
                } else {
                    this.predictedY -= 0.08D;
                }

                this.predictedY *= 0.9800000190734863D;
                this.simulation = true;
            }
        }
    }

    public void runSimulator(Motion motion, long timestamp) {

        World world = getData().getPlayer().getWorld();

        if (world == null) return;

        if (this.simulation && this.getData().getMovementProcessor().getTick() > 3 && this.currentYaw != null) {

            double expectedX = motion.getMotionX().get();
            double expectedZ = motion.getMotionZ().get();


            double yaw = Math.toRadians(this.projectile ? getData().getMovementProcessor().getTo().getYaw()
                    : this.currentYaw + 180);

            double distance = Math.hypot(expectedX, expectedZ)
                    + MathUtil.checkMax(this.lastVelocityOffset, .1)
                    + Anticheat.INSTANCE.getConfigValues().getVelocityAmount();

            // cancelled transactions, so we will force them back more since we don't have the data
            if (this.getData().getVelocityProcessor().isTriggerVelocitySimulations()
                    || getData().getVelocityProcessor().getTriggerTime() > 0) {
                distance = .85;
            }

            double deltaX = -Math.sin(yaw) * -distance;
            double deltaZ = Math.cos(yaw) * -distance;

            Location location = new Location(getData().getPlayer().getWorld(),
                    getData().getMovementProcessor().getTo().getPosX() + deltaX,
                    getData().getMovementProcessor().getTo().getPosY() + (this.getData().getMovementProcessor().getTo()
                            .isOnGround()
                            ? .1 : this.predictedY),
                    getData().getMovementProcessor().getTo().getPosZ() + deltaZ,
                    getData().getMovementProcessor().getTo().getYaw(),
                    getData().getMovementProcessor().getTo().getPitch()
            );

            double fromX = this.getData().getMovementProcessor().getFrom().getPosX();
            double fromZ = this.getData().getMovementProcessor().getFrom().getPosZ();

            double offsetX = Math.abs(location.getX() - fromX);
            double offsetZ = Math.abs(location.getZ() - fromZ);

            loop:
            {

                if (this.predictedY < 0) {
                    for (double offset = 0.1; offset < 1; offset += 0.05) {
                        Material below = Anticheat.INSTANCE.getInstanceManager().getInstance()
                                .getType(world, this.getData().getMovementProcessor().getTo().getPosX(),
                                        this.getData().getMovementProcessor().getTo().getPosY() - offset,
                                        this.getData().getMovementProcessor().getTo().getPosZ()
                                );

                        if (below != Material.AIR || this.totals++ > 300) {
                            this.lastInvalidLocation = timestamp;
                            this.simulation = false;
                            this.currentYaw = null;
                            break loop;
                        }
                    }
                }


                if (offsetX > 3 || offsetZ > 3) {
                    break loop;
                }

                int radius = 5;

                for (int bx = location.getBlockX() - radius; bx <= location.getBlockX() + radius; bx++) {
                    for (int bz = location.getBlockZ() - radius; bz <= location.getBlockZ() + radius; bz++) {

                        Material closestBlock = Anticheat.INSTANCE.getInstanceManager().getInstance()
                                .getType(world, location.getX(), location.getY(), location.getZ());

                        if (closestBlock != Material.AIR) {
                            this.lastInvalidLocation = timestamp;
                            this.simulation = false;
                            this.currentYaw = null;
                            break loop;
                        }
                    }
                }

                this.simulationTimer.resetBoth();


                if ((timestamp - this.lastRun) > 0L) {

                    this.lastRun = timestamp;
                    double offset = Math.abs(this.getData().getMovementProcessor().getLastDeltaY() -
                            this.getData().getMovementProcessor().getDeltaY());

                    //TODO: add better teleport checking
                    if (offset < 0.03 && this.invalids++ > 8 && getData().getVelocityProcessor().getTriggerTime() < 1) {

                        //TODO: find outt this??
                        this.simulationTimer.skip();

                        this.simulation = false;
                        this.lastVelocitySimulatedTimer.reset();
                        this.getData().getHorizontalProcessor().triggerTeleport();

                        break loop;
                    }

                    this.lastVelocitySimulatedTimer.reset();

                    if (this.simulation && this.velocitySimulateTicks < 2) {
                        this.start = location.toVector();
                    }

                    Anticheat.INSTANCE.getInstanceManager().getInstance().teleport(getData(), location);
                    this.toLocation = location;
                }
            }
        }
    }

    public void triggerSimulator(boolean vertical) {

        if (this.getData().getLastFallDamageTimer().hasNotPassed(20)) return;

        if (vertical) {
            this.getData().getVelocityProcessor().setTriggerVelocitySimulations(true);
        } else {
            this.getData().getVelocityProcessor().setTriggerVelocitySimulations(false);
        }

        if (!this.simulation) {
            this.ticks = 0;
        }

        this.totals = 0;
        this.simulation = true;

        // If for some reason the yaw is null from the entity that attacked last,
        // Then we set it the current players yaw.
        if (this.currentYaw == null) {
            //   this.currentYaw = getData().getMovementProcessor().getTo().getYaw();
        }
    }

    public boolean isSimulatorRunning() {
        return this.simulationTimer.getServerDelta() < 7;
    }
}

