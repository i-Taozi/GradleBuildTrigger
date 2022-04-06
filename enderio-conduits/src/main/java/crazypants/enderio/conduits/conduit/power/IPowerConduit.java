package crazypants.enderio.conduits.conduit.power;

import javax.annotation.Nonnull;

import crazypants.enderio.base.conduit.IClientConduit;
import crazypants.enderio.base.conduit.IExtractor;
import crazypants.enderio.base.conduit.IServerConduit;
import crazypants.enderio.base.power.IPowerInterface;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;

public interface IPowerConduit extends IEnergyStorage, IExtractor, IServerConduit, IClientConduit {

  public static final @Nonnull String ICON_KEY = "blocks/power_conduit";
  public static final @Nonnull String ICON_CORE_KEY = "blocks/power_conduit_core";

  public static final String COLOR_CONTROLLER_ID = "ColorController";

  IPowerInterface getExternalPowerReceptor(@Nonnull EnumFacing direction);

  boolean getConnectionsDirty();

  void setEnergyStored(int energy);

  int getMaxEnergyRecieved(@Nonnull EnumFacing dir);

  int getMaxEnergyExtracted(@Nonnull EnumFacing dir);

  void setConnectionsDirty();
}
