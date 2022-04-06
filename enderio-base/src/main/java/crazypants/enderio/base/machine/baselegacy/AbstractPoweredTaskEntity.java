package crazypants.enderio.base.machine.baselegacy;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.common.util.IProgressTile;
import com.enderio.core.common.util.NNList;

import crazypants.enderio.api.capacitor.ICapacitorKey;
import crazypants.enderio.base.config.config.MachineConfig;
import crazypants.enderio.base.machine.interfaces.IPoweredTask;
import crazypants.enderio.base.machine.task.PoweredTask;
import crazypants.enderio.base.machine.task.PoweredTaskProgress;
import crazypants.enderio.base.recipe.IMachineRecipe;
import crazypants.enderio.base.recipe.IMachineRecipe.ResultStack;
import crazypants.enderio.base.recipe.MachineRecipeInput;
import crazypants.enderio.base.recipe.MachineRecipeRegistry;
import crazypants.enderio.util.Prep;
import info.loenwind.autosave.annotations.Storable;
import info.loenwind.autosave.annotations.Store;
import info.loenwind.autosave.util.NBTAction;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

@Storable
public abstract class AbstractPoweredTaskEntity extends AbstractPowerConsumerEntity implements IProgressTile {

  @Store({ NBTAction.SAVE, NBTAction.ITEM })
  protected IPoweredTask currentTask = null;
  @Store({ NBTAction.SAVE, NBTAction.ITEM })
  protected IMachineRecipe lastCompletedRecipe;
  protected IMachineRecipe cachedNextRecipe;

  protected int ticksSinceCheckedRecipe = 0;
  protected boolean startFailed = false;
  private Long theNextSeed = null;

  protected AbstractPoweredTaskEntity(@Nonnull SlotDefinition slotDefinition, @Nonnull ICapacitorKey maxEnergyRecieved, @Nonnull ICapacitorKey maxEnergyStored,
      @Nonnull ICapacitorKey maxEnergyUsed) {
    super(slotDefinition, maxEnergyRecieved, maxEnergyStored, maxEnergyUsed);
  }

  @Override
  public boolean isActive() {
    return currentTask == null ? false : currentTask.getProgress() >= 0 && hasPower() && redstoneCheckPassed;
  }

  @Override
  public float getProgress() {
    return currentTask == null ? -1 : currentTask.getProgress();
  }

  @Override
  public @Nonnull TileEntity getTileEntity() {
    return this;
  }

  @Override
  public void setProgress(float progress) {
    this.currentTask = progress < 0 ? null : new PoweredTaskProgress(progress);
  }

  public IPoweredTask getCurrentTask() {
    return currentTask;
  }

  public float getExperienceForOutput(@Nonnull ItemStack output) {
    if (lastCompletedRecipe == null) {
      return 0;
    }
    return lastCompletedRecipe.getExperienceForOutput(output);
  }

  public boolean getRedstoneChecksPassed() {
    return redstoneCheckPassed;
  }

  @Override
  protected void processTasks(boolean redstoneChecksPassed) {

    if (!redstoneChecksPassed) {
      return;
    }

    // Process any current items
    checkProgress(redstoneChecksPassed);

    if (currentTask != null || !hasPower() || !hasInputStacks()) {
      return;
    }

    if (startFailed) {
      ticksSinceCheckedRecipe++;
      if (ticksSinceCheckedRecipe < MachineConfig.sleepBetweenFailedTries.get()) {
        return;
      }
    }
    ticksSinceCheckedRecipe = 0;

    // Get a new chance when we don't have one yet
    // If a recipe could not be started we will try with the same chance next time
    if (theNextSeed == null) {
      theNextSeed = random.nextLong();
    }

    // Then see if we need to start a new one
    IMachineRecipe nextRecipe = canStartNextTask(theNextSeed);
    if (nextRecipe != null) {
      boolean started = startNextTask(nextRecipe, theNextSeed);
      if (started) {
        // this chance value has been used up
        theNextSeed = null;
      }
      startFailed = !started;
    } else {
      startFailed = true;
    }
  }

