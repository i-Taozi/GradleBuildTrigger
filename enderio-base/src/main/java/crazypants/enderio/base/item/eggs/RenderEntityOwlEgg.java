package crazypants.enderio.base.item.eggs;

import javax.annotation.Nonnull;

import crazypants.enderio.base.init.ModObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEntityOwlEgg extends RenderSnowball<EntityOwlEgg> {

  public static final Factory FACTORY = new Factory();

  public RenderEntityOwlEgg(RenderManager renderManagerIn, RenderItem itemRendererIn) {
    super(renderManagerIn, ModObject.item_owl_egg.getItemNN(), itemRendererIn);
  }

  @Override
  public void doRender(@Nonnull EntityOwlEgg entity, double x, double y, double z, float entityYaw, float partialTicks) {
    super.doRender(entity, x, y, z, entityYaw, partialTicks);
  }

  @Override
  public @Nonnull ItemStack getStackToRender(@Nonnull EntityOwlEgg entityIn) {
    return new ItemStack(ModObject.item_owl_egg.getItemNN());
  }

  public static class Factory implements IRenderFactory<EntityOwlEgg> {

    @Override
    public Render<? super EntityOwlEgg> createRenderFor(RenderManager manager) {
      return new RenderEntityOwlEgg(manager, Minecraft.getMinecraft().getRenderItem());
    }
  }
}
