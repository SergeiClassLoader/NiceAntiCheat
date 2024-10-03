package pro.cyrent.anticheat.api.check.impl.combat.entity.utils;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.math.MathUtil;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EntityHook18 extends CustomEntity {

    private EntityPlayer player;
    private EntityPlayer playerAdvanced;
    private EntityData.EntityType entityType;

    public EntityHook18(PlayerData user, PlayerData forcedUser, UUID uuid, EntityData creationData) {
        super(user, forcedUser, uuid, creationData);
    }

    @Override
    public void removeEntity(PlayerData user, PlayerData forcedUser) {

        if (this.entityType == null) return;

        setActive(false);

        switch (this.entityType) {

            case BASIC: {
                if (this.player == null) return;

                sendPacket(new PacketPlayOutEntityDestroy(this.player.getId()), user, forcedUser);
                break;
            }

            case ADVANCED: {
                if (this.playerAdvanced == null) return;

           //     sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
             //           this.playerAdvanced), user, forcedUser);
                sendPacket(new PacketPlayOutEntityDestroy(this.playerAdvanced.getId()), user, forcedUser);
                break;
            }
        }
    }

    @Override
    public void tickEntity(PlayerData user, PlayerData forcedUser, FlyingLocation customLocation, boolean onGround) {

        if (this.entityType == null) return;

        switch (this.entityType) {

            case BASIC: {


                if (this.player == null) return;

                MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();

                player.onGround = user.getMovementProcessor().getTick() % 3 == 0;

                player.setInvisible(false);

                player.setHealth((float) MathUtil
                        .getRandomDouble(MathUtil.getRandomDouble(5.32, 0.0), 20.0));

                player.setSneaking(false);

                if (user.getMovementProcessor().getTick() % 5 == 0) {
                    player.setSprinting(true);
                } else {
                    player.setSprinting(false);
                }

                if (!player.onGround) {
                    player.setAirTicks((int) MathUtil
                            .getRandomDouble(10, 1));
                }

                player.velocityChanged = user.getMovementProcessor().getTick() % 9 == 0;

                player.playerConnection = new PlayerConnection(minecraftServer,
                        new NetworkManager(EnumProtocolDirection.CLIENTBOUND), player);

                player.hurtTicks = user.getMovementProcessor().getTick() % 8 == 0 ? 0 : 1;

                if (Math.abs(customLocation.getPitch()) > 90.0F) {
                    customLocation.setPitch(90.0F);
                }

                customLocation.setPitch(customLocation.getPitch()
                        + MathUtil.getRandomFloat(90F, -90F));

                customLocation.setYaw(customLocation.getYaw()
                        + MathUtil.getRandomFloat(360f, -360f));

                sendPacket(new PacketPlayOutEntityHeadRotation(this.player,
                        (byte) (customLocation.getYaw() * 256 / 360)), user, forcedUser);

                this.player.setLocation(customLocation.getPosX(), customLocation.getPosY(), customLocation.getPosZ(),
                        customLocation.getYaw(), customLocation.getPitch());

                setLastReportedLocation(customLocation);

                sendPacket(new PacketPlayOutEntityTeleport(this.player), user, forcedUser);
                break;
            }

            case ADVANCED: {

                if (this.playerAdvanced == null) return;

                MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();

                playerAdvanced.onGround = user.getMovementProcessor().getTick() % 3 == 0;

                playerAdvanced.setInvisible(false);

                playerAdvanced.setHealth((float) MathUtil
                        .getRandomDouble(MathUtil.getRandomDouble(5.32, 0.0), 20.0));

                if (user.getMovementProcessor().getTick() % 4 == 0) {
                    playerAdvanced.setSneaking(true);
                } else {
                    playerAdvanced.setSneaking(false);
                }

                if (user.getMovementProcessor().getTick() % 5 == 0) {
                    playerAdvanced.setSprinting(true);
                } else {
                    playerAdvanced.setSprinting(false);
                }

                if (!playerAdvanced.onGround) {
                    playerAdvanced.setAirTicks((int) MathUtil
                            .getRandomDouble(10, 0));
                }

                playerAdvanced.velocityChanged = user.getMovementProcessor().getTick() % 9 == 0;

                playerAdvanced.playerConnection = new PlayerConnection(minecraftServer,
                        new NetworkManager(EnumProtocolDirection.CLIENTBOUND), playerAdvanced);

                playerAdvanced.hurtTicks = user.getMovementProcessor().getTick() % 8 == 0 ? 0 : 1;

                if (Math.abs(customLocation.getPitch()) > 90.0F) {
                    customLocation.setPitch(90.0F);
                }

                customLocation.setPitch(customLocation.getPitch()
                        + MathUtil.getRandomFloat(90F, -90F));

                customLocation.setYaw(customLocation.getYaw()
                        + MathUtil.getRandomFloat(360f, -360f));

                sendPacket(new PacketPlayOutEntityHeadRotation(this.playerAdvanced,
                        (byte) (customLocation.getYaw() * 256 / 360)), user, forcedUser);

                this.playerAdvanced.setLocation(customLocation.getPosX(), customLocation.getPosY(), customLocation.getPosZ(),
                        customLocation.getYaw(), customLocation.getPitch());

                setLastReportedLocation(customLocation);

                sendPacket(new PacketPlayOutEntityTeleport(this.playerAdvanced), user, forcedUser);
                break;
            }
        }

        this.setLastUpdateTick(user.getMovementProcessor().getTick());
    }

    @Override
    public void createEntity(PlayerData user, PlayerData forcedUser, EntityData.EntityType entityType, EntityData entityData) {

        if (entityType == null || entityData == null) return;

        WorldServer worldServer = ((CraftWorld) user.getPlayer().getWorld()).getHandle();

        setCreationData(entityData);

        switch (entityType) {
            case ADVANCED: {
                Player randomPlayer = getRandomPlayer(user);

                if (randomPlayer == null) {
                    return;
                }

                String name = randomPlayer.getName();

                MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();

                EntityPlayer entityPlayer = new EntityPlayer(minecraftServer, worldServer,
                        new GameProfile(UUID.fromString(String.valueOf(randomPlayer.getUniqueId())), name),
                        new PlayerInteractManager(worldServer));

                entityPlayer.onGround = user.getMovementProcessor().getTick() % 5 == 0;

                entityPlayer.playerInteractManager.b(WorldSettings.EnumGamemode.SURVIVAL);
                entityPlayer.setInvisible(false);

                entityPlayer.setHealth(MathUtil
                        .getRandomFloat(MathUtil.getRandomFloat(5.32F, Float.NaN), Float.MAX_VALUE));

                entityPlayer.setSneaking(user.getMovementProcessor().getTick() % 9 == 0);

                entityPlayer.setSprinting(user.getMovementProcessor().getTick() % 3 == 0);

                if (!entityPlayer.onGround) {
                    entityPlayer.setAirTicks((int) MathUtil
                            .getRandomDouble(10, 0));
                }

                entityPlayer.velocityChanged = user.getMovementProcessor().getTick() % 9 == 0;

                entityPlayer.ping = ((CraftPlayer) randomPlayer).getHandle().ping;

                entityPlayer.playerConnection = new PlayerConnection(minecraftServer,
                        new NetworkManager(EnumProtocolDirection.CLIENTBOUND), entityPlayer);

            //    entityPlayer.getDataWatcher().add(3, 3);

                entityPlayer.setLocation(entityData.getSpawnLocation().getPosX(), entityData.getSpawnLocation().getPosY(),
                        entityData.getSpawnLocation().getPosZ(),
                        entityData.getSpawnLocation().getYaw(), entityData.getSpawnLocation().getPitch());


                sendPacket(new PacketPlayOutNamedEntitySpawn(entityPlayer), user, forcedUser);

                sendPacket(new PacketPlayOutEntityTeleport(entityPlayer), user, forcedUser);
                sendPacket(new PacketPlayOutUpdateAttributes(), user, forcedUser);

                if (randomPlayer.getItemInHand() != null) sendPacket(
                        new PacketPlayOutEntityEquipment(entityPlayer.getId(), 0,
                                CraftItemStack.asNMSCopy(randomPlayer.getItemInHand())), user, forcedUser);

                sendPacket(new PacketPlayOutEntityEquipment(entityPlayer.getId(), 1,
                        CraftItemStack.asNMSCopy(randomPlayer.getInventory().getBoots())), user, forcedUser);
                sendPacket( new PacketPlayOutEntityEquipment(entityPlayer.getId(), 2,
                        CraftItemStack.asNMSCopy(randomPlayer.getInventory().getLeggings())), user, forcedUser);
                sendPacket(new PacketPlayOutEntityEquipment(entityPlayer.getId(), 3,
                        CraftItemStack.asNMSCopy(randomPlayer.getInventory().getChestplate())), user, forcedUser);
                sendPacket(new PacketPlayOutEntityEquipment(entityPlayer.getId(), 4,
                        CraftItemStack.asNMSCopy(randomPlayer.getInventory().getHelmet())), user, forcedUser);
                sendPacket(new PacketPlayOutUpdateAttributes(), user, forcedUser);

                sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                                  entityPlayer), user, forcedUser);

                setEntityID(entityPlayer.getId());
                setLastReportedLocation(entityData.getSpawnLocation());

                user.getCurrentSpawnedEntites().add(getEntityID());
                this.playerAdvanced = entityPlayer;
                break;
            }

            case BASIC: {

                Player randomPlayer = getRandomPlayer(user);

                if (randomPlayer == null) {
                    return;
                }

                String name = randomPlayer.getName();

                MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();

                EntityPlayer entityPlayer = new EntityPlayer(minecraftServer, worldServer,
                        new GameProfile(UUID.fromString(String.valueOf(randomPlayer.getUniqueId())), name),
                        new PlayerInteractManager(worldServer));

                entityPlayer.onGround = user.getMovementProcessor().getTick() % 5 == 0;

                entityPlayer.playerInteractManager.b(WorldSettings.EnumGamemode.SURVIVAL);
                entityPlayer.setInvisible(false);

                entityPlayer.setHealth((float) MathUtil
                        .getRandomDouble(MathUtil.getRandomDouble(5.32, 0.0), 20.0));

                entityPlayer.setSneaking(user.getMovementProcessor().getTick() % 9 == 0);

                entityPlayer.setSprinting(user.getMovementProcessor().getTick() % 3 == 0);

                if (!entityPlayer.onGround) {
                    entityPlayer.setAirTicks((int) MathUtil
                            .getRandomDouble(10, 1));
                }

                entityPlayer.velocityChanged = user.getMovementProcessor().getTick() % 9 == 0;

                entityPlayer.ping = ((CraftPlayer) randomPlayer).getHandle().ping;

                entityPlayer.playerConnection = new PlayerConnection(minecraftServer,
                        new NetworkManager(EnumProtocolDirection.CLIENTBOUND), entityPlayer);


                entityPlayer.setLocation(entityData.getSpawnLocation().getPosX(), entityData.getSpawnLocation().getPosY(),
                        entityData.getSpawnLocation().getPosZ(),
                        entityData.getSpawnLocation().getYaw(), entityData.getSpawnLocation().getPitch());

                sendPacket(new PacketPlayOutNamedEntitySpawn(entityPlayer), user, forcedUser);
                sendPacket(new PacketPlayOutEntityTeleport(entityPlayer), user, forcedUser);
                sendPacket(new PacketPlayOutUpdateAttributes(), user, forcedUser);

                if (randomPlayer.getItemInHand() != null) sendPacket(
                        new PacketPlayOutEntityEquipment(entityPlayer.getId(), 0,
                                CraftItemStack.asNMSCopy(randomPlayer.getItemInHand())), user, forcedUser);

                sendPacket(new PacketPlayOutEntityEquipment(entityPlayer.getId(), 1,
                        CraftItemStack.asNMSCopy(randomPlayer.getInventory().getBoots())), user, forcedUser);
                sendPacket( new PacketPlayOutEntityEquipment(entityPlayer.getId(), 2,
                        CraftItemStack.asNMSCopy(randomPlayer.getInventory().getLeggings())), user, forcedUser);
                sendPacket(new PacketPlayOutEntityEquipment(entityPlayer.getId(), 3,
                        CraftItemStack.asNMSCopy(randomPlayer.getInventory().getChestplate())), user, forcedUser);
                sendPacket(new PacketPlayOutEntityEquipment(entityPlayer.getId(), 4,
                        CraftItemStack.asNMSCopy(randomPlayer.getInventory().getHelmet())), user, forcedUser);
                sendPacket(new PacketPlayOutUpdateAttributes(), user, forcedUser);

                setEntityID(entityPlayer.getId());
                setLastReportedLocation(entityData.getSpawnLocation());

                user.getCurrentSpawnedEntites().add(getEntityID());
                this.player = entityPlayer;
                break;
            }
        }

        this.entityType = entityType;
    }

    public EntityPlayer findPlayerByEntityId(int entityId) {
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            EntityPlayer nmsPlayer = ((CraftPlayer) onlinePlayer).getHandle();
            if (nmsPlayer.getId() == entityId) {
                return nmsPlayer;
            }
        }
        return null; // Player with the given entity ID not found
    }


    private void sendPacket(Packet packet, PlayerData user, PlayerData forcedUser) {
        Anticheat.INSTANCE.getInstanceManager().getInstance().sendPacket(packet, user);

        if (forcedUser != null && user.getPlayer().getUniqueId() != forcedUser.getPlayer().getUniqueId()) {
            Anticheat.INSTANCE.getInstanceManager().getInstance().sendPacket(packet, forcedUser);
        }
    }

    private static Player getRandomPlayer(PlayerData user) {
        Player randomPlayer = null;

        try {
            if (Bukkit.getServer().getOnlinePlayers().size() > 1) {
                List<Player> onlinePlayers = new ArrayList<>();

                for (Player online : user.getPlayer().getWorld().getPlayers()) {
                    if (online.getUniqueId().toString().equalsIgnoreCase(user.getPlayer().getUniqueId().toString()))
                        continue;
                    onlinePlayers.add(online);
                }

                randomPlayer = onlinePlayers.get(new Random().nextInt(onlinePlayers.size()));
            } else {
                randomPlayer = user.getPlayer();
            }
        } catch (Exception ignored) {}

        return randomPlayer;
    }
}
