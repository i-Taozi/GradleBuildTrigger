package crazypants.enderio.base.filter.redstone;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.DyeColor;

import crazypants.enderio.base.conduit.redstone.signals.BundledSignal;
import crazypants.enderio.base.conduit.redstone.signals.CombinedSignal;

/**
 * A filter that can be added to a redstone conduit to filter its output
 *
 */
public interface IOutputSignalFilter extends IRedstoneSignalFilter {

  /**
   * Apply the filter to the signal
   * 
   * @param color
   *          color of the signal
   * @param bundledSignal
   *          bundle to get the signal from
   * @return the signal after being modified by the filter
   */
  @Nonnull
  CombinedSignal apply(@Nonnull DyeColor color, @Nonnull BundledSignal bundledSignal);

}