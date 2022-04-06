package crazypants.enderio.api.farm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * This class is a handler for handling planting and/or harvesting for the Farming Station.
 * <p>
 * The handler needs to be registered in the {@link net.minecraftforge.event.RegistryEvent.Register}&lt;IFarmerJoe&gt; event.
 * <p>
 * Note: Using an existing handler is much easier than writing one from scratch!
 * 
 * @author Henry Loenwind
 *
 */
public interface IFarmerJoe extends IForgeRegistryEntry<IFarmerJoe> {

  /**
   * Check if the given itemStack is a seed that can be planted by this Handler.
   * <p>
   * This will be called to see if an item is allowed to be inserted into one of the seed slots of the Farming Station.
   * 
   * @param stack
   *          The itemStack to be checked.
   * @return True if this handler can handle the item.
   */
  boolean canPlant(@Nonnull ItemStack stack);

  @Deprecated // TODO 1.13: remove
  default boolean prepareBlock(@Nonnull IFarmer farm, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
    return tryPrepareBlock(farm, pos, state) != IFarmerJoe.Result.NEXT;
  }

  /**
   * This will be called whenever the Farming Station encounters a block that is empty (air or replaceable).
   * <p>
   * The Handler is expected to check if there's a matching seed in the correct seed slot, then to till the land and plant it.
   * 
   * @param farm
   *          The call back to the Farming Station.
   * @param pos
   *          The location to work on.
   * @param state
   *          The blockstate at that location.
   * @return ACTION if this handler wants to handle this location. CLAIM if it didn't actually do something, just wants no other handler to mess with this
   *         location. NEXT to give the next handler a try.
   */
  default IFarmerJoe.Result tryPrepareBlock(@Nonnull IFarmer farm, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
    return prepareBlock(farm, pos, state) ? IFarmerJoe.Result.ACTION : IFarmerJoe.Result.NEXT;
  }

  public enum Result {
    /**
     * The Handler did something. Fire particle effects etc.
     */
    ACTION,
    /**
     * The Handler claims that location. Skip all other handlers.
     */
    CLAIM,
    /**
     * The Handler won't handle that location, skip to the next one.
     */
    NEXT;
  }

  /**
   * This will be called whenever the Farming Station encounters a block that is not empty.
   * <p>
   * The Handler is expected to check if there's a matching plant. If there is, {@link #canHarvest(IFarmer, BlockPos, IBlockState)} will be called next.
   * <p>
   * Note: This is not a check if the plant is ready for harvest, but to find out which handler is responsible for it.
   * 
   * @param farm
   *          The call back to the Farming Station.
   * @param pos
   *          The location to work on.
   * @param state
   *          The blockstate at that location.
   * @return True if this handler wants to handle this location. Doesn't mean that it actually will harvest something, just that no other handler will get the
   *         chance to do so.
   */
  boolean canHarvest(@Nonnull IFarmer farm, @Nonnull BlockPos pos, @Nonnull IBlockState state);

  /**
   * This will be called after {@link #canHarvest(IFarmer, BlockPos, IBlockState)}.
   * <p>
   * The Handler is expected to check if the plant is ready for harvest, and if it is, to harvest it. If possible, the plant should also be replanted directly.
   * 
   * @param farm
   *          The call back to the Farming Station.
   * @param pos
   *          The location to work on.
   * @param state
   *          The blockstate at that location.
   * @return Either <code>null</code> if no harvest happened or {@link IHarvestResult} with the result of the harvest.
   */
  @Nullable
  IHarvestResult harvestBlock(@Nonnull IFarmer farm, @Nonnull BlockPos pos, @Nonnull IBlockState state);

  @Nonnull
  EventPriority getPriority();
}
