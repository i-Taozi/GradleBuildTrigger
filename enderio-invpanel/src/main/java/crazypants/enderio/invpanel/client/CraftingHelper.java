package crazypants.enderio.invpanel.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.ItemUtil;
import com.enderio.core.common.util.NNList;

import crazypants.enderio.base.network.PacketHandler;
import crazypants.enderio.invpanel.invpanel.GuiInventoryPanel;
import crazypants.enderio.invpanel.invpanel.InventoryPanelContainer;
import crazypants.enderio.invpanel.network.PacketFetchItem;
import crazypants.enderio.invpanel.util.StoredCraftingRecipe;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class CraftingHelper {
  final @Nonnull NNList<ItemStack>[] ingredients;

  public CraftingHelper(@Nonnull NNList<ItemStack>[] ingredients) {
    this.ingredients = ingredients;
  }

  @Nonnull
  public static CraftingHelper createFromRecipe(StoredCraftingRecipe recipe) {
    NNList<ItemStack>[] ingredients = new NNList[9];
    for (int idx = 0; idx < 9; idx++) {
      ItemStack stack = recipe.get(idx);
      if(!stack.isEmpty()) {
        ingredients[idx] = new NNList<ItemStack>(stack);
      }
    }
    return new CraftingHelper(ingredients);
  }

  @Nullable
  public static CraftingHelper createFromSlots(@Nonnull List<Slot> slots) {
    if (slots.size() != 9) {
      return null;
    }
    NNList<ItemStack>[] ingredients = new NNList[9];
    int count = 0;
    for (int idx = 0; idx < 9; idx++) {
      Slot slot = slots.get(idx);
      ItemStack stack = slot.getStack();
      if(!stack.isEmpty()) {
        stack = stack.copy();
        stack.setCount(1);
        ingredients[idx] = new NNList<ItemStack>(stack);
        count++;
      }
    }
    if (count > 0) {
      return new CraftingHelper(ingredients);
    }
    return null;
  }

  public void install() {
  }

  public void remove() {
  }

  public void refill(@Nonnull GuiInventoryPanel gui, int amount) {
    InventoryPanelContainer container = gui.getContainer();
    refill(container, amount);
  }

  public void refill(@Nonnull InventoryPanelContainer container, int amount) {
    InventoryDatabaseClient db = container.getTe().getDatabaseClient();
    if (db == null) {
      return;
    }
    List<Slot> craftingGrid = container.getCraftingGridSlots();
    int slotsToProcess = (1 << 9) - 1;
    boolean madeProgress;
    int maxAmount = 64;
    do {
      Candidate[] candidates = new Candidate[9];
      for (int idx = 0; idx < 9; idx++) {
        if ((slotsToProcess & (1 << idx)) != 0) {
          NNList<ItemStack> pstack = ingredients[idx];
          Slot slot = craftingGrid.get(idx);
          ItemStack stack = slot.getStack();
          if (pstack == null) {
            if (!stack.isEmpty()) {
              return;
            }
          } else {
            Candidate candidate;
            if (!stack.isEmpty()) {
              if (!isStackCompatible(pstack, stack)) {
                return;
              }
              candidate = findCandidates(stack, container, db, candidates);
            } else {
              candidate = findAllCandidates(pstack, container, db, candidates);
            }
            if (candidate == null) {
              return;
            }
            candidate.used++;
            candidates[idx] = candidate;
          }
        }
      }
      int targetAmount = maxAmount;
      int currentAmount = 0;
      for (int idx = 0; idx < 9; idx++) {
        Candidate candidate = candidates[idx];
        if (candidate != null) {
          Slot slot = craftingGrid.get(idx);
          int current = getSlotStackSize(slot);
          int maxStackSize = candidate.stack.getMaxStackSize();
          currentAmount = Math.max(currentAmount, current);
          if (candidate.stack.isStackable() && maxStackSize > 1) {
            targetAmount = Math.min(targetAmount, current + Math.min(maxStackSize, candidate.getAvailable()));
          }
        }
      }
      targetAmount = Math.min(targetAmount, currentAmount + amount);
      madeProgress = false;
      for (int idx = 0; idx < 9; idx++) {
        final int mask = 1 << idx;
        Candidate candidate = candidates[idx];
        if (candidate != null) {
          Slot slot = craftingGrid.get(idx);
          for (Slot srcSlot : candidate.sourceSlots) {
            int current = getSlotStackSize(slot);
            if (current >= targetAmount) {
              break;
            }
            if (container.moveItems(srcSlot.slotNumber, slot.slotNumber, slot.slotNumber + 1, targetAmount - current)) {
              slotsToProcess &= ~mask;
              madeProgress = true;
            }
          }
          int current = getSlotStackSize(slot);
          if (candidate.entry != null) {
            if (current < targetAmount) {
              int toMove = Math.min(candidate.entry.getCount(), targetAmount - current);
              PacketHandler.INSTANCE.sendToServer(new PacketFetchItem(db.getGeneration(), candidate.entry, slot.slotNumber, toMove));
              slotsToProcess &= ~mask;
              current += toMove;
            }
          }
          if (current > 0) {
            maxAmount = Math.min(maxAmount, current);
          }
        }
      }
    } while (madeProgress && slotsToProcess != 0);
  }

  private static int getSlotStackSize(@Nonnull Slot slot) {
    ItemStack stack = slot.getStack();
    return stack.getCount();
  }

  private static boolean isStackCompatible(@Nonnull NNList<ItemStack> pstack, @Nonnull ItemStack stack) {
    for (ItemStack istack : pstack) {
      if (ItemUtil.areStackMergable(stack, istack)) {
        return true;
      }
    }
    return false;
  }
  @Nullable
  private Candidate findAllCandidates(@Nonnull NNList<ItemStack> pstack, @Nonnull InventoryPanelContainer container, @Nonnull InventoryDatabaseClient db, @Nonnull Candidate[] candidates) {
    Candidate bestInventory = null;
    Candidate bestNetwork = null;
    for (ItemStack istack : pstack) {
      Candidate candidate = findCandidates(istack, container, db, candidates);
      if (candidate.available > 0) {
        if (bestInventory == null || bestInventory.available < candidate.available) {
          bestInventory = candidate;
        }
      }
      if (candidate.entry != null) {
        if (bestNetwork == null || bestNetwork.entry.getCount() < candidate.entry.getCount()) {
          bestNetwork = candidate;
        }
      }
    }
    if (bestInventory != null) {
      return bestInventory;
    } else {
      return bestNetwork;
    }
  }

  @Nonnull
  private Candidate findCandidates(@Nonnull ItemStack stack, @Nonnull InventoryPanelContainer container, @Nonnull InventoryDatabaseClient db, @Nonnull Candidate[] candidates) {
    for (Candidate candidate : candidates) {
      if (candidate != null && ItemUtil.areStackMergable(candidate.stack, stack)) {
        return candidate;
      }
    }
    Candidate candidate = new Candidate(stack);
    if(container.getTe().isExtractionDisabled()) {
      findCandidates(candidate, stack, container.getReturnAreaSlots());
    }
    findCandidates(candidate, stack, container.getPlayerInventorySlots());
    findCandidates(candidate, stack, container.getPlayerHotbarSlots());
    if (candidate.available == 0 && db != null) {
      candidate.entry = db.lookupItem(stack, null, false);
      if (candidate.entry != null && candidate.entry.getCount() <= 0) {
        candidate.entry = null;
      }
    }
    return candidate;
  }

  private void findCandidates(@Nonnull Candidate candidates, @Nonnull ItemStack stack, @Nonnull Collection<Slot> slots) {
    for (Slot slot : slots) {
      ItemStack slotStack = slot.getStack();
      if (ItemUtil.areStackMergable(slotStack, stack)) {
        candidates.sourceSlots.add(slot);
        candidates.available += slotStack.getCount();
      }
    }
  }

  static class Candidate {
    final @Nonnull ItemStack stack;
    final ArrayList<Slot> sourceSlots = new ArrayList<Slot>();
    ItemEntry entry;
    int available;
    int used;

    public Candidate(@Nonnull ItemStack stack) {
      this.stack = stack;
    }

    public int getAvailable() {
      int avail = available;
      if(entry != null) {
        avail += entry.getCount();
      }
      if(avail > 0 && used > 1) {
        avail = Math.max(1, avail / used);
      }
      return avail;
    }
  }
}
