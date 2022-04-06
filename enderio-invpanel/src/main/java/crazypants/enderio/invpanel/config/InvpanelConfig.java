package crazypants.enderio.invpanel.config;

import java.util.ArrayList;
import java.util.List;

import com.enderio.core.common.util.NullHelper;

import crazypants.enderio.invpanel.remote.ItemRemoteInvAccessType;
import info.loenwind.autoconfig.factory.IValue;
import info.loenwind.autoconfig.factory.IValueFactory;

public final class InvpanelConfig {

  public static final IValueFactory F = Config.F.section("invpanel");

  public static final IValue<Boolean> inventoryPanelFree = F.make("inventoryPanelFree", false,
      "If true, the inv panel will not accept fluids and will be active permanently.").sync();

  public static final IValue<Float> inventoryPanelPowerPerMB = F.make("inventoryPanelPowerPerMB", 800F,
      "Internal power generated per mB. The default of 800/mB matches the RF generation of the Zombie generator. "
          + "A panel tries to refill only once every second - setting this value too low slows down the scanning speed.")
      .sync().setMin(1);

  public static final IValue<Float> inventoryPanelScanCostPerSlot = F.make("inventoryPanelScanCostPerSlot", 0.1F, "Internal power used for scanning a slot")
      .sync().setMin(0);

  public static final IValue<Float> inventoryPanelExtractCostPerItem = F.make("inventoryPanelExtractCostPerItem", 12F,
      "Internal power used per item extracted (not a stack of items)").sync().setMin(0);

  public static final IValue<Float> inventoryPanelExtractCostPerOperation = F.make("inventoryPanelExtractCostPerOperation", 32F,
      "Internal power used per extract operation (independent of stack size)").sync().setMin(0);

  public static final IValue<Boolean> inventoryPanelScaleText = F.make("inventoryPanelScaleText", true,
      "If true stack sizes will be drawn at a smaller size with a little more detail.");

  public static final List<IValue<Integer>> remoteInventoryMBPerOpen = new ArrayList<>();
  public static final List<IValue<Integer>> remoteInventoryRFPerTick = new ArrayList<>();
  public static final List<IValue<Integer>> remoteInventoryMBCapacity = new ArrayList<>();
  public static final List<IValue<Integer>> remoteInventoryRFCapacity = new ArrayList<>();
  public static final List<IValue<String>> remoteInventoryFluidTypes = new ArrayList<>();

  private static final int[] DEF_MB_OPEN = { 100, 25, 15 };
  private static final int[] DEF_RF_TICK = { 4, 6, 8 };
  private static final int[] DEF_MB_CAP = { 2000, 1000, 1500 };
  private static final int[] DEF_RF_CAP = { 60000, 120000, 150000 };
  private static final String[] DEF_FLUID = { "nutrient_distillation", "ender_distillation", "vapor_of_levity" };

  static {
    for (ItemRemoteInvAccessType type : ItemRemoteInvAccessType.values()) {
      int i = type.ordinal();
      remoteInventoryMBPerOpen.add(F.make("remoteInventoryMBPerOpenTier" + i, DEF_MB_OPEN[i], "MB required to open the panel").sync().setMin(0));

      remoteInventoryRFPerTick.add(F.make("remoteInventoryRFPerTickTier" + i, DEF_RF_TICK[i], "RF used per tick when the panel is open").sync().setMin(0));

      remoteInventoryMBCapacity.add(F.make("remoteInventoryMBCapacityTier" + i, DEF_MB_CAP[i], "Capacity of the internal tank in MB").sync().setMin(0));

      remoteInventoryRFCapacity.add(F.make("remoteInventoryRFCapacityTier" + i, DEF_RF_CAP[i], "Capacity of the internal energy storage").sync().setMin(0));

      remoteInventoryFluidTypes
          .add(F.make("remoteInventoryFluidTypesTier" + i, NullHelper.notnull(DEF_FLUID[i], "DEF_FLUID"), "The type of fluid required").sync());
    }
  }

  public static final IValue<Boolean> respectsGravity = F.make("respectsGravity", true,
      "If true, the inv panel will respect gravity and fall like an anvil when not attached to a block.").sync();

}
