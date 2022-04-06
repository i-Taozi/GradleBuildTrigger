package crazypants.enderio.conduits.conduit.power;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NullHelper;

import crazypants.enderio.base.Log;
import crazypants.enderio.base.conduit.ConnectionMode;
import crazypants.enderio.base.diagnostics.Prof;
import crazypants.enderio.base.power.IPowerInterface;
import crazypants.enderio.base.power.IPowerStorage;
import crazypants.enderio.conduits.conduit.power.PowerConduitNetwork.ReceptorEntry;
import crazypants.enderio.conduits.config.ConduitConfig;
import crazypants.enderio.util.MathUtil;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class NetworkPowerManager {

  private final PowerConduitNetwork network;

  private long maxEnergyStored;
  private long energyStored;

  private final @Nonnull List<ReceptorEntry> receptors = new ArrayList<PowerConduitNetwork.ReceptorEntry>();
  private @Nonnull ListIterator<ReceptorEntry> receptorIterator = NullHelper.notnullJ(receptors.listIterator(), "List.listIterator()");

  private final @Nonnull List<ReceptorEntry> storageReceptors = new ArrayList<ReceptorEntry>();

  private boolean receptorsDirty = true;

  private final @Nonnull Map<IPowerConduit, PowerTracker> powerTrackers = new HashMap<IPowerConduit, PowerTracker>();

  private final @Nonnull PowerTracker networkPowerTracker = new PowerTracker();

  private final @Nonnull CapBankSupply capSupply = new CapBankSupply();

  public NetworkPowerManager(@Nonnull PowerConduitNetwork network, @Nonnull World world) {
    this.network = network;
    maxEnergyStored = 64;
  }

  public @Nullable PowerTracker getTracker(@Nonnull IPowerConduit conduit) {
    return powerTrackers.get(conduit);
  }

  public @Nonnull PowerTracker getNetworkPowerTracker() {
    return networkPowerTracker;
  }

  public long getPowerInConduits() {
    return energyStored;
  }

  public long getMaxPowerInConduits() {
    return maxEnergyStored;
  }

  public long getPowerInCapacitorBanks() {
    return capSupply.stored;
  }

  public long getMaxPowerInCapacitorBanks() {
    return capSupply.maxCap;
  }

  public long getPowerInReceptors() {
    long result = 0;
    Set<Object> done = new HashSet<Object>();
    for (ReceptorEntry re : receptors) {
      if (!re.emmiter.getConnectionsDirty()) {
        IPowerInterface powerReceptor = re.getPowerInterface();
        if (powerReceptor != null && !done.contains(powerReceptor.getProvider())) {
          done.add(powerReceptor.getProvider());
          result += powerReceptor.getEnergyStored();
        }
      }
    }
    return result;
  }

  public long getMaxPowerInReceptors() {
    long result = 0;
    Set<Object> done = new HashSet<Object>();
    for (ReceptorEntry re : receptors) {
      if (!re.emmiter.getConnectionsDirty()) {
        IPowerInterface powerReceptor = re.getPowerInterface();
        if (powerReceptor != null && !done.contains(powerReceptor.getProvider())) {
          done.add(powerReceptor.getProvider());
          result += powerReceptor.getMaxEnergyStored();
        }
      }
    }
    return result;
  }

  private int errorSupressionA = 0;
  private int errorSupressionB = 0;

  public void applyRecievedPower(@Nullable Profiler theProfiler) {
    try {
      doApplyRecievedPower(theProfiler);
    } catch (Exception e) {
      if (errorSupressionA-- <= 0) {
        Log.warn("NetworkPowerManager: Exception thrown when updating power network " + e);
        e.printStackTrace();
        errorSupressionA = 200;
        errorSupressionB = 20;
      } else if (errorSupressionB-- <= 0) {
        Log.warn("NetworkPowerManager: Exception thrown when updating power network " + e);
        errorSupressionB = 20;
      }
    }
  }

  public void doApplyRecievedPower(@Nullable Profiler profiler) {

    trackerStartTick();

    Prof.start(profiler, "checkReceptors");
    checkReceptors();

    // Update our energy stored based on what's in our conduits
    Prof.next(profiler, "updateNetworkStorage");
    updateNetworkStorage();
    networkPowerTracker.tickStart(energyStored);

    Prof.next(profiler, "capSupplyInit");
    capSupply.init();

    int appliedCount = 0;
    int numReceptors = receptors.size();
    long available = energyStored + capSupply.canExtract;
    long wasAvailable = available;

    if (available <= 0 || (receptors.isEmpty() && storageReceptors.isEmpty())) {
      trackerEndTick();
      networkPowerTracker.tickEnd(energyStored);
      Prof.stop(profiler);
      return;
    }

    Prof.next(profiler, "sendEnergy");
    while (appliedCount < numReceptors) {

      if (!receptors.isEmpty() && !receptorIterator.hasNext()) {
        receptorIterator = NullHelper.notnullJ(receptors.listIterator(), "List.listIterator()");
      }
      ReceptorEntry r = receptorIterator.next();
      IPowerInterface pp = r.getPowerInterface();
      if (pp != null) {
        int canOffer = (int) Math.min(r.emmiter.getMaxEnergyExtracted(r.direction), available);
        Prof.start(profiler, "", pp.getProvider());
        int used = pp.receiveEnergy(canOffer, false);
        Prof.next(profiler, "trackEnergy");
        used = Math.max(0, used);
        trackerSend(r.emmiter, used, false);
        available -= used;
        Prof.stop(profiler);
        if (available <= 0) {
          break;
        }
      }
      appliedCount++;
    }

    long used = wasAvailable - available;
    // use all the capacator storage first
    energyStored -= used;

    Prof.next(profiler, "capBankUpdate");
    if (!capSupply.capBanks.isEmpty()) {
      long capBankChange = 0;
      if (energyStored < 0) {
        // not enough so get the rest from the capacitor bank
        capBankChange = energyStored;
        energyStored = 0;
      } else if (energyStored > 0) {
        // push as much as we can back to the cap banks
        capBankChange = Math.min(energyStored, capSupply.canFill);
        energyStored -= capBankChange;
      }

      if (capBankChange < 0) {
        capSupply.remove(Math.abs(capBankChange));
      } else if (capBankChange > 0) {
        energyStored += capSupply.add(capBankChange);
      }

      capSupply.balance();
    }

    Prof.next(profiler, "conduitUpdate");
    distributeStorageToConduits();

    Prof.next(profiler, "trackEnergy");
    trackerEndTick();

    networkPowerTracker.tickEnd(energyStored);
    Prof.stop(profiler);
  }

  private void trackerStartTick() {
    if (!ConduitConfig.detailedTracking.get()) {
      return;
    }
    for (IPowerConduit con : network.getConduits()) {
      if (con.hasExternalConnections()) {
        PowerTracker tracker = getOrCreateTracker(con);
        tracker.tickStart(con.getEnergyStored());
      }
    }
  }

  private void trackerSend(@Nonnull IPowerConduit con, int sent, boolean fromBank) {
    if (!fromBank) {
      networkPowerTracker.powerSent(sent);
    }
    if (!ConduitConfig.detailedTracking.get()) {
      return;
    }
    getOrCreateTracker(con).powerSent(sent);
  }

  private void trackerRecieve(@Nonnull IPowerConduit con, int recieved, boolean fromBank) {
    if (!fromBank) {
      networkPowerTracker.powerRecieved(recieved);
    }
    if (!ConduitConfig.detailedTracking.get()) {
      return;
    }
    getOrCreateTracker(con).powerRecieved(recieved);
  }

  private void trackerEndTick() {
    if (!ConduitConfig.detailedTracking.get()) {
      return;
    }
    for (IPowerConduit con : network.getConduits()) {
      if (con.hasExternalConnections()) {
        PowerTracker tracker = getOrCreateTracker(con);
        tracker.tickEnd(con.getEnergyStored());
      }
    }
  }

  private PowerTracker getOrCreateTracker(@Nonnull IPowerConduit con) {
    PowerTracker result = powerTrackers.get(con);
    if (result == null) {
      result = new PowerTracker();
      powerTrackers.put(con, result);
    }
    return result;
  }

  private void distributeStorageToConduits() {
    if (maxEnergyStored <= 0 || energyStored <= 0) {
      for (IPowerConduit con : network.getConduits()) {
        con.setEnergyStored(0);
      }
      return;
    }
    energyStored = MathUtil.clamp(energyStored, 0, maxEnergyStored);

    float filledRatio = (float) energyStored / maxEnergyStored;
    long energyLeft = energyStored;

    for (IPowerConduit con : network.getConduits()) {
      if (energyLeft > 0) {
        // NB: use ceil() to ensure we don't throw away any energy due to
        // rounding errors
        int give = (int) Math.ceil(con.getMaxEnergyStored() * filledRatio);
        give = Math.min(give, con.getMaxEnergyStored());
        give = Math.min(give, (int) Math.min(Integer.MAX_VALUE, energyLeft));
        con.setEnergyStored(give);
        energyLeft -= give;
      } else {
        con.setEnergyStored(0);
      }
    }
  }

  private void updateNetworkStorage() {
    maxEnergyStored = 0;
    energyStored = 0;
    for (IPowerConduit con : network.getConduits()) {
      maxEnergyStored += con.getMaxEnergyStored();
      energyStored += con.getEnergyStored();
    }
    energyStored = energyStored < 0 ? 0 : energyStored > maxEnergyStored ? maxEnergyStored : energyStored;
  }

  public void receptorsChanged() {
    receptorsDirty = true;
  }

  private void checkReceptors() {
    if (!receptorsDirty) {
      return;
    }
    receptors.clear();
    storageReceptors.clear();
    for (ReceptorEntry rec : network.getPowerReceptors()) {
      final IPowerInterface powerInterface = rec.getPowerInterface();
      if (powerInterface != null) {
        if (powerInterface.getProvider() instanceof IPowerStorage) {
          storageReceptors.add(rec);
        } else {
          receptors.add(rec);
        }
      } else {
        // we can ignore that connection here, but the conduit should also update and remove its external connection
        rec.emmiter.setConnectionsDirty();
      }
    }
    receptorIterator = NullHelper.notnullJ(receptors.listIterator(), "List.listIterator()");

    receptorsDirty = false;
  }

  void onNetworkDestroyed() {
  }

  private int minAbs(int amount, int limit) {
    if (amount < 0) {
      return Math.max(amount, -limit);
    } else {
      return Math.min(amount, limit);
    }
  }

  private class CapBankSupply {

    int canExtract;
    int canFill;
    final @Nonnull Set<IPowerStorage> capBanks = new HashSet<IPowerStorage>();

    double filledRatio;
    long stored = 0;
    long maxCap = 0;

    final @Nonnull List<CapBankSupplyEntry> enteries = new ArrayList<NetworkPowerManager.CapBankSupplyEntry>();

    CapBankSupply() {
    }

    void init() {
      capBanks.clear();
      enteries.clear();
      canExtract = 0;
      canFill = 0;
      stored = 0;
      maxCap = 0;

      double toBalance = 0;
      double maxToBalance = 0;

      for (ReceptorEntry rec : storageReceptors) {
        final IPowerInterface powerInterface = rec.getPowerInterface();
        if (powerInterface == null) {
          // This should be impossible for connections to capBanks. They send proper block updates when they decide to no longer have an energy capability ;)
          continue;
        }
        IPowerStorage cb = (IPowerStorage) powerInterface.getProvider();

        boolean processed = capBanks.contains(cb.getController());

        if (!processed) {
          stored += cb.getEnergyStoredL();
          maxCap += cb.getMaxEnergyStoredL();
          capBanks.add(cb.getController());
        }

        if (rec.emmiter.getConnectionMode(rec.direction) == ConnectionMode.IN_OUT) {
          toBalance += cb.getEnergyStoredL();
          maxToBalance += cb.getMaxEnergyStoredL();
        }

        long canGet = 0;
        long canFillLocal = 0;
        if (cb.isNetworkControlledIo(rec.direction.getOpposite())) {
          if (cb.isOutputEnabled(rec.direction.getOpposite())) {
            canGet = Math.min(cb.getEnergyStoredL(), cb.getMaxOutput());
            canGet = Math.min(canGet, rec.emmiter.getMaxEnergyRecieved(rec.direction));
            canExtract += (int) canGet; // already clamped to int by ^
          }

          if (cb.isInputEnabled(rec.direction.getOpposite())) {
            canFillLocal = Math.min(cb.getMaxEnergyStoredL() - cb.getEnergyStoredL(), cb.getMaxInput());
            canFillLocal = Math.min(canFillLocal, rec.emmiter.getMaxEnergyExtracted(rec.direction));
            this.canFill += (int) canFillLocal; // already clamped to int by ^
          }
          enteries.add(new CapBankSupplyEntry(cb, (int) canGet, (int) canFillLocal, rec.emmiter, rec.direction));
        }

      }

      filledRatio = 0;
      if (maxToBalance > 0) {
        filledRatio = toBalance / maxToBalance;
      }
    }

    void balance() {
      if (enteries.size() < 2) {
        return;
      }
      init();
      int canRemove = 0;
      int canAdd = 0;
      for (CapBankSupplyEntry entry : enteries) {
        if (entry.emmiter.getConnectionMode(entry.direction) == ConnectionMode.IN_OUT) {
          entry.calcToBalance(filledRatio);
          if (entry.toBalance < 0) {
            canRemove += -entry.toBalance;
          } else {
            canAdd += entry.toBalance;
          }
        }
      }

      int toalTransferAmount = Math.min(canAdd, canRemove);

      for (int i = 0; i < enteries.size() && toalTransferAmount > 0; i++) {
        CapBankSupplyEntry from = enteries.get(i);
        if (from.emmiter.getConnectionMode(from.direction) == ConnectionMode.IN_OUT) {

          int amount = from.toBalance;
          amount = minAbs(amount, toalTransferAmount);
          from.capBank.addEnergy(amount);
          toalTransferAmount -= Math.abs(amount);
          int toTranfser = Math.abs(amount);

          for (int j = i + 1; j < enteries.size() && toTranfser > 0; j++) {
            CapBankSupplyEntry to = enteries.get(j);
            if (Math.signum(amount) != Math.signum(to.toBalance)) {
              int toAmount = Math.min(toTranfser, Math.abs(to.toBalance));
              to.capBank.addEnergy(toAmount * (int) Math.signum(to.toBalance));
              toTranfser -= toAmount;
            }
          }
        }
      }

    }

    void remove(long amount) {
      if (canExtract <= 0 || amount <= 0) {
        return;
      }
      double ratio = (double) amount / canExtract;

      for (CapBankSupplyEntry entry : enteries) {
        long use = (int) Math.ceil(ratio * entry.canExtract);
        use = Math.min(use, amount);
        use = Math.min(use, entry.canExtract);
        entry.capBank.addEnergy((int) -use);
        trackerRecieve(entry.emmiter, (int) use, true);
        amount -= use;
        if (amount == 0) {
          return;
        }
      }
    }

    long add(long amount) {
      if (canFill <= 0 || amount <= 0) {
        return amount;
      }
      double ratio = (double) amount / canFill;

      for (CapBankSupplyEntry entry : enteries) {
        long add = (int) Math.ceil(ratio * entry.canFill);
        add = Math.min(add, entry.canFill);
        add = Math.min(add, amount);
        add -= entry.capBank.addEnergy((int) add);
        trackerSend(entry.emmiter, (int) add, true);
        amount -= add;
        if (amount == 0) {
          return amount;
        }
      }
      return amount;
    }

  }

  private static class CapBankSupplyEntry {

    final @Nonnull IPowerStorage capBank;
    final int canExtract;
    final int canFill;
    int toBalance;
    final @Nonnull IPowerConduit emmiter;
    final @Nonnull EnumFacing direction;

    private CapBankSupplyEntry(@Nonnull IPowerStorage capBank, int available, int canFill, @Nonnull IPowerConduit emmiter, @Nonnull EnumFacing direction) {
      this.capBank = capBank;
      this.canExtract = available;
      this.canFill = canFill;
      this.emmiter = emmiter;
      this.direction = direction;
    }

    void calcToBalance(double targetRatio) {
      if (capBank.isCreative()) {
        toBalance = 0;
        return;
      }

      long targetAmount = (long) Math.floor(capBank.getMaxEnergyStoredL() * targetRatio);
      long b = targetAmount - capBank.getEnergyStoredL();
      if (b < 0) {
        toBalance = -canExtract;
      } else {
        toBalance = canFill;
      }

    }

  }

}
