package crazypants.enderio.machines.machine.solar;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.client.gui.IAdvancedTooltipProvider;
import com.enderio.core.api.client.gui.IResourceTooltipProvider;
import com.enderio.core.client.handlers.SpecialTooltipHandler;

import crazypants.enderio.base.EnderIOTab;
import crazypants.enderio.base.ItemEIO;
import crazypants.enderio.base.lang.LangPower;
import crazypants.enderio.machines.lang.Lang;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockItemSolarPanel extends ItemEIO implements IAdvancedTooltipProvider, IResourceTooltipProvider {

  public BlockItemSolarPanel(@Nonnull BlockSolarPanel blockSolarPanel) {
    super(blockSolarPanel);
    setHasSubtypes(true);
    setMaxDamage(0);
    setCreativeTab(EnderIOTab.tabEnderIOMachines);
  }

  @Override
  public @Nonnull String getUnlocalizedName(@Nonnull ItemStack par1ItemStack) {
    int meta = par1ItemStack.getMetadata();
    ISolarType type = ISolarType.getTypeFromMeta(meta);
    return super.getUnlocalizedName(par1ItemStack) + type.getUnlocalisedName();
  }

  @Override
  public int getMetadata(int damage) {
    return damage;
  }

  @Override
  public void addCommonEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer, @Nonnull List<String> list, boolean flag) {
    SpecialTooltipHandler.addCommonTooltipFromResources(list, itemstack);
  }

  @Override
  public void addBasicEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer, @Nonnull List<String> list, boolean flag) {
  }

  @Override
  public void addDetailedEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer, @Nonnull List<String> list, boolean flag) {
    SpecialTooltipHandler.addDetailedTooltipFromResources(list, itemstack);
    int prod = ISolarType.getTypeFromMeta(itemstack.getMetadata()).getRfperTick();
    list.add(Lang.SOLAR_MAXOUTPUT.get(LangPower.RFt(prod)));
  }

  @Override
  public @Nonnull String getUnlocalizedNameForTooltip(@Nonnull ItemStack itemStack) {
    return super.getUnlocalizedName(itemStack);
  }

  @Override
  public @Nonnull EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumHand hand,
      @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
    if (facing.getAxis() == Axis.Y && worldIn.getBlockState(pos).getBlock() == this.block) {
      // prevent panels being placed above/below existing panels
      return EnumActionResult.FAIL;
    }
    return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
  }

}
