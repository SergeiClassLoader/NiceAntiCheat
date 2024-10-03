package pro.cyrent.anticheat.impl.processor.connection.backtrack;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.check.impl.misc.netanalysis.*;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.evicting.EvictingList;
import pro.cyrent.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Deque;
import java.util.LinkedList;

@Getter
@Setter
public class BackTrackProcessor extends Event {
    private final PlayerData data;
    private boolean sentFlying;
    private int movements;
    private int lagTicks;
    private double buffer;

    private boolean pauseNettyQueue = false;

    private final Deque<Object> serverQueue = new LinkedList<>();

    private final Deque<Integer> movementSamples = new EvictingList<>(100);

    private double bufferH;
    private int lastDelay;

    private boolean startDelayer = false;

    private double checkCThreshold;

    private int lastCancel;
    private int stage = 0;

    private double ConnectionIThreshold, ConnectionIVerbose;

    private double ConnectionJThreshold, ConnectionJVerbose;

    private double times;

    private boolean flaggedCheckH = false, flaggedCheckJ = false, flaggedCheckI = false;
    private boolean recentHighSpike = false;

    private int lastTransaction;

    private int cancelHits1;
    private long lastFlying1;
    private int amount;
    private long lastFail;
    private long prevDelta;

    private double checkBufferB, checkBufferD, bufferE, bufferESecond;

    private long lastTransactionTime;
    private long lastMovementTime, currentMovementTime;

    private boolean averageMovement = false, sentTransaction, lastSentTransaction;

    private int cancelHits;

    private long timeSent;

    private int counts;

    private double bufferF, checkBufferG;

    private boolean flaggedCheckC = false, flaggedCheckA = false, flaggedCheckB = false, flaggedCheckD = false,
            flaggedCheckE = false, flaggedCheckF = false, flaggedCheckG = false;

    private int lastCombat, lastFlying;

    public BackTrackProcessor(PlayerData user) {
        this.data = user;
    }

