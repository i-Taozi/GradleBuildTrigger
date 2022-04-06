package binnie.extratrees.integration.jei.multifence;

import binnie.extratrees.blocks.decor.FenceDescription;
import binnie.extratrees.blocks.decor.FenceType;
import binnie.extratrees.wood.WoodManager;
import binnie.extratrees.wood.planks.IPlankType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class MultiFenceRecipeEmbeddedWrapper implements IShapedCraftingRecipeWrapper {

	private final FenceType fenceType;
	@Nullable
	private IPlankType plankType;
	@Nullable
	private IPlankType plankTypeSecondary;

	public MultiFenceRecipeEmbeddedWrapper(FenceType fenceType) {
		this.fenceType = fenceType;
	}

	public MultiFenceRecipeEmbeddedWrapper(IPlankType plankType, FenceType fenceType) {
		this.plankType = plankType;
		this.fenceType = fenceType;
	}

	public MultiFenceRecipeEmbeddedWrapper(FenceDescription fenceDesc) {
		this.plankType = fenceDesc.getPlankType();
		this.plankTypeSecondary = fenceDesc.getSecondaryPlankType();
		this.fenceType = fenceDesc.getFenceType();
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		List<ItemStack> fences = new ArrayList<>();
		List<ItemStack> planks = new ArrayList<>();
		List<ItemStack> outputFences = new ArrayList<>();
		if (plankType != null) {
			if (plankTypeSecondary == null) {
				for (Entry<IPlankType, ItemStack> secondary : WoodManager.getAllPlankStacks().entrySet()) {
					IPlankType plankTypeSecondary = secondary.getKey();
					if (plankType != plankTypeSecondary) {
						planks.add(plankType.getStack(false));
						fences.add(WoodManager.getFence(plankType, plankTypeSecondary, new FenceType(fenceType.getSize(), fenceType.isSolid(), false), 1));
						outputFences.add(WoodManager.getFence(plankType, plankTypeSecondary, new FenceType(fenceType.getSize(), fenceType.isSolid(), true), 2));
					}
				}
			} else {
				planks.add(plankType.getStack(false));
				fences.add(WoodManager.getFence(plankType, plankTypeSecondary, new FenceType(fenceType.getSize(), fenceType.isSolid(), false), 1));
				outputFences.add(WoodManager.getFence(plankType, plankTypeSecondary, new FenceType(fenceType.getSize(), fenceType.isSolid(), true), 2));
			}
		} else {
			for (IPlankType plankTypeSecondary : WoodManager.getAllPlankTypes()) {
				for (IPlankType plankType : WoodManager.getAllPlankTypes()) {
					if (plankType != plankTypeSecondary) {
						planks.add(plankType.getStack(false));
						fences.add(WoodManager.getFence(plankType, plankTypeSecondary, new FenceType(fenceType.getSize(), fenceType.isSolid(), false), 1));
						outputFences.add(WoodManager.getFence(plankType, plankTypeSecondary, new FenceType(fenceType.getSize(), fenceType.isSolid(), true), 2));
					}
				}
			}
		}
		List<List<ItemStack>> itemInputs = new ArrayList<>();
		itemInputs.add(fences);
		itemInputs.add(planks);
		itemInputs.add(fences);
		List<List<ItemStack>> itemOutputs = new ArrayList<>();
		itemOutputs.add(outputFences);
		ingredients.setOutputLists(ItemStack.class, itemOutputs);
		ingredients.setInputLists(ItemStack.class, itemInputs);
	}

	@Override
	public int getWidth() {
		return 3;
	}

	@Override
	public int getHeight() {
		return 1;
	}
}
