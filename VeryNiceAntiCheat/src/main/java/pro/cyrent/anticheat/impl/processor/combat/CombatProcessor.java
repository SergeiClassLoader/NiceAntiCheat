package pro.cyrent.anticheat.impl.processor.combat;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.event.EventTimer;
import pro.cyrent.anticheat.util.evicting.EvictingList;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import pro.cyrent.anticheat.util.math.MathUtil;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public class CombatProcessor extends Event {
    private final PlayerData data;

    private final EventTimer lastUseEntityTimer, lastCancel;
    private Player lastPlayerAttacked, lastPlayerEntityAttacked;
    private Player lastPlayerAllTypes;
    private int lastAttackTick;
    private double cancelTicks;

    private boolean useEntity = false;

    private long lastFailLag;

    private int attacksSentToTarget;
    private int cancelHits;

    private int jumps;
    private int lastSetShot;
    private boolean exemptPunchBow;
    private int bypassTicks;
    private FlyingLocation punchLocation;

    private int lastPlayerAttack, lastPlayerAttackTick;
    private boolean attack = false;

    private boolean cancelMovements = false;
    private boolean cancelTimedAttack = false;

    private Deque<Player> playersList = new EvictingList<>(100);
    private List<Player> vanishQueue = new CopyOnWriteArrayList<>();

    private int cancelTime;

    private int chance;
    private int fixLagTicks;

    private boolean lastAttack = false;
    private boolean create;

    private int hurtTime;

    public CombatProcessor(PlayerData user) {
        this.data = user;
        this.lastUseEntityTimer = new EventTimer(20, user);
        this.lastCancel = new EventTimer(20, user);
    }

    public void postFlying(PacketEvent event) {
        if (event.isMovement()) {
            this.lastAttack = this.attack;
            this.attack = false;
        }
    }

    public void updateTeamTest(String... data) {

        if (!this.create && Anticheat.INSTANCE.getConfigValues().isTeamSpoofer()) {

            //spoofs the players team on the server. (doesn't work for color)
            getData().getTransactionProcessor().sendPacket(new WrapperPlayServerTeams(
                    "dev", WrapperPlayServerTeams.TeamMode.CREATE,
                    (WrapperPlayServerTeams.ScoreBoardTeamInfo) null,
                    data), getData().getPlayer());

            this.create = true;
        }
    }

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getPacketSendEvent() != null) {

            if (Anticheat.INSTANCE.getConfigValues().isHealthSpoofer()) {
                if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
                    WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(event.getPacketSendEvent());

                    Entity entity = SpigotReflectionUtil.getEntityById(metadata.getEntityId());

                    if (!(entity instanceof Player)) {
                        return;
                    }

                    int id;

                    if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_8_8)) {
                        id = 6;
                    } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_13_2)) {
                        id = 7;
                    } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_16_5)) {
                        id = 8;
                    } else {
                        id = 9;
                    }

                    List<EntityData> entityMetadataList = metadata.getEntityMetadata();
                    for (EntityData data : entityMetadataList) {
                        if (data.getIndex() == id && metadata.getEntityId() != getData().getPlayer().getEntityId()) {
                            this.chance++;

                            if (this.chance % 2 == 0) {
                                data.setValue(MathUtil.getRandomFloat(0, 20.0F));
                            } else if (this.chance % 3 == 0) {
                                data.setValue(Float.NaN);
                            } else if (this.chance % 5 == 0) {
                                data.setValue(Float.POSITIVE_INFINITY);
                            } else {
                                data.setValue(Float.MIN_VALUE);
                            }

                            if (this.chance > 100) this.chance = 0;
                        }
                    }
                }
            }
        }

        if (event.getPacketSendEvent() != null) {
            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.ENTITY_STATUS) {
                WrapperPlayServerEntityStatus entityStatus =
                        new WrapperPlayServerEntityStatus(event.getPacketSendEvent());

                if (getData().getPlayer().getEntityId() == entityStatus.getEntityId()) {

                    if (entityStatus.getStatus() == 2) {
                        //confirm with transactions on target player to get their proper hurt-time.
                        getData().getTransactionProcessor().confirmPre(() -> this.hurtTime = 10);
                    }
                }
            }
        }

        if (event.getPacketReceiveEvent() != null) {
            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interactEntity =
                        new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                this.useEntity = true;

                if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {

                    this.lastUseEntityTimer.reset();

                    Entity player = SpigotReflectionUtil.getEntityById(interactEntity.getEntityId());

                    if (player instanceof Player) {

                        this.attack = true;
                        this.lastAttackTick = 0;
                        this.lastPlayerAttack = 0;
                        this.lastPlayerAttackTick = 0;
                        this.lastPlayerEntityAttacked = (Player) player;

                        if (getData().getVelocityProcessor().getCombatStopTicks() > 0) {
                            if (!Anticheat.INSTANCE.getConfigValues().isDisableBlock()) {
                                event.getPacketReceiveEvent().setCancelled(true);
                            }

                        }

                        if (Math.abs(System.currentTimeMillis() - this.lastFailLag) < 3000) {
                            if (!Anticheat.INSTANCE.getConfigValues().isDisableBlock()) {
                                event.getPacketReceiveEvent().setCancelled(true);
                            }
                        }

                        PlayerData playerData = Anticheat.INSTANCE.getUserManager().getUser((Player) player);

                        if (playerData != null) {
                            this.lastPlayerAttacked = (Player) player;
                            this.attacksSentToTarget++;
                            this.updateTeamTest(playerData.getUsername(), getData().getUsername());

                            if (this.cancelTimedAttack) {
                                if (!Anticheat.INSTANCE.getConfigValues().isDisableBlock()) {
                                    event.getPacketReceiveEvent().setCancelled(true);
                                }
                            }

                            if (this.cancelHits-- > 0) {
                                if (!Anticheat.INSTANCE.getConfigValues().isDisableBlock()) {
                                    event.getPacketReceiveEvent().setCancelled(true);
                                }
                            }
                        }

                        this.lastPlayerAllTypes = (Player) player;
                    }
                }
            }

            if (event.isMovement()) {
                ++this.lastAttackTick;
                this.lastPlayerAttack++;
                this.lastPlayerAttackTick++;
                this.lastSetShot++;

                if (this.cancelMovements) {
                    //   getData().debug("cancel");
                    //       event.getPacketReceiveEvent().setCancelled(true);
                }

                if (getData().getReachProcessor().getLastEntitySwitch().getDelta() < 3 && this.create) {
                    this.create = false;
                }

                /**
                 * Punch bow shit for Speed B
                 */
                if (getData().getVelocityProcessor()
                        .getLastVelocityData() != null && getData().getLastShotByArrowTimer().getDelta() < 3) {

                    double speed = getData().getVelocityProcessor()
                            .getLastVelocityData().getSpeed();

                    if (speed > 1.0) {

                        FlyingLocation to = getData().getMovementProcessor().getTo();

                        if (to == null) return;

                        this.punchLocation = new FlyingLocation(getData().getPlayer().getWorld().getName(),
                                to.getPosX(), to.getPosY(), to.getPosZ());

                        this.exemptPunchBow = true;

                        this.lastSetShot = 0;

                    }
                }

                if (this.exemptPunchBow && this.punchLocation != null) {

                    FlyingLocation to = getData().getMovementProcessor().getTo();

                    if (to == null) return;

                    double distance = to.distanceSquaredXZ(this.punchLocation);

                    double deltaXZ = getData().getMovementProcessor().getDeltaXZ();

                    double deltaY = getData().getMovementProcessor().getDeltaY();

                    if (deltaY > 0.0 && this.lastSetShot > 10) {
                        this.bypassTicks++;

                        if (this.bypassTicks > 60) {
                            this.exemptPunchBow = false;
                            this.punchLocation = null;
                            this.bypassTicks = 0;
                        }
                    }

                    if (deltaXZ > .5 && this.lastSetShot > 20) {
                        if (++this.bypassTicks > 170) {
                            this.exemptPunchBow = false;
                            this.punchLocation = null;
                            this.bypassTicks = 0;
                        }
                    }

                    if (distance > 350 && getData().getMovementProcessor().getAirTicks() >= 20) {
                        this.exemptPunchBow = false;
                        this.punchLocation = null;
                    }

                    if (!getData().getMovementProcessor().getTo().isOnGround() &&
                            getData().getMovementProcessor().getFrom().isOnGround()) {
                        this.jumps++;

                        if (this.jumps > 3) {
                            this.exemptPunchBow = false;
                            this.jumps = 0;
                            this.punchLocation = null;
                        }
                    }

                    if (getData().getMovementProcessor().getGroundTicks() >= 20) {
                        this.exemptPunchBow = false;
                        this.punchLocation = null;
                    }

                    if (getData().getMovementProcessor().getAirTicks() >= 20) {
                        ++this.bypassTicks;

                        if (this.bypassTicks > 100) {
                            this.exemptPunchBow = false;
                            this.punchLocation = null;
                            this.bypassTicks = 0;
                        }
                    }

                    if (this.lastSetShot > 250) {
                        this.exemptPunchBow = false;
                        this.punchLocation = null;
                    }
                }

                this.hurtTime -= Math.min(this.hurtTime, 1);
            }
        }
    }
}