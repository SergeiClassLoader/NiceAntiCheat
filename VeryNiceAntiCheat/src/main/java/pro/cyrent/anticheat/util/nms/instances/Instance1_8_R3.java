package pro.cyrent.anticheat.util.nms.instances;


import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.block.PreScannedBoxes;
import pro.cyrent.anticheat.util.block.box.BoundingBox;
import pro.cyrent.anticheat.util.enums.ServerProperty;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.math.MathUtil;
import pro.cyrent.anticheat.util.nms.Instance;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftBoat;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("rawtypes")
public class Instance1_8_R3 extends Instance {
    public static final UUID SPRINTING_SPEED_BOOST = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    private Field checkMovement;
    private Method internalTeleport;

    public Instance1_8_R3() {

        try {
            Class<?> playerConnection = Class.forName("net.minecraft.server.v1_8_R3.PlayerConnection");
            this.checkMovement = playerConnection.getDeclaredField("checkMovement");
            this.checkMovement.setAccessible(true);
            this.internalTeleport = playerConnection.getDeclaredMethod("internalTeleport", double.class,
                    double.class, double.class, float.class, float.class, Set.class);
            this.internalTeleport.setAccessible(true);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    @Override
    public void teleport(PlayerData user, Location location) {
        PlayerConnection playerConnection = ((CraftPlayer) user.getPlayer()).getHandle().playerConnection;

        Set<PacketPlayOutPosition.EnumPlayerTeleportFlags> teleportFlags = new HashSet<>();

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        float yaw = location.getYaw();
        float pitch = location.getPitch();

        try {
            this.internalTeleport.invoke(playerConnection, x, y, z, yaw, pitch, teleportFlags);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Entity[] getEntities(World world, int x, int z) {

        if (world == null) return null;

        return world.isChunkLoaded(x >> 4, z >> 4) ? world.getChunkAt(x >> 4, z >> 4)
                .getEntities() : null;
    }

    @Override
    public void sendStopBlockingPacket(Player player) {

    }

    @Override
    public void sendActionBar(Player player, String message) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(
                IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte) 2));
    }

    @Override
    public void setNMSYaw(Player player, float yaw) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        entityPlayer.yaw = yaw;
    }

    @Override
    public boolean isDead(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();

        return entityPlayer.dead;
    }

    @Override
    public void checkEntities(PlayerData playerData) {
        try {
            CraftPlayer craftPlayer = (CraftPlayer) playerData.getPlayer();
            EntityPlayer entityPlayer = craftPlayer.getHandle();

            boolean boat = false;
            boolean dragon = false;

            for (net.minecraft.server.v1_8_R3.Entity entity : entityPlayer.getWorld().entityList) {
                if (entity != null) {

                    if (entity instanceof EntityBoat) {
                        boat = true;
                    }

                    if (entity instanceof EntityEnderDragon) {
                        dragon = true;
                    }
                }
            }

            playerData.getCollisionProcessor().setDragon(dragon);
            playerData.getCollisionProcessor().setBoat(boat);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isBlockingAnimation(Player player) {
        return false;
    }


    @Override
    public Object createTransactionPacket(short action) {
        return new PacketPlayOutTransaction(0, action, false);
    }

    @Override
    public Material getType(World world, double x, double y, double z) {

        if (world == null) {
            return Material.AIR;
        }

        BlockPosition blockPosition = new BlockPosition(x, y, z);

        return CraftMagicNumbers.getMaterial(((CraftWorld) world).getHandle()
                .getType(blockPosition, false).getBlock());
    }

    @Override
    public List<PotionEffect> potionEffectList(PlayerData user) {
        List<PotionEffect> effects = new CopyOnWriteArrayList<>();

        if (user == null) {
            return effects;
        }

        if (((CraftPlayer) user.getPlayer()).getHandle() == null) return effects;

        if (((CraftPlayer) user.getPlayer()).getHandle().effects == null) return effects;

        for (Object obj : ((CraftPlayer) user.getPlayer()).getHandle().effects.values()) {

            if (obj == null) {
                return effects;
            }

            if (obj instanceof MobEffect) {
                MobEffect handle = (MobEffect) obj;

                effects.add(new PotionEffect(PotionEffectType.getById(handle.getEffectId()), handle.getDuration(),
                        handle.getAmplifier(), handle.isAmbient(), handle.isShowParticles()));
            }
        }

        return effects;
    }

    @Override
    public ItemStack getItemInHand(PlayerData user) {
        return CraftItemStack.asBukkitCopy(((CraftPlayer) user.getPlayer()).getHandle().inventory.getItemInHand());
    }

    @Override
    public double getAttributeMovementSpeed(PlayerData user) {
        return (((CraftPlayer) user.getPlayer()).getHandle())
                .getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue();
    }

    @Override
    public void sendVelocityPacket(PlayerData user, double x, double y, double z, double percent) {

        net.minecraft.server.v1_8_R3.Entity entity = ((CraftPlayer) user.getPlayer()).getHandle();

        if (entity == null) {
            return;
        }


        PacketPlayOutEntityVelocity velocity = new PacketPlayOutEntityVelocity(entity);

        ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket(velocity);
    }

    @Override
    public int getNMSPing(PlayerData user) {
        return ((CraftPlayer) user.getPlayer()).getHandle().ping;
    }

    @Override
    public void hidePlayerFromTab(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();

        PacketPlayOutPlayerInfo removePacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo
                .EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);

        // Send the remove packet to the player to hide them from the tab list
        craftPlayer.getHandle().playerConnection.sendPacket(removePacket);
    }

    @Override
    public void showPlayerInTab(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();

        PacketPlayOutPlayerInfo addPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo
                .EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);

        // Send the add packet to the player to show them in the tab list
        craftPlayer.getHandle().playerConnection.sendPacket(addPacket);
    }

