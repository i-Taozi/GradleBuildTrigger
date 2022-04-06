package crazypants.enderio.machines.config.config;

import crazypants.enderio.machines.config.Config;
import info.loenwind.autoconfig.factory.IValue;
import info.loenwind.autoconfig.factory.IValueFactory;

public final class KillerJoeConfig {

  public static final IValueFactory F = Config.F.section("killerjoe");

  public static final IValue<Double> killerJoeAttackHeight = F.make("killerJoeAttackHeight", 2.0, //
      "The reach of attacks above and below Joe.").setRange(1, 32).sync();
  public static final IValue<Double> killerJoeAttackWidth = F.make("killerJoeAttackWidth", 2.0, //
      "The reach of attacks to each side of Joe.").setRange(1, 32).sync();
  public static final IValue<Double> killerJoeAttackLength = F.make("killerJoeAttackLength", 4.0, //
      "The reach of attacks in front of Joe.").setRange(1, 32).sync();
  public static final IValue<Double> killerJoeHooverXpHeight = F.make("killerJoeHooverXpHeight", 2.0, //
      "The distance from which XP will be gathered above and below Joe. (only used when killerMendingEnabled is enabled)").setRange(1, 32).sync();
  public static final IValue<Double> killerJoeHooverXpWidth = F.make("killerJoeHooverXpWidth", 5.0, //
      "The distance from which XP will be gathered to each side of Joe. (only used when killerMendingEnabled is enabled)").setRange(1, 32).sync();
  public static final IValue<Double> killerJoeHooverXpLength = F.make("killerJoeHooverXpLength", 10.0, //
      "The distance from which XP will be gathered in front of Joe. (only used when killerMendingEnabled is enabled)").setRange(1, 32).sync();
  public static final IValue<Boolean> killerJoeMustSee = F.make("killerJoeMustSee", false, //
      "Set whether the Killer Joe can attack through blocks.").sync();
  public static final IValue<Boolean> killerPvPoffDisablesSwing = F.make("killerPvPoffDisablesSwing", false, //
      "Set whether the Killer Joe swings even if PvP is off (that swing will do nothing unless killerPvPoffIsIgnored is enabled).").sync();
  public static final IValue<Boolean> killerPvPoffIsIgnored = F.make("killerPvPoffIsIgnored", false, //
      "Set whether the Killer Joe ignores PvP settings and always hits players (killerPvPoffDisablesSwing must be off for this to work).").sync();
  public static final IValue<Boolean> killerMendingEnabled = F.make("killerMendingEnabled", true, //
      "If enabled, the Killer Joe will pick up XP for the enchantement 'Mending' on the weapon.").sync();
  public static final IValue<Integer> killerJoeNutrientUsePerAttackMb = F.make("killerJoeNutrientUsePerAttackMb", 5, //
      "The number of millibuckets of nutrient fluid used per attack.").setMin(1).sync();
  public static final IValue<Boolean> killerProvokesCreeperExplosions = F.make("killerProvokesCreeperExplosions", false, //
      "If enabled, Creepers will explode for the Killer Joe just like for any player.").sync();
  public static final IValue<Boolean> killerProvokesZombieHordes = F.make("killerProvokesZombieHordes", false, //
      "If enabled, Zombies will summon help for the Killer Joe just like for any player.").sync();

}
