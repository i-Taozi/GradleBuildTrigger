package crazypants.enderio.machines.machine.enchanter;

import java.util.List;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import com.enderio.core.client.render.ManagedTESR;
import com.enderio.core.client.render.RenderUtil;

import crazypants.enderio.base.render.property.EnumRenderMode;
import crazypants.enderio.machines.init.MachineObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static crazypants.enderio.machines.init.MachineObject.block_enchanter;

@SideOnly(Side.CLIENT)
public class EnchanterModelRenderer extends ManagedTESR<TileEnchanter> {

  public EnchanterModelRenderer() {
    super(block_enchanter.getBlock());
  }

  @Nonnull
  private static final String TEXTURE = "enderio:textures/blocks/book_stand.png";

  private EnchanterModel model = new EnchanterModel();

  @Override
  protected void renderTileEntity(@Nonnull TileEnchanter te, @Nonnull IBlockState blockState, float partialTicks, int destroyStage) {
    renderModel(te.getFacing());
  }

  @Override
  protected void renderItem() {
    renderBase();
    renderModel(EnumFacing.NORTH);
  }

  private void renderBase() {
    BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
    BlockModelShapes modelShapes = blockrendererdispatcher.getBlockModelShapes();
    IBakedModel bakedModel = modelShapes
        .getModelForState(MachineObject.block_enchanter.getBlockNN().getDefaultState().withProperty(EnumRenderMode.RENDER, EnumRenderMode.FRONT));

    RenderUtil.bindBlockTexture();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.enableRescaleNormal();
    GlStateManager.pushMatrix();

    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder vertexbuffer = tessellator.getBuffer();
    vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

    for (EnumFacing enumfacing : EnumFacing.values()) {
      this.renderQuads(vertexbuffer, bakedModel.getQuads((IBlockState) null, enumfacing, 0L));
    }

    this.renderQuads(vertexbuffer, bakedModel.getQuads((IBlockState) null, (EnumFacing) null, 0L));
    tessellator.draw();

    GlStateManager.popMatrix();
    GlStateManager.disableRescaleNormal();
  }

  @SuppressWarnings("null")
  private void renderQuads(@Nonnull BufferBuilder renderer, @Nonnull List<BakedQuad> quads) {
    for (BakedQuad quad : quads) {
      LightUtil.renderQuadColor(renderer, quad, -1);
    }
  }

  private void renderModel(EnumFacing facing) {

    GlStateManager.pushMatrix();

    GlStateManager.translate(0.5, 1.5, 0.5);
    GlStateManager.rotate(180, 1, 0, 0);

    GlStateManager.rotate(facing.getHorizontalIndex() * 90f, 0, 1, 0);

    RenderUtil.bindTexture(TEXTURE);
    model.render(0.0625F - 0.006f);

    GlStateManager.popMatrix();
  }

}
