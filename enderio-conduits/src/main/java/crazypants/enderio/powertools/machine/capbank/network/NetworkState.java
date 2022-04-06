package crazypants.enderio.powertools.machine.capbank.network;

import javax.annotation.Nonnull;

import crazypants.enderio.base.machine.modes.RedstoneControlMode;
import io.netty.buffer.ByteBuf;

public class NetworkState {

  private final long energyStored;
  private final long maxEnergyStored;
  private final int maxIO;
  private final int maxInput;
  private final int maxOutput;
  private final @Nonnull RedstoneControlMode inputMode;
  private final @Nonnull RedstoneControlMode outputMode;
  private final float averageInput;
  private final float averageOutput;

  public NetworkState(long energyStored, long maxEnergyStored, int maxIO, int maxInput, int maxOutput, @Nonnull RedstoneControlMode inputMode,
      @Nonnull RedstoneControlMode outputMode, float averageInput, float averageOutput) {
    this.energyStored = energyStored;
    this.maxEnergyStored = maxEnergyStored;
    this.maxIO = maxIO;
    this.maxInput = maxInput;
    this.maxOutput = maxOutput;
    this.inputMode = inputMode;
    this.outputMode = outputMode;
    this.averageInput = averageInput;
    this.averageOutput = averageOutput;
  }

  public NetworkState(ICapBankNetwork network) {
    energyStored = network.getEnergyStoredL();
    maxEnergyStored = network.getMaxEnergyStoredL();
    maxIO = network.getMaxIO();
    maxInput = network.getMaxInput();
    maxOutput = network.getMaxOutput();
    inputMode = network.getInputControlMode();
    outputMode = network.getOutputControlMode();
    averageInput = network.getAverageInputPerTick();
    averageOutput = network.getAverageOutputPerTick();
  }

  public long getEnergyStored() {
    return energyStored;
  }

  public long getMaxEnergyStored() {
    return maxEnergyStored;
  }

  public int getMaxOutput() {
    return maxOutput;
  }

  public int getMaxInput() {
    return maxInput;
  }

  public int getMaxIO() {
    return maxIO;
  }

  public @Nonnull RedstoneControlMode getInputMode() {
    return inputMode;
  }

  public @Nonnull RedstoneControlMode getOutputMode() {
    return outputMode;
  }

  public float getAverageInput() {
    return averageInput;
  }

  public float getAverageOutput() {
    return averageOutput;
  }

  public void writeToBuf(@Nonnull ByteBuf buf) {
    buf.writeLong(energyStored);
    buf.writeLong(maxEnergyStored);
    buf.writeInt(maxIO);
    buf.writeInt(maxInput);
    buf.writeInt(maxOutput);
    buf.writeShort(inputMode.ordinal());
    buf.writeShort(outputMode.ordinal());
    buf.writeFloat(averageInput);
    buf.writeFloat(averageOutput);
  }

  @SuppressWarnings("null")
  public static NetworkState readFromBuf(@Nonnull ByteBuf buf) {
    return new NetworkState(buf.readLong(), buf.readLong(), buf.readInt(), buf.readInt(), buf.readInt(), RedstoneControlMode.values()[buf.readShort()],
        RedstoneControlMode.values()[buf.readShort()], buf.readFloat(), buf.readFloat());
  }

  @Override
  public String toString() {
    return "NetworkClientState [energyStored=" + energyStored + ", maxEnergyStored=" + maxEnergyStored + ", maxIO=" + maxIO + ", maxInput=" + maxInput
        + ", maxOutput=" + maxOutput + "]";
  }

}
