package crazypants.enderio.base.block.painted;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.NullHelper;

import crazypants.enderio.api.IModObject;
import crazypants.enderio.base.init.ModObject;
import crazypants.enderio.base.paint.IPaintable;
import crazypants.enderio.base.paint.PaintUtil;
import crazypants.enderio.base.paint.render.PaintHelper;
import crazypants.enderio.base.paint.render.PaintRegistry;
import crazypants.enderio.base.paint.render.UVLock;
import crazypants.enderio.base.recipe.MachineRecipeRegistry;
import crazypants.enderio.base.recipe.painter.BasicPainterTemplate;
import crazypants.enderio.base.render.IBlockStateWrapper;
import crazypants.enderio.base.render.ICacheKey;
import crazypants.enderio.base.render.ICustomSubItems;
import crazypants.enderio.base.render.IRenderMapper;
import crazypants.enderio.base.render.ISmartRenderAwareBlock;
import crazypants.enderio.base.render.pipeline.BlockStateWrapperBase;
import crazypants.enderio.base.render.property.EnumRenderPart;
import crazypants.enderio.base.render.registry.SmartModelAttacher;
import crazypants.enderio.base.render.util.QuadCollector;
import crazypants.enderio.util.Prep;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockPaintedStairs extends BlockStairs implements ITileEntityProvider, IPaintable.ITexturePaintableBlock, ISmartRenderAwareBlock,
    IRenderMapper.IBlockRenderMapper.IRenderLayerAware, IRenderMapper.IItemRenderMapper.IItemModelMapper, IModObject.WithBlockItem, ICustomSubItems {

  public static BlockPaintedStairs create(@Nonnull IModObject modObject) {
    BlockPaintedStairs woodStairs = new BlockPaintedStairs(modObject, Blocks.OAK_STAIRS);
    woodStairs.init(modObject);
    MachineRecipeRegistry.instance.registerRecipe(MachineRecipeRegistry.PAINTER, new BasicPainterTemplate<BlockPaintedStairs>(woodStairs, Blocks.OAK_STAIRS,
        Blocks.ACACIA_STAIRS, Blocks.SPRUCE_STAIRS, Blocks.BIRCH_STAIRS, Blocks.JUNGLE_STAIRS, Blocks.DARK_OAK_STAIRS));

    return woodStairs;
  }

  public static BlockPaintedStairs create_stone(@Nonnull IModObject modObject) {
    BlockPaintedStairs stoneStairs = new BlockPaintedStairs(modObject, Blocks.STONE_STAIRS);
    stoneStairs.init(modObject);
    MachineRecipeRegistry.instance.registerRecipe(MachineRecipeRegistry.PAINTER,
        new BasicPainterTemplate<BlockPaintedStairs>(stoneStairs, Blocks.STONE_STAIRS, Blocks.BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS,
            Blocks.NETHER_BRICK_STAIRS, Blocks.SANDSTONE_STAIRS, Blocks.QUARTZ_STAIRS, Blocks.RED_SANDSTONE_STAIRS, Blocks.PURPUR_STAIRS));

    return stoneStairs;
  }

  public BlockPaintedStairs(@Nonnull IModObject modObject, Block model) {
    super(model.getDefaultState());
    if (!(model instanceof BlockStairs)) {
      throw new RuntimeException();
    }
    Prep.setNoCreativeTab(this);
    useNeighborBrightness = true;
    modObject.apply(this);
  }

  private void init(@Nonnull IModObject modObject) {
    SmartModelAttacher.registerNoProps(this);
    PaintRegistry.registerModel("stairs", new ResourceLocation("minecraft", "block/oak_stairs"), PaintRegistry.PaintMode.ALL_TEXTURES);
    PaintRegistry.registerModel("outer_stairs", new ResourceLocation("minecraft", "block/oak_outer_stairs"), PaintRegistry.PaintMode.ALL_TEXTURES);
    PaintRegistry.registerModel("inner_stairs", new ResourceLocation("minecraft", "block/oak_inner_stairs"), PaintRegistry.PaintMode.ALL_TEXTURES);
  }

  @Override
  public Item createBlockItem(@Nonnull IModObject modObject) {
    return modObject.apply(new BlockItemPaintedBlock(this));
  }

  @Override
  public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
    return new TileEntityPaintedBlock();
  }

  @Override
  public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase player,
      @Nonnull ItemStack stack) {
    setPaintSource(state, world, pos, PaintUtil.getSourceBlock(stack));
    if (!world.isRemote) {
      world.notifyBlockUpdate(pos, state, state, 3);
    }
  }

  @Override
  public boolean removedByPlayer(@Nonnull IBlockState bs, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
    if (willHarvest) {
      return true;
    }
    return super.removedByPlayer(bs, world, pos, player, willHarvest);
  }

  @Override
  public void harvestBlock(@Nonnull World worldIn, @Nonnull EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable TileEntity te,
      @Nonnull ItemStack stack) {
    super.harvestBlock(worldIn, player, pos, state, te, stack);
    super.removedByPlayer(state, worldIn, pos, player, true);
  }

  @Override
  public void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
    NNList<ItemStack> drops2 = new NNList<>();
    super.getDrops(drops2, world, pos, state, fortune);
    for (ItemStack drop : drops2) {
      PaintUtil.setSourceBlock(NullHelper.notnullM(drop, "null stack from getDrops()"), getPaintSource(state, world, pos));
    }
    drops.addAll(drops2);
  }

  @Override
  public @Nonnull ItemStack getPickBlock(@Nonnull IBlockState bs, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos,
      @Nonnull EntityPlayer player) {
    final ItemStack pickBlock = super.getPickBlock(bs, target, world, pos, player);
    PaintUtil.setSourceBlock(pickBlock, getPaintSource(bs, world, pos));
    return pickBlock;
  }

  @Override
  public @Nonnull IBlockState getExtendedState(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
    IBlockStateWrapper blockStateWrapper = new BlockStateWrapperBase(state, world, pos, this);
    blockStateWrapper.addCacheKey(getPaintSource(state, world, pos)).addCacheKey(state.getValue(HALF)).addCacheKey(state.getValue(FACING))
        .addCacheKey(state.getValue(SHAPE));
    blockStateWrapper.bakeModel();
    return blockStateWrapper;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public @Nonnull IItemRenderMapper getItemRenderMapper() {
    return this;
  }

  @SideOnly(Side.CLIENT)
  private IBakedModel mapRender(IBlockState state, @Nullable IBlockState paint) {

    final boolean top = state.getValue(HALF) == EnumHalf.TOP;
    int xRot = top ? 180 : 0;
    int yRot = top ? 90 : 0;

    switch (state.getValue(FACING)) {
    case EAST:
      yRot += 0;
      break;
    case SOUTH:
      yRot += 90;
      break;
    case WEST:
      yRot += 180;
      break;
    case NORTH:
      yRot += 270;
      break;
    default:
      return null;
    }

    String model;
    switch (state.getValue(SHAPE)) {
    case STRAIGHT:
      model = "stairs";
      yRot += top ? -90 : 0;
      break;
    case OUTER_RIGHT:
      model = "outer_stairs";
      break;
    case OUTER_LEFT:
      model = "outer_stairs";
      yRot += -90;
      break;
    case INNER_RIGHT:
      model = "inner_stairs";
      break;
    case INNER_LEFT:
      model = "inner_stairs";
      yRot += -90;
      break;
    default:
      return null;
    }

    return PaintRegistry.getModel(IBakedModel.class, model, paint, new UVLock(ModelRotation.getModelRotation(xRot, yRot)));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public @Nonnull ICacheKey getCacheKey(@Nonnull Block block, @Nonnull ItemStack stack, @Nonnull ICacheKey cacheKey) {
    return cacheKey.addCacheKey(getPaintSource(block, stack));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public List<IBakedModel> mapItemRender(@Nonnull Block block, @Nonnull ItemStack stack) {
    IBlockState paintSource = getPaintSource(block, stack);
    IBlockState stdOverlay = ModObject.block_machine_base.getBlockNN().getDefaultState().withProperty(EnumRenderPart.SUB, EnumRenderPart.PAINT_OVERLAY);
    IBakedModel model1 = PaintRegistry.getModel(IBakedModel.class, "stairs", paintSource, new UVLock(null));
    IBakedModel model2 = PaintRegistry.getModel(IBakedModel.class, "stairs", stdOverlay, PaintRegistry.OVERLAY_TRANSFORMATION4);
    List<IBakedModel> list = new ArrayList<IBakedModel>();
    list.add(model1);
    list.add(model2);
    return list;
  }

  @Override
  public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
    return true;
  }

  @Override
  public int getFlammability(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
    IBlockState bs = world.getBlockState(pos);
    return bs.getMaterial() == Material.WOOD ? 20 : super.getFlammability(world, pos, face);
  }

  @Override
  public int getFireSpreadSpeed(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
    IBlockState bs = world.getBlockState(pos);
    return bs.getMaterial() == Material.WOOD ? 5 : super.getFireSpreadSpeed(world, pos, face);
  }

  @Override
  public boolean doesSideBlockRendering(@Nonnull IBlockState bs, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
    return false;
  }

  @Deprecated
  @Override
  public boolean shouldSideBeRendered(@Nonnull IBlockState bs, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
    // disabled because it fails once the player uses a wrench to rotate the block
    // if (worldIn.getBlockState(pos.offset(side)).getBlock() instanceof BlockPaintedStairs
    // && getPaintSource(null, worldIn, pos) == getPaintSource(null, worldIn, pos)) {
    // return false;
    // }
    return super.shouldSideBeRendered(bs, worldIn, pos, side);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list) {
    // Painted blocks don't show in the Creative Inventory or JEI
  }

  @Override
  @Nonnull
  public NNList<ItemStack> getSubItems() {
    return getSubItems(this, 0);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public List<IBlockState> mapBlockRender(@Nonnull IBlockStateWrapper state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, BlockRenderLayer blockLayer,
      @Nonnull QuadCollector quadCollector) {
    IBlockState paintSource = getPaintSource(state, world, pos);
    if (blockLayer == null || PaintUtil.canRenderInLayer(paintSource, blockLayer)) {
      quadCollector.addFriendlybakedModel(blockLayer, mapRender(state, paintSource), paintSource, MathHelper.getPositionRandom(pos));
    }
    return null;
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
