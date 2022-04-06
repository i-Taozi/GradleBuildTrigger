package crazypants.enderio.base.item.darksteel.upgrade.energy;

import java.util.Random;

import javax.annotation.Nonnull;

import crazypants.enderio.api.upgrades.IDarkSteelItem;
import crazypants.enderio.api.upgrades.IDarkSteelUpgrade;
import crazypants.enderio.base.item.darksteel.upgrade.energy.EnergyUpgrade.EnergyUpgradeHolder;
import crazypants.enderio.base.lang.LangPower;
import crazypants.enderio.util.NbtComparer;
import info.loenwind.autoconfig.factory.IValue;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class EnergyUpgradeManager {

  protected static final @Nonnull String UPGRADE_NAME = "energyUpgrade";
  protected static final @Nonnull String HIGH_UPGRADE_NAME = "batteryUpgrade";
  protected static final @Nonnull String KEY_ENERGY = "energy";
  protected static final @Nonnull Random RANDOM = new Random();

  // Should this really be here.
  // TODO: Consider refactoring in the future
  private static final NbtComparer energyInvarientNbtComparer;

  static {
    energyInvarientNbtComparer = new NbtComparer();
    energyInvarientNbtComparer.addInvarientTagKey(EnergyUpgradeManager.KEY_ENERGY);
  }

  public static EnergyUpgrade.EnergyUpgradeHolder loadFromItem(@Nonnull ItemStack stack) {
    if (stack.getItem() instanceof IDarkSteelItem) {
      IDarkSteelItem item = (IDarkSteelItem) stack.getItem();
      EnergyUpgrade energyUpgrade = EnergyUpgrade.loadAnyFromItem(stack);
      return energyUpgrade != null ? energyUpgrade.getEnergyUpgradeHolder(stack, item) : null;
    } else {
      return null;
    }
  }

  public static boolean itemHasAnyPowerUpgrade(@Nonnull ItemStack itemstack) {
    return EnergyUpgrade.loadAnyFromItem(itemstack) != null;
  }

  /**
   * Returns the level of Empowered (0-4) on the given item or -1 if it isn't Empowered.
   */
  public static int getPowerUpgradeLevel(@Nonnull ItemStack itemstack) {
    final EnergyUpgrade upgrade = EnergyUpgrade.loadAnyFromItem(itemstack);
    return upgrade != null ? upgrade.getLevel() : -1;
  }

  public static boolean isLowestPowerUpgrade(@Nonnull IDarkSteelUpgrade upgrade) {
    return (upgrade instanceof EnergyUpgrade) && ((EnergyUpgrade) upgrade).getLevel() == 0;
  }

  public static int extractEnergy(@Nonnull ItemStack container, int maxExtract, boolean simulate) {
    if (maxExtract > 0 && container.getItem() instanceof IDarkSteelItem) {
      return extractEnergy(container, (IDarkSteelItem) container.getItem(), maxExtract, simulate);
    }
    return 0;
  }

  public static int extractEnergy(@Nonnull ItemStack container, @Nonnull IDarkSteelItem item, int maxExtract, boolean simulate) {
    EnergyUpgradeHolder eu = loadFromItem(container);
    if (eu == null) {
      return 0;
    }
    int res = eu.extractEnergy(maxExtract, simulate);
    if (!simulate && res > 0) {
      eu.writeToItem();
    }
    return res;
  }

  public static int extractEnergy(@Nonnull ItemStack container, @Nonnull IDarkSteelItem item, IValue<Integer> maxExtract, boolean simulate) {
    EnergyUpgradeHolder eu = loadFromItem(container);
    if (eu == null) {
      return 0;
    }
    int res = eu.extractEnergy(maxExtract.get(), simulate);
    if (!simulate && res > 0) {
      eu.writeToItem();
    }
    return res;
  }

  public static int receiveEnergy(@Nonnull ItemStack container, int maxReceive, boolean simulate) {
    if (container.getItem() instanceof IDarkSteelItem) {
      return receiveEnergy(container, (IDarkSteelItem) container.getItem(), maxReceive, simulate);
    }
    return 0;
  }

  public static int receiveEnergy(@Nonnull ItemStack container, @Nonnull IDarkSteelItem item, int maxReceive, boolean simulate) {
    EnergyUpgradeHolder eu = loadFromItem(container);
    if (eu == null) {
      return 0;
    }
    int res = eu.receiveEnergy(maxReceive, simulate);
    if (!simulate && res > 0) {
      eu.writeToItem();
    }
    return res;
  }

  public static void setPowerFull(@Nonnull ItemStack container, @Nonnull IDarkSteelItem item) {
    if (!itemHasAnyPowerUpgrade(container)) {
      return;
    }
    EnergyUpgradeHolder eu = loadFromItem(container);
    eu.setEnergy(eu.getCapacity());
    eu.writeToItem();
  }

  public static String getStoredEnergyString(@Nonnull ItemStack itemstack) {
    EnergyUpgradeHolder up = loadFromItem(itemstack);
    if (up == null) {
      return null;
    }
    return LangPower.RF(up.getEnergy(), up.getCapacity());
  }

  public static int getEnergyStored(@Nonnull ItemStack container) {
    EnergyUpgradeHolder eu = loadFromItem(container);
    if (eu == null) {
      return 0;
    }
    return eu.getEnergy();
  }

  public static int getMaxEnergyStored(@Nonnull ItemStack container) {
    EnergyUpgradeHolder eu = loadFromItem(container);
    if (eu == null) {
      return 0;
    }
    return eu.getCapacity();
  }

  public static boolean compareNbt(NBTTagCompound oldNbt, NBTTagCompound newNbt) {
    return energyInvarientNbtComparer.compare(oldNbt, newNbt);
  }

}