package binnie.botany.farming;

import forestry.api.farming.ICrop;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;

public class FlowerCrop implements ICrop {
	private final NonNullList<ItemStack> drops = NonNullList.create();
	private final BlockPos position;

	public FlowerCrop(BlockPos pos, ItemStack... drops) {
		Collections.addAll(this.drops, drops);
		position = pos;
	}

	@Nullable
	@Override
	public NonNullList<ItemStack> harvest() {
		return drops;
	}

	@Override
	public BlockPos getPosition() {
		return position;
	}
}
