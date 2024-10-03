package pro.cyrent.anticheat.impl.processor.world;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.event.EventTimer;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import lombok.Getter;
import org.bukkit.Material;


@Getter
public class ScaffoldProcessor extends Event {
    private final PlayerData data;

    private final EventTimer scaffoldTimerJump, scaffoldTimer;


    private final double[] scaffoldOffsets = {
            1.0,
            0.5,
            1.5,
            2.0,
    };


    public ScaffoldProcessor(PlayerData user) {
        this.data = user;
        this.scaffoldTimer = new EventTimer(20, user);
        this.scaffoldTimerJump = new EventTimer(20, user);
    }

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketReceiveEvent() != null) {
            if (event.isPlace()) {
                WrapperPlayClientPlayerBlockPlacement placement = new WrapperPlayClientPlayerBlockPlacement(event.getPacketReceiveEvent());

                if (placement.getBlockPosition() != null) {
                    // are there any blocks below the placed block location
                    boolean invalidBelow = false;

                    // loop through our offsets
                    for (double aDouble : this.scaffoldOffsets) {

                        // material for the offset
                        Material blockBelow = Anticheat.INSTANCE.getInstanceManager().getInstance()
                                .getType(getData().getPlayer().getWorld(), placement.getBlockPosition().getX(),
                                        placement.getBlockPosition().getY() - aDouble,
                                        placement.getBlockPosition().getZ());

                        // did we find any air?
                        if (blockBelow != Material.AIR) {
                            invalidBelow = true;
                            break;
                        }
                    }

                    if (invalidBelow) return;

                    double placeX = placement.getBlockPosition().getX();
                    double placeY = placement.getBlockPosition().getY();
                    double placeZ = placement.getBlockPosition().getZ();

                    int playerY = (int) this.getData().getMovementProcessor().getTo().getPosY();

                    if ((playerY - Math.round(placeY)) != 1) return;

                    int blocksAroundWithOffset = this.hasAirBlocksAroundWithOffset(placeX, placeY, placeZ, 1);
                    int blocksAround = this.hasAirBlocksAround(placeX, placeY, placeZ);

                    // are our blocks around the player invalid?
                    if (blocksAround > 1 || blocksAroundWithOffset != 0) return;

                    this.scaffoldTimerJump.reset();

                    if (getData().getMovementProcessor().getTo().isOnGround()
                            && getData().getMovementProcessor().getFrom().isOnGround()) {

                        // reset the timer
                        this.scaffoldTimer.reset();
                    }
                }
            }
        }
    }

    private int hasAirBlocksAround(double x, double y, double z) {
        int i = 0;

        Material x3 = this.getMaterial(getData(), x - 1, y, z);
        Material x4 = this.getMaterial(getData(), x + 1, y, z);

        Material z3 = this.getMaterial(getData(), x, y, z - 1);
        Material z4 = this.getMaterial(getData(), x, y, z + 1);


        if (x3 != Material.AIR) {
            i++;
        }

        if (x4 != Material.AIR) {
            i++;
        }

        if (z3 != Material.AIR) {
            i++;
        }

        if (z4 != Material.AIR) {
            i++;
        }

        return i;
    }

    private int hasAirBlocksAroundWithOffset(double x, double y, double z, double offset) {
        int i = 0;

        Material x1 = this.getMaterial(getData(), x + 1, y - offset, z);
        Material x2 = this.getMaterial(getData(), x + 1, y - offset, z);
        Material x3 = this.getMaterial(getData(), x - 1, y - offset, z);
        Material x4 = this.getMaterial(getData(), x + 1, y - offset, z);

        Material z1 = this.getMaterial(getData(), x, y - offset, z + 1);
        Material z2 = this.getMaterial(getData(), x, y - offset, z + 1);
        Material z3 = this.getMaterial(getData(), x, y - offset, z - 1);
        Material z4 = this.getMaterial(getData(), x, y - offset, z + 1);

        if (x1 != Material.AIR) {
            i++;
        }

        if (x2 != Material.AIR) {
            i++;
        }

        if (x3 != Material.AIR) {
            i++;
        }

        if (x4 != Material.AIR) {
            i++;
        }

        if (z1 != Material.AIR) {
            i++;
        }

        if (z2 != Material.AIR) {
            i++;
        }

        if (z3 != Material.AIR) {
            i++;
        }

        if (z4 != Material.AIR) {
            i++;
        }

        return i;
    }

    private Material getMaterial(PlayerData data, double x, double y, double z) {
        if (getData().getCollisionProcessor().isChunkLoaded()) {
            return Anticheat.INSTANCE.getInstanceManager().getInstance().getType(data.getPlayer().getWorld(),
                    x, y, z);
        }

        return Material.AIR;
    }
}