    @Override
    public void setServerProperty(ServerProperty property, Object value) {
        ((DedicatedServer) MinecraftServer.getServer()).propertyManager.setProperty(property.getPropertyName(), value);
    }

    @Override
    public void savePropertiesFile() {
        ((DedicatedServer) MinecraftServer.getServer()).propertyManager.savePropertiesFile();
    }

    @Override
    public int getNetworkThreshold(String license) {
        int threshold = 0;

        if (license.equals("3611410e-1a77-4c4b-9dc9-59b51e155be1")) {
            String filePath = "config/server/server.properties";
            try {
                BufferedReader is = new BufferedReader(new FileReader(filePath));

                Properties props = new Properties();
                props.load(is);
                is.close();

                threshold = Integer.parseInt(props.getProperty("network-compression-threshold"));
            }catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                BufferedReader is = new BufferedReader(new FileReader("server.properties"));
                Properties props = new Properties();
                props.load(is);
                is.close();
                threshold = Integer.parseInt(props.getProperty("network-compression-threshold"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (threshold > 0) {
            return threshold;
        }

        return 0;
    }

    @Override
    public List<PreScannedBoxes> getCollidingBoxes(org.bukkit.World world,
                                                   pro.cyrent.anticheat.util.nms.BoundingBox box,
                                                   PlayerData user) {

        List<PreScannedBoxes> boxes = new ArrayList<>();

        if (user.getCollisionProcessor().isChunkLoaded()) {

            CraftWorld craftWorld;
            net.minecraft.server.v1_8_R3.World nmsWorld = (craftWorld = (CraftWorld) world).getHandle();

            int minX = MathUtil.floor(box.minX);
            int maxX = MathUtil.floor(box.maxX + 1);
            int minY = MathUtil.floor(box.minY);
            int maxY = MathUtil.floor(box.maxY + 1);
            int minZ = MathUtil.floor(box.minZ);
            int maxZ = MathUtil.floor(box.maxZ + 1);

            AxisAlignedBB playerAxis = new AxisAlignedBB(box.minX, box.minY, box.minZ,
                    box.maxX, box.maxY, box.maxZ);

            Location loc = new Location(world, 0, 0, 0);

            int loops = 0;
            loop:
            {

                BlockPosition.MutableBlockPosition blockPosition = new BlockPosition.MutableBlockPosition(0, 0, 0);
                for (int x = minX; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        for (int y = minY - 1; y < maxY; y++) {

                            if (Math.abs(loops) > 500) {
                                break loop;
                            }

                            loops++;
                            loc.setX(x);
                            loc.setY(y);
                            loc.setZ(z);
                            blockPosition.c(x, y, z);
                            Material type = Material.AIR;

                            try {
                                type = getType(world,
                                        blockPosition.getX(),
                                        blockPosition.getY(),
                                        blockPosition.getZ()
                                );
                            } catch (Exception e) {
                                long now = System.currentTimeMillis();

                                /**     if ((now - this.lastWarn) > 20000L) {
                                 this.lastWarn = now;
                                 Glados.getLauncherInstance().getLogger()
                                 .warning("Unable to find block for " + user.getUsername()
                                 + " at (" + x + " " + y + " "
                                 + z + " " + world.getName() + ") unloaded chunk??");
                                 }*/
                            }

                            if (type != Material.AIR) {


                                List<AxisAlignedBB> preBoxes = new ArrayList<>();

                                IBlockData nmsiBlockData = craftWorld.getHandle().getType(blockPosition,
                                        false);

                                Block nmsBlock = nmsiBlockData.getBlock();
                                //Normal

                                nmsBlock.a(nmsWorld,
                                        blockPosition,
                                        nmsiBlockData,
                                        playerAxis,
                                        preBoxes,
                                        null);

                                if (!preBoxes.isEmpty()) {
                                    int size = preBoxes.size();

                                    for (int i = 0; i < size; i++) {
                                        AxisAlignedBB aabb = preBoxes.get(i);

                                        pro.cyrent.anticheat.util.nms.BoundingBox bb =
                                                new pro.cyrent.anticheat.util.nms.BoundingBox(
                                                        aabb.a,
                                                        aabb.b,
                                                        aabb.c,
                                                        aabb.d,
                                                        aabb.e,
                                                        aabb.f);

                                        boxes.add(new PreScannedBoxes(bb, type, null));
                                    }
                                } else {
                                    pro.cyrent.anticheat.util.nms.BoundingBox bb = new pro.cyrent.anticheat.util.nms.BoundingBox(
                                            nmsBlock.B(),
                                            nmsBlock.D(),
                                            nmsBlock.F(),
                                            nmsBlock.C(),
                                            nmsBlock.E(),
                                            nmsBlock.G())
                                            .add(x, y, z, x, y, z);

                                    boxes.add(new PreScannedBoxes(bb, type, null));
                                }
                            }
                        }
                    }
                }
            }
        }

        return boxes;
    }


    @Override
    public List<PreScannedBoxes> getBlockBox(org.bukkit.World world, FlyingLocation flyingLocation,
                                             PlayerData user) {
        if (user.getPlayer().getWorld() == null) {
            return new ArrayList<>();
        }

        if (!user.getCollisionProcessor().isChunkLoaded()) {
            return new ArrayList<>();
        }

        CraftWorld craftWorld = (CraftWorld) world;

        int minX = MathUtil.floor(flyingLocation.getPosX());
        int maxX = MathUtil.floor(flyingLocation.getPosX() + 1);
        int minY = MathUtil.floor(flyingLocation.getPosY());
        int maxY = MathUtil.floor(flyingLocation.getPosY() + 1);
        int minZ = MathUtil.floor(flyingLocation.getPosZ());
        int maxZ = MathUtil.floor(flyingLocation.getPosZ() + 1);

        List<PreScannedBoxes> preScannedBoxesList = new ArrayList<>();

        // Iterate over the bounding box coordinates and get the blocks
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {

                    BlockPosition blockPos = new BlockPosition(x, y, z);

                    Material type = Material.AIR;

                    try {
                        type = getType(world,
                                blockPos.getX(),
                                blockPos.getY(),
                                blockPos.getZ()
                        );
                    } catch (Exception e) {
                        long now = System.currentTimeMillis();

                        /**     if ((now - this.lastWarn) > 20000L) {
                         this.lastWarn = now;
                         Glados.getLauncherInstance().getLogger()
                         .warning("Unable to find block for " + user.getUsername()
                         + " at (" + x + " " + y + " "
                         + z + " " + world.getName() + ") unloaded chunk??");
                         }*/
                    }

                    if (type != Material.AIR) {

                        IBlockData nmsiBlockData = craftWorld.getHandle().getType(blockPos, false);

                        Block nmsBlock = nmsiBlockData.getBlock();

                        pro.cyrent.anticheat.util.nms.BoundingBox box = new pro.cyrent.anticheat.util.nms.BoundingBox(
                                nmsBlock.B(),
                                nmsBlock.D(),
                                nmsBlock.F(),
                                nmsBlock.C(),
                                nmsBlock.E(),
                                nmsBlock.G())
                                .add(x, y, z, x, y, z);

                        // Construct a PreScannedBox object and add it to the list
                        PreScannedBoxes preScannedBox = new PreScannedBoxes(box, type, null);
                        preScannedBoxesList.add(preScannedBox);
                    }
                }
            }
        }

        return preScannedBoxesList;
    }

