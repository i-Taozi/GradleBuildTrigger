package crazypants.enderio.base.render;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import crazypants.enderio.base.paint.YetaUtil.YetaDisplayMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * The block state wrapper is the object that is responsible for building the final baked model. This happens in the method bakeModel(), which must be called
 * before the wrapper is returned from getExtendedState().
 * <p>
 * <em>Caching</em><br />
 * To enable caching, call addCacheKey() at least once. If it is not called before bakeModel(), no caching will be performed.
 *
 */
public interface IBlockStateWrapper extends IBlockState, ICacheKey {

  @Nonnull
  BlockPos getPos();

  TileEntity getTileEntity();

  @Nonnull
  IBlockAccess getWorld();

  @Nonnull
  IBlockState getState();

  @Override
  @Nonnull
  IBlockStateWrapper addCacheKey(@Nullable Object addlCacheKey);

  void bakeModel();

  @Nonnull
  YetaDisplayMode getYetaDisplayMode();

}
