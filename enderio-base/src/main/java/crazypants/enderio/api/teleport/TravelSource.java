package crazypants.enderio.api.teleport;

import crazypants.enderio.base.config.config.TeleportConfig;
import crazypants.enderio.base.sound.IModSound;
import crazypants.enderio.base.sound.SoundRegistry;

public enum TravelSource {

  BLOCK(SoundRegistry.TRAVEL_SOURCE_BLOCK) {
    @Override
    public int getMaxDistanceTravelled() {
      return TeleportConfig.rangeBlocks.get();
    }
  },
  STAFF(SoundRegistry.TRAVEL_SOURCE_ITEM) {
    @Override
    public int getMaxDistanceTravelled() {
      return TeleportConfig.rangeItem2Block.get();
    }

    @Override
    public float getPowerCostPerBlockTraveledRF() {
      return TeleportConfig.costItem2Block.get();
    }
  },
  STAFF_BLINK(SoundRegistry.TRAVEL_SOURCE_ITEM) {
    @Override
    public int getMaxDistanceTravelled() {
      return TeleportConfig.rangeItem2Blink.get();
    }

    @Override
    public float getPowerCostPerBlockTraveledRF() {
      return TeleportConfig.costItem2Blink.get();
    }
  },
  TELEPAD(SoundRegistry.TELEPAD);

  public static int getMaxDistanceSq() {
    int result = 0;
    for (TravelSource source : values()) {
      if (source.getMaxDistanceTravelled() > result) {
        result = source.getMaxDistanceTravelled();
      }
    }
    return result * result;
  }

  public final IModSound sound;

  private TravelSource(IModSound sound) {
    this.sound = sound;
  }

  public boolean getConserveMomentum() {
    return this == STAFF_BLINK;
  }

  public int getMaxDistanceTravelled() {
    return 0;
  }

  public int getMaxDistanceTravelledSq() {
    return getMaxDistanceTravelled() * getMaxDistanceTravelled();
  }

  public float getPowerCostPerBlockTraveledRF() {
    return 0;
  }

}