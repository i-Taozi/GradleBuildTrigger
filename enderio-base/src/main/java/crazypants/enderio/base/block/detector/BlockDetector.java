package crazypants.enderio.base.block.detector;

import java.util.Map;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.NNList;

import crazypants.enderio.api.IModObject;
import crazypants.enderio.base.BlockEio;
import crazypants.enderio.base.EnderIOTab;
import crazypants.enderio.base.block.painted.BlockItemPaintedBlock;
import crazypants.enderio.base.block.painted.TileEntityPaintedBlock;
import crazypants.enderio.base.paint.IPaintable;
import crazypants.enderio.base.paint.render.PaintHelper;
import crazypants.enderio.base.render.IBlockStateWrapper;
import crazypants.enderio.base.render.IHaveRenderers;
import crazypants.enderio.base.render.pipeline.BlockStateWrapperRelay;
import crazypants.enderio.base.render.registry.SmartModelAttacher;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDetector extends BlockEio<TileEntityPaintedBlock> implements IPaintable.ISolidBlockPaintableBlock, IHaveRenderers {

  public static final @Nonnull PropertyBool IS_ON = PropertyBool.create("on");
  public static final @Nonnull PropertyDirection FACING = PropertyDirection.create("facing");

  public static BlockDetector create(@Nonnull IModObject modObject) {
    BlockDetector result = new BlockDetector(modObject, false);
    result.init();
    return result;
  }

  public static BlockDetector createSilent(@Nonnull IModObject modObject) {
    BlockDetector result = new BlockDetector(modObject, true);
    result.init();
    return result;
  }

  private final boolean silent;

  protected BlockDetector(@Nonnull IModObject modObject, boolean silent) {
    super(modObject);
    this.silent = silent;
    setCreativeTab(EnderIOTab.tabEnderIOMachines);
    initDefaultState();
    setShape(mkShape(BlockFaceShape.SOLID));
  }

  @Override
  protected void init() {
    super.init();
    SmartModelAttacher.registerNoProps(this);
  }

  protected void initDefaultState() {
    setDefaultState(getBlockState().getBaseState());
  }

  @Override
  protected @Nonnull BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, new IProperty[] { IS_ON, FACING });
  }

  @Override
  public @Nonnull IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty(IS_ON, (meta & 0x08) != 0).withProperty(FACING, NNList.FACING.get(meta & 0x7));
  }

  @Override
  public int getMetaFromState(@Nonnull IBlockState state) {
    return (state.getValue(IS_ON) ? 8 : 0) + state.getValue(FACING).ordinal();
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerRenderers(@Nonnull IModObject modObject) {
    Item item = Item.getItemFromBlock(this);
    Map<IBlockState, ModelResourceLocation> locations = new DefaultStateMapper().putStateModelLocations(this);
    IBlockState state = getDefaultState().withProperty(IS_ON, true).withProperty(FACING, EnumFacing.UP);
    ModelResourceLocation mrl = locations.get(state);
    if (mrl != null) {
      ModelLoader.setCustomModelResourceLocation(item, 0, mrl);
    }
  }

  @Override
  public @Nonnull IBlockState withRotation(@Nonnull IBlockState state, @Nonnull Rotation rot) {
    return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
  }

  @Override
  public @Nonnull IBlockState withMirror(@Nonnull IBlockState state, @Nonnull Mirror mirrorIn) {
    return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
  }

  @Override
  public @Nonnull IBlockState getExtendedState(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
    IBlockStateWrapper blockStateWrapper = new BlockStateWrapperRelay(state, world, pos);
    blockStateWrapper.bakeModel();
    return blockStateWrapper;
  }

  @Override
  public BlockItemPaintedBlock createBlockItem(@Nonnull IModObject modObject) {
    return modObject.apply(new BlockItemPaintedBlock(this));
  }

  @Override
  public int getWeakPower(@Nonnull IBlockState state, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
    return side.getOpposite() != state.getValue(FACING) && state.getValue(IS_ON) ? 15 : 0;
  }

  @Override
  public boolean canProvidePower(@Nonnull IBlockState state) {
    return true;
  }

  protected void playClickSound(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
    if (state.getValue(IS_ON)) {
      worldIn.playSound((EntityPlayer) null, pos, SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
    } else {
      worldIn.playSound((EntityPlayer) null, pos, SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
    }
  }

  @Override
  public void neighborChanged(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
    IBlockState newState = state.withProperty(IS_ON, isTargetBlockAir(state, world, pos));
    if (newState != state) {
      world.setBlockState(pos, newState);
      if (!silent) {
        playClickSound(world, pos, newState);
      }
    }
  }

  protected boolean isTargetBlockAir(IBlockState state, World world, BlockPos pos) {
    return world.isAirBlock(pos.offset(state.getValue(FACING)));
  }

  @Override
  public @Nonnull IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ,
      int meta, @Nonnull EntityLivingBase placer, @Nonnull EnumHand hand) {
    final IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(FACING, facing);
    return state.withProperty(IS_ON, isTargetBlockAir(state, world, pos));
  }

  @Override
  public @Nonnull BlockRenderLayer getBlockLayer() {
    return BlockRenderLayer.SOLID;
  }

  @Override
  public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
    return true;
  }

  @Override
  public boolean isSideSolid(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
    return state.getValue(FACING) == side.getOpposite();
  }

  @SideOnly(Side.CLIENT)
  @Override
  public boolean addHitEffects(@Nonnull IBlockState state, @Nonnull World world, @Nonnull RayTraceResult target, @Nonnull ParticleManager effectRenderer) {
    return PaintHelper.addHitEffects(state, world, target, effectRenderer);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public boolean addDestroyEffects(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull ParticleManager effectRenderer) {
    return PaintHelper.addDestroyEffects(world, pos, effectRenderer);
  }

}
