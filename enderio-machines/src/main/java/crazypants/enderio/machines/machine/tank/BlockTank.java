package crazypants.enderio.machines.machine.tank;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.client.gui.IAdvancedTooltipProvider;
import com.enderio.core.client.handlers.SpecialTooltipHandler;
import com.enderio.core.common.BlockEnder;
import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.NNList.Callback;

import crazypants.enderio.api.IModObject;
import crazypants.enderio.base.config.config.BaseConfig;
import crazypants.enderio.base.lang.Lang;
import crazypants.enderio.base.machine.baselegacy.AbstractInventoryMachineBlock;
import crazypants.enderio.base.paint.IPaintable;
import crazypants.enderio.base.render.IBlockStateWrapper;
import crazypants.enderio.base.render.ICustomSubItems;
import crazypants.enderio.base.render.IHaveTESR;
import crazypants.enderio.base.render.IRenderMapper;
import crazypants.enderio.base.render.IRenderMapper.IItemRenderMapper;
import crazypants.enderio.base.render.property.EnumRenderMode;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTank extends AbstractInventoryMachineBlock<TileTank>
    implements IAdvancedTooltipProvider, IPaintable.INonSolidBlockPaintableBlock, IPaintable.IWrenchHideablePaint, IHaveTESR, ICustomSubItems {

  public static BlockTank create(@Nonnull IModObject modObject) {
    BlockTank res = new BlockTank(modObject);
    res.init();
    return res;
  }

  protected BlockTank(@Nonnull IModObject modObject) {
    super(modObject);
    setSoundType(SoundType.GLASS);
    setLightOpacity(0);
    setDefaultState(
        getBlockState().getBaseState().withProperty(EnumRenderMode.RENDER, EnumRenderMode.AUTO).withProperty(EnumTankType.KIND, EnumTankType.NORMAL));
    setShape(mkShape(BlockFaceShape.SOLID, BlockFaceShape.SOLID, BlockFaceShape.UNDEFINED));
  }

  @Override
  public BlockItemTank createBlockItem(@Nonnull IModObject modObject) {
    return modObject.apply(new BlockItemTank(this));
  }

  @Override
  protected @Nonnull BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, new IProperty[] { EnumRenderMode.RENDER, EnumTankType.KIND });
  }

  @Override
  public @Nonnull IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty(EnumTankType.KIND, EnumTankType.getType(meta));
  }

  @Override
  public int getMetaFromState(@Nonnull IBlockState state) {
    return EnumTankType.getMeta(state.getValue(EnumTankType.KIND));
  }

  @Override
  public @Nonnull IBlockState getActualState(@Nonnull IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
    return state.withProperty(EnumRenderMode.RENDER, EnumRenderMode.AUTO);
  }

  @Override
  public int damageDropped(@Nonnull IBlockState st) {
    return getMetaFromState(st);
  }

  @Override
  public @Nonnull TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
    return new TileTank(state.getValue(EnumTankType.KIND));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean shouldSideBeRendered(@Nonnull IBlockState bs, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
    return true;
  }

  @Override
  public @Nullable Container getServerGuiElement(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nullable EnumFacing facing,
      int param1, @Nonnull TileTank te) {
    return new ContainerTank(player.inventory, te);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public @Nullable GuiScreen getClientGuiElement(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nullable EnumFacing facing,
      int param1, @Nonnull TileTank te) {
    return new GuiTank(player.inventory, te);
  }

  @Override
  public boolean isOpaqueCube(@Nonnull IBlockState bs) {
    return false;
  }

  @Override
  public int getLightValue(@Nonnull IBlockState bs, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
    TileTank tank = BlockEnder.getAnyTileEntitySafe(world, pos, TileTank.class);
    if (tank != null) {
      FluidStack stack = tank.tank.getFluid();
      return stack == null || stack.amount <= 0 ? 0 : stack.getFluid().getLuminosity(stack);
    }
    return super.getLightValue(bs, world, pos);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void addCommonEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer, @Nonnull List<String> list, boolean flag) {
  }

  @Override
  public float getExplosionResistance(@Nonnull World world, @Nonnull BlockPos pos, @Nullable Entity par1Entity, @Nonnull Explosion explosion) {
    IBlockState state = world.getBlockState(pos);
    if (state.getValue(EnumTankType.KIND).isExplosionResistant()) {
      return BaseConfig.explosionResistantBlockHardness.get();
    } else {
      return super.getExplosionResistance(world, pos, par1Entity, explosion);
    }
  }

  @Override
  public boolean hasComparatorInputOverride(@Nonnull IBlockState bs) {
    return true;
  }

  @Override
  public int getComparatorInputOverride(@Nonnull IBlockState bs, @Nonnull World w, @Nonnull BlockPos pos) {
    TileTank te = getTileEntity(w, pos);
    if (te != null) {
      return te.getComparatorOutput();
    }
    return 0;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void addBasicEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer, @Nonnull List<String> list, boolean flag) {
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void addDetailedEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer, @Nonnull List<String> list, boolean flag) {
    SpecialTooltipHandler.addDetailedTooltipFromResources(list, itemstack);
    if (EnumTankType.getType(itemstack).isExplosionResistant()) {
      list.add(TextFormatting.ITALIC + Lang.BLOCK_BLAST_RESISTANT.get());
    }
  }

  @Override
  public @Nonnull String getUnlocalizedNameForTooltip(@Nonnull ItemStack stack) {
    return stack.getUnlocalizedName();
  }

  @Override
  @SideOnly(Side.CLIENT)
  public @Nonnull IItemRenderMapper getItemRenderMapper() {
    return TankItemRenderMapper.instance;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IRenderMapper.IBlockRenderMapper getBlockRenderMapper() {
    return TankItemRenderMapper.instance;
  }

  @Override
  protected void setBlockStateWrapperCache(@Nonnull IBlockStateWrapper blockStateWrapper, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
      @Nonnull TileTank tileEntity) {
    blockStateWrapper.addCacheKey(tileEntity.getFacing()).addCacheKey(blockStateWrapper.getValue(EnumTankType.KIND));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void bindTileEntitySpecialRenderer() {
    ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, new TankFluidRenderer());
  }

  @Override
  public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list) {
    NNList.of(EnumTankType.class).apply(new Callback<EnumTankType>() {
      @Override
      public void apply(@Nonnull EnumTankType e) {
        list.add(new ItemStack(BlockTank.this, 1, EnumTankType.getMeta(e)));
      }
    });
  }

  @Override
  @Nonnull
  public NNList<ItemStack> getSubItems() {
    return getSubItems(this, EnumTankType.values().length - 1);
  }

}