  protected void checkProgress(boolean redstoneChecksPassed) {
    if (currentTask == null || !hasPower()) {
      return;
    }
    if (redstoneChecksPassed && !currentTask.isComplete()) {
      usePower();
      if (shouldDoubleTick(currentTask, getPowerUsePerTick())) {
        usePower();
      }
    }
    // then check if we are done
    if (currentTask.isComplete()) {
      taskComplete();
      return;
    }

    return;
  }

  @Override
  protected int usePower(int wantToUse) {
    int used = super.usePower(wantToUse);
    if (currentTask != null) {
      currentTask.update(used * getEfficiencyMultiplier());
    }
    return used;
  }

  protected void taskComplete() {
    if (currentTask != null) {
      lastCompletedRecipe = currentTask.getRecipe();
      ResultStack[] output = currentTask.getCompletedResult();
      if (output.length > 0) {
        mergeResults(output);
      }
    }
    damageCapacitor();
    markDirty();
    currentTask = null;
  }

  protected void mergeResults(@Nonnull ResultStack[] results) {
    final int numOutputSlots = slotDefinition.getNumOutputSlots();
    if (numOutputSlots > 0) {

      List<ItemStack> outputStacks = new ArrayList<ItemStack>(numOutputSlots);
      for (int i = slotDefinition.minOutputSlot; i <= slotDefinition.maxOutputSlot; i++) {
        ItemStack it = inventory[i];
        if (it != null && Prep.isValid(it)) {
          it = it.copy();
        }
        outputStacks.add(it);
      }

      for (ResultStack result : results) {
        if (Prep.isValid(result.item)) {
          int numMerged = mergeItemResult(result.item, outputStacks);
          if (numMerged > 0) {
            result.item.shrink(numMerged);
          }
        } else if (result.fluid != null) {
          mergeFluidResult(result);
        }
      }

      int listIndex = 0;
      for (int i = slotDefinition.minOutputSlot; i <= slotDefinition.maxOutputSlot; i++) {
        ItemStack st = outputStacks.get(listIndex);
        if (st != null && Prep.isValid(st)) {
          st = st.copy();
        }
        inventory[i] = st;
        listIndex++;
      }

    } else {
      for (ResultStack result : results) {
        if (Prep.isValid(result.item)) {
          Block.spawnAsEntity(world, pos, result.item.copy());
          result.item.setCount(0);
        } else if (result.fluid != null) {
          mergeFluidResult(result);
        }
      }
    }
    cachedNextRecipe = null;
  }

  protected void mergeFluidResult(@Nonnull ResultStack result) {
  }

  protected void drainInputFluid(@Nonnull MachineRecipeInput fluid) {
  }

  protected boolean canInsertResultFluid(@Nonnull ResultStack fluid) {
    return false;
  }

  protected int mergeItemResult(@Nonnull ItemStack item, @Nonnull List<ItemStack> outputStacks) {

    ItemStack copy = item.copy();
    if (Prep.isInvalid(copy)) {
      return 0;
    }
    int firstFreeSlot = -1;

    // try to add it to existing stacks first
    for (int i = 0; i < outputStacks.size(); i++) {
      ItemStack outStack = outputStacks.get(i);
      if (outStack != null && Prep.isValid(outStack)) {
        int num = getNumCanMerge(outStack, copy);
        outStack.grow(num);
        copy.shrink(num);
        if (Prep.isInvalid(copy)) {
          return item.getCount();
        }
      } else if (firstFreeSlot < 0) {
        firstFreeSlot = i;
      }
    }

    // Try and add it to an empty slot
    if (firstFreeSlot >= 0) {
      outputStacks.set(firstFreeSlot, copy);
      return item.getCount();
    }

    return 0;
  }

  protected @Nonnull NNList<MachineRecipeInput> getRecipeInputs() {
    NNList<MachineRecipeInput> res = new NNList<>();
    for (int slot = slotDefinition.minInputSlot; slot <= slotDefinition.maxInputSlot; slot++) {
      final ItemStack item = getStackInSlot(slot);
      if (Prep.isValid(item)) {
        res.add(new MachineRecipeInput(slot, item));
      }
    }
    return res;
  }

