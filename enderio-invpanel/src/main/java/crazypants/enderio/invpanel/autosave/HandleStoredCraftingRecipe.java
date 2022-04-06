package crazypants.enderio.invpanel.autosave;

import java.lang.reflect.Type;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import crazypants.enderio.invpanel.util.StoredCraftingRecipe;
import info.loenwind.autosave.Registry;
import info.loenwind.autosave.exceptions.NoHandlerFoundException;
import info.loenwind.autosave.handlers.IHandler;
import info.loenwind.autosave.util.NBTAction;
import net.minecraft.nbt.NBTTagCompound;

public class HandleStoredCraftingRecipe implements IHandler<StoredCraftingRecipe> {

  public HandleStoredCraftingRecipe() {
  }

  @Override
  public @Nonnull Class<?> getRootType() {
    return StoredCraftingRecipe.class;
  }

  @Override
  public boolean store(@Nonnull Registry registry, @Nonnull Set<NBTAction> phase, @Nonnull NBTTagCompound nbt, @Nonnull Type type, @Nonnull String name,
      @Nonnull StoredCraftingRecipe object) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoHandlerFoundException {
    NBTTagCompound tag = new NBTTagCompound();
    object.writeToNBT(tag);
    nbt.setTag(name, tag);
    return true;
  }

  @Override
  public StoredCraftingRecipe read(@Nonnull Registry registry, @Nonnull Set<NBTAction> phase, @Nonnull NBTTagCompound nbt, @Nonnull Type type,
      @Nonnull String name, @Nullable StoredCraftingRecipe object)
      throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoHandlerFoundException {
    if (nbt.hasKey(name)) {
      StoredCraftingRecipe recipe = new StoredCraftingRecipe();
      if (recipe.readFromNBT(nbt.getCompoundTag(name))) {
        return recipe;
      }
    }
    return null;
  }
}