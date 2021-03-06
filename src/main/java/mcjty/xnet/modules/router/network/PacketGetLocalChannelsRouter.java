package mcjty.xnet.modules.router.network;

import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.xnet.client.ControllerChannelClientInfo;
import mcjty.xnet.modules.router.blocks.TileEntityRouter;
import mcjty.xnet.setup.XNetMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetLocalChannelsRouter {

    protected BlockPos pos;
    protected TypedMap params;

    public PacketGetLocalChannelsRouter() {
    }

    public PacketGetLocalChannelsRouter(PacketBuffer buf) {
        pos = buf.readBlockPos();
        params = TypedMapTools.readArguments(buf);
    }

    public PacketGetLocalChannelsRouter(BlockPos pos) {
        this.pos = pos;
        this.params = TypedMap.EMPTY;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        TypedMapTools.writeArguments(buf, params);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            World world = ctx.getSender().getEntityWorld();
            if (world.isBlockLoaded(pos)) {
                TileEntity te = world.getTileEntity(pos);
                ICommandHandler commandHandler = (ICommandHandler) te;
                List<ControllerChannelClientInfo> list = commandHandler.executeWithResultList(TileEntityRouter.CMD_GETCHANNELS, params, Type.create(ControllerChannelClientInfo.class));
                XNetMessages.INSTANCE.sendTo(new PacketLocalChannelsRouterReady(pos, TileEntityRouter.CLIENTCMD_CHANNELSREADY, list), ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            }
        });
        ctx.setPacketHandled(true);
    }
}