    public void onPacketPre(PacketEvent event) {
        if (event.getPacketReceiveEvent() != null) {


            if (event.getType() == PacketType.Play.Client.INTERACT_ENTITY) {
                long delta = (System.currentTimeMillis() - this.lastFlying);

                if (this.cancelHits-- > 0) {
                    if (!Anticheat.INSTANCE.getConfigValues().isDisableBlock()) {
                        event.getPacketReceiveEvent().setCancelled(true);
                    }
                }

                if (delta < 27L && this.prevDelta > 50L
                        && !Anticheat.INSTANCE.isServerLagging()) {

                    if (delta < 10L && this.amount++ >= 12 && this.cancelHits <= -3) {
                        int stage = 6;

                        if (this.amount >= 20) {
                            stage = 10;
                        }

                        data.sendDevAlert("Fake Lag",
                                "Fake Lag Detection",
                                "Cancel Hits=" + this.cancelHits);

                        this.cancelHits = stage;
                    }

                    this.lastFail = System.currentTimeMillis();
                }

                this.prevDelta = delta;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {

                if (this.startDelayer) {
                    this.lastCancel = 0;
                }

                this.lastTransactionTime = System.currentTimeMillis();

                this.sentTransaction = true;
                this.lastTransaction++;

                //check a
                ConnectionA ConnectionA = (ConnectionA) getData().getCheckManager().forClass(ConnectionA.class);
                ConnectionD ConnectionD = (ConnectionD) getData().getCheckManager().forClass(ConnectionD.class);
                ConnectionE ConnectionE = (ConnectionE) getData().getCheckManager().forClass(ConnectionE.class);

                ConnectionF ConnectionF = (ConnectionF) getData().getCheckManager().forClass(ConnectionF.class);
                ConnectionI ConnectionI = (ConnectionI) getData().getCheckManager().forClass(ConnectionI.class);
                ConnectionJ ConnectionJ = (ConnectionJ) getData().getCheckManager().forClass(ConnectionJ.class);

                if (ConnectionA != null && ConnectionA.isEnabled() && !this.startDelayer) {
                    this.runCheckA(event);
                }

                if (ConnectionD != null && ConnectionD.isEnabled() && !this.startDelayer) {
                    this.runCheckD(event);
                }

                if (ConnectionE != null && ConnectionE.isEnabled() && !this.startDelayer) {
                    this.runCheckE(event);
                }

                if (ConnectionF != null && ConnectionF.isEnabled() && !this.startDelayer) {
                    this.runCheckF(event);
                }


                if (ConnectionI != null && ConnectionI.isEnabled() && !this.startDelayer) {
                    this.runCheckI(event);
                }

                if (ConnectionJ != null && ConnectionJ.isEnabled() && !this.startDelayer) {
                    this.runCheckJ();
                }


                //random interval between delays when detected
                int time = MathUtil.getRandomInteger(35, 23);

                if (this.counts > 8 && this.startDelayer) {
                    this.startDelayer = false;
                    this.counts = 0;
                } else if (!this.startDelayer && this.counts > 10) {
                    this.counts = 0;
                }

                //check if last delay time has passed while delayer is going.
                if (this.lastDelay > time && this.startDelayer) {
                    //sends all packets.
                    this.sendAll();

                    this.counts++;
                    this.lastDelay = 0;
                    this.lastCancel = 0;
                }


                this.lastDelay++;
                this.sentFlying = false;
                this.movements = 0;
            }

            if (event.isMovement()) {

                this.lastFlying1 = System.currentTimeMillis();

                if ((System.currentTimeMillis() - this.lastFail) > 40000L) {
                    if (this.cancelHits1 > 0) {
                        this.cancelHits1 = 0;
                    }

                    this.amount = 0;
                }

                //check B
                this.lastMovementTime = currentMovementTime;
                this.currentMovementTime = System.currentTimeMillis();

                ConnectionB ConnectionB = (ConnectionB) getData().getCheckManager().forClass(ConnectionB.class);

                if (ConnectionB != null && ConnectionB.isEnabled() && !this.startDelayer) {
                    this.runCheckB(event);
                }

                ConnectionC ConnectionC = (ConnectionC) getData().getCheckManager().forClass(ConnectionC.class);

                if (ConnectionC != null && ConnectionC.isEnabled() && !this.startDelayer) {
                    this.runCheckC(event);
                }

                ConnectionG ConnectionG = (ConnectionG) getData().getCheckManager().forClass(ConnectionG.class);

                if (ConnectionG != null && ConnectionG.isEnabled() && !this.startDelayer) {
                    this.runCheckG(event);
                }

                ConnectionH ConnectionH = (ConnectionH) getData().getCheckManager().forClass(ConnectionH.class);

                if (ConnectionH != null && ConnectionH.isEnabled() && !this.startDelayer) {
                    this.runCheckH(event);
                }

                this.sentFlying = true;
                this.movements++;
                this.lagTicks--;
                this.lastCancel++;
                this.lastTransaction = 0;
                this.sentTransaction = false;
            }

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interactEntity = new WrapperPlayClientInteractEntity(event.getPacketReceiveEvent());

                if (interactEntity.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                    if (this.startDelayer && this.cancelHits++ < 40) {
                        if (Anticheat.INSTANCE.getConfigValues().isUseBackTrack()) {
                            event.getPacketReceiveEvent().setCancelled(true);
                        }

                    }
                }
            }
        }


        //backtrack only delays incomming packets not outgoing

        if (event.getPacketSendEvent() != null) {
            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION) {
                if (getData().getMovementProcessor().getSkippedPackets() >= 5)  {
                    this.lagTicks = 20;
                }
            }
        }


