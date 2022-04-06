package crazypants.enderio.base.machine.modes;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.NNList;

import crazypants.enderio.base.EnderIO;

public enum IoMode {

  NONE("gui.machine.ioMode.none"),
  PULL("gui.machine.ioMode.pull"),
  PUSH("gui.machine.ioMode.push"),
  PUSH_PULL("gui.machine.ioMode.pullPush"),
  DISABLED("gui.machine.ioMode.disabled");

  private final @Nonnull String unlocalisedName;

  IoMode(@Nonnull String unlocalisedName) {
    this.unlocalisedName = unlocalisedName;
  }

  public @Nonnull String getUnlocalisedName() {
    return unlocalisedName;
  }

  public boolean pulls() {
    return this == PULL || this == PUSH_PULL;
  }

  public boolean pushes() {
    return this == PUSH || this == PUSH_PULL;
  }

  public boolean canOutput() {
    return pushes() || this == NONE;
  }

  public boolean canRecieveInput() {
    return pulls() || this == NONE;
  }

  public boolean canInputOrOutput() {
    return this != DISABLED;
  }

  public @Nonnull String getLocalisedName() {
    return EnderIO.lang.localize(unlocalisedName);
  }

  public @Nonnull String colorLocalisedName() {
    return EnderIO.lang.localize(getColorerUnlocalizedName());
  }

  public @Nonnull String getColorerUnlocalizedName() {
    return this.getUnlocalisedName() + ".colored";
  }

  public @Nonnull IoMode next() {
    return NNList.of(IoMode.class).next(this);
  }

  public @Nonnull IoMode prev() {
    return NNList.of(IoMode.class).prev(this);
  }

}
