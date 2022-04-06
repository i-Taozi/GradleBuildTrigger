package crazypants.enderio.base.recipe;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.NNList;

import crazypants.enderio.util.Prep;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

public class MachineRecipeInput {

  public static @Nonnull ItemStack getInputForSlot(int slot, NNList<MachineRecipeInput> inputs) {
    ItemStack ret = Prep.getEmpty();
    for (MachineRecipeInput ri : inputs) {
      ret = getInputForSlot(slot, ri);
      if (Prep.isValid(ret)) {
        return ret;
      }
    }
    return ret;
  }
  
  public static @Nonnull ItemStack getInputForSlot(int slot, MachineRecipeInput input) {
    if (input.slotNumber == slot) {
      return input.item;
    }
    return Prep.getEmpty();
  }

  public static @Nonnull MachineRecipeInput create(int slotNumber, @Nonnull ItemStack item) {
    return new MachineRecipeInput(slotNumber, item);
  }

  public final int slotNumber;
  public final @Nonnull ItemStack item;
  public final FluidStack fluid;

  public MachineRecipeInput(int slotNumber, @Nonnull ItemStack item) {
    this.slotNumber = slotNumber;
    this.item = item;
    fluid = null;
  }

  public MachineRecipeInput(int slotNumber, FluidStack fluid) {
    this.slotNumber = slotNumber;
    item = Prep.getEmpty();
    this.fluid = fluid;
  }

  public MachineRecipeInput(int slotNumber, @Nonnull ItemStack item, FluidStack fluid) {
    this.slotNumber = slotNumber;
    this.item = item;
    this.fluid = fluid;
  }

  public MachineRecipeInput copy() {
    if (isFluid()) {
      return new MachineRecipeInput(slotNumber, fluid.copy());
    } else {
      return new MachineRecipeInput(slotNumber, item.copy());
    }
  }

  public static MachineRecipeInput readFromNBT(@Nonnull NBTTagCompound root) {
    int slotNum = root.getInteger("slotNum");
    ItemStack item = Prep.getEmpty();
    FluidStack fluid = null;
    if (root.hasKey("itemStack")) {
      NBTTagCompound stackRoot = root.getCompoundTag("itemStack");
      item = new ItemStack(stackRoot);
    } else if (root.hasKey("fluidStack")) {
      NBTTagCompound stackRoot = root.getCompoundTag("fluidStack");
      fluid = FluidStack.loadFluidStackFromNBT(stackRoot);
    }
    return new MachineRecipeInput(slotNum, item, fluid);
  }

  public void writeToNbt(@Nonnull NBTTagCompound root) {
    if (Prep.isValid(item)) {
      NBTTagCompound stackRoot = new NBTTagCompound();
      item.writeToNBT(stackRoot);
      root.setTag("itemStack", stackRoot);
    } else if (fluid != null) {
      NBTTagCompound stackRoot = new NBTTagCompound();
      fluid.writeToNBT(stackRoot);
      root.setTag("fluidStack", stackRoot);
    }
    root.setInteger("slotNum", slotNumber);
  }

  public boolean isFluid() {
    return fluid != null;
  }

}