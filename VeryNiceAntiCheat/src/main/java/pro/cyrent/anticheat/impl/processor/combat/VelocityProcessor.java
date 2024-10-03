package pro.cyrent.anticheat.impl.processor.combat;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerExplosion;
import pro.cyrent.anticheat.api.check.impl.combat.velocity.VelocityB;
import pro.cyrent.anticheat.api.check.impl.combat.velocity.VelocityD;
import pro.cyrent.anticheat.api.check.impl.movement.fly.FlyA;
import pro.cyrent.anticheat.api.check.impl.movement.invalidmove.InvalidMoveC;
import pro.cyrent.anticheat.api.check.impl.movement.invalidmove.InvalidMoveG;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.impl.processor.prediction.horizontal.HorizontalProcessor;
import pro.cyrent.anticheat.util.math.MathUtil;
import pro.cyrent.anticheat.util.task.TransactionPacketAction;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public class VelocityProcessor extends Event {
    private final PlayerData data;

    private int serverVelocityTicks;
    private int velocityTicksConfirmed;
    private double deltaY, lastDeltaY, lastLastDeltaY, fromY;
    private int confirmVelocity;
    private int lastMovement;
    private int missedVelocity;
    private double velocityXZ = 0;
    private boolean triggerVelocitySimulations = false;
    private boolean split;

    private double velocityH, velocityY;
    private int extraVelocityTicks;
    private int velocityATicks;
    private boolean confirmed, confirmed2;
    private VelocityDataExtra lastVelocityData;
    private VelocityData velocityDataPre;

    private final Deque<VelocityData> velocityFlyQueues = new ConcurrentLinkedDeque<>();
    private final Deque<VelocityData> velocityQueuesInvalidC = new ConcurrentLinkedDeque<>();
    private final List<VelocityDataB> confirmedVelocities = new CopyOnWriteArrayList<>();

    private int bypassing, ticksSinceVel;
    private int combatStopTicks = 0;
    private int triggerTime;
    private double zeroKnockbackFix;
    private int lastSplit;

    private List<VelocityData> velocityEntries = new ArrayList<>();

    public VelocityProcessor(PlayerData user) {
        this.data = user;
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketSendEvent() != null) {

            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
                WrapperPlayServerEntityVelocity velocity = new WrapperPlayServerEntityVelocity(event.getPacketSendEvent());

                if (velocity.getEntityId() == getData().getPlayer().getEntityId()) {
                    this.confirmVelocity++;
                    this.serverVelocityTicks = 0;
                    this.velocityY = velocity.getVelocity().getY();

                    int maxVelocity = 20 + getData().getTransactionProcessor().getPingTicks();
                    int maxMovement = 5;


                    this.velocityH = Math.hypot((velocity.getVelocity().getX()),
                            (velocity.getVelocity().getZ()));

                    boolean velocityHigh = velocity.getVelocity().getX() > .8
                            || velocity.getVelocity().getY() > .8
                            || velocity.getVelocity().getZ() > .8;

                    if (velocityHigh) {
                        maxMovement = 0;
                        maxVelocity = 20;
                    }

                    if (this.confirmVelocity > maxVelocity && this.lastMovement < maxMovement) {
                        if (++this.bypassing > 5) {
                            getData().kickPlayer("Not sending back transactions while taking knockback.");
                        }
                    } else {
                        this.bypassing = 0;
                    }

                    if (getData().getTickHolder().getTickHandler().getActionMap2().size() > 60
                            && !velocityHigh
                            && this.lastMovement < 20) {
                        if (++this.missedVelocity > 3) {
                            getData().kickPlayer("Too high transaction queue while taking knockback.");
                        }
                    } else {
                        this.missedVelocity = 0;
                    }

                    this.lastVelocityData = new VelocityDataExtra(
                            velocity.getVelocity().getX(),
                            velocity.getVelocity().getY(),
                            velocity.getVelocity().getZ(),
                            MathUtil.hypot(velocity.getVelocity().getX(), velocity.getVelocity().getZ()) * 2
                    );


                    if (Math.abs(velocity.getVelocity().getX()) > 0.9
                            || velocity.getVelocity().getY() > .8
                            || Math.abs(velocity.getVelocity().getZ()) > 0.9) {
                        this.extraVelocityTicks = 0;
                    }

                    if (Math.abs(velocity.getVelocity().getX()) < .10
                            && Math.abs(velocity.getVelocity().getZ()) < .10
                            && velocity.getVelocity().getY() > .2
                            && (getData().getMovementProcessor().getDeltaXZ() == 0
                            || getData().getFishingRodTimer().getDelta() < 20)) {
                        this.extraVelocityTicks = 0;
                    }

                    getData().getHorizontalProcessor().getVelocitySimulator().onVelocity(
                            new HorizontalProcessor.TransactionVelocityEntry(velocity.getVelocity().getX(),
                                    velocity.getVelocity().getY(), velocity.getVelocity().getZ()));

                    TransactionPacketAction pre = () -> {
                        this.velocityY = velocity.getVelocity().getY();
                        this.velocityATicks = 0;

                        this.ticksSinceVel = 0;

                        this.confirmed = true;
                        this.confirmed2 = true;


                        this.velocityH = Math.hypot((velocity.getVelocity().getX()),
                                (velocity.getVelocity().getZ()));

                        this.velocityXZ = Math.hypot((velocity.getVelocity().getX() * 0.6F),
                                (velocity.getVelocity().getZ() * 0.6));

                        if (Math.abs(velocity.getVelocity().getX()) > 0.9
                                || velocity.getVelocity().getY() > .8
                                || Math.abs(velocity.getVelocity().getZ()) > 0.9) {
                            this.extraVelocityTicks = 0;
                        }

                        if (Math.abs(velocity.getVelocity().getX()) < .10
                                && Math.abs(velocity.getVelocity().getZ()) < .10
                                && velocity.getVelocity().getY() > .2
                                && (getData().getMovementProcessor().getDeltaXZ() == 0
                                || getData().getFishingRodTimer().getDelta() < 20)) {
                            this.extraVelocityTicks = 0;
                        }


                        VelocityData velocityData = new VelocityData(
                                velocity.getVelocity().getX(),
                                velocity.getVelocity().getY(),
                                velocity.getVelocity().getZ()
                        );

                        this.velocityDataPre = velocityData;

                        this.velocityFlyQueues.add(velocityData);
                        this.velocityQueuesInvalidC.add(velocityData);

                        this.confirmedVelocities.add(new VelocityDataB(
                                velocity.getVelocity().getX(),
                                velocity.getVelocity().getY(),
                                velocity.getVelocity().getZ(),
                                VelocityType.PRE
                        ));

                        this.confirmVelocity = 0;

                        this.split = true;

                        this.velocityTicksConfirmed = 0;
                    };

                    TransactionPacketAction post = () -> {

                        this.confirmed = false;
                        this.confirmed2 = false;

                        this.split = false;

                        this.confirmedVelocities.add(new VelocityDataB(
                                velocity.getVelocity().getX(),
                                velocity.getVelocity().getY(),
                                velocity.getVelocity().getZ(),
                                VelocityType.POST
                        ));

                        this.confirmVelocity = 0;
                        this.velocityTicksConfirmed = 0;
                    };

                    getData().getTickHolder().confirmFunctionVel(pre);
                    getData().getTickHolder().confirmFunctionAndTickVelocity(post);
                    CompletableFuture.runAsync(() -> this.getData().getTickHolder().pushVelTick());
                }
            }

            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION) {
                this.lastMovement++;
            }
        }

        if (event.getPacketReceiveEvent() != null) {

            if (event.isMovement()) {

                WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

                if (this.triggerVelocitySimulations) {
                    if (this.triggerTime < 20) {
                        this.triggerTime += 2;
                    }
                } else {
                    this.triggerTime -= Math.min(this.triggerTime, 1);
                }
                this.serverVelocityTicks++;
                this.velocityTicksConfirmed++;
                this.extraVelocityTicks++;
                this.ticksSinceVel++;
                this.lastSplit++;

                if (flying.hasPositionChanged()) {
                    this.velocityATicks++;
                }

                this.lastMovement = 0;


                if (this.serverVelocityTicks < 3
                        || this.velocityTicksConfirmed < 3) {

                    if (!getData().generalCancel()
                            && getData().getLastEnderPearl().getDelta() > 40
                            && getData().getLastTeleport().getDelta() > 40) {

                        if (getData().getActionProcessor().getTeleportTicks() < 10
                                && getData().getCollisionWorldProcessor().isGround()) {
                            if (++this.zeroKnockbackFix > 5.0) {
                                this.zeroKnockbackFix = 4.5;
                                this.combatStopTicks = 40;
                            }
                        } else {
                            this.zeroKnockbackFix -= Math.min(this.zeroKnockbackFix, 0.035);
                        }
                    }
                }

                VelocityB velocityB = (VelocityB) getData().getCheckManager().forClass(VelocityB.class);

                if (velocityB != null && velocityB.isEnabled()) {
                    velocityB.onVelocity(event);
                }

                VelocityD velocityD = (VelocityD) getData().getCheckManager().forClass(VelocityD.class);

                if (velocityD != null && velocityD.isEnabled()) {
                    velocityD.onVelocity(event);
                }

                InvalidMoveC invalidMoveC = (InvalidMoveC) getData().getCheckManager().forClass(InvalidMoveC.class);

                if (invalidMoveC != null && invalidMoveC.isEnabled()) {
                    invalidMoveC.runPacket(event);
                }

                List<VelocityData> entries = new ArrayList<>();

                // add null otherwise loop will have nothing if there are no entries
                entries.add(null);

                if (this.confirmed) {
                    if (!this.velocityFlyQueues.isEmpty()) {
                        entries.addAll(this.velocityFlyQueues);
                    }
                    this.confirmed = false;
                } else {
                    if (!this.velocityFlyQueues.isEmpty()) {
                        VelocityData velocityEntry = this.velocityFlyQueues.peekLast();

                        if (velocityEntry != null) {
                            entries.add(velocityEntry);
                        }
                    }
                }

                this.velocityEntries = entries;

                FlyA flyA1 = (FlyA) getData().getCheckManager().forClass(FlyA.class);

                if (flyA1 != null && flyA1.isEnabled()) {
                    flyA1.onFlyCheck(event);
                }

                if (!this.confirmedVelocities.isEmpty()) {
                    for (VelocityDataB data : this.confirmedVelocities) {
                        this.confirmedVelocities.remove(data);
                    }
                }

                if (flying.hasPositionChanged()) {
                    double yPos = flying.getLocation().getY();
                    this.lastLastDeltaY = this.lastDeltaY;
                    this.lastDeltaY = this.deltaY;
                    this.deltaY = yPos - this.fromY;
                    this.fromY = yPos;
                }

                this.velocityDataPre = null;
            }
        }
    }

    @Getter @AllArgsConstructor
    public static final class VelocityData {
        private final double x;
        private final double y;
        private final double z;
    }

    @Getter @AllArgsConstructor
    public static final class VelocityDataExtra {
        private final double x;
        private final double y;
        private final double z;

        private final double speed;
    }

    @Getter @AllArgsConstructor
    public static final class VelocityDataB {
        private final double x;
        private final double y;
        private final double z;
        private final VelocityType velocityType;
    }

    public enum VelocityType {
        PRE, POST
    }
}
