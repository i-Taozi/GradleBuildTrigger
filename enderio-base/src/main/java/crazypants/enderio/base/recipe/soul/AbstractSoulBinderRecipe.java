package crazypants.enderio.base.recipe.soul;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.NNList;

import crazypants.enderio.base.recipe.MachineRecipeInput;
import crazypants.enderio.base.recipe.MachineRecipeRegistry;
import crazypants.enderio.base.recipe.RecipeBonusType;
import crazypants.enderio.base.recipe.RecipeLevel;
import crazypants.enderio.util.CapturedMob;
import crazypants.enderio.util.Prep;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import static crazypants.enderio.base.init.ModObject.itemSoulVial;

public abstract class AbstractSoulBinderRecipe implements ISoulBinderRecipe {

  private final int energyRequired;
  private final @Nonnull String uid;
  private final int xpLevelsRequired;
  private final @Nonnull RecipeLevel recipeLevel;

  private final @Nonnull NNList<ResourceLocation> supportedEntities;

  protected AbstractSoulBinderRecipe(int energyRequired, int xpLevelsRequired, @Nonnull String uid, @Nonnull RecipeLevel recipeLevel,
      @Nonnull Class<? extends Entity> entityClass) {
    this(energyRequired, xpLevelsRequired, uid, recipeLevel, EntityList.getKey(entityClass));
  }

  protected AbstractSoulBinderRecipe(int energyRequired, int xpLevelsRequired, @Nonnull String uid, @Nonnull RecipeLevel recipeLevel,
      @Nonnull ResourceLocation... entityNames) {
    this(energyRequired, xpLevelsRequired, uid, recipeLevel, new NNList<>(entityNames));
  }

  protected AbstractSoulBinderRecipe(int energyRequired, int xpLevelsRequired, @Nonnull String uid, @Nonnull RecipeLevel recipeLevel,
      @Nonnull NNList<ResourceLocation> entityNames) {
    this.energyRequired = energyRequired;
    this.xpLevelsRequired = xpLevelsRequired;
    this.uid = uid;
    this.supportedEntities = entityNames;
    this.recipeLevel = recipeLevel;
  }

  @Override
  public final @Nonnull String getUid() {
    return uid;
  }

  @Override
  public final int getExperienceLevelsRequired() {
    return xpLevelsRequired;
  }

  @Override
  public final int getEnergyRequired(@Nonnull NNList<MachineRecipeInput> inputs) {
    return getEnergyRequired();
  }

  @Override
  public final @Nonnull RecipeBonusType getBonusType(@Nonnull NNList<MachineRecipeInput> inputs) {
    return RecipeBonusType.NONE;
  }

  @Override
  public boolean isRecipe(@Nonnull RecipeLevel machineLevel, @Nonnull NNList<MachineRecipeInput> inputs) {
    int validCount = 0;
    for (MachineRecipeInput input : inputs) {
      if (input != null && isValidInput(machineLevel, input)) {
        validCount++;
      } else {
        return false;
      }
    }
    return validCount == 2;
  }

  @Override
  public @Nonnull ResultStack[] getCompletedResult(long nextSeed, float chanceMultiplier, @Nonnull NNList<MachineRecipeInput> inputs) {
    CapturedMob mobType = null;
    ItemStack inputItem = null;
    for (MachineRecipeInput input : inputs) {
      if (input != null && input.slotNumber == 0 && CapturedMob.containsSoul(input.item)) {
        mobType = CapturedMob.create(input.item);
      } else if (input != null && input.slotNumber == 1 && isValidInputItem(input.item)) {
        inputItem = input.item;
      }
    }
    if (mobType == null || !isValidInputSoul(mobType) || inputItem == null) {
      return new ResultStack[0];
    }
    ItemStack resultStack = getOutputStack(inputItem, mobType);
    ItemStack soulVessel = new ItemStack(itemSoulVial.getItemNN());
    return new ResultStack[] { new ResultStack(soulVessel), new ResultStack(resultStack) };
  }

  @Override
  public final float getExperienceForOutput(@Nonnull ItemStack output) {
    return 0;
  }

  @Override
  public boolean isValidInput(@Nonnull RecipeLevel machineLevel, @Nonnull MachineRecipeInput input) {
    if (!recipeLevel.canMake(machineLevel)) {
      return false;
    }
    if (Prep.isInvalid(input.item)) {
      return false;
    }
    int slot = input.slotNumber;
    ItemStack item = input.item;
    if (slot == 0) {
      CapturedMob mobType = CapturedMob.create(item);
      return mobType != null && item.getItem() == itemSoulVial.getItemNN() && isValidInputSoul(mobType);
    }
    if (slot == 1) {
      return isValidInputItem(item);
    }
    return false;
  }

  protected boolean isValidInputSoul(@Nonnull CapturedMob mobType) {
    return getSupportedSouls().contains(mobType.getEntityName());
  }

  protected boolean isValidInputItem(@Nonnull ItemStack item) {
    return item.isItemEqual(getInputStack());
  }

  @Override
  public @Nonnull String getMachineName() {
    return MachineRecipeRegistry.SOULBINDER;
  }

  @Override
  public @Nonnull List<MachineRecipeInput> getQuantitiesConsumed(@Nonnull NNList<MachineRecipeInput> inputs) {
    List<MachineRecipeInput> result = new ArrayList<MachineRecipeInput>(inputs.size());
    for (MachineRecipeInput input : inputs) {
      if (input != null && Prep.isValid(input.item)) {
        ItemStack resStack = input.item.copy();
        resStack.setCount(1);
        MachineRecipeInput mri = new MachineRecipeInput(input.slotNumber, resStack);
        result.add(mri);
      }
    }
    return result;
  }

  @Override
  public @Nonnull NNList<ResourceLocation> getSupportedSouls() {
    return supportedEntities;
  }

  @Override
  public final int getEnergyRequired() {
    return energyRequired;
  }

  @Override
  public @Nonnull RecipeLevel getRecipeLevel() {
    return recipeLevel;
  }

}
