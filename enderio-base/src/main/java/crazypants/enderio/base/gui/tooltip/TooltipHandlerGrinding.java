package crazypants.enderio.base.gui.tooltip;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.client.handlers.SpecialTooltipHandler;
import com.enderio.core.client.handlers.SpecialTooltipHandler.ITooltipCallback;

import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.events.EnderIOLifecycleEvent;
import crazypants.enderio.base.lang.Lang;
import crazypants.enderio.base.lang.LangPower;
import crazypants.enderio.base.recipe.sagmill.IGrindingMultiplier;
import crazypants.enderio.base.recipe.sagmill.SagMillRecipeManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber(modid = EnderIO.MODID, value = Side.CLIENT)
public class TooltipHandlerGrinding implements ITooltipCallback {

  @SubscribeEvent
  public static void init(EnderIOLifecycleEvent.PreInit event) {
    SpecialTooltipHandler.addCallback(new TooltipHandlerGrinding());
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void addCommonEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer, @Nonnull List<String> list, boolean flag) {

  }

  @Override
  @SideOnly(Side.CLIENT)
  public void addBasicEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer, @Nonnull List<String> list, boolean flag) {
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void addDetailedEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer, @Nonnull List<String> list, boolean flag) {
    addEntries(itemstack, list, null);
  }

  public void addEntries(@Nonnull ItemStack itemstack, @Nonnull List<String> list, @Nullable String withGrindingMultiplier) {
    IGrindingMultiplier ball = SagMillRecipeManager.getInstance().getGrindballFromStack(itemstack);
    list.add(Lang.GRINDING_BALL_1.get(TextFormatting.BLUE));
    if (withGrindingMultiplier == null) {
      list.add(Lang.GRINDING_BALL_2.get(TextFormatting.GRAY, LangPower.toPercent(ball.getGrindingMultiplier())));
    } else {
      list.add(Lang.GRINDING_BALL_2.get(TextFormatting.GRAY, withGrindingMultiplier));
    }
    list.add(Lang.GRINDING_BALL_3.get(TextFormatting.GRAY, LangPower.toPercent(ball.getChanceMultiplier())));
    list.add(Lang.GRINDING_BALL_4.get(TextFormatting.GRAY, LangPower.toPercent(ball.getPowerMultiplier())));
  }

  @Override
  public boolean shouldHandleItem(@Nonnull ItemStack item) {
    return SagMillRecipeManager.getInstance().getGrindballFromStack(item) != null;
  }

}