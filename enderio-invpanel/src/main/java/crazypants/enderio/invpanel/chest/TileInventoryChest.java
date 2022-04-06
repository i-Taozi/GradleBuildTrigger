package crazypants.enderio.invpanel.chest;

import javax.annotation.Nonnull;

import com.enderio.core.common.inventory.EnderInventory;
import com.enderio.core.common.inventory.InventorySlot;

import crazypants.enderio.base.capacitor.DefaultCapacitorData;
import crazypants.enderio.base.init.ModObject;
import crazypants.enderio.base.machine.base.te.AbstractCapabilityMachineEntity;
import crazypants.enderio.base.machine.base.te.EnergyLogic;
import crazypants.enderio.base.machine.base.te.ICap;
import crazypants.enderio.base.machine.interfaces.IHasFillLevel;
import crazypants.enderio.base.paint.IPaintable;
import crazypants.enderio.util.Prep;
import info.loenwind.autosave.annotations.Storable;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;

@Storable
public abstract class TileInventoryChest extends AbstractCapabilityMachineEntity implements IPaintable.IPaintableTileEntity, IHasFillLevel {

  @Storable
  public static class Tiny extends TileInventoryChest {
    public Tiny() {
      super(EnumChestSize.TINY);
    }
  }

  @Storable
  public static class Small extends TileInventoryChest {
    public Small() {
      super(EnumChestSize.SMALL);
    }
  }

  @Storable
  public static class Medium extends TileInventoryChest {
    public Medium() {
      super(EnumChestSize.MEDIUM);
    }
  }

  @Storable
  public static class Big extends TileInventoryChest {
    public Big() {
      super(EnumChestSize.BIG);
    }
  }

  @Storable
  public static class Large extends TileInventoryChest {
    public Large() {
      super(EnumChestSize.LARGE);
    }
  }

  @Storable
  public static class Huge extends TileInventoryChest {
    public Huge() {
      super(EnumChestSize.HUGE);
    }
  }

  @Storable
  public static class Enormous extends TileInventoryChest {
    public Enormous() {
      super(EnumChestSize.ENORMOUS);
    }
  }

  @Storable
  public static class Warehouse extends TileInventoryChest {
    public Warehouse() {
      super(EnumChestSize.WAREHOUSE);
    }
  }

  @Storable
  public static class Warehouse13 extends TileInventoryChest {
    public Warehouse13() {
      super(EnumChestSize.WAREHOUSE13);
    }
  }

  private final @Nonnull EnumChestSize size;

  private TileInventoryChest(@Nonnull EnumChestSize size) {
    super(null, size.getIntake(), size.getBuffer(), size.getUse());
    this.size = size;
    for (int i = 0; i < size.getSlots(); i++) {
      getInventory().add(EnderInventory.Type.INOUT, "slot" + i, new InventorySlot());
    }
    getInventory().getSlot(EnergyLogic.CAPSLOT).set(new ItemStack(ModObject.itemBasicCapacitor.getItemNN(), 1, DefaultCapacitorData.BASIC_CAPACITOR.ordinal()));
    addICap(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facingIn -> hasPower() ? ICap.NEXT : ICap.DENY);
  }

  @Override
  public boolean isActive() {
    return hasPower();
  }

  private boolean lastState = false;

  @Override
  protected void processTasks(boolean redstoneCheck) {
    boolean hasPower = getEnergy().useEnergy();
    if (lastState != hasPower) {
      lastState = hasPower;
      updateClients = true;
    }
  }

  public int getComparatorInputOverride() {
    int count = 0;
    for (InventorySlot slot : getInventory().getView(EnderInventory.Type.INOUT)) {
      if (Prep.isValid(slot.getStackInSlot(0))) {
        count++;
      }
    }
    return count == 0 ? 0 : (14 * count / size.getSlots() + 1);
  }

}
