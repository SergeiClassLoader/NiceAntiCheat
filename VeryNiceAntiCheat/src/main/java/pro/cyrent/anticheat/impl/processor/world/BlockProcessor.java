package pro.cyrent.anticheat.impl.processor.world;

import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import pro.cyrent.anticheat.Anticheat;
import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.bukkit.RunUtils;
import pro.cyrent.anticheat.util.event.EventTimer;
import pro.cyrent.anticheat.util.evicting.EvictingList;
import pro.cyrent.anticheat.util.location.FlyingLocation;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.Deque;

@Getter
@Setter
public class BlockProcessor extends Event {
    private final PlayerData data;
    private final EventTimer lastConfirmedBlockPlaceTimer;
    private final EventTimer lastConfirmedScaffoldPlaceTimer;
    private final EventTimer lastConfirmedCancelPlaceTimer;
    private final EventTimer packetDigTimer;
    private final EventTimer lastSwordBlockTimer;
    private final EventTimer lastPlacementPacket;
    private Material materialPlaced;

    private int blockUpdateTicks;

    private boolean legacyHasBeenUsingItem, usingBow, usingSword, hasPlacedBlock = false, recentC2SPacket = false;

    private int lastDig;

    private Vector currentBlockCords;

    private Material blockPlaceMaterial;
    private Material main;

    private Material lastBlockChangeMaterial, lastBlockChangeMultiMaterial;

    private int placeTicks;

    private int lastScaffoldTick;

    private int lastWebUpdateTick;

    private int fixTicks;

    private int face;

    private final Deque<Vector> combatWalls = new EvictingList<>(40);

    private int lastCombatWallTicks;

    private double distanceFromUpdate, distanceFromUpdateMulti;

    public BlockProcessor(PlayerData user) {
        this.data = user;

        this.lastConfirmedBlockPlaceTimer = new EventTimer(20, user);
        this.lastConfirmedCancelPlaceTimer = new EventTimer(20, user);
        this.packetDigTimer = new EventTimer(20, user);
        this.lastSwordBlockTimer = new EventTimer(20, user);
        this.lastConfirmedScaffoldPlaceTimer = new EventTimer(20, user);
        this.lastPlacementPacket = new EventTimer(20, user);
    }

    @Override
    public void onPacket(PacketEvent event) {

        if (event.isMovement()) {

            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event.getPacketReceiveEvent());

            this.blockUpdateTicks++;
            this.lastWebUpdateTick++;

            this.recentC2SPacket = false;

            if (flying.hasRotationChanged() && !flying.hasPositionChanged()) {
                this.recentC2SPacket = true;
            }


            if (flying.hasPositionChanged() && getData().getMovementProcessor().getTo() != null) {

                this.lastCombatWallTicks++;

                if (!this.combatWalls.isEmpty()) {

                    Vector vector = new Vector(getData().getMovementProcessor().getTo().getPosX(),
                            getData().getMovementProcessor().getTo().getPosY(),
                            getData().getMovementProcessor().getTo().getPosZ());

                    boolean combat = this.combatWalls.stream()
                            .anyMatch(f -> f.distance(vector) < 3.0);

                    if (combat) {
                        this.lastCombatWallTicks = 0;
                    }
                }
            }

            if (this.currentBlockCords != null
                    && this.blockPlaceMaterial != null
                    && getData().getPlayer().getWorld() != null) {

                RunUtils.task(() -> {

                    if (this.currentBlockCords == null) return;

                    Material material = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(
                            getData().getPlayer().getWorld(), this.currentBlockCords.getX(),
                            this.currentBlockCords.getY(), this.currentBlockCords.getZ());

                    if (material == null) {
                        return;
                    }

                    if (material.isBlock()) {

                        if (material != Material.AIR && material == this.blockPlaceMaterial) {
                            this.main = material;
                        }
                    }


                    this.blockPlaceMaterial = null;
                });
            }

            if (this.main != null) {
                this.lastConfirmedBlockPlaceTimer.reset();
                this.placeTicks = 0;
                this.main = null;

                if (this.currentBlockCords != null) {
                    this.currentBlockCords = null;
                }
            } else {
                this.lastConfirmedCancelPlaceTimer.reset();
            }
        }

