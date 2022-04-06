package crazypants.enderio.base.farming.farmers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.NNList.NNIterator;

import crazypants.enderio.api.farm.FarmingAction;
import crazypants.enderio.api.farm.IFarmer;
import crazypants.enderio.base.farming.FarmingTool;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.minecraft.block.BlockHorizontal.FACING;

public class CocoaFarmer extends CustomSeedFarmer {
  public CocoaFarmer() {
    super(Blocks.COCOA, new ItemStack(Items.DYE, 1, 3));
    this.requiresTilling = false;
    this.disableTreeFarm = true;
  }

  @Override
  public boolean canHarvest(@Nonnull IFarmer farm, @Nonnull BlockPos bc, @Nonnull IBlockState state) {
    return state.getBlock() == getPlantedBlock() && state.getValue(BlockCocoa.AGE) == 2;
  }

  @Override
  protected boolean plant(@Nonnull IFarmer farm, @Nonnull World world, @Nonnull BlockPos bc) {
    EnumFacing dir = getPlantDirection(world, bc);
    if (dir == null) {
      return false;
    }
    IBlockState iBlockState = getPlantedBlock().getDefaultState().withProperty(FACING, dir);
    if (farm.checkAction(FarmingAction.PLANT, FarmingTool.HAND) && world.setBlockState(bc, iBlockState, 1 | 2)) {
      farm.registerAction(FarmingAction.PLANT, FarmingTool.HAND, iBlockState, bc);
      return true;
    }
    return false;
  }

  @Override
  protected boolean canPlant(@Nonnull IFarmer farm, @Nonnull World world, @Nonnull BlockPos bc) {
    return getPlantDirection(world, bc) != null;
  }

  private @Nullable EnumFacing getPlantDirection(@Nonnull World world, @Nonnull BlockPos bc) {
    if (!world.isAirBlock(bc)) {
      return null;
    }

    NNIterator<EnumFacing> iterator = NNList.FACING_HORIZONTAL.iterator();
    while (iterator.hasNext()) {
      EnumFacing dir = iterator.next();
      BlockPos p = bc.offset(dir);
      if (validBlock(world.getBlockState(p))) {
        return dir;
      }
    }

    return null;
  }

  private boolean validBlock(@Nonnull IBlockState iblockstate) {
    return iblockstate.getBlock() == Blocks.LOG && iblockstate.getValue(BlockOldLog.VARIANT) == BlockPlanks.EnumType.JUNGLE;
  }

}
