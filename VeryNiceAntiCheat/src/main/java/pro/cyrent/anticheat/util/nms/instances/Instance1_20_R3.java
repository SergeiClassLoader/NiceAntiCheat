package pro.cyrent.anticheat.util.nms.instances;

/*
import dev.demon.data.PlayerData;
import dev.demon.util.MathUtil;
import dev.demon.util.bb.BoundingBox;
import dev.demon.util.nms.Instance;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.Particles;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutExplosion;
import net.minecraft.network.protocol.game.PacketPlayOutGameStateChange;
import net.minecraft.network.protocol.game.PacketPlayOutWorldParticles;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("rawtypes")
public class Instance1_20_R3 extends Instance {
    public static final UUID SPRINTING_SPEED_BOOST = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    private Field checkMovement, frictionFactor;

    public Instance1_20_R3() {
        try {
            Class<?> playerConnection = Class.forName("net.minecraft.server.v1_13_R2.PlayerConnection");
            this.checkMovement = playerConnection.getDeclaredField("teleportPos");
            this.checkMovement.setAccessible(true);

            Class<?> frictionFactor = Class.forName("net.minecraft.server.v1_13_R2.Block");
            this.frictionFactor = frictionFactor.getDeclaredField("frictionFactor");
            this.frictionFactor.setAccessible(true);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    @Override
    public void sendStopBlockingPacket(Player player) {

    }

    @Override
    public boolean isBlockingAnimation(Player player) {
        return false;
    }

    @Override
    public Material getType(World world, double x, double y, double z) {
        net.minecraft.world.level.World handle = ((CraftWorld) world).getHandle();

        if (handle == null) {
            return Material.AIR;
        }

        Chunk nmsChunk = handle.d(((int) x) >> 4, ((int) z) >> 4);

        if (nmsChunk == null) {
            return Material.AIR;
        }

        BlockPosition blockPosition = new BlockPosition((int) x, (int) y, (int) z);

        return CraftMagicNumbers.getMaterial(handle.a_(blockPosition).b());
    }


    @Override
    public List<PotionEffect> potionEffectList(PlayerData user) {
        return (List<PotionEffect>) new ArrayList(user.getPlayer().getActivePotionEffects());
    }

    @Override
    public ItemStack getItemInHand(PlayerData user) {
        return CraftItemStack.asBukkitCopy(((CraftPlayer) user.getPlayer()).getHandle().fS().f());
    }

    @Override
    public double getAttributeMovementSpeed(PlayerData user) {
        return user.getPlayer().getWalkSpeed() / 2;
    }

    @Override
    public void sendVelocityPacket(PlayerData user, double x, double y, double z, double percent) {

    }

    @Override
    public int getNMSPing(PlayerData user) {
        return ((CraftPlayer) user.getPlayer()).getPing();
    }

    @Override
    public void hidePlayerFromTab(Player player) {

    }

    @Override
    public void showPlayerInTab(Player player) {

    }

    @Override
    public double getBaseSpeed(PlayerData user) {
        return 0;
    }

    @Override
    public float getSlipperiness(PlayerData user, Location location) {

        if (location == null || location.getWorld() == null || user == null) {
            return 0F;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        BlockPosition blockPos = new BlockPosition(x, y - 1, z);

        Location location1 = new Location(user.getPlayer().getWorld(), x, y - 1, z);

       /* Block nmsBlock
                = user.getPlayer().getWorld().getBlockAt(location1).getType();*/

/*
        return 0F;
    }

    @Override
    public void crashClient(PlayerData user, CrashTypes crashType) {

        switch (crashType) {
            case EXPLODE: {
                ((CraftPlayer) user.getPlayer()).getHandle().c.a(new PacketPlayOutExplosion(
                        Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE,
                        Float.MAX_VALUE, Collections.EMPTY_LIST,
                        new Vec3D(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE),
                        null, null, null, null));
                break;
            }

            case PARTICLE: {
                Location location = user.getPlayer().getLocation();

                int[] values = {Integer.MAX_VALUE, Integer.MIN_VALUE,
                        MathUtil.getRandomInteger(1000000, 9500000)};

                for (int i = 0; i < 25; i++) {
                    for (int value : values) {

                        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                                Particles.A,
                                true, (float) location.getX(), (float) location.getY(),
                                (float) location.getZ(), value, value, value,
                                value, value
                        );

                        for (int a = 0; a < 400; a++) {
                            ((CraftPlayer) user.getPlayer()).getHandle().c.a(packet);
                        }
                    }
                }

                for (int i = 0; i < 450; i++) {
                    float value = 20 + i;


                    PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                            Particles.F,
                            true, (float) location.getX(), (float) location.getY(),
                            (float) location.getZ(), value + MathUtil.getRandomFloat(-20, 50),
                            (float) (value + MathUtil.getRandomFloat(-20, 50) + Math.random()),
                            (float) (value + MathUtil.getRandomFloat(-20, 50) + Math.random()),
                            (float) (value + MathUtil.getRandomFloat(-20, 50) + Math.random()),
                            (int) MathUtil.getRandomFloat(30, 60)
                    );

                    ((CraftPlayer) user.getPlayer()).getHandle().c.a(packet);
                }

                break;
            }
        }
    }

    @Override
    public void dropFPS(PlayerData user) {

    }

    @Override
    public void potionCrash(PlayerData user) {
    }

    @Override
    public void disconnect(PlayerData user) {
      //  (((CraftPlayer) user.getPlayer()).getHandle()).c.;
    }

    @Override
    public void sendPacket(Object packet, PlayerData user) {
        ((CraftPlayer) user.getPlayer()).getHandle().c.a((Packet<?>) packet);
    }

    @Override
    public boolean checkMovement(PlayerData data) {


        return false;
    }

    @Override
    public double getMotionYEntity(PlayerData data) {
        return 0;
    }

    @Override
    public void showDemo(PlayerData user) {
        float[] windows = new float[]{0, 101, 102, 103, 104};

        ((CraftPlayer) user.getPlayer()).getHandle().c.a(
                new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.f,
                        windows[ThreadLocalRandom.current().nextInt(windows.length)]));

        ((CraftPlayer) user.getPlayer()).getHandle().c.a(
                new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.f, 0));
    }


    @Override
    public void sendBlockUpdate(PlayerData user, double x, double y, double z) {

        if (user.getPlayer().getWorld() == null) return;

      //  PacketPlayOutBlockChange packetPlayOutBlockChange =
          //      new PacketPlayOutBlockChange((((CraftWorld) user.getPlayer().getWorld()).getHandle()),
        //                new BlockPosition(x, y, z));
      //  ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket(packetPlayOutBlockChange);
    }

    @Override
    public Entity[] getEntities(World world, int x, int z) {
        return new Entity[0];
    }

    @Override
    public BoundingBox getEntityBoundingBox(Entity entity) {
        return null;
    }

    @Override
    public boolean getEntityBoundingBoxGround(Entity entity) {
        return false;
    }
}
*/