        //start delayer.
//        if (this.startDelayer) {
//            this.delay(event);
//            this.lastCancel = 0;
    }

    public void runCheckI(PacketEvent event) {

        if (!this.sentFlying) return;

        boolean attacking = getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3;
        boolean velocity = getData().getVelocityProcessor().getVelocityTicksConfirmed() < 60;

        long delta = Math.abs(getData().getTransactionProcessor().getKeepAlivePing()
                - getData().getTransactionProcessor().getTransactionPing());
        long deltaTransactionLast = Math.abs(getData().getTransactionProcessor().getTransactionPing()
                - getData().getTransactionProcessor().getLastTransactionPing());

        if (attacking && velocity) {
            if (this.lagTicks >= 19
                    && delta >= 50
                    && deltaTransactionLast >= 50
                    && this.movements >= 1
                    && getData().getMovementProcessor().getTick() > 60
                    && this.sentTransaction && this.sentFlying) {
                this.ConnectionIThreshold++;
            } else {
                this.ConnectionIThreshold -= Math.min(this.ConnectionIThreshold, .3);
            }
        } else {
            if (this.lagTicks < 12 && this.ConnectionIThreshold > 20 && getData().getMovementProcessor().getTick() > 60) {
                if (++this.ConnectionIVerbose > 50) {
                    this.ConnectionIThreshold = 40;
                    this.startDelayer = true;
                    this.flaggedCheckI = true;
                    this.lastDelay = 0;
                    this.times++;
                }
            } else {
                this.ConnectionIVerbose -= Math.min(this.ConnectionIVerbose, .25);
            }

            this.ConnectionIThreshold -= Math.min(this.ConnectionIThreshold, .1);
        }

    }

    public void runCheckJ() {

        if (!this.sentFlying) return;

        boolean attacking = getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 4;

        long delta = Math.abs(getData().getTransactionProcessor().getKeepAlivePing()
                - getData().getTransactionProcessor().getTransactionPing());

        long deltaTransaction = Math.abs(getData().getTransactionProcessor().getPostTransactionPing()
                - getData().getTransactionProcessor().getTransactionPing());

        long deltaTransactionLast = Math.abs(getData().getTransactionProcessor().getTransactionPing()
                - getData().getTransactionProcessor().getLastTransactionPing());

        if (this.ConnectionJVerbose > 4.0 && attacking && getData().getMovementProcessor().getTick() > 100) {
            this.ConnectionJVerbose = 3.5;
            //this.startDelayer = true;
            this.flaggedCheckJ = true;
            //this.lastDelay = 0;

        }

        if (attacking && getData().getMovementProcessor().getTick() > 100) {
            if (delta >= 100 || deltaTransaction >= 100) {
                this.ConnectionJThreshold++;
            } else {
                this.ConnectionJThreshold -= Math.min(this.ConnectionJThreshold, .1);
            }

            this.ConnectionJThreshold -= Math.min(this.ConnectionJThreshold, .00725);
        } else {
            if (deltaTransaction < 60 && delta < 60 && deltaTransactionLast < 60
                    && getData().getMovementProcessor().getTick() > 100
                    && this.ConnectionJThreshold > 15) {
                this.ConnectionJThreshold = 7;
                ++this.ConnectionJVerbose;
            } else {
                this.ConnectionJVerbose -= Math.min(this.ConnectionJVerbose, .00925);
            }

            this.ConnectionJVerbose -= Math.min(this.ConnectionJVerbose, .0125);
        }
    }

    public void runCheckB(PacketEvent event) {
        boolean attacking = getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3;
        boolean velocity = getData().getVelocityProcessor().getVelocityTicksConfirmed() < 60;

        if (velocity && attacking) {

            if (this.sentTransaction && this.lastTransaction > 20 && !this.lastSentTransaction
                    && getData().getMovementProcessor().getTick() > 100) {
                if (++this.checkBufferB > 18) {
                    this.checkBufferB = 16;

                    if (!this.startDelayer) {
                        this.startDelayer = true;
                        this.flaggedCheckB = true;
                        this.lastDelay = 0;

                    }
                    this.lastCancel = 0;

                }
            } else {
                this.checkBufferB -= Math.min(this.checkBufferB, 0.02);

            }
        } else {
            this.checkBufferB -= Math.min(this.checkBufferB, 0.0025);
        }

        this.lastSentTransaction = this.sentTransaction;
    }


    //vape check
    public void runCheckD(PacketEvent event) {
        boolean attacking = getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3;
        boolean velocity = getData().getVelocityProcessor().getVelocityTicksConfirmed() < 60;

        long delta = Math.abs(getData().getTransactionProcessor().getKeepAlivePing()
                - getData().getTransactionProcessor().getTransactionPing());

        long deltaTransaction = Math.abs(getData().getTransactionProcessor().getPostTransactionPing()
                - getData().getTransactionProcessor().getTransactionPing());

        int preSize = getData().getTransactionProcessor().getTransactionQueue().size();


        if (attacking && velocity) {
            if (preSize > 4 && delta > 100 && deltaTransaction > 100
                    && getData().getMovementProcessor().getTick() > 300) {
                if (this.checkBufferD++ > 65) {
                    this.checkBufferD = 30;

                    if (!this.startDelayer) {
                        this.flaggedCheckD = true;
                        this.startDelayer = true;
                        this.lastDelay = 0;
                        this.lastCancel = 0;
                        this.cancelHits = 0;
                    }
                }
            } else {
                this.checkBufferD -= Math.min(this.checkBufferD, 0.02);
            }
        } else {
            this.checkBufferD -= Math.min(this.checkBufferD, 0.02);
        }
    }

    public void runCheckE(PacketEvent event) {
        if (!this.sentFlying) {
            boolean attacking = getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3;


            if (attacking && getData().getConnectionProcessor().isFlyingSet()) {
                int delta = getData().getConnectionProcessor().getFlyingCombatDifference();
                int combat = getData().getConnectionProcessor().getAverageCombatFlying();
                int flying = getData().getConnectionProcessor().getAverageFlying();
                int skipped = getData().getMovementProcessor().getSkippedPackets();

                if (skipped > 2
                        && (combat > 3 && delta > 3 && flying < 3 || flying > 3 && delta > 3 && combat < 3)
                        && getData().getMovementProcessor().getTick() > 60) {
                    if (++this.bufferE >= 4.5) {
                        this.bufferE = 2.5;

                        if (!this.startDelayer) {
                            this.flaggedCheckE = true;
                            this.startDelayer = true;
                            this.lastDelay = 0;
                        }
                    }
                } else {
                    this.bufferE -= Math.min(this.bufferE, 0.035);
                }

                this.lastCombat = combat;
                this.lastFlying = flying;
            } else {
                this.bufferE -= Math.min(this.bufferE, 0.02);
                this.bufferESecond -= Math.min(this.bufferESecond, 0.008);
            }
        } else {
            this.bufferE -= Math.min(this.bufferE, 0.2);
            this.bufferESecond -= Math.min(this.bufferESecond, 0.008);
        }
    }


    public void runCheckF(PacketEvent event) {
        boolean attacking = getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3;


        if (attacking && getData().getConnectionProcessor().isFlyingSet()) {
            int delta = getData().getConnectionProcessor().getFlyingCombatDifference();
            int combat = getData().getConnectionProcessor().getAverageCombatFlying();
            int flying = getData().getConnectionProcessor().getAverageFlying();
            int skipped = getData().getMovementProcessor().getSkippedPackets();

            if (skipped > 17 && flying > 18 && combat > 18 && delta < 2 && !this.startDelayer
                    && getData().getMovementProcessor().getTick() > 60) {
                if (++this.bufferF > 70) {
                    this.bufferF = 0;

                    this.flaggedCheckF = true;
                    this.startDelayer = true;
                    this.lastDelay = 0;
                }
            }
        }
    }

    public void runCheckG(PacketEvent event) {
        boolean attacking = getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3;

        if (!attacking) {
            if (getData().getVelocityProcessor().getServerVelocityTicks() < 20
                    && getData().getTransactionProcessor().getLastConfirmationTick() > 20) {
                this.recentHighSpike = true;
            }
        }

        if (attacking && getData().getMovementProcessor().getTick() > 60
                && this.recentHighSpike && getData().getTransactionProcessor().getLastConfirmationTick() < 10) {

            if (++this.checkBufferG > 5) {
                this.checkBufferG = 3;
                this.flaggedCheckG = true;
                this.startDelayer = true;
                this.lastDelay = 0;
            }

            this.recentHighSpike = false;
        } else {
            this.checkBufferG -= Math.min(this.checkBufferG, 0.007);
        }
    }

    public void runCheckR() {
        if (!this.startDelayer) {
            this.startDelayer = true;
            this.lastDelay = 0;
            this.times++;
        }
    }

    public void runCheckH(PacketEvent event) {

        boolean attacking = getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 2;
        boolean velocity = getData().getVelocityProcessor().getVelocityTicksConfirmed() < 200;

        int combatPing = getData().getConnectionProcessor().getAverageCombatPing();
        int pingOutCombat = getData().getConnectionProcessor().getAverageNonCombatPing();
        int ping = (int) getData().getTransactionProcessor().getTransactionPing();

        if (attacking && velocity && this.lagTicks < 1
                && getData().getMovementProcessor().getTick() > 60) {

            int deltaCombatInOut = Math.abs(combatPing - pingOutCombat);

            int deltaPingOut = Math.abs(ping - combatPing);
            int deltaPingIn = Math.abs(ping - pingOutCombat);

            if (deltaPingIn > 75 && deltaCombatInOut > 75 && deltaPingOut < 20) {
                if (++this.bufferH > 35) {
                    this.bufferH = 20;
                    this.startDelayer = true;
                    this.lastDelay = 0;

                    this.flaggedCheckH = true;

                    //cancel reach check
                    this.lastCancel = 0;
                }
            } else {
                this.bufferH -= Math.min(this.bufferH, .5);
            }
        } else {
            this.bufferH -= Math.min(this.bufferH, 0.035);
        }
    }

    public void runCheckA(PacketEvent event) {
        if (this.sentFlying) {

            boolean attacking = getData().getCombatProcessor().getLastUseEntityTimer().getDelta() <= 2;
            // boolean velocity = getData().getVelocityProcessor().getVelocityTicksConfirmed() <= 60;

            boolean verifyConnection = getData().getConnectionProcessor().isConnectionSet()
                    && getData().getConnectionProcessor().getAverageCombatPing()
                    > (getData().getConnectionProcessor().getAverageNonCombatPing() + 50);

            //slinky backtrack check aka any backtrack that sends flyings before the confirmation.
            if (attacking && this.movements >= 3 && this.lagTicks < 1 && verifyConnection
                    && getData().getMovementProcessor().getTick() > 60) {

                //7 buffer
                if ((this.buffer += (this.movements == 3 ? .50 : 1)) > 7) {
                    this.buffer = 6;

                    //start delayer
                    if (!this.startDelayer) {
                        this.startDelayer = true;
                        this.lastDelay = 0;

                        this.flaggedCheckA = true;

                        //amount of hits for canceling gets reset
                        //       this.cancelHits = 0;

                        //if greater than 2 then cancel 3 hits.
                        this.times++;

                        //cancel reach check
                        this.lastCancel = 0;
                    }
                }
            } else {
                this.buffer -= this.buffer > 0 ? 0.005 : 0;
                this.times -= Math.min(this.times, 0.02);
            }
        }
    }

    public void runCheckC(PacketEvent event) {
        long deltaFlying = Math.abs(this.currentMovementTime - this.lastMovementTime);
        long deltaTransactionFlying = Math.abs(this.currentMovementTime - this.lastTransactionTime);

        boolean attacking = getData().getCombatProcessor().getLastUseEntityTimer().getDelta() < 3;
        boolean velocity = getData().getVelocityProcessor().getVelocityTicksConfirmed() < 60;

        boolean isDelayingOutgoingOnly = deltaFlying < 70 && deltaFlying > 40 && deltaTransactionFlying > 500;

        if (this.lagTicks > 0) {
            this.checkCThreshold -= Math.min(this.checkCThreshold, 0.1);
        }

        if (this.checkCThreshold <= 20) {
            this.flaggedCheckC = false;
        }

        if (attacking && velocity && getData().getMovementProcessor().getTick() > 60) {
            if (isDelayingOutgoingOnly && this.lagTicks < 1) {
                if (++this.checkCThreshold > 20) {
                    this.checkCThreshold = 14;

                    if (!this.startDelayer) {
                        this.flaggedCheckC = true;
                        this.startDelayer = true;
                        this.lastDelay = 0;
                        this.lastCancel = 0;
                        this.times++;
                    }

                }
            } else {
                this.checkCThreshold -= Math.min(this.checkCThreshold, 0.008);
            }
        } else {
            this.checkCThreshold -= Math.min(this.checkCThreshold, 0.008);
        }
    }

