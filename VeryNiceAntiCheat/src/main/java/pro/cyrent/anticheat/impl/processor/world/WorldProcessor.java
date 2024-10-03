package pro.cyrent.anticheat.impl.processor.world;

import pro.cyrent.anticheat.api.event.Event;
import pro.cyrent.anticheat.api.user.PlayerData;
import pro.cyrent.anticheat.impl.events.PacketEvent;
import pro.cyrent.anticheat.util.math.MathHelper;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v1_16.Chunk_v1_9;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v1_8.Chunk_v1_8;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v_1_18.Chunk_v1_18;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.DataPalette;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Getter
public class WorldProcessor extends Event {
    private final PlayerData data;

    private final LinkedHashMap<Long, BaseChunk[]> chunks = new LinkedHashMap<>(100
            + 1, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, BaseChunk[]> eldest) {
            return size() > 100;
        }
    };

    public WorldProcessor(PlayerData user) {
        this.data = user;
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacketSendEvent() != null) {

            if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.CHUNK_DATA) {
                WrapperPlayServerChunkData mapChunk = new WrapperPlayServerChunkData(event.getPacketSendEvent());

                getData().getTransactionProcessor().confirmPre(() -> {
                    long xz = toLong(mapChunk.getColumn().getX(), mapChunk.getColumn().getZ());

                    if (mapChunk.getColumn().isFullChunk()) {
                        chunks.put(xz, mapChunk.getColumn().getChunks());
                    } else {
                        if (chunks.containsKey(xz)) {
                            BaseChunk[] currentChunks = chunks.get(xz);

                            for (int i = 0; i < 15; i++) {
                                BaseChunk chunk = mapChunk.getColumn().getChunks()[i];

                                if (chunk != null) {
                                    currentChunks[i] = chunk;
                                }
                            }

                            chunks.put(xz, currentChunks);
                        }
                    }
                });
            } else if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.MAP_CHUNK_BULK) {
                WrapperPlayServerChunkDataBulk mapChunkBulk = new WrapperPlayServerChunkDataBulk(event.getPacketSendEvent());

                getData().getTransactionProcessor().confirmPre(() -> {
                    for (int i = 0; i < mapChunkBulk.getX().length; i++) {
                        int x = mapChunkBulk.getX()[i];
                        int z = mapChunkBulk.getZ()[i];

                        long xz = toLong(x, z);

                        chunks.put(xz, mapChunkBulk.getChunks()[i]);
                    }
                });
            } else if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.UNLOAD_CHUNK) {
                WrapperPlayServerUnloadChunk unloadChunk = new WrapperPlayServerUnloadChunk(event.getPacketSendEvent());

                long xz = toLong(unloadChunk.getChunkX(), unloadChunk.getChunkZ());

                event.getData().getTransactionProcessor().confirmPost(() -> chunks.remove(xz));

            } else if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
                WrapperPlayServerBlockChange blockChange = new WrapperPlayServerBlockChange(event.getPacketSendEvent());

                int x = blockChange.getBlockPosition().getX();
                int y = blockChange.getBlockPosition().getY();
                int z = blockChange.getBlockPosition().getZ();

                long xz = toLong(x >> 4, z >> 4);

                getData().getTransactionProcessor().confirmPre(() -> {
                    if (!chunks.containsKey(xz)) {
                        chunks.put(xz, new BaseChunk[16]);
                    }

                    int length = chunks.get(xz).length;

                    if (length <= (y >> 4)) {
                        return;
                    }

                    BaseChunk chunk = chunks.get(xz)[y >> 4];

                    if (chunk == null) {
                        chunk = create();
                        chunk.set(0, 0, 0 ,0);
                        chunks.get(xz)[y >> 4] = chunk;
                    }

                    chunks.get(xz)[y >> 4].set(x & 0xF, y & 0xF, z & 0xF, blockChange.getBlockId());
                });
            } else if (event.getPacketSendEvent().getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
                WrapperPlayServerMultiBlockChange multiBlockChange = new WrapperPlayServerMultiBlockChange(event
                        .getPacketSendEvent());

                getData().getTransactionProcessor().confirmPre(() -> {
                    for (WrapperPlayServerMultiBlockChange.EncodedBlock block : multiBlockChange.getBlocks()) {
                        int x = block.getX();
                        int y = block.getY();
                        int z = block.getZ();

                        long xz = toLong(x >> 4, z >> 4);

                        if (!chunks.containsKey(xz)) {
                            chunks.put(xz, new BaseChunk[16]);
                        }

                        int length = chunks.get(xz).length;

                        if (length <= (y >> 4)) {
                            return;
                        }

                        BaseChunk chunk = chunks.get(xz)[y >> 4];

                        if (chunk == null) {
                            chunk = create();
                            chunk.set(0, 0, 0 ,0);
                            chunks.get(xz)[y >> 4] = chunk;
                        }

                        chunks.get(xz)[y >> 4].set(x & 0xF, y & 0xF, z & 0xF, block.getBlockId());
                    }
                });
            }
        }

        if (event.getPacketReceiveEvent() != null) {

            if (event.getPacketReceiveEvent().getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
                WrapperPlayClientPlayerBlockPlacement blockPlace = new WrapperPlayClientPlayerBlockPlacement(event.
                        getPacketReceiveEvent());

                Vector3i pos = shift(blockPlace.getBlockPosition(), blockPlace.getFace());

                long xz = toLong(pos.getX() >> 4, pos.getZ() >> 4);

                boolean block = blockPlace.getItemStack().isPresent() && blockPlace.getItemStack().get().getType().getPlacedType() != null;

                if (block && chunks.containsKey(xz)) {
                    int length = chunks.get(xz).length;

                    if (length <= (pos.getY() >> 4)) {
                        return;
                    }

                    BaseChunk chunk = chunks.get(xz)[pos.getY() >> 4];

                    if (chunk == null) {
                        chunk = create();
                        chunk.set(0, 0, 0, 0);
                        chunks.get(xz)[pos.getY() >> 4] = chunk;
                    }

                    chunks.get(xz)[pos.getY() >> 4].set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15,
                            Objects.requireNonNull(blockPlace.getItemStack().get().getType().getPlacedType())
                                    .createBlockState().getGlobalId());
                }
            }
        }
    }

    public WrappedBlockState getBlock(double x, double y, double z) {
        return getBlock(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
    }

    public long toLong(int x, int z) {
        return ((x & 0xFFFFFFFFL) << 32L) | (z & 0xFFFFFFFFL);
    }

    public WrappedBlockState getBlock(int x, int y, int z) {
        long xz = toLong(x >> 4, z >> 4);

        if (chunks.containsKey(xz)) {
            BaseChunk[] baseChunks = chunks.get(xz);

            if (y < 0 || (y >> 4) > (baseChunks.length - 1) || baseChunks[y >> 4] == null) {
                return WrappedBlockState.getByGlobalId(0);
            }

            return baseChunks[y >> 4].get(x & 0xF, y & 0xF, z & 0xF);
        }

        return WrappedBlockState.getByGlobalId(0);
    }

    public boolean isChunkLoaded(double x, double z) {
        long xz = toLong((int) x >> 4, (int) z >> 4);

        if (chunks.containsKey(xz)) {
            return chunks.get(xz) != null;
        } else {
            return false;
        }
    }

    public BaseChunk getChunk(double x, double y, double z) {
        long xz = toLong((int) x >> 4, (int) z >> 4);

        if (chunks.containsKey(xz)) {
            int length = chunks.get(xz).length;

            if ((int) y >> 4 >= length) {
                return null;
            }

            return chunks.get(xz)[(int) y >> 4];
        } else {
            return null;
        }
    }

    // Taken from grim cause im lazy tbh
    private static BaseChunk create() {
        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_18)) {
            return new Chunk_v1_18();
        } else if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_16)) {
            return new Chunk_v1_9(0, DataPalette.createForChunk());
        }

        return new Chunk_v1_8(false);
    }

    public Vector3i shift(Vector3i pos, BlockFace facing) {
        switch (facing) {
            case UP -> {
                return new Vector3i(pos.getX(), pos.getY() + 1, pos.getZ());
            }
            case DOWN -> {
                return new Vector3i(pos.getX(), pos.getY() - 1, pos.getZ());
            }
            case NORTH -> {
                return new Vector3i(pos.getX(), pos.getY(), pos.getZ() - 1);
            }
            case SOUTH -> {
                return new Vector3i(pos.getX(), pos.getY(), pos.getZ() + 1);
            }
            case WEST -> {
                return new Vector3i(pos.getX() - 1, pos.getY(), pos.getZ());
            }
            case EAST -> {
                return new Vector3i(pos.getX() + 1, pos.getY(), pos.getZ());
            }
            default -> {
                // TODO: fix this shit
                return pos;
            }
        }
    }
}