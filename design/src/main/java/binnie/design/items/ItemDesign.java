package binnie.design.items;

import binnie.core.block.ItemMetadata;
import binnie.core.block.TileEntityMetadata;
import binnie.design.DesignHelper;
import binnie.design.blocks.BlockDesign;
import binnie.design.blocks.DesignBlock;
import forestry.core.items.IColoredItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDesign extends ItemMetadata implements IColoredItem {

	public ItemDesign(BlockDesign block) {
		super(block);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getColorFromItemstack(ItemStack stack, int tintIndex) {
		DesignBlock block = DesignHelper.getDesignBlock(((BlockDesign) this.block).getDesignSystem(), TileEntityMetadata.getItemDamage(stack));
		if (tintIndex > 0) {
			return block.getSecondaryColour();
		}
		return block.getPrimaryColour();
	}
}
