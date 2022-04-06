package crazypants.enderio.machines.config.config;

import info.loenwind.autoconfig.factory.IValue;
import info.loenwind.autoconfig.factory.IValueFactory;
import crazypants.enderio.machines.config.Config;

public final class SoulBinderConfig {

  public static final IValueFactory F = Config.F.section("soulbinder");

  public static final IValue<Integer> soulFluidInputRate = F.make("soulFluidInputRate", 50, //
      "Amount of XP fluid in mB the Soul Binder can accept per tick.").setMin(1).sync();

  public static final IValue<Integer> soulFluidOutputRate = F.make("soulFluidOutputRate", 50, //
      "Amount of XP fluid in mB that can be extracted from the Soul Binder per tick.").setMin(1).sync();

}
