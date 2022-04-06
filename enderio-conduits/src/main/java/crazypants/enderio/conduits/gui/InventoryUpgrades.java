package crazypants.enderio.conduits.gui;

import java.util.List;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.NNList;

import crazypants.enderio.base.conduit.IConduit;
import crazypants.enderio.base.conduit.item.ItemFunctionUpgrade;
import crazypants.enderio.base.filter.IFilter;
import crazypants.enderio.base.filter.capability.IFilterHolder;
import crazypants.enderio.conduits.capability.IUpgradeHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * The Inventory for Holding Conduit Upgrades
 */
public class InventoryUpgrades implements IItemHandlerModifiable {

  private @Nonnull EnumFacing dir;

  private IFilterHolder<IFilter> filterHolder;
  private IUpgradeHolder upgradeHolder;

  public InventoryUpgrades(@Nonnull EnumFacing dir) {
    this.dir = dir;
  }

  public void setFilterHolder(IFilterHolder<IFilter> filterHolder) {
    this.filterHolder = filterHolder;
  }

  public void setUpgradeHolder(IUpgradeHolder upgradeHolder) {
    this.upgradeHolder = upgradeHolder;
  }

  @Override
  @Nonnull
  public ItemStack getStackInSlot(int slot) {
    switch (slot) {
    case 0:
      return upgradeHolder != null ? upgradeHolder.getUpgradeStack(dir.ordinal()) : ItemStack.EMPTY;
    case 2:
      return filterHolder != null ? filterHolder.getFilterStack(filterHolder.getInputFilterIndex(), dir) : ItemStack.EMPTY;
    case 3:
      return filterHolder != null ? filterHolder.getFilterStack(filterHolder.getOutputFilterIndex(), dir) : ItemStack.EMPTY;
    default:
      return ItemStack.EMPTY;
    }
  }

  @Nonnull
  @Override
  public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
    ItemStack slotStack = stack.splitStack(getSlotLimit(slot));

    if (!simulate) {
      setInventorySlotContents(slot, slotStack);
    }
    return stack;
  }

  @Nonnull
  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    ItemStack current = getStackInSlot(slot);
    if (current.isEmpty()) {
      return current;
    }
    ItemStack result;
    ItemStack remaining;
    if (amount >= current.getCount()) {
      result = current.copy();
      remaining = ItemStack.EMPTY;
    } else {
      result = current.copy();
      result.setCount(amount);
      remaining = current.copy();
      remaining.shrink(amount);
    }

    if (!simulate) {
      setInventorySlotContents(slot, remaining);
    }
    return result;
  }

  private void setInventorySlotContents(int slot, @Nonnull ItemStack stack) {
    switch (slot) {
    case 0:
      if (upgradeHolder != null) {
        upgradeHolder.setUpgradeStack(dir.ordinal(), stack);
      }
      break;
    case 2:
      if (filterHolder != null) {
        filterHolder.setFilterStack(filterHolder.getInputFilterIndex(), dir, stack);
      }
      break;
    case 3:
      if (filterHolder != null) {
        filterHolder.setFilterStack(filterHolder.getOutputFilterIndex(), dir, stack);
      }
      break;
    }
  }

  public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack, IConduit con) {
    if (stack.isEmpty()) {
      return false;
    }
    switch (slot) {
    case 0:
      return isFunctionUpgradeAccepted(stack, con);
    case 2:
      return isFilterUpgradeAccepted(stack, con, true);
    case 3:
      return isFilterUpgradeAccepted(stack, con, false);
    }
    return false;
  }

  private boolean isFilterUpgradeAccepted(@Nonnull ItemStack stack, IConduit con, boolean isInput) {
    if (con instanceof IFilterHolder) {
      return ((IFilterHolder<?>) con).isFilterUpgradeAccepted(stack, isInput);
    }
    return false;
  }

  private boolean isFunctionUpgradeAccepted(@Nonnull ItemStack stack, IConduit con) {
    if (stack.getItem() instanceof ItemFunctionUpgrade && con instanceof IUpgradeHolder) {
      return ((IUpgradeHolder) con).isFunctionUpgradeAccepted(stack);
    }
    return false;
  }

  @Override
  public int getSlots() {
    return 4;
  }

  @Override
  public int getSlotLimit(int slot) {
    return slot == 0 && upgradeHolder != null ? upgradeHolder.getUpgradeSlotLimit() : 1;
  }

  public int getSlotLimit(int slot, @Nonnull ItemStack stack) {
    return slot == 0 && upgradeHolder != null ? upgradeHolder.getUpgradeSlotLimit(stack) : getSlotLimit(slot);
  }

  @Override
  public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
    setInventorySlotContents(slot, stack);
  }

  public @Nonnull List<String> getFunctionUpgradeToolTipText() {
    return upgradeHolder == null ? NNList.emptyList() : upgradeHolder.getFunctionUpgradeToolTipText(dir);
  }

}