  protected @Nullable IMachineRecipe getNextRecipe() {
    if (cachedNextRecipe == null) {
      cachedNextRecipe = MachineRecipeRegistry.instance.getRecipeForInputs(getMachineLevel(), getMachineName(), getRecipeInputs());
    }
    return cachedNextRecipe;
  }

  protected IMachineRecipe canStartNextTask(long nextSeed) {
    IMachineRecipe nextRecipe = getNextRecipe();
    if (nextRecipe == null) {
      return null; // no template
    }
    // make sure we have room for the next output
    return canInsertResult(nextSeed, nextRecipe) ? nextRecipe : null;
  }

  protected boolean canInsertResult(long nextSeed, @Nonnull IMachineRecipe nextRecipe) {
    final IPoweredTask task = createTask(nextRecipe, nextSeed);
    if (task == null) {
      return false;
    }
    ResultStack[] nextResults = task.getCompletedResult();
    List<ItemStack> outputStacks = null;

    final int numOutputSlots = slotDefinition.getNumOutputSlots();
    if (numOutputSlots > 0) {

      outputStacks = new ArrayList<ItemStack>(numOutputSlots);
      boolean allFull = true;

      for (int i = slotDefinition.minOutputSlot; i <= slotDefinition.maxOutputSlot; i++) {
        ItemStack st = inventory[i];
        if (st != null && Prep.isValid(st)) {
          st = st.copy();
          if (allFull && st.getCount() < st.getMaxStackSize()) {
            allFull = false;
          }
        } else {
          allFull = false;
        }
        outputStacks.add(st);
      }
      if (allFull) {
        return false;
      }
    }

    for (ResultStack result : nextResults) {
      if (Prep.isValid(result.item)) {
        if (outputStacks == null || mergeItemResult(result.item, outputStacks) == 0) {
          return false;
        }
      } else if (result.fluid != null) {
        if (!canInsertResultFluid(result)) {
          return false;
        }
      }
    }

    return true;
  }

  protected boolean hasInputStacks() {
    int fromSlot = slotDefinition.minInputSlot;
    for (int i = 0; i < slotDefinition.getNumInputSlots(); i++) {
      final ItemStack itemStack = inventory[fromSlot];
      if (itemStack != null && Prep.isValid(itemStack)) {
        return true;
      }
      fromSlot++;
    }
    return false;
  }

  protected boolean startNextTask(@Nonnull IMachineRecipe nextRecipe, long nextSeed) {
    if (hasPower() && nextRecipe.isRecipe(getMachineLevel(), getRecipeInputs())) {
      // then get our recipe and take away the source items
      currentTask = createTask(nextRecipe, nextSeed);
      List<MachineRecipeInput> consumed = nextRecipe.getQuantitiesConsumed(getRecipeInputs());
      for (MachineRecipeInput item : consumed) {
        if (item != null) {
          if (Prep.isValid(item.item)) {
            getStackInSlot(item.slotNumber).shrink(item.item.getCount());
          } else if (item.fluid != null) {
            drainInputFluid(item);
          }

        }
      }
      return true;
    }
    return false;
  }

  protected @Nullable IPoweredTask createTask(@Nonnull IMachineRecipe nextRecipe, long nextSeed) {
    return new PoweredTask(nextRecipe, nextSeed, getRecipeInputs());
  }

  @Override
  protected void onAfterNbtRead() {
    super.onAfterNbtRead();
    cachedNextRecipe = null;
  }

  @Override
  public void setInventorySlotContents(int slot, @Nonnull ItemStack contents) {
    super.setInventorySlotContents(slot, contents);
    if (slotDefinition.isInputSlot(slot)) {
      cachedNextRecipe = null;
      startFailed = false; // skip re-check delay, see #ticksSinceCheckedRecipe
    }
  }

  // task machines need to return a valid constant from MachineRecipeRegistry
  @Override
  public abstract @Nonnull String getMachineName();

  protected boolean shouldDoubleTick(@Nonnull IPoweredTask task, int usedEnergy) {
    return false;
  }

}
