package pro.cyrent.anticheat.impl.processor.debug;

import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.time.TimeUtils;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PacketDebugProcessor extends Event {
    private final PlayerData data;


    public PacketDebugProcessor(PlayerData user) {
        this.data = user;

    }

    public void handlePacketDebugger(PacketEvent event) {
        if (Anticheat.INSTANCE.getPacketLogPlayer() != null && Anticheat.INSTANCE.getPacketLogPlayer()
                .getUniqueId().toString().equals(getData().getUuid().toString())) {
            if (event.getPacketSendEvent() != null) {
                this.handleServerDebug(event.getPacketSendEvent());
            }

            if (event.getPacketReceiveEvent() != null) {
                this.handleClientDebug(event);
            }
        }
    }

    public void handleServerDebug(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.KEEP_ALIVE) {
            WrapperPlayServerKeepAlive keepAlive = new WrapperPlayServerKeepAlive(event);
            Anticheat.INSTANCE.getConnectionPacketLogList().add(
                    "[" + TimeUtils.getSystemTime2() + "] ServerKeepAlivePacket time: "
                            + keepAlive.getId()
                            + "\n"
            );
        }


        if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            WrapperPlayServerEntityVelocity velocity = new WrapperPlayServerEntityVelocity(event);

            if (velocity.getEntityId() == getData().getPlayer().getEntityId()) {
                Anticheat.INSTANCE.getConnectionPacketLogList().add(
                        "[" + TimeUtils.getSystemTime2() + "] ServerVelocityPacket "
                                + "id: " + velocity.getEntityId()
                                + " x: " + velocity.getVelocity().getX()
                                + " y: " + velocity.getVelocity().getY()
                                + " z: " + velocity.getVelocity().getZ()
                                + " deltaY: "+ getData().getMovementProcessor().getDeltaY()
                                + " deltaXZ: "+getData().getMovementProcessor().getDeltaXZ()
                                + "\n");
            }
        }
        
        if (event.getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION) {
            WrapperPlayServerWindowConfirmation confirmation = new WrapperPlayServerWindowConfirmation(event);
            Anticheat.INSTANCE.getConnectionPacketLogList().add(
                    "[" + TimeUtils.getSystemTime2() + "] ServerTransactionPacket "
                            + "action: " + confirmation.getActionId()
                            + " window: " + confirmation.getWindowId()
                            + "\n"
            );
        }
        
        if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
            WrapperPlayServerPlayerPositionAndLook positionAndLook = new WrapperPlayServerPlayerPositionAndLook(event);


            Anticheat.INSTANCE.getPacketLogList().add(
                    "[" + TimeUtils.getSystemTime2() + "] " + "ServerPosition ="
                            + " posX: " + positionAndLook.getX()
                            + " posY: " + positionAndLook.getY()
                            + " posZ: " + positionAndLook.getZ()
                            + " yaw: " + positionAndLook.getYaw()
                            + " pitch: " + positionAndLook.getPitch() + "\n\n\n"
            );
        }
    }

    public void handleClientDebug(PacketEvent packetEvent) {
        
        PacketReceiveEvent event = packetEvent.getPacketReceiveEvent();
        
        if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
            WrapperPlayClientKeepAlive keepAlive = new WrapperPlayClientKeepAlive(event);

            Anticheat.INSTANCE.getConnectionPacketLogList().add(
                    "[" + TimeUtils.getSystemTime2() + "] ClientKeepAlivePacket time: "
                            + keepAlive.getId() + " ping: "
                            + getData().getTransactionProcessor().getKeepAlivePing()
                            + "\n"
            );
        }
        
        if (event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            WrapperPlayClientWindowConfirmation confirmation = new WrapperPlayClientWindowConfirmation(event);

            Anticheat.INSTANCE.getConnectionPacketLogList().add(
                    "[" + TimeUtils.getSystemTime2() + "] ClientTransactionPacket accepted: " +
                            confirmation.isAccepted()
                            + " action: " + confirmation.getActionId()
                            + " window: " + confirmation.getWindowId()
                            + " ping: " + getData().getTransactionProcessor().getTransactionPing()
                            + " queue: " + getData().getTransactionProcessor().getTransactionQueue().size()
                            + " actionInQueue: " + (getData().getTransactionProcessor().getQueuedTransactionsMap()
                            .containsKey(confirmation.getActionId()))
                            + "\n"
            );
        }
        
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {

            WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);
            
            if (digging.getBlockPosition() != null) {
                Anticheat.INSTANCE.getPacketLogList().add(
                        "[" + TimeUtils.getSystemTime2() + "] " + "DigPacket ="
                                + " action: " + digging.getAction().name()
                                + " direction: " + digging.getBlockFace().name()
                                + " positionX: " + digging.getBlockPosition().getX()
                                + " positionY: " + digging.getBlockPosition().getY()
                                + " positionZ: " + digging.getBlockPosition().getZ()
                                + " sequence: " + digging.getSequence() + "\n\n\n"
                );
            } else {
                Anticheat.INSTANCE.getPacketLogList().add(
                        "[" + TimeUtils.getSystemTime2() + "] " + "DigPacket ="
                                + " action: " + digging.getAction().name()
                                + " direction: " + digging.getBlockFace().name()
                                + " blockPosition: null"
                                + " sequence: " + digging.getSequence() + "\n\n\n"
                );
            }
        }
        
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {

            WrapperPlayClientPlayerBlockPlacement placement = new WrapperPlayClientPlayerBlockPlacement(event);
            
            if (placement.getBlockPosition() != null && placement.getCursorPosition() != null) {
                Anticheat.INSTANCE.getPacketLogList().add(
                        "[" + TimeUtils.getSystemTime2() + "] " + "PlacePacket ="
                                + " positionX: " + placement.getBlockPosition().getX()
                                + " positionY: " + placement.getBlockPosition().getY()
                                + " positionZ: " + placement.getBlockPosition().getZ()
                                + " vecX: " + placement.getCursorPosition().getX()
                                + " vecY: " + placement.getCursorPosition().getY()
                                + " vecZ: " + placement.getCursorPosition().getX() + "\n\n\n"
                );
            } else if (placement.getBlockPosition() != null) {

                Anticheat.INSTANCE.getPacketLogList().add(
                        "[" + TimeUtils.getSystemTime2() + "] " + "PlacePacket ="
                                + " positionX: " + placement.getBlockPosition().getX()
                                + " positionY: " + placement.getBlockPosition().getY()
                                + " positionZ: " + placement.getBlockPosition().getZ() 
                                + " cursorPosition: null" + "\n\n\n"
                );
                
            } else if (placement.getCursorPosition() != null) {
                Anticheat.INSTANCE.getPacketLogList().add(
                        "[" + TimeUtils.getSystemTime2() + "] " + "PlacePacket ="
                                + " blockPosition: null"
                                + " vecX: " + placement.getCursorPosition().getX()
                                + " vecY: " + placement.getCursorPosition().getY()
                                + " vecZ: " + placement.getCursorPosition().getX() + "\n\n\n"
                );
            } else {
                Anticheat.INSTANCE.getPacketLogList().add(
                        "[" + TimeUtils.getSystemTime2() + "] " + "PlacePacket ="
                                + " blockPosition: null"
                                + " cursorPosition: null" + "\n\n\n"
                );
            }
        }
        
        if (event.getPacketType() == PacketType.Play.Client.CLIENT_SETTINGS) {
            Anticheat.INSTANCE.getPacketLogList().add(
                    "[" + TimeUtils.getSystemTime2() + "] " + "SettingsPacket\n");
        }
        
        if (event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
            WrapperPlayClientPluginMessage playClientPluginMessage = new WrapperPlayClientPluginMessage(event);

            String brand = new String(playClientPluginMessage.getData());
            
            Anticheat.INSTANCE.getPacketLogList().add(
                    "[" + TimeUtils.getSystemTime2() + "] " + "CustomPayload = " +
                            "| Channel: " + playClientPluginMessage.getChannelName() + ", clientBrand: " + brand + "\n"
            );
        }
        
        if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {
            WrapperPlayClientSteerVehicle steerVehicle = new WrapperPlayClientSteerVehicle(event);

            Anticheat.INSTANCE.getPacketLogList().add(
                    "[" + TimeUtils.getSystemTime2() + "] " + "SteerPacket ="
                            + " forward: " + steerVehicle.getForward()
                            + " strafe: " + steerVehicle.getSideways()
                            + " unmounting: " + steerVehicle.isUnmount()
                            + " jumping: " + steerVehicle.isJump()
                            + "\n\n\n"
            );
        }
        
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ABILITIES) {
            WrapperPlayClientPlayerAbilities abilities = new WrapperPlayClientPlayerAbilities(event);

            Anticheat.INSTANCE.getPacketLogList().add(
                    "[" + TimeUtils.getSystemTime2() + "] " + "AbilitiesPacket =" 
                            + "isFlying:" + abilities.isFlying()
                            + "\n\n\n"
            );
        }
        
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interactEntity = new WrapperPlayClientInteractEntity(event);

            if (interactEntity.getTarget().isPresent()) {
                Anticheat.INSTANCE.getPacketLogList().add(
                        "[" + TimeUtils.getSystemTime2() + "] " + "ActionPacket ="
                                + " action: " + interactEntity.getAction().name() 
                                + " entityId: " + interactEntity.getEntityId()
                                + " vecX: " + interactEntity.getTarget().get().x
                                + " vecY: " + interactEntity.getTarget().get().y
                                + " vecZ: " + interactEntity.getTarget().get().z
                                + "\n\n\n"
                );
            } else {
                Anticheat.INSTANCE.getPacketLogList().add(
                        "[" + TimeUtils.getSystemTime2() + "] " + "ActionPacket ="
                                + " entityId: " + interactEntity.getEntityId()
                                + " action: " + interactEntity.getAction().name() + "\n\n\n"
                );
            }
        }
        
        if (packetEvent.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);

            PlayerData user = getData();

            Anticheat.INSTANCE.getPacketLogList().add(
                    "[" + TimeUtils.getSystemTime2() + "] " + "FlyingPacket = " +
                            "isPos: " + flying.hasPositionChanged()
                            + " isLook: " + flying.hasRotationChanged()
                            + " onGround: " + flying.isOnGround()
                            + " posX: " + flying.getLocation().getX()
                            + " posY: " + flying.getLocation().getY()
                            + " posZ: " + flying.getLocation().getZ()
                            + " yaw: " + flying.getLocation().getYaw()
                            + " yawDelta: " + user.getMovementProcessor().getDeltaYaw()
                            + " lastYawDelta: " + user.getMovementProcessor().getLastDeltaYaw()
                            + " yawDeltaClamped: " + user.getMovementProcessor().getYawDeltaClamped()
                            + " pitch: " + flying.getLocation().getPitch()
                            + " pitchDelta: " + user.getMovementProcessor().getDeltaPitch()
                            + " lastPitchDelta: " + user.getMovementProcessor().getLastDeltaPitch()
                            + " skippedFlyings: " + (getData().getMovementProcessor().getSkippedPackets())
                            + " tick: " + user.getMovementProcessor().getTick()
                            + " deltaY: " + user.getMovementProcessor().getDeltaY()
                            + " lastDeltaY: " + user.getMovementProcessor().getLastDeltaY()
                            + " lastLastDeltaY: " + user.getMovementProcessor().getLastLastDeltaY()
                            + " deltaXZ: " + user.getMovementProcessor().getDeltaXZ()
                            + " lastDeltaXZ: " + user.getMovementProcessor().getLastDeltaXZ()
               //             + " serverVelocityMoveSpeed: " + user.getVelocityProcessor().getVelocityH()
                            + " clientVersion: " + user.getProtocolVersion() + " : "
                            + Anticheat.INSTANCE.getVersionSupport().getClientVersion(user)
                            + " clientBrand: " + user.getCustomPayloadProcessor().getBrand()
                            + " brand-channel: " + user.getCustomPayloadProcessor().getChannel()
                            + " lastTeleport: " + user.getActionProcessor().getTeleportTicks()
                            + " isTeleporting: " + user.getActionProcessor().isTeleporting()
                            + " bukkit-Teleport: " + user.getLastTeleport().getDelta()
                            + " server-velocity: " + user.getVelocityProcessor().getServerVelocityTicks()
                            + " confirm-velocity: " + user.getVelocityProcessor().getVelocityTicksConfirmed()
                            + " chunkLoaded: " + user.getCollisionProcessor().isChunkLoaded()
                            + " transactionPing: " + user.getTransactionProcessor().getTransactionPing()
                            + " transactionPingPost: " + user.getTransactionProcessor().getPostTransactionPing()
                       //     + " skipped: " + user.getTransactionProcessor().getSkipped()
                      //      + " average-Ping: " + user.getTransactionProcessor().getAveragePing()
                            + " mounted: " + (user.getActionProcessor().getLastVehicleTimer().getDelta() < 20)
                            + " mountedTicks: " + user.getActionProcessor().getLastVehicleTimer().getDelta()
                            + " validReturn: " + user.generalCancel()
                            + " allowFlight: " + user.getActionProcessor().isAllowFlight()
                            + " dead: " + getData().getActionProcessor().isDead()
                            + " current-health: " + user.getPlayer().getHealth()
                            + " server-lag-tick: " + Anticheat.INSTANCE.getLastServerLagTick()
                            + " current-game-mode: " + user.getActionProcessor().getGamemode()
                            + " chunkLoaded: " + user.getCollisionProcessor().isChunkLoaded()
                            + " isExplode: " + (user.getLastExplosionTimer().getDelta() < 20)
                            + " lastValidBlockPlace: " + user.getBlockProcessor().getLastConfirmedBlockPlaceTimer()
                            .getDelta()
                            + " isDead: " + getData().getActionProcessor().isDead()
                            + " walkSpeed: " + this.getData().getActionProcessor().getWalkSpeed()
                            + " worldSwitchTicks: " + user.getLastWorldChange().getDelta()
                            + " boatTicks: " + user.getCollisionProcessor().getNearBoatTicks()
                            + " nearBoat: " + (user.getCollisionProcessor().getNearBoatTicks() > 0)
                            + " speedPotionEnded: " + ((user.getPotionProcessor().getSpeedPotionTicks() > 0
                            && !user.getPotionProcessor().isSpeedPotion()))
                            + " hasSpeedPotion: " + user.getPotionProcessor().isSpeedPotion()
                            + " speedPotionTicks: " + user.getPotionProcessor().getSpeedPotionTicks()
                            + " speedPotionEffectLevel: " + user.getPotionProcessor().getSpeedPotionAmplifier()
                            + " desyncFix: " + user.getDesyncProcessor().isInvalid()
                            + " teleportQueue: " + user.getActionProcessor().getTeleportDataList().size()
                            + " desync-kys: " + user.getDesyncProcessor().getKys()
                            + " desync-fix: " + user.getDesyncProcessor().getFix()
                            + " desync-ticks: " + user.getDesyncProcessor().getTicksSinceDetect()
                            + "\n\n\n"
            );
        }
    }
}
