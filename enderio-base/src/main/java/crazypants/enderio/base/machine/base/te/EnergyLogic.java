package crazypants.enderio.base.machine.base.te;

import java.util.Random;

import javax.annotation.Nonnull;

import com.enderio.core.common.inventory.Callback;
import com.enderio.core.common.inventory.EnderInventory.Type;
import com.enderio.core.common.inventory.InventorySlot;
import com.enderio.core.common.util.NullHelper;

import crazypants.enderio.api.capacitor.ICapacitorData;
import crazypants.enderio.base.capability.Filters;
import crazypants.enderio.base.machine.base.network.PacketPowerStorage;
import crazypants.enderio.base.network.PacketHandler;
import crazypants.enderio.base.power.IEnergyTank;
import crazypants.enderio.util.NbtValue;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyLogic implements IEnergyLogic, ICap.NNFunction, Callback<ItemStack> {

  public static final @Nonnull String CAPSLOT = "cap";

  private final @Nonnull AbstractCapabilityMachineEntity owner;
  private final @Nonnull IEnergyTank energy;

  private final @Nonnull Random random = new Random();
  private float lastSyncPowerStored = -1;
  private long lastSyncPowerTick = -1;
  private boolean isCapacitorDamageable = false;

  public EnergyLogic(AbstractCapabilityMachineEntity owner, @Nonnull IEnergyTank energy) {
    this.owner = NullHelper.notnull(owner, "Logic Error: Owner missing");
    this.energy = energy;
    owner.getInventory().add(Type.UPGRADE, EnergyLogic.CAPSLOT, new InventorySlot(Filters.CAPACITORS, null, this, 1));
    updateCapacitorFromSlot();
    owner.addICap(CapabilityEnergy.ENERGY, ICap.facedOnly(this));
  }

  @Override
  public void serverTick() {
    energy.loseEnergy();
    final int scaledPower = getScaledPower();
    if ((lastSyncPowerStored != scaledPower && (lastSyncPowerStored == 0 || scaledPower == 0 || lastSyncWasLongAgo()))) {
      lastSyncPowerStored = scaledPower;
      PacketHandler.sendToAllAround(new PacketPowerStorage(owner), owner);
    }
  }

  private boolean lastSyncWasLongAgo() {
    final long tick = owner.getWorld().getTotalWorldTime();
    if (tick != lastSyncPowerTick) {
      lastSyncPowerTick = tick;
      return true;
    }
    return false;
  }

  @Override
  public void processTasks(boolean redstoneCheck) {
    if (redstoneCheck) {
      damageCapacitor();
    }
  }

  @Override
  public int getScaledPower() {
    if (getEnergy().getEnergyStored() == 0) {
      return 0;
    } else {
      return 1 + getEnergy().getEnergyStored() / 1000;
    }
  }

  @Override
  public boolean displayPower() {
    return true;
  }

  @Override
  public boolean hasPower() {
    return getEnergy().getEnergyStored() > 0;
  }

  @Override
  @Nonnull
  public ICapacitorData getCapacitorData() {
    return getEnergy().getCapacitorData();
  }

  @Override
  @Nonnull
  public IEnergyTank getEnergy() {
    return energy;
  }

  @Override
  public final void onChange(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack) {
    updateCapacitorFromSlot();
  }

  @Override
  public void updateCapacitorFromSlot() {
    final InventorySlot slot = owner.getInventory().getSlot(EnergyLogic.CAPSLOT);
    if (getEnergy().updateCapacitorFromSlot(slot)) {
      isCapacitorDamageable = slot.get().isItemStackDamageable();
      owner.onCapacitorDataChange();
    }
  }

  @Override
  public void damageCapacitor() {
    if (isCapacitorDamageable) {
      ItemStack cap = owner.getInventory().getSlot(EnergyLogic.CAPSLOT).get();
      if (cap.attemptDamageItem(1, random, null)) {
        owner.getInventory().getSlot(EnergyLogic.CAPSLOT).clear();
      }
    }
  }

  @Override
  public void readCustomNBT(@Nonnull ItemStack stack) {
    energy.setEnergyStored(NbtValue.ENERGY.getInt(stack));
  }

  @Override
  public void writeCustomNBT(@Nonnull ItemStack stack) {
    NbtValue.ENERGY.setInt(stack, energy.getEnergyStored());
    NbtValue.ENERGY_BUFFER.setInt(stack, energy.getMaxEnergyStored());
  }

  @Override
  public IEnergyStorage apply(@Nonnull EnumFacing facingIn) {
    return owner.getIoMode(facingIn).canInputOrOutput() ? getEnergy().get(facingIn) : null;
  }

}
