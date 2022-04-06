package crazypants.enderio.powertools.machine.capbank.network;

import javax.annotation.Nonnull;

import crazypants.enderio.base.conduit.IConduitBundle;
import crazypants.enderio.base.machine.modes.IoMode;
import crazypants.enderio.base.power.IPowerInterface;
import crazypants.enderio.conduits.conduit.power.IPowerConduit;
import crazypants.enderio.powertools.machine.capbank.TileCapBank;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class EnergyReceptor {

  private final @Nonnull IPowerInterface receptor;
  private final @Nonnull EnumFacing fromDir;
  private final @Nonnull IoMode mode;
  private final BlockPos location;

  private final IPowerConduit conduit;

  public EnergyReceptor(@Nonnull TileCapBank cb, @Nonnull IPowerInterface receptor, @Nonnull EnumFacing dir) {
    this.receptor = receptor;
    fromDir = dir;
    mode = cb.getIoMode(dir);
    if (receptor.getProvider() instanceof IConduitBundle) {
      conduit = ((IConduitBundle) receptor.getProvider()).getConduit(IPowerConduit.class);
    } else {
      conduit = null;
    }
    location = cb.getLocation();
  }

  public IPowerConduit getConduit() {
    return conduit;
  }

  public @Nonnull IPowerInterface getReceptor() {
    return receptor;
  }

  public @Nonnull EnumFacing getDir() {
    return fromDir;
  }

  public @Nonnull IoMode getMode() {
    return mode;
  }

  // NB: Special impl of equals and hash code based solely on the location and dir
  // This is done to ensure the receptors in the Networks Set are added / removed correctly

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + fromDir.hashCode();
    result = prime * result + ((location == null) ? 0 : location.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    EnergyReceptor other = (EnergyReceptor) obj;
    if (fromDir != other.fromDir) {
      return false;
    }
    if (location == null) {
      if (other.location != null) {
        return false;
      }
    } else if (!location.equals(other.location)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "EnergyReceptor [receptor=" + receptor + ", fromDir=" + fromDir + ", mode=" + mode + ", conduit=" + conduit + "]";
  }

}
