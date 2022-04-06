package crazypants.enderio.machines.machine.solar;

import java.util.Random;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.gui.IResourceTooltipProvider;
import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.NullHelper;
import com.enderio.core.common.vecmath.Vector3d;

import crazypants.enderio.api.IModObject;
import crazypants.enderio.base.BlockEio;
import crazypants.enderio.base.config.config.PersonalConfig;
import crazypants.enderio.base.render.IBlockStateWrapper;
import crazypants.enderio.base.render.ICustomSubItems;
import crazypants.enderio.base.render.IRenderMapper.IItemRenderMapper;
import crazypants.enderio.base.render.ISmartRenderAwareBlock;
import crazypants.enderio.base.render.pipeline.BlockStateWrapperBase;
import crazypants.enderio.base.render.property.EnumMergingBlockRenderMode;
import crazypants.enderio.base.render.registry.SmartModelAttacher;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSolarPanel extends BlockEio<TileSolarPanel> implements IResourceTooltipProvider, ISmartRenderAwareBlock, ICustomSubItems {

  public static BlockSolarPanel create(@Nonnull IModObject modObject) {
    BlockSolarPanel result = new BlockSolarPanel(modObject);
    result.init();
    return result;
  }

  private static final float BLOCK_HEIGHT = 2.5f / 16f;

  private BlockSolarPanel(@Nonnull IModObject modObject) {
    super(modObject);
    setLightOpacity(255);
    useNeighborBrightness = true;
    setDefaultState(getBlockState().getBaseState().withProperty(EnumMergingBlockRenderMode.RENDER, EnumMergingBlockRenderMode.AUTO).withProperty(SolarType.KIND,
        SolarType.SIMPLE));
    setShape(mkShape(BlockFaceShape.SOLID, BlockFaceShape.UNDEFINED, BlockFaceShape.UNDEFINED));
  }

  @Override
  public BlockItemSolarPanel createBlockItem(@Nonnull IModObject modObject) {
    return modObject.apply(new BlockItemSolarPanel(this));
  }

  @Override
  protected void init() {
    super.init();
    SmartModelAttacher.register(this, EnumMergingBlockRenderMode.RENDER, EnumMergingBlockRenderMode.DEFAULTS, EnumMergingBlockRenderMode.AUTO);
  }

  @Override
  protected @Nonnull BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, new IProperty[] { EnumMergingBlockRenderMode.RENDER, SolarType.KIND });
  }

  @Override
  public @Nonnull IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty(SolarType.KIND, ISolarType.getTypeFromMeta(meta));
  }

  @Override
  public int getMetaFromState(@Nonnull IBlockState state) {
    return ISolarType.getMetaFromType(state.getValue(SolarType.KIND));
  }

  @Override
  public @Nonnull IBlockState getActualState(@Nonnull IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
    return state.withProperty(EnumMergingBlockRenderMode.RENDER, EnumMergingBlockRenderMode.AUTO);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public @Nonnull IBlockState getExtendedState(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
    SolarBlockRenderMapper renderMapper = new SolarBlockRenderMapper(state, world, pos);
    IBlockStateWrapper blockStateWrapper = new BlockStateWrapperBase(state, world, pos, renderMapper);
    blockStateWrapper.addCacheKey(state.getValue(SolarType.KIND));
    blockStateWrapper.addCacheKey(renderMapper);
    blockStateWrapper.bakeModel();
    return blockStateWrapper;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public @Nonnull IItemRenderMapper getItemRenderMapper() {
    return SolarItemRenderMapper.instance;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public @Nonnull BlockRenderLayer getBlockLayer() {
    return BlockRenderLayer.SOLID;
  }

  @Override
  public int damageDropped(@Nonnull IBlockState bs) {
    return getMetaFromState(bs);
  }

  @Override
  public @Nonnull AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
    return new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, BLOCK_HEIGHT, 1.0F);
  }

  @Override
  public @Nonnull String getUnlocalizedNameForTooltip(@Nonnull ItemStack itemStack) {
    return getUnlocalizedName();
  }

  @Override
  public boolean isOpaqueCube(@Nonnull IBlockState bs) {
    return false;
  }

  @Override
  public boolean isFullCube(@Nonnull IBlockState bs) {
    return false;
  }

  @Override
  public boolean doesSideBlockRendering(@Nonnull IBlockState bs, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
    return face == EnumFacing.DOWN;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list) {
    ISolarType.KIND.getOrderedValues().stream()
        .forEach(solarType -> list.add(new ItemStack(BlockSolarPanel.this, 1, ISolarType.getMetaFromType(NullHelper.notnull(solarType, "solarType")))));
  }

  @Override
  @Nonnull
  public NNList<ItemStack> getSubItems() {
    return getSubItems(this, ISolarType.KIND.getOrderedValues().size() - 1);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void randomDisplayTick(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Random rand) {
    if (PersonalConfig.machineParticlesEnabled.get() && state.getValue(SolarType.KIND).hasParticles() && TileSolarPanel.isPowered(world, pos)
        && TileSolarPanel.calculateLocalLightRatio(world, pos, TileSolarPanel.calculateLightRatio(world)) / 3 > rand.nextFloat()) {
      double d0 = pos.getX() + 0.5D + (Math.random() - 0.5D) * 0.5D;
      double d1 = pos.getY() + BLOCK_HEIGHT;
      double d2 = pos.getZ() + 0.5D + (Math.random() - 0.5D) * 0.5D;
      Vector3d color = state.getValue(SolarType.KIND).getParticleColor();
      world.spawnParticle(EnumParticleTypes.REDSTONE, d0, d1, d2, color.x, color.y, color.z);
    }
  }

}
