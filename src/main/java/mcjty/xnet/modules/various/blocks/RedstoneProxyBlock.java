package mcjty.xnet.modules.various.blocks;

import mcjty.lib.McJtyLib;
import mcjty.lib.tooltips.ITooltipSettings;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.List;

public class RedstoneProxyBlock extends Block implements ITooltipSettings {

    public RedstoneProxyBlock() {
        this(Material.IRON);
    }

    public RedstoneProxyBlock(Material materialIn) {
        super(Properties.create(materialIn)
                .hardnessAndResistance(2.0f)
                .harvestLevel(0)
                .harvestTool(ToolType.PICKAXE)
                .sound(SoundType.METAL)
        );
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (McJtyLib.proxy.isSneaking()) {
            tooltip.add(new TranslationTextComponent("message.xnet.redstone_proxy.header").mergeStyle(TextFormatting.GREEN));
            tooltip.add(new TranslationTextComponent("message.xnet.redstone_proxy.gold").mergeStyle(TextFormatting.GOLD));
        } else {
            tooltip.add(new TranslationTextComponent("message.xnet.shiftmessage"));
        }
    }

}
