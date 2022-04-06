package crazypants.enderio.base.block.painted;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.NNList;

import crazypants.enderio.api.IModObject;
import crazypants.enderio.base.block.darksteel.obsidian.BlockReinforcedObsidianBase;
import crazypants.enderio.base.config.config.RecipeConfig;
import crazypants.enderio.base.init.ModObject;
import crazypants.enderio.base.paint.IPaintable;
import crazypants.enderio.base.paint.render.PaintHelper;
import crazypants.enderio.base.recipe.MachineRecipeInput;
import crazypants.enderio.base.recipe.MachineRecipeRegistry;
import crazypants.enderio.base.recipe.painter.BasicPainterTemplate;
import crazypants.enderio.base.render.IBlockStateWrapper;
import crazypants.enderio.base.render.ICustomSubItems;
import crazypants.enderio.base.render.pipeline.BlockStateWrapperBase;
import crazypants.enderio.base.render.registry.SmartModelAttacher;
import crazypants.enderio.util.Prep;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockPaintedReinforcedObsidian extends BlockReinforcedObsidianBase
    implements ITileEntityProvider, IPaintable.IBlockPaintableBlock, IModObject.WithBlockItem, ICustomSubItems {

  public static BlockPaintedReinforcedObsidian create_solid(@Nonnull IModObject modObject) {
    BlockPaintedReinforcedObsidian result = new BlockPaintedReinforcedObsidianSolid(modObject);
    result.init();
    return result;
  }

  public static BlockPaintedReinforcedObsidian create(@Nonnull IModObject modObject) {
    BlockPaintedReinforcedObsidian result = new BlockPaintedReinforcedObsidianNonSolid(modObject);
    result.init();
    return result;
  }

  public static class BlockPaintedReinforcedObsidianSolid extends BlockPaintedReinforcedObsidian implements IPaintable.ISolidBlockPaintableBlock {

    protected BlockPaintedReinforcedObsidianSolid(@Nonnull IModObject modObject) {
      super(modObject);
    }

  }

  public static class BlockPaintedReinforcedObsidianNonSolid extends BlockPaintedReinforcedObsidian implements IPaintable.INonSolidBlockPaintableBlock {

    protected BlockPaintedReinforcedObsidianNonSolid(@Nonnull IModObject modObject) {
      super(modObject);
      useNeighborBrightness = true;
      setLightOpacity(0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getAmbientOcclusionLightValue(@Nonnull IBlockState bs) {
      return 1;
    }

    @Override
    public boolean doesSideBlockRendering(@Nonnull IBlockState bs, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
      return false;
    }

  }

  protected BlockPaintedReinforcedObsidian(@Nonnull IModObject modObject) {
    super(modObject);
    Prep.setNoCreativeTab(this);
  }

  @Override
  protected void init() {
    MachineRecipeRegistry.instance.registerRecipe(MachineRecipeRegistry.PAINTER,
        new BasicPainterTemplate<BlockPaintedReinforcedObsidian>(this, ModObject.blockReinforcedObsidian.getBlockNN()) {

          @Override
          public int getEnergyRequired(@Nonnull NNList<MachineRecipeInput> inputs) {
            return (int) (super.getEnergyRequired(inputs) * RecipeConfig.energyFactorForReinforcedObsidian.get());
          }

        });
    SmartModelAttacher.registerNoProps(this);
  }

  @Override
  public BlockItemPaintedBlock createBlockItem(@Nonnull IModObject modObject) {
    return modObject.apply(new BlockItemPaintedBlock(this));
  }

  @Override
  public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
    return new TileEntityPaintedBlock();
  }

  @Override
  public @Nonnull IBlockState getExtendedState(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
    IBlockStateWrapper blockStateWrapper = new BlockStateWrapperBase(state, world, pos, null);
    blockStateWrapper.addCacheKey(0);
    blockStateWrapper.bakeModel();
    return blockStateWrapper;
  }

  @Override
  public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
    return true;
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
