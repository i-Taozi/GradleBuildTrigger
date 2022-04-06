package binnie.extrabees.items.types;

import binnie.core.util.I18N;
import binnie.extrabees.modules.ModuleCore;
import binnie.extrabees.utils.Utils;
import forestry.api.recipes.RecipeManagers;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public enum EnumPropolis implements IEBEnumItem {
	WATER(2405321, 12762791, "Water"),
	OIL(1519411, 12762791, "oil"),
	FUEL(10718482, 12762791, "fuel"),
	MILK,
	FRUIT,
	SEED,
	ALCOHOL,
	CREOSOTE(8877313, 12428819, "creosote"),
	GLACIAL,
	PEAT;

	private final int primaryColor;
	private final int secondaryColor;
	private final String liquidName;
	private boolean active;

	EnumPropolis() {
		this(16777215, 16777215, "");
		this.active = false;
	}

	EnumPropolis(int primaryColor, int secondaryColor, String liquidName) {
		this.active = true;
		this.primaryColor = primaryColor;
		this.secondaryColor = secondaryColor;
		this.liquidName = liquidName;
	}

	public static EnumPropolis get(final ItemStack itemStack) {
		final int i = itemStack.getItemDamage();
		if (i >= 0 && i < values().length) {
			return values()[i];
		}
		return values()[0];
	}

	public void addRecipe() {
		final FluidStack liquid = Utils.getFluidFromName(this.liquidName, 500);
		if (liquid != null) {
			RecipeManagers.squeezerManager.addRecipe(20, this.get(1), liquid, ItemStack.EMPTY, 0);
		}
	}

	public int getSpriteColour(int renderPass) {
		if (renderPass == 0) {
			return primaryColor;
		}
		if (renderPass == 1) {
			return secondaryColor;
		}
		return 0xffffff;
	}

	@Override
	public boolean isActive() {
		return this.active && Utils.getFluidFromName(this.liquidName, 100) != null;
	}

	@Override
	public ItemStack get(final int amount) {
		return new ItemStack(ModuleCore.propolis, amount, this.ordinal());
	}

	@Override
	public String getName(final ItemStack itemStack) {
		return I18N.localise("extrabees.item.propolis." + this.name().toLowerCase());
	}

}
