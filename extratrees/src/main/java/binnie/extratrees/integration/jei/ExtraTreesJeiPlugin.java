package binnie.extratrees.integration.jei;

import binnie.core.Constants;
import binnie.core.integration.jei.Drawables;
import binnie.core.modules.ExtraTreesModuleUIDs;
import binnie.extratrees.blocks.decor.FenceDescription;
import binnie.extratrees.blocks.decor.FenceType;
import binnie.extratrees.integration.jei.brewery.BreweryRecipeCategory;
import binnie.extratrees.integration.jei.brewery.BreweryRecipeMaker;
import binnie.extratrees.integration.jei.distillery.DistilleryRecipeCategory;
import binnie.extratrees.integration.jei.distillery.DistilleryRecipeMaker;
import binnie.extratrees.integration.jei.fruitpress.FruitPressRecipeCategory;
import binnie.extratrees.integration.jei.fruitpress.FruitPressRecipeMaker;
import binnie.extratrees.integration.jei.lumbermill.LumbermillRecipeCategory;
import binnie.extratrees.integration.jei.lumbermill.LumbermillRecipeMaker;
import binnie.extratrees.integration.jei.multifence.MultiFenceRecipeRegistryPlugin;
import binnie.extratrees.machines.ExtraTreeMachine;
import binnie.extratrees.modules.ModuleAlcohol;
import binnie.extratrees.modules.ModuleWood;
import binnie.extratrees.wood.WoodManager;
import forestry.api.core.ForestryAPI;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

@JEIPlugin
public class ExtraTreesJeiPlugin implements IModPlugin {
	public static IJeiHelpers jeiHelpers;
	public static IGuiHelper guiHelper;
	public static Drawables drawables;

	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
		if (ForestryAPI.moduleManager.isModuleEnabled(Constants.EXTRA_TREES_MOD_ID, ExtraTreesModuleUIDs.WOOD)) {
			subtypeRegistry.registerSubtypeInterpreter(Item.getItemFromBlock(ModuleWood.blockMultiFence), (ItemStack itemStack) -> {
				FenceDescription desc = WoodManager.getFenceDescription(itemStack);
				if (desc != null) {
					FenceType type = WoodManager.getFenceType(itemStack);
					return type + ":" + desc.getPlankType().getDesignMaterialName().toLowerCase() + ':' + desc.getSecondaryPlankType().getDesignMaterialName().toLowerCase();
				}
				return Integer.toString(itemStack.getItemDamage());
			});
		}
		if (ForestryAPI.moduleManager.isModuleEnabled(Constants.EXTRA_TREES_MOD_ID, ExtraTreesModuleUIDs.ALCOHOL)) {
			subtypeRegistry.registerSubtypeInterpreter(ModuleAlcohol.drink, (ItemStack itemStack) -> {
				String glassware = ModuleAlcohol.drink.getGlassware(itemStack).getName();
				FluidStack fluidStack = FluidUtil.getFluidContained(itemStack);
				if (fluidStack == null) {
					return glassware;
				}
				return glassware + ':' + fluidStack.getFluid().getName();
			});
		}
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		ExtraTreesJeiPlugin.jeiHelpers = registry.getJeiHelpers();
		ExtraTreesJeiPlugin.guiHelper = jeiHelpers.getGuiHelper();
		ExtraTreesJeiPlugin.drawables = Drawables.getDrawables(guiHelper);

		if (ForestryAPI.moduleManager.isModuleEnabled(Constants.EXTRA_TREES_MOD_ID, ExtraTreesModuleUIDs.MACHINES)) {
			registry.addRecipeCategories(
				new LumbermillRecipeCategory(),
				new FruitPressRecipeCategory(),
				new BreweryRecipeCategory(),
				new DistilleryRecipeCategory()
			);
		}
	}

	@Override
	public void register(IModRegistry registry) {
		IJeiHelpers jeiHelpers = registry.getJeiHelpers();
		if (ForestryAPI.moduleManager.isModuleEnabled(Constants.EXTRA_TREES_MOD_ID, ExtraTreesModuleUIDs.MACHINES)) {
			registry.addRecipeCatalyst(ExtraTreeMachine.Lumbermill.get(1), RecipeUids.LUMBERMILL);
			registry.addRecipeCatalyst(ExtraTreeMachine.Press.get(1), RecipeUids.FRUIT_PRESS);
			registry.addRecipeCatalyst(ExtraTreeMachine.BREWERY.get(1), RecipeUids.BREWING);
			registry.addRecipeCatalyst(ExtraTreeMachine.Distillery.get(1), RecipeUids.DISTILLING);

			registry.addRecipes(LumbermillRecipeMaker.create(jeiHelpers), RecipeUids.LUMBERMILL);
			registry.addRecipes(FruitPressRecipeMaker.create(), RecipeUids.FRUIT_PRESS);
			registry.addRecipes(BreweryRecipeMaker.create(), RecipeUids.BREWING);
			registry.addRecipes(DistilleryRecipeMaker.create(), RecipeUids.DISTILLING);
		}

		if (ForestryAPI.moduleManager.isModuleEnabled(Constants.EXTRA_TREES_MOD_ID, ExtraTreesModuleUIDs.WOOD)) {
			registry.addRecipeRegistryPlugin(new MultiFenceRecipeRegistryPlugin());
		}
	}
}