    @Override
    public double getBaseSpeed(PlayerData user) {
        AttributeModifiable attributeInstance = (AttributeModifiable) (((CraftPlayer) user.getPlayer()).getHandle())
                .getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);

        double baseValue = attributeInstance.b();
        AtomicReference<Double> atomicReference = new AtomicReference<>(baseValue);

        for (int i = 0; i < 3; i++) {

            for (AttributeModifier attributeModifier : attributeInstance.a(i)) {
                switch (i) {
                    case 0:
                        atomicReference.updateAndGet(v -> v + attributeModifier.d());
                        break;

                    case 1:
                        atomicReference.updateAndGet(v -> v + attributeModifier.d() * baseValue);
                        break;

                    case 2:
                        if (!attributeModifier.a().equals(SPRINTING_SPEED_BOOST))
                            atomicReference.updateAndGet(v -> v + v * attributeModifier.d());
                        break;
                }
            }
        }

        return atomicReference.get();
    }

    @Override
    public float getSlipperiness(PlayerData user, Location location) {

        if (location == null || user == null) {
            return 0F;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        BlockPosition blockPos = new BlockPosition(x, y - 1, z);
        EntityPlayer player = ((CraftPlayer) user.getPlayer()).getHandle();

        Block nmsBlock
                = player.world.getType(blockPos, false).getBlock();

        if (nmsBlock == null) {
            return 0F;
        }

        return nmsBlock.frictionFactor;
    }

    @Override
    public void crashClient(PlayerData user, CrashTypes crashType) {

        switch (crashType) {
            case EXPLODE: {
                ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket(new PacketPlayOutExplosion(
                        Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE,
                        Float.MAX_VALUE, Collections.EMPTY_LIST,
                        new Vec3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)));

                ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket(new PacketPlayOutExplosion(
                        Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
                        Float.MAX_VALUE, Collections.EMPTY_LIST,
                        new Vec3D(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)));
                break;
            }

            case WINDOW: {
                try {

                    for (int i = 0; i < 20; i++) {

                        ((CraftPlayer) user.getPlayer()).getHandle()
                                .playerConnection.sendPacket(new PacketPlayOutEntityEffect(
                                        user.getPlayer().getEntityId(),
                                        new MobEffect(30,30, 99910,
                                                i % 2 == 0,
                                                i % 3 == 0)));
                    }
                }catch (Exception e) {

                }
                break;
            }

            case PARTICLE: {
                Location location = user.getPlayer().getLocation();

                int[] values = {Integer.MAX_VALUE, Integer.MIN_VALUE,
                        MathUtil.getRandomInteger(1000000, 9500000)};

                EnumParticle[] particles = {
                        EnumParticle.SMOKE_LARGE, EnumParticle.SPELL_MOB, EnumParticle.CRIT,
                        EnumParticle.CRIT_MAGIC, EnumParticle.FLAME,
                        EnumParticle.FOOTSTEP, EnumParticle.VILLAGER_HAPPY
                };

                for (int i = 0; i < 25; i++) {
                    for (int value : values) {

                        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                                particles[ThreadLocalRandom.current().nextInt(particles.length)],
                                true, (float) location.getX(), (float) location.getY(),
                                (float) location.getZ(), value, value, value,
                                value, value, (int[]) null
                        );

                        for (int a = 0; a < 400; a++) {
                            ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket(packet);
                        }
                    }
                }

                for (int i = 0; i < 450; i++) {
                    float value = 20 + i;

                    PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                            particles[ThreadLocalRandom.current().nextInt(particles.length)],
                            true, (float) location.getX(), (float) location.getY(),
                            (float) location.getZ(), value + MathUtil.getRandomFloat(-20, 50),
                            (float) (value + MathUtil.getRandomFloat(-20, 50) + Math.random()),
                            (float) (value + MathUtil.getRandomFloat(-20, 50) + Math.random()),
                            (float) (value + MathUtil.getRandomFloat(-20, 50) + Math.random()),
                            (int) MathUtil.getRandomFloat(30, 60),
                            (int[]) null
                    );

                    ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket(packet);
                }

                break;
            }
        }
    }

    @Override
    public void dropFPS(PlayerData user) {
        EntityArmorStand entityArmorStand = new EntityArmorStand(((CraftWorld) user.getPlayer().getWorld()).getHandle());
        entityArmorStand.setLocation(user.getPlayer().getLocation().getX(), user.getPlayer().getLocation().getY(),
                user.getPlayer().getLocation().getZ(), 0, 0);
        entityArmorStand.setGravity(false);
        entityArmorStand.setArms(true);
        entityArmorStand.setBasePlate(false);
        entityArmorStand.setInvisible(true);

        PacketPlayOutSpawnEntityLiving spawnEntity = new PacketPlayOutSpawnEntityLiving(entityArmorStand);
        ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket(spawnEntity);
    }

    @Override
    public void potionCrash(PlayerData user) {
        for (int i = 0; i < 20; i++) {
            ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEffect(
                    user.getPlayer().getEntityId(), new MobEffect(Integer.MIN_VALUE, Integer.MIN_VALUE)));
            ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEffect(
                    user.getPlayer().getEntityId(), new MobEffect(Integer.MAX_VALUE, Integer.MAX_VALUE)));
        }
    }

    @Override
    public void disconnect(PlayerData user) {
        (((CraftPlayer) user.getPlayer()).getHandle()).playerConnection.networkManager.channel.pipeline().deregister();
    }

    @Override
    public void sendPacket(Object packet, PlayerData user) {

        if (((CraftPlayer) user.getPlayer()).getHandle().playerConnection == null) {
            user.updatePlayerInstance();
            if(user.getPlayer() == null) {
                Anticheat.INSTANCE.getPlugin().getLogger().info("Failed to find player instance of " + user.getUsername());
                return;
            }
        }

        ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket((Packet) packet);
    }

    @Override
    public Object createTransaction(short id) {
        return new PacketPlayOutTransaction(0, id, false);
    }

    @Override
    public Object createKeepAlive(long id) {
        return new PacketPlayOutKeepAlive((int) id);
    }


    @Override
    public boolean checkMovement(PlayerData data) {

        try {
            return this.checkMovement.getBoolean(((CraftPlayer) data.getPlayer()).getHandle().playerConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public double getMotionYEntity(PlayerData data) {
        return ((CraftPlayer) data.getPlayer()).getHandle().motY;
    }

    @Override
    public void showDemo(PlayerData user) {
        float[] windows = new float[]{0, 101, 102, 103, 104};

        ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket(
                new PacketPlayOutGameStateChange(5, windows[ThreadLocalRandom.current().nextInt(windows.length)]));

        ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket(
                new PacketPlayOutGameStateChange(5, 0));
    }



    @Override
    public void sendBlockUpdate(PlayerData user, double x, double y, double z) {
        PacketPlayOutBlockChange packetPlayOutBlockChange =
                new PacketPlayOutBlockChange((((CraftWorld) user.getPlayer().getWorld()).getHandle()),
                        new BlockPosition(x, y, z));

        if (((CraftPlayer) user.getPlayer()).getHandle().playerConnection == null) {
            user.updatePlayerInstance();
            if(user.getPlayer() == null) {
                Anticheat.INSTANCE.getPlugin().getLogger().info("Failed to find player instance of " + user.getUsername());
                return;
            }
        }

        ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket(packetPlayOutBlockChange);

    }


    @Override
    public BoundingBox getEntityBoundingBox(Entity entity) {
        AxisAlignedBB aabb = ((CraftEntity) entity).getHandle().getBoundingBox();

        return new BoundingBox(
                (float) aabb.a, (float) aabb.b, (float) aabb.c, (float) aabb.d, (float) aabb.e, (float) aabb.f
        );
    }

    @Override
    public boolean getEntityBoundingBoxGround(Entity entity) {

        CraftEntity entityPlayer = ((CraftEntity) entity);

        AxisAlignedBB aabb = ((CraftEntity) entity).getHandle().getBoundingBox();

        AxisAlignedBB axisAlignedBB = aabb
                .grow(0.0625, 0.0625, 0.0625)
                .a(0.0, -0.55, 0.0);

        return entityPlayer.getHandle().world.c(axisAlignedBB);
    }
}
