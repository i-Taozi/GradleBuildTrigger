package crazypants.enderio.base.integration.techreborn;

import javax.annotation.Nonnull;

import crazypants.enderio.api.farm.IFarmerJoe;
import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.Log;
import crazypants.enderio.base.events.EnderIOLifecycleEvent;
import crazypants.enderio.base.farming.FarmersRegistry;
import crazypants.enderio.base.farming.farmers.RubberTreeFarmer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = EnderIO.MODID)
public class TechRebornUtil {

  private TechRebornUtil() {
  }

  @SubscribeEvent
  public static void registerFarmers(@Nonnull RegistryEvent.Register<IFarmerJoe> event) {
    RubberTreeFarmer farmer = RubberTreeFarmerTechReborn.create();
    if (farmer != null) {
      event.getRegistry().register(farmer.setRegistryName("techreborn", "trees"));
      Log.info("Farming Station: TechReborn integration fully loaded");
    } else {
      Log.info("Farming Station: TechReborn integration not loaded");
    }
  }

  @SubscribeEvent
  public static void registerHoes(@Nonnull EnderIOLifecycleEvent.Init.Pre event) {
    FarmersRegistry.registerHoes("techreborn", "bronzehoe", "rubyhoe", "sapphirehoe", "peridothoe");
    FarmersRegistry.registerTreetaps("techreborn", "treetap");
  }

}
