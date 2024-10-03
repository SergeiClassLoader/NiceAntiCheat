package pro.cyrent.anticheat.api.check.impl.combat.entity;


import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.*;
import pro.cyrent.anticheat.api.check.impl.combat.entity.utils.EntityData;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.math.MathUtil;
import pro.cyrent.anticheat.util.stream.StreamUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import pro.cyrent.anticheat.api.check.*;

@CheckInformation(
        name = "Entity",
        checkType = CheckType.COMBAT,
        checkNameEnum = CheckName.ENTITY,
        description = "Detects if the player constantly attacks the kill aura bot",
        state = CheckState.BETA)
public class EntityA extends Check {

    private double threshold;

    private boolean flickerCreated = false;
    private boolean flicker = false;
    private int ticks, flickerTicks, flickerWait;
    public boolean created = false;

    private int headSnaps;
    private int botAttacks;

    private double maxHeadSpeed, maxYawSpeed, maxPitchSpeed;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interactEntity =
                        new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                Player playerTy = getData().getCombatProcessor().getLastPlayerAllTypes();
                Player player = getData().getPlayer();


                if (playerTy != null && player != null) {

                    long now = System.currentTimeMillis();
                    long flag = getData().getLastFlag();


                    //todod: check when to run it now always run it.
                    if (!this.created && (now - flag) < 450L) {
                        try {

                            createEntity(getData(), null,
                                    player.getLocation().getY() + 3.0, player.getLocation().getX(),
                                    player.getLocation().getZ(), 0, 0, 0, false);

                        }catch (NullPointerException e) {
                            e.printStackTrace();
                        }

                        this.ticks = 0;
                    }

                    int entityID = interactEntity.getEntityId();

                    if (this.created && getData().getForcedUser() == null) {
                        if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                            if (StreamUtil.anyMatch(getData().getEntityHelper1_8().getCustomEntities(), customEntity ->
                                    customEntity.getCreationData().getEntityType() == EntityData.EntityType.ADVANCED
                                            && customEntity.getEntityID() == entityID)) {
                                getData().setLastAttackedBot(0);

                                this.threshold++;

                                this.ticks = 0;

                                if (this.threshold > 20) {
                                    this.fail("attacks="+this.threshold);
                                }
                            } else {
                                this.threshold -= Math.min(this.threshold, 1);
                            }
                        }
                    } else if (getData().getForcedUser() != null && this.created) {
                        if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                            if (StreamUtil.anyMatch(getData().getEntityHelper1_8().getCustomEntities(), customEntity ->
                                    customEntity.getCreationData().getEntityType() == EntityData.EntityType.ADVANCED
                                            && customEntity.getEntityID() == entityID)) {
                                this.botAttacks++;
                            }
                        }
                    }
                }
            }

            if (event.isMovement()) {
                if (this.created && getData().getForcedUser() != null) {
                    double deltaYaw = getData().getMovementProcessor().getDeltaYawAbs();
                    double deltaPitch = getData().getMovementProcessor().getDeltaPitchAbs();

                    if (deltaYaw > 100 || deltaPitch > 40) {
                        this.headSnaps++;
                    }

                    this.maxPitchSpeed = Math.max(this.maxPitchSpeed, deltaPitch);
                    this.maxYawSpeed = Math.max(this.maxYawSpeed, deltaYaw);
                    this.maxHeadSpeed = Math.max(this.maxPitchSpeed, this.maxYawSpeed);
                }
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {

                if (!this.created || getData().getForcedUser() == null) {
                    this.botAttacks = 0;
                    this.maxHeadSpeed = 0;
                    this.maxYawSpeed = 0;
                    this.maxPitchSpeed = 0;
                    this.headSnaps = 0;
                }

                getData().setBotRunning(this.created);

                getData().setLastAttackedBot(getData().getLastAttackedBot() + 1);

                if (this.created) {
                    this.ticks++;

                    if (this.ticks > (this.flicker ? 350 : 137)) {
                        Anticheat.INSTANCE.getEntityManager().remove(getData(), getData().getForcedUser(),
                                EntityData.EntityType.ADVANCED);

                        if (getData().getForcedUser() != null) {
                            getData().getForcedUser().getPlayer().sendMessage(Anticheat.INSTANCE.getMessageValues().getLineMessage());
                            getData().getForcedUser().getPlayer().sendMessage(ChatColor.RED + ""
                                    + ChatColor.UNDERLINE + "Player Results (" + getData().getUsername() + ")");

                            getData().getForcedUser().getPlayer().sendMessage("");

                            getData().getForcedUser().getPlayer()
                                    .sendMessage(ChatColor.GREEN + "Bot-Attacks: " + this.botAttacks);

                            getData().getForcedUser().getPlayer()
                                    .sendMessage(ChatColor.GREEN + "Head Spins: " + this.headSnaps);

                            getData().getForcedUser().getPlayer()
                                    .sendMessage(ChatColor.GREEN + "Overall Max Head Speed: " + this.maxHeadSpeed);

                            getData().getForcedUser().getPlayer()
                                    .sendMessage(ChatColor.GREEN + "Max Yaw Speed: " + this.maxYawSpeed);

                            getData().getForcedUser().getPlayer()
                                    .sendMessage(ChatColor.GREEN + "Max Pitch Speed: " + this.maxPitchSpeed);

                            getData().getForcedUser().getPlayer().sendMessage(Anticheat.INSTANCE.getMessageValues().getLineMessage());
                        }

                        getData().setForcedUser(null);
                        this.created = false;
                    }
                }

                if (this.created && this.flicker) {

                    Anticheat.INSTANCE.getEntityManager().removeAll(getData(), getData().getForcedUser());

                    if (this.flickerWait-- < 1) {
                        this.flickerTicks+=2;

                        createEntity(getData(), getData().getForcedUser(),
                                getData().getPlayer().getLocation().getY() + 3.0, getData().getPlayer().getLocation().getX(),
                                getData().getPlayer().getLocation().getZ(), 0, 0, 0, true);

                        this.flickerWait = MathUtil.getRandomInteger(40, 3) + this.flickerTicks;

                        if (this.flickerTicks > 40) {
                            this.flickerTicks = 0;
                        }
                    }
                }

                if (getData().getEntityHelper1_8().getCustomEntities().size() > 0 && this.created
                        && getData().getEntityHelper1_8() != null && !this.flicker) {

                    StreamUtil.filter(getData().getEntityHelper1_8().getCustomEntities(), customEntity -> customEntity.
                                    getCreationData() != null && customEntity.getCreationData().getEntityType() != null
                                    && customEntity.getCreationData().getEntityType() == EntityData.EntityType.ADVANCED)
                            .forEach(customEntity -> {

                                if (customEntity == null || getData() == null) return;

                                Location location = getBehind(getData().getPlayer(),
                                        customEntity.getLastReportedLocation());

                                if (location == null) {
                                    return;
                                }

                                customEntity.tickEntity(getData(), getData().getForcedUser(),
                                        new FlyingLocation(location.getX(),
                                                location.getY() + .42F,
                                                location.getZ(), location.getYaw(), location.getPitch()),
                                        true);
                            });
                }
            }
        }
    }


    public void createEntity(PlayerData user,
                             PlayerData forcedUser,
                             double y, double x, double z, double offsetX, double offsetY, double offsetZ,
                             boolean flicker) {

        if (forcedUser != null) {
            user.setForcedUser(forcedUser);
            getData().setDidBotCommand(false);
            this.ticks = 0;
        }

        this.flicker = flicker;

        EntityData entityData = new EntityData();

        entityData.setEntityType(EntityData.EntityType.ADVANCED);
        entityData.setOnGround(false);


        //fix for sparky antibot.
        float yaw = MathUtil.getRandomFloat(360f, -360f);
        float pitch = MathUtil.getRandomFloat(90F, -90F);

        if (yaw == 0F || pitch == 0F) {
            yaw = MathUtil.getRandomFloat(360F, 0.5F);
            pitch = MathUtil.getRandomFloat(90F, 0.5F);
        }

        entityData.setSpawnLocation(new FlyingLocation(x + offsetX, y + offsetY, z + offsetZ,
                yaw,
                pitch));
        entityData.setPostLocation(new FlyingLocation(x + offsetX, y + offsetY, z + offsetZ,
                yaw, //yaw
                pitch)); //pitch
        entityData.setOffsetX(offsetX);
        entityData.setOffsetY(offsetY);
        entityData.setOffsetZ(offsetZ);


        user.getEntityHelper1_8().getCustomEntities().add(
                user.getEntityHelper1_8().lastEntityBot = ((Anticheat.INSTANCE.getEntityManager()
                        .createEntity(EntityData.EntityType.ADVANCED, user, forcedUser, entityData))));

        this.created = true;
    }

    public Location getBehind(Player player, FlyingLocation flyingLocation) {
        Location location;

        Vector eyeLoc = player.getEyeLocation().getDirection();

        if (flyingLocation != null) {
            boolean look = isPlayerLookingAtEntity(player,
                    new Vector(flyingLocation.getPosX(),
                            flyingLocation.getPosY(),
                            flyingLocation.getPosZ()), 145);

            float random = -MathUtil.getRandomFloat(2.4f, 3.7f);

            if (look || getData().getMovementProcessor().getDeltaYawAbs() > 4.0) {
                random = -MathUtil.getRandomFloat(6.5f, 5.5f);
            }

            if (look) {
                getData().setLookingAtBot(true);
            } else {
                getData().setLookingAtBot(false);
            }

            eyeLoc.setX(eyeLoc.getX() * random);
            eyeLoc.setY(eyeLoc.getY() * -MathUtil.getRandomFloat(0.5f, 2.5f));
            eyeLoc.setZ(eyeLoc.getZ() * random);

            location = player.getLocation().add(eyeLoc);

        } else {
            int chance = MathUtil.getRandomInteger(0, 100);
            float random = chance < 30 ? -MathUtil.getRandomFloat(4.9F,
                    3.9F) : -MathUtil.getRandomFloat(6.5f, 5.5f);

            eyeLoc.setX(eyeLoc.getX() * random);
            eyeLoc.setY(eyeLoc.getY() * -MathUtil.getRandomFloat(0.5f, 2.5f));
            eyeLoc.setZ(eyeLoc.getZ() * random);

            location = player.getLocation().add(eyeLoc);
        }

        return location;
    }

    public static Location getBehindNew(Player player) {
        Location location = player.getLocation();

        double yaw = Math.toRadians(player.getLocation().getYaw());

        float random = -MathUtil.getRandomFloat(3.4f, 2.3f);

        double x = location.getX() - random * Math.sin(yaw);
        double z = location.getZ() + random * Math.cos(yaw);

        location.setX(x);
        location.setZ(z);

        return location;
    }

    public static Entity getEntityById(World world, int entityId) {
        for (Entity entity : world.getEntities()) {
            if (entity.getEntityId() == entityId) {
                return entity;
            }
        }
        return null;
    }

    public static boolean isPlayerLookingAtEntity(Player player, Vector vector, double angleThreshold) {
        // Get the direction vector from the player to the entity

        Vector playerToEntity = vector.subtract(player.getEyeLocation().toVector());

        // Normalize the vectors to simplify the calculations
        playerToEntity.normalize();
        Vector playerDirection = player.getEyeLocation().getDirection().normalize();

        // Calculate the dot product of the two normalized vectors
        double dotProduct = playerToEntity.dot(playerDirection);

        // Calculate the angle between the player's view direction and the direction to the entity
        double angle = Math.acos(dotProduct);

        // Convert the angle from radians to degrees
        double angleDegrees = Math.toDegrees(angle);

        // Check if the angle is within the specified threshold
        return angleDegrees <= angleThreshold;
    }
}