        if (event.isPlace()) {
            WrapperPlayClientPlayerBlockPlacement wrapped = new WrapperPlayClientPlayerBlockPlacement(event.getPacketReceiveEvent());

            if (getData().getPlayer().getWorld() == null) return;

            if (wrapped.getBlockPosition() != null) {

                Material material = Anticheat.INSTANCE.getInstanceManager().getInstance().getType(
                        getData().getPlayer().getWorld(), wrapped.getBlockPosition().getX(),
                        wrapped.getBlockPosition().getY() + 1, wrapped.getBlockPosition().getZ());

                if (material.isBlock()) {

                    Material materialItemStack = null;

                    if (wrapped.getItemStack().isPresent()) {
                        String test = wrapped.getItemStack().get().getType().getName().toString();

                        //TODO: heavy method.
                        Material materialFound = Material.matchMaterial(test);

                        if (materialFound != null) {
                            materialItemStack = materialFound;
                        }
                    }

                    this.blockPlaceMaterial = materialItemStack != null ? materialItemStack
                            : getData().getPlayer().getItemInHand().getType() != null
                            ? getData().getPlayer().getItemInHand().getType() : null;

                    if (wrapped.getFace().getFaceValue() == 1) {
                        this.currentBlockCords = new Vector(wrapped.getBlockPosition().getX(),
                                wrapped.getBlockPosition().getY() + 1, wrapped.getBlockPosition().getZ());
                    } else if (wrapped.getFace().getFaceValue() == 4) {
                        this.currentBlockCords = new Vector(wrapped.getBlockPosition().getX() - 1,
                                wrapped.getBlockPosition().getY(), wrapped.getBlockPosition().getZ());
                    } else if (wrapped.getFace().getFaceValue() == 2) {
                        this.currentBlockCords = new Vector(wrapped.getBlockPosition().getX(),
                                wrapped.getBlockPosition().getY(), wrapped.getBlockPosition().getZ() - 1);
                    } else if (wrapped.getFace().getFaceValue() == 3) {
                        this.currentBlockCords = new Vector(wrapped.getBlockPosition().getX(),
                                wrapped.getBlockPosition().getY(), wrapped.getBlockPosition().getZ() + 1);
                    } else if (wrapped.getFace().getFaceValue() == 5) {
                        this.currentBlockCords = new Vector(wrapped.getBlockPosition().getX() + 1,
                                wrapped.getBlockPosition().getY(), wrapped.getBlockPosition().getZ());
                    } else if (wrapped.getFace().getFaceValue() == 0) {
                        this.currentBlockCords = new Vector(wrapped.getBlockPosition().getX(),
                                wrapped.getBlockPosition().getY() - 1, wrapped.getBlockPosition().getZ());
                    }

                    this.face = wrapped.getFace().getFaceValue();

                    this.materialPlaced = material;
                    this.placeTicks++;
                }

                if (this.recentC2SPacket) {
                    this.hasPlacedBlock = true;
                } else {
                    this.hasPlacedBlock = false;
                }
            }
        }

        if (event.getPacketSendEvent() != null) {
            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
                WrapperPlayServerMultiBlockChange playServerMultiBlockChange =
                        new WrapperPlayServerMultiBlockChange(event.getPacketSendEvent());

                FlyingLocation toCurrent = getData().getMovementProcessor().getTo();

                if (toCurrent == null) {
                    return;
                }

                if (playServerMultiBlockChange.getBlocks() != null) {

                    for (WrapperPlayServerMultiBlockChange.EncodedBlock blocksData
                            : playServerMultiBlockChange.getBlocks()) {

                        FlyingLocation flyingLocation = new FlyingLocation(getData().getPlayer().getWorld().getName(),
                                blocksData.getX(),
                                blocksData.getY(),
                                blocksData.getZ());


                        this.distanceFromUpdateMulti = flyingLocation.distanceSquaredXZ(toCurrent);

                        RunUtils.task(() -> {
                            Material blockMaterial = Anticheat.INSTANCE.getInstanceManager().getInstance()
                                    .getType(getData().getPlayer().getWorld(), blocksData.getX(),
                                            blocksData.getY(),
                                            blocksData.getZ());

                            if (distanceFromUpdateMulti < 3) {
                                if (blockMaterial == Material.WEB) {
                                    this.lastWebUpdateTick = 0;
                                }
                            }

                            Vector vector = new Vector(blocksData.getX(), blocksData
                                    .getY(), blocksData.getZ());

                            if (blockMaterial == Material.AIR) {
                                StateType types = blocksData.getBlockState(getData().getVersion()).getType();

                                if (!types.isAir() && types.getName().contains("glass")) {
                                    if (!this.combatWalls.contains(vector)) {
                                        this.combatWalls.add(vector);
                                    }
                                }
                            }
                        });
                    }
                }
            }

            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
                WrapperPlayServerBlockChange blockChange = new WrapperPlayServerBlockChange(event.getPacketSendEvent());


                FlyingLocation toCurrent = getData().getMovementProcessor().getTo();

                if (toCurrent == null) {
                    return;
                }

                if (blockChange.getBlockPosition() != null) {


                    FlyingLocation flyingLocation = new FlyingLocation(getData().getPlayer().getWorld().getName(),
                            blockChange.getBlockPosition().getX(),
                            blockChange.getBlockPosition().getY(),
                            blockChange.getBlockPosition().getZ());

                    this.distanceFromUpdate = flyingLocation.distanceSquaredXZ(toCurrent);


                    RunUtils.task(() -> {
                        Material blockMaterial = Anticheat.INSTANCE.getInstanceManager().getInstance()
                                .getType(getData().getPlayer().getWorld(), blockChange.getBlockPosition().getX(),
                                        blockChange.getBlockPosition().getY(),
                                        blockChange.getBlockPosition().getZ());


                        if (distanceFromUpdate < 3) {
                            if (blockMaterial == Material.WEB) {
                                this.lastWebUpdateTick = 0;
                            }
                        }

                        Vector vector = new Vector(blockChange.getBlockPosition().getX(), blockChange.getBlockPosition()
                                .getY(), blockChange.getBlockPosition().getZ());

                        if (blockMaterial == Material.AIR) {

                            StateType types = blockChange.getBlockState().getType();

                            if (!types.isAir() && types.getName().contains("glass")) {
                                if (!this.combatWalls.contains(vector)) {
                                    this.combatWalls.add(vector);
                                }
                            }
                        }
                    });
                }
            }
        }
    }
}