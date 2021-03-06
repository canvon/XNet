package mcjty.xnet.datagen;

import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.CableModule;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeBlockTagsProvider;

public class ItemTags extends ItemTagsProvider {

    public ItemTags(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, new ForgeBlockTagsProvider(generator, helper));
    }

    @Override
    protected void registerTags() {
        getOrCreateBuilder(CableModule.TAG_CABLES)
                .add(CableModule.NETCABLE_BLUE.get(), CableModule.NETCABLE_YELLOW.get(), CableModule.NETCABLE_GREEN.get(), CableModule.NETCABLE_RED.get(), CableModule.NETCABLE_ROUTING.get());
        getOrCreateBuilder(CableModule.TAG_CABLES)
                .add(CableModule.NETCABLE_BLUE.get(), CableModule.NETCABLE_YELLOW.get(), CableModule.NETCABLE_GREEN.get(), CableModule.NETCABLE_RED.get(), CableModule.NETCABLE_ROUTING.get());
        getOrCreateBuilder(CableModule.TAG_CONNECTORS)
                .add(CableModule.CONNECTOR_BLUE.get(), CableModule.CONNECTOR_YELLOW.get(), CableModule.CONNECTOR_GREEN.get(), CableModule.CONNECTOR_RED.get(), CableModule.CONNECTOR_ROUTING.get());
        getOrCreateBuilder(CableModule.TAG_ADVANCED_CONNECTORS)
                .add(CableModule.ADVANCED_CONNECTOR_BLUE.get(), CableModule.ADVANCED_CONNECTOR_YELLOW.get(), CableModule.ADVANCED_CONNECTOR_GREEN.get(), CableModule.ADVANCED_CONNECTOR_RED.get(), CableModule.ADVANCED_CONNECTOR_ROUTING.get());
    }

    @Override
    public String getName() {
        return "XNet Tags";
    }
}
