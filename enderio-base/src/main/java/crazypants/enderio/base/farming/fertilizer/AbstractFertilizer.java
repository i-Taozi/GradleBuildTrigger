package crazypants.enderio.base.farming.fertilizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;

import crazypants.enderio.api.farm.IFertilizer;
import crazypants.enderio.api.farm.IFertilizerResult;
import crazypants.enderio.util.Prep;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class AbstractFertilizer extends IForgeRegistryEntry.Impl<IFertilizer> implements IFertilizer {

  protected final @Nonnull ItemStack fertilizer;

  protected AbstractFertilizer(@Nullable Item item) {
    this(item == null ? Prep.getEmpty() : new ItemStack(item));
  }

  protected AbstractFertilizer(@Nullable Block block) {
    this(block == null ? Prep.getEmpty() : new ItemStack(block));
  }

  protected AbstractFertilizer(@Nonnull ItemStack stack) {
    fertilizer = stack;
    if (Prep.isValid(fertilizer)) {
      setRegistryName(fertilizer.getItem().getRegistryName());
    }
  }

  public boolean isValid() {
    return Prep.isValid(fertilizer);
  }

  @Override
  public boolean matches(@Nonnull ItemStack stack) {
    return OreDictionary.itemMatches(fertilizer, stack, false);
  }

  @Override
  public abstract IFertilizerResult apply(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos bc);

  @Override
  public boolean applyOnAir() {
    return false;
  }

  @Override
  public boolean applyOnPlant() {
    return true;
  }

  @Override
  @Nonnull
  public NonNullList<ItemStack> getGuiItem() {
    return new NNList<>(fertilizer);
  }
}
