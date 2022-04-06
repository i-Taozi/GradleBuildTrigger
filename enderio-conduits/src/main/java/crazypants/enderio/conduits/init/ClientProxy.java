package crazypants.enderio.conduits.init;

import javax.annotation.Nonnull;

import crazypants.enderio.conduits.EnderIOConduits;
import crazypants.enderio.conduits.conduit.ConduitBundleStateMapper;
import crazypants.enderio.conduits.render.ConduitBundleRenderManager;
import crazypants.enderio.powertools.machine.capbank.network.ClientNetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid = EnderIOConduits.MODID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

  @SubscribeEvent
  public static void onModelRegistryEvent(@Nonnull ModelRegistryEvent event) {
    ConduitBundleStateMapper.create();
  }

  @Override
  public void init(@Nonnull FMLPreInitializationEvent event) {
    super.init(event);
    ConduitBundleRenderManager.instance.init(event);
  }

  @Override
  public void init(@Nonnull FMLInitializationEvent event) {
    super.init(event);
    MinecraftForge.EVENT_BUS.register(ClientNetworkManager.getInstance());
  }

  @Override
  public void init(@Nonnull FMLPostInitializationEvent event) {
    super.init(event);
  }
  
  @Override
  public float getPartialTicks() {
    return Minecraft.getMinecraft().getRenderPartialTicks();
  }

}
