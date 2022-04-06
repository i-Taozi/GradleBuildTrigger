package crazypants.enderio.machines.config.config;

import info.loenwind.autoconfig.factory.IValue;
import info.loenwind.autoconfig.factory.IValueFactory;
import crazypants.enderio.machines.config.Config;

public final class ExperienceConfig {

  private static final int MAX = 2_000_000_000; // 0x77359400, keep some headroom to MAX_INT
  private static final int MAXIO = MAX / 2;

  public static final IValueFactory F = Config.F.section("experience");

  public static final IValue<Integer> maxIO = F.make("maxIO", 200, //
      "Millibuckets per tick that can get in or out.").setRange(1, MAXIO).sync();

}