//    public void delay(PacketEvent packetEvent) {
//
//
//        if (packetEvent.getPacketSendEvent() != null) {
//
//            //pause if sending out packets.
//            if (this.pauseNettyQueue) {
//                return;
//            }
//
//            if (packetEvent.getPacketSendEvent().getPacketType()
//                    == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
//                WrapperPlayServerEntityRelativeMoveAndRotation relativeMove =
//                        new WrapperPlayServerEntityRelativeMoveAndRotation(packetEvent.getPacketSendEvent());
//                if (relativeMove.getEntityId() != getData().getPlayer().getEntityId()) {
//
//                    Entity entity = SpigotReflectionUtil.getEntityById(relativeMove.getEntityId());
//
//                    //delay packets & make sure they are player rel moves & etc....
//                    if (entity instanceof Player) {
//
//                        WrapperPlayServerEntityRelativeMoveAndRotation copy =
//                                new WrapperPlayServerEntityRelativeMoveAndRotation(relativeMove.getEntityId(),
//                                        relativeMove.getDeltaX(), relativeMove.getDeltaY(),
//                                        relativeMove.getDeltaZ(), relativeMove.getYaw(), relativeMove.getPitch(),
//                                        relativeMove.isOnGround());
//
//                        this.serverQueue.add(copy);
//                        this.lastCancel = 0;
//                        packetEvent.getPacketSendEvent().setCancelled(true);
//                    }
//                }
//            }
//
//            if (packetEvent.getPacketSendEvent().getPacketType()
//                    == PacketType.Play.Server.ENTITY_ROTATION) {
//                WrapperPlayServerEntityRotation rotation = new WrapperPlayServerEntityRotation(packetEvent.getPacketSendEvent());
//                if (rotation.getEntityId() != getData().getPlayer().getEntityId()) {
//
//                    Entity entity = SpigotReflectionUtil.getEntityById(rotation.getEntityId());
//
//                    if (entity instanceof Player) {
//
//                        WrapperPlayServerEntityRotation copy =
//                                new WrapperPlayServerEntityRotation(rotation.getEntityId(), rotation.getYaw(),
//                                        rotation.getPitch(), rotation.isOnGround());
//
//                        this.serverQueue.add(copy);
//                        packetEvent.getPacketSendEvent().setCancelled(true);
//                    }
//                }
//            }
//
//            if (packetEvent.getPacketSendEvent().getPacketType()
//                    == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
//                WrapperPlayServerEntityRelativeMove relativeMove = new WrapperPlayServerEntityRelativeMove(packetEvent.getPacketSendEvent());
//                if (relativeMove.getEntityId() != getData().getPlayer().getEntityId()) {
//
//                    Entity entity = SpigotReflectionUtil.getEntityById(relativeMove.getEntityId());
//
//                    if (entity instanceof Player) {
//
//                        WrapperPlayServerEntityRelativeMove copy =
//                                new WrapperPlayServerEntityRelativeMove(relativeMove.getEntityId(),
//                                        relativeMove.getDeltaX(), relativeMove.getDeltaY(),
//                                        relativeMove.getDeltaZ(),
//                                        relativeMove.isOnGround());
//
//                        this.serverQueue.add(copy);
//                        this.lastCancel = 0;
//                        packetEvent.getPacketSendEvent().setCancelled(true);
//
//                    }
//                }
//            }
//
//
//            if (packetEvent.getPacketSendEvent().getPacketType()
//                    == PacketType.Play.Server.ENTITY_TELEPORT) {
//                WrapperPlayServerEntityTeleport teleport =
//                        new WrapperPlayServerEntityTeleport(packetEvent.getPacketSendEvent());
//
//                if (teleport.getEntityId() != getData().getPlayer().getEntityId()) {
//
//                    Entity entity = SpigotReflectionUtil.getEntityById(teleport.getEntityId());
//
//                    if (entity instanceof Player) {
//
//                        WrapperPlayServerEntityTeleport copy =
//                                new WrapperPlayServerEntityTeleport(teleport.getEntityId(),
//                                        teleport.getPosition(), teleport.getYaw(), teleport.getPitch(), teleport
//                                        .isOnGround());
//
//                        this.serverQueue.add(copy);
//                        this.lastCancel = 0;
//                        packetEvent.getPacketSendEvent().setCancelled(true);
//                    }
//                }
//            }
//        }
//    }


    public void sendAll() {

        int allSize = this.serverQueue.size();

        //make sure there is data in the queue before running
        if (allSize > 0) {
            //pause anything being added.
            this.pauseNettyQueue = true;

            try {

                while (!this.serverQueue.isEmpty()) {
                    Object object = this.serverQueue.pollFirst();

                    if (object != null) {
                        getData().getTransactionProcessor().sendPacketSilently(object, getData().getPlayer());
                        this.lastCancel = 0;
                    }
                }
            }catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

            //unpause at the end to continue adding later.
            this.pauseNettyQueue = false;
        }
    }
}