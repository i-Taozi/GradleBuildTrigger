package crazypants.enderio.base.paint;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.ItemUtil;

import crazypants.enderio.base.config.config.RecipeConfig;
import crazypants.enderio.base.recipe.IRecipeInput;
import crazypants.enderio.base.recipe.RecipeInput;
import crazypants.enderio.util.Prep;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.item.ItemStack;

public class PaintSourceValidator {

  public static final PaintSourceValidator instance = new PaintSourceValidator();

  private final List<RecipeInput> whitelist = new ArrayList<RecipeInput>();
  private final List<RecipeInput> blacklist = new ArrayList<RecipeInput>();

  public boolean isValidSourceDefault(@Nonnull ItemStack paintSource) {
    if (Prep.isInvalid(paintSource)) {
      return false;
    }
    Block block = PaintUtil.getBlockFromItem(paintSource);
    if (block == null) {
      return false;
    }
    if (isBlacklisted(paintSource)) {
      return false;
    }
    if (isWhitelisted(paintSource)) {
      return true;
    }
    if (!RecipeConfig.allowTileEntitiesAsPaintSource.get() && block instanceof ITileEntityProvider) {
      return false;
    }
    return true;
  }

  public boolean isWhitelisted(@Nonnull ItemStack paintSource) {
    return isInList(paintSource, whitelist);
  }

  public boolean isBlacklisted(@Nonnull ItemStack paintSource) {
    return isInList(paintSource, blacklist);
  }

  public void addToWhitelist(@Nonnull ItemStack input) {
    addToWhitelist(new RecipeInput(input, true));
  }

  public void addToWhitelist(RecipeInput input) {
    whitelist.add(input);
  }

  public void addToBlacklist(@Nonnull ItemStack input) {
    addToBlacklist(new RecipeInput(input, true));
  }

  public void addToBlacklist(RecipeInput input) {
    blacklist.add(input);
  }

  public void removeFromWhitelist(IRecipeInput input) {
    removeFromList(input, whitelist);
  }

  public void removeFromBlackList(IRecipeInput input) {
    removeFromList(input, blacklist);
  }

  protected boolean isInList(@Nonnull ItemStack paintSource, List<RecipeInput> list) {
    if (Prep.isInvalid(paintSource)) {
      return false;
    }
    for (IRecipeInput ri : list) {
      if (ri != null && ri.isInput(paintSource)) {
        return true;
      }
    }
    return false;
  }

  protected void removeFromList(IRecipeInput input, List<RecipeInput> list) {
    ItemStack inStack = input.getInput();
    if (Prep.isInvalid(inStack)) {
      return;
    }
    IRecipeInput toRemove = null;
    for (IRecipeInput in : list) {
      if (ItemUtil.areStacksEqual(inStack, in.getInput())) {
        toRemove = in;
        break;
      }
    }
    if (toRemove != null) {
      list.remove(toRemove);
    }
  }

  public void loadConfig() {
    PaintSourceParser.loadConfig();
  }

}
