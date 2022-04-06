package binnie.extrabees.items;

import binnie.extrabees.items.types.IEBEnumItem;
import forestry.api.core.IItemModelRegister;
import forestry.api.core.IModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemProduct<E extends IEBEnumItem> extends Item implements IItemModelRegister {

	protected final E[] types;

	public ItemProduct(E[] types) {
		this.setMaxStackSize(64);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.types = types;
	}

	public E get(ItemStack stack) {
		int damage = stack.getItemDamage();
		if (damage >= 0 && damage < this.types.length) {
			return this.types[damage];
		}
		return this.types[0];
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		E item = get(itemstack);
		return item.getName(itemstack);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			for (E type : this.types) {
				if (type.isActive()) {
					items.add(new ItemStack(this, 1, type.ordinal()));
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("all")
	public void registerModel(Item item, IModelManager manager) {
		for (E type : types) {
			ModelLoader.setCustomModelResourceLocation(item, type.ordinal(), new ModelResourceLocation(getRegistryName(), "inventory"));
		}
	}
}
