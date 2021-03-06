package mcjty.xnet.modules.controller.client;

import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.xnet.modules.controller.blocks.TileEntityController;
import net.minecraft.client.Minecraft;

import java.util.Map;

import static mcjty.xnet.modules.controller.blocks.TileEntityController.PARAM_CHANNEL;

public class ChannelEditorPanel extends AbstractEditorPanel {

    private final int channel;

    @Override
    public boolean isAdvanced() {
        return false;
    }

    @Override
    protected void update(String tag, Object value) {
        data.put(tag, value);
        TypedMap.Builder builder = TypedMap.builder();
        int i = 0;
        builder.put(PARAM_CHANNEL, channel);
        performUpdate(builder, i, TileEntityController.CMD_UPDATECHANNEL);
    }

    public ChannelEditorPanel(Panel panel, Minecraft mc, GuiController gui, int channel) {
        super(panel, mc, gui);
        this.channel = channel;
    }

    public void setState(IChannelSettings settings) {
        for (Map.Entry<String, Widget<?>> entry : components.entrySet()) {
            entry.getValue().enabled(settings.isEnabled(entry.getKey()));
        }
    }
}
