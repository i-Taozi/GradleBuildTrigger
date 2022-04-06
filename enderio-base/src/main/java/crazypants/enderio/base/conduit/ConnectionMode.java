package crazypants.enderio.base.conduit;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.NullHelper;

import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.Log;

public enum ConnectionMode {

  IN_OUT("gui.conduit.io_mode.in_out"),
  INPUT("gui.conduit.io_mode.input"),
  OUTPUT("gui.conduit.io_mode.output"),
  DISABLED("gui.conduit.io_mode.disabled"),
  NOT_SET("gui.conduit.io_mode.not_set");

  private final @Nonnull String unlocalisedName;

  ConnectionMode(@Nonnull String unlocalisedName) {
    this.unlocalisedName = unlocalisedName;
  }

  public @Nonnull String getUnlocalisedName() {
    return unlocalisedName;
  }

  public static @Nonnull ConnectionMode getNext(@Nonnull ConnectionMode mode) {
    int ord = mode.ordinal() + 1;
    if (ord >= ConnectionMode.values().length) {
      ord = 0;
    }
    return NullHelper.first(ConnectionMode.values()[ord], NOT_SET);
  }

  public static @Nonnull ConnectionMode getPrevious(@Nonnull ConnectionMode mode) {
    int ord = mode.ordinal() - 1;
    if (ord < 0) {
      ord = ConnectionMode.values().length - 1;
    }
    return NullHelper.first(ConnectionMode.values()[ord], NOT_SET);
  }

  public boolean acceptsInput() {
    return this == IN_OUT || this == INPUT;
  }

  public boolean acceptsOutput() {
    return this == IN_OUT || this == OUTPUT;
  }

  public boolean isActive() {
    return this != DISABLED;
  }

  public @Nonnull String getLocalisedName() {
    return EnderIO.lang.localize(unlocalisedName);
  }

  static {
    for (ConnectionMode lang : values()) {
      if (!EnderIO.lang.canLocalize(lang.unlocalisedName)) {
        Log.error("Missing translation for '" + lang.unlocalisedName);
      }
    }
  }

}
