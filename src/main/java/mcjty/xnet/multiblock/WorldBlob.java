package mcjty.xnet.multiblock;

import mcjty.xnet.api.keys.ConsumerId;
import mcjty.xnet.api.keys.NetworkId;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class WorldBlob {

    private final int dimId;
    private final Map<Long, ChunkBlob> chunkBlobMap = new HashMap<>();
    private int lastNetworkId = 0;              // Network ID
    private int lastConsumerId = 0;             // Network consumer ID

    // All consumers (as position) for a given network. If an entry in this map does not
    // exist for a certain network that means the information has to be calculated
    private final Map<NetworkId, Set<BlockPos>> consumersOnNetwork = new HashMap<>();


    public WorldBlob(int dimId) {
        this.dimId = dimId;
    }

    public int getDimId() {
        return dimId;
    }

    @Nonnull
    public NetworkId newNetwork() {
        lastNetworkId++;
        return new NetworkId(lastNetworkId);
    }

    @Nonnull
    public ConsumerId newConsumer() {
        lastConsumerId++;
        return new ConsumerId(lastConsumerId);
    }

    @Nullable
    public ConsumerId getConsumerAt(BlockPos pos) {
        ChunkBlob blob = getBlob(pos);
        if (blob == null) {
            return null;
        }
        IntPos intPos = new IntPos(pos);
        return blob.getNetworkConsumers().get(intPos);
    }

    @Nonnull
    public Set<BlockPos> getConsumers(NetworkId network) {
        if (!consumersOnNetwork.containsKey(network)) {
            Set<BlockPos> positions = new HashSet<>();

            // @todo can this be done more optimal instead of traversing all the chunk blobs?
            for (ChunkBlob blob : chunkBlobMap.values()) {
                Set<IntPos> consumersForNetwork = blob.getConsumersForNetwork(network);
                for (IntPos intPos : consumersForNetwork) {
                    BlockPos pos = blob.getPosition(intPos);
                    positions.add(pos);
                }
            }
            consumersOnNetwork.put(network, positions);
        }
        return consumersOnNetwork.get(network);
    }

    private void removeCachedNetworksForBlob(ChunkBlob blob) {
        for (NetworkId id : blob.getNetworks()) {
            consumersOnNetwork.remove(id);
        }
    }

    /**
     * Create a cable segment that is also a network provider at this section
     */
    public void createNetworkProvider(BlockPos pos, ColorId color, NetworkId network) {
        ChunkBlob blob = getOrCreateBlob(pos);
        if (blob.createNetworkProvider(pos, color, network)) {
            recalculateNetwork(blob);
        } else {
            removeCachedNetworksForBlob(blob);
        }
    }

    /**
     * Create a cable segment that is also a network consumer at this section
     */
    public void createNetworkConsumer(BlockPos pos, ColorId color, ConsumerId consumer) {
        ChunkBlob blob = getOrCreateBlob(pos);
        if (blob.createNetworkConsumer(pos, color, consumer)) {
            recalculateNetwork(blob);
        } else {
            removeCachedNetworksForBlob(blob);
        }
    }

    /**
     * Create a cable segment at a position
     */
    public void createCableSegment(BlockPos pos, ColorId color) {
        ChunkBlob blob = getOrCreateBlob(pos);
        if (blob.createCableSegment(pos, color)) {
            recalculateNetwork(blob);
        } else {
            removeCachedNetworksForBlob(blob);
        }
    }

    @Nonnull
    private ChunkBlob getOrCreateBlob(BlockPos pos) {
        ChunkPos cpos = new ChunkPos(pos);
        long chunkId = ChunkPos.asLong(cpos.chunkXPos, cpos.chunkZPos);
        if (!chunkBlobMap.containsKey(chunkId)) {
            chunkBlobMap.put(chunkId, new ChunkBlob(cpos));
        }
        return chunkBlobMap.get(chunkId);
    }

    @Nullable
    private ChunkBlob getBlob(BlockPos pos) {
        ChunkPos cpos = new ChunkPos(pos);
        long chunkId = ChunkPos.asLong(cpos.chunkXPos, cpos.chunkZPos);
        return chunkBlobMap.get(chunkId);
    }

    public void removeCableSegment(BlockPos pos) {
        ChunkBlob blob = getOrCreateBlob(pos);
        if (blob.removeCableSegment(pos)) {
            recalculateNetwork();
        } else {
            blob.fixNetworkAllocations();
        }
    }

    private void fixNetworkAllocations() {
        // First make sure that every chunk has its network mappings correct (mapping
        // from blob id to network id)
        for (ChunkBlob blob : chunkBlobMap.values()) {
            blob.fixNetworkAllocations();
            removeCachedNetworksForBlob(blob);
        }
    }

    /**
     * Recalculate the network starting from the given block
     */
    public void recalculateNetwork(ChunkBlob blob) {
        fixNetworkAllocations();

        Set<ChunkBlob> todo = new HashSet<>();
        todo.add(blob);
        recalculateNetwork(todo);
    }

    /**
     * Recalculate the entire network
     */
    public void recalculateNetwork() {
        fixNetworkAllocations();

        // For every chunk we check all border positions and see where they connect with
        // adjacent chunks
        Set<ChunkBlob> todo = new HashSet<>(chunkBlobMap.values());
        recalculateNetwork(todo);
    }

    private void recalculateNetwork(Set<ChunkBlob> todo) {
        while (!todo.isEmpty()) {
            ChunkBlob blob = todo.iterator().next();
            todo.remove(blob);
            removeCachedNetworksForBlob(blob);

            Set<IntPos> borderPositions = blob.getBorderPositions();
            ChunkPos chunkPos = blob.getChunkPos();
            for (IntPos pos : borderPositions) {
                Set<NetworkId> networks = blob.getOrCreateNetworksForPosition(pos);
                for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                    if (pos.isBorder(facing)) {
                        Vec3i vec = facing.getDirectionVec();
                        ChunkBlob adjacent = chunkBlobMap.get(
                                ChunkPos.asLong(chunkPos.chunkXPos+vec.getX(), chunkPos.chunkZPos+vec.getZ()));
                        if (adjacent != null) {
                            IntPos connectedPos = pos.otherSide(facing);
                            if (adjacent.getBorderPositions().contains(connectedPos)) {
                                // We have a connection!
                                Set<NetworkId> adjacentNetworks = adjacent.getOrCreateNetworksForPosition(connectedPos);
                                if (networks.addAll(adjacentNetworks)) {
                                    todo.add(blob);     // We changed this blob so need to push back on todo
                                }
                                if (adjacentNetworks.addAll(networks)) {
                                    todo.add(adjacent);
                                }
                            }
                        }
                    }
                }
            }

        }
    }


    public void dump() {
        for (ChunkBlob blob : chunkBlobMap.values()) {
            blob.dump();
        }
    }


    public void readFromNBT(NBTTagCompound compound) {
        chunkBlobMap.clear();
        lastNetworkId = compound.getInteger("lastNetwork");
        lastConsumerId = compound.getInteger("lastConsumer");
        if (compound.hasKey("chunks")) {
            NBTTagList chunks = (NBTTagList) compound.getTag("chunks");
            for (int i = 0 ; i < chunks.tagCount() ; i++) {
                NBTTagCompound tc = (NBTTagCompound) chunks.get(i);
                int chunkX = tc.getInteger("chunkX");
                int chunkZ = tc.getInteger("chunkZ");
                ChunkBlob blob = new ChunkBlob(new ChunkPos(chunkX, chunkZ));
                blob.readFromNBT(tc);
                chunkBlobMap.put(blob.getChunkNum(), blob);
            }
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("lastNetwork", lastNetworkId);
        compound.setInteger("lastConsumer", lastConsumerId);
        NBTTagList list = new NBTTagList();
        for (Map.Entry<Long, ChunkBlob> entry : chunkBlobMap.entrySet()) {
            ChunkBlob blob = entry.getValue();
            NBTTagCompound tc = new NBTTagCompound();
            tc.setInteger("chunkX", blob.getChunkPos().chunkXPos);
            tc.setInteger("chunkZ", blob.getChunkPos().chunkZPos);
            blob.writeToNBT(tc);
            list.appendTag(tc);
        }
        compound.setTag("chunks", list);

        return compound;
    }

}
