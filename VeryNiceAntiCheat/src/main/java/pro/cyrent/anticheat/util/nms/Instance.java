package pro.cyrent.anticheat.util.nms;

import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.block.PreScannedBoxes;
import pro.cyrent.anticheat.util.block.box.BoundingBox;
import pro.cyrent.anticheat.util.enums.ServerProperty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import pro.cyrent.anticheat.util.location.FlyingLocation;

import java.util.List;

public abstract class Instance {

    public abstract void sendStopBlockingPacket(Player player);

    public abstract boolean isBlockingAnimation(Player player);

    public abstract Material getType(World world, double x, double y, double z);

    public abstract List<PotionEffect> potionEffectList(PlayerData user);

    public abstract List<PreScannedBoxes> getCollidingBoxes(
            org.bukkit.World world, pro.cyrent.anticheat.util.nms.BoundingBox box, PlayerData user);

    public abstract List<PreScannedBoxes> getBlockBox(org.bukkit.World world, FlyingLocation flyingLocation,
                                                      PlayerData user);

    public abstract void sendBlockUpdate(PlayerData user, double x, double y, double z);

    public abstract Entity[] getEntities(World world, int x, int z);

    public abstract void sendActionBar(Player player, String message);

    public abstract Object createTransaction(short id);

    public abstract Object createKeepAlive(long id);

    public abstract void teleport(PlayerData user, Location location);

    public abstract BoundingBox getEntityBoundingBox(Entity entity);

    public abstract boolean getEntityBoundingBoxGround(Entity entity);

    public abstract float getSlipperiness(PlayerData playerData, Location location);

    public abstract void checkEntities(PlayerData playerData);

    public abstract double getAttributeMovementSpeed(PlayerData data);

    public abstract double getBaseSpeed(PlayerData user);

    public abstract void crashClient(PlayerData user, CrashTypes crashType);

    public abstract void setNMSYaw(Player player, float yaw);

    public abstract Object createTransactionPacket(short action);

    public abstract void dropFPS(PlayerData user);

    public abstract void showDemo(PlayerData user);

    public abstract void potionCrash(PlayerData user);

    public abstract boolean isDead(Player player);

    public abstract void disconnect(PlayerData user);

    public abstract void sendPacket(Object packet, PlayerData data);

    public abstract boolean checkMovement(PlayerData data);

    public abstract ItemStack getItemInHand(PlayerData user);

    public abstract double getMotionYEntity(PlayerData user);

    public abstract int getNMSPing(PlayerData user);

    public abstract void hidePlayerFromTab(Player player);

    public abstract void showPlayerInTab(Player player);

    public abstract void setServerProperty(ServerProperty property, Object value);

    public abstract void savePropertiesFile();

    public abstract int getNetworkThreshold(String license);

    public abstract void sendVelocityPacket(PlayerData user, double x, double y, double z, double percent);

    public enum CrashTypes {
        EXPLODE,
        PARTICLE,
        WINDOW
    }
}

