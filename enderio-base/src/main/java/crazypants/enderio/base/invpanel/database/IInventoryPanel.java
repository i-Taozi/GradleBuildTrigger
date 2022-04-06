package crazypants.enderio.base.invpanel.database;

import javax.annotation.Nonnull;

public interface IInventoryPanel {

  float getAvailablePower();

  void refuelPower(@Nonnull IInventoryDatabaseServer db);

  float getPowerLevel();

  boolean usePower(float amount);

  void addPower(float amount);

}
