package crazypants.enderio.base.loot;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.NullHelper;

import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.config.config.PersonalConfig;
import crazypants.enderio.base.config.config.PersonalConfig.LootConfig;
import crazypants.enderio.base.events.EnderIOLifecycleEvent;
import crazypants.enderio.base.fluid.Fluids;
import crazypants.enderio.base.init.ModObject;
import crazypants.enderio.base.material.alloy.Alloy;
import crazypants.enderio.base.material.material.Material;
import crazypants.enderio.util.CapturedMob;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetDamage;
import net.minecraft.world.storage.loot.functions.SetMetadata;
import net.minecraft.world.storage.loot.functions.SetNBT;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static crazypants.enderio.base.init.ModObject.itemBasicCapacitor;
import static crazypants.enderio.base.init.ModObject.itemConduitProbe;
import static crazypants.enderio.base.init.ModObject.itemTravelStaff;

@EventBusSubscriber(modid = EnderIO.MODID)
public class LootManager {

  // Note: Testing code is on the capacitor item. Right-click a chest in creative mode to fill it with loot. Edit the code to select which loot table to use.

  private static final @Nonnull String ERROR_UNREGISTERED_ITEM = "found unregistered item";

  private static final @Nonnull String RL_MINECRAFT = "minecraft";

  private static final @Nonnull LootCondition[] NO_CONDITIONS = new LootCondition[0];

  public static void init(@Nonnull EnderIOLifecycleEvent.Init.Post event) {
    if (PersonalConfig.lootGeneration.get() == LootConfig.VANILLA) {
      for (ResourceLocation resourceLocation : MC_TABLES) {
        LootTableList.register(eio(resourceLocation));
      }
    }
  }

  // to re-create the json tables from the code below, set PersonalConfig.lootGeneration to DEVELOPMENT then run the game with
  // https://minecraft.curseforge.com/projects/loottweaker and dump the tables with "/mt loottables all". Open the generated json files and remove the vanilla
  // entries. Note that some tables may already be out of sync with this code...

  private static final @Nonnull Set<ResourceLocation> MC_TABLES = new HashSet<>();
  static {
    MC_TABLES.add(LootTableList.CHESTS_SIMPLE_DUNGEON);
    MC_TABLES.add(LootTableList.CHESTS_ABANDONED_MINESHAFT);
    MC_TABLES.add(LootTableList.CHESTS_NETHER_BRIDGE);
    MC_TABLES.add(LootTableList.CHESTS_IGLOO_CHEST);
    MC_TABLES.add(LootTableList.CHESTS_JUNGLE_TEMPLE_DISPENSER);
    MC_TABLES.add(LootTableList.CHESTS_VILLAGE_BLACKSMITH);
    MC_TABLES.add(LootTableList.CHESTS_DESERT_PYRAMID);
    MC_TABLES.add(LootTableList.CHESTS_JUNGLE_TEMPLE);
    MC_TABLES.add(LootTableList.CHESTS_WOODLAND_MANSION);
    MC_TABLES.add(LootTableList.CHESTS_END_CITY_TREASURE);
  }

  private static void injectTables(@Nonnull LootTableLoadEvent evt) {
    if (MC_TABLES.contains(evt.getName())) {
      LootPool lp = new LootPool(new LootEntry[0], NO_CONDITIONS, new RandomValueRange(1, 3), new RandomValueRange(0, 0), EnderIO.MOD_NAME);
      addTable(lp, eio(evt.getName()));
      evt.getTable().addPool(lp);
    }
  }

  @SubscribeEvent
  public static void onLootTableLoad(@Nonnull LootTableLoadEvent evt) {

    switch (PersonalConfig.lootGeneration.get()) {
    case VANILLA:
      injectTables(evt); // fallthrough on purpose
    case DISABLED:
      return;
    case DEVELOPMENT:
    default:
      break;
    }

    LootTable table = evt.getTable();

    LootPool lp = new LootPool(new LootEntry[0], NO_CONDITIONS, new RandomValueRange(1, 3), new RandomValueRange(0, 0), EnderIO.MOD_NAME);

    if (evt.getName().equals(LootTableList.CHESTS_SIMPLE_DUNGEON)) {

      lp.addEntry(createLootEntry(Alloy.DARK_STEEL.getStackIngot(), 1, 3, 0.25F));
      lp.addEntry(createLootEntry(itemConduitProbe.getItemNN(), 0.10F));
      lp.addEntry(createLootEntry(Items.QUARTZ, 3, 16, 0.25F));
      lp.addEntry(createLootEntry(Items.NETHER_WART, 1, 4, 0.20F));
      lp.addEntry(createLootEntry(Items.ENDER_PEARL, 1, 2, 0.30F));
      lp.addEntry(createDarkSteelLootEntry(ModObject.itemDarkSteelSword.getItemNN(), 0.1F));
      lp.addEntry(createDarkSteelLootEntry(ModObject.itemDarkSteelBoots.getItemNN(), 0.1F));
      lp.addEntry(createLootEntry(Material.GEAR_WOOD.getStack(), 1, 2, 0.5F));
      lp.addEntry(createLootCapacitor(0.15F));
      lp.addEntry(createLootCapacitor(0.15F));
      lp.addEntry(createLootCapacitor(0.15F));

    } else if (evt.getName().equals(LootTableList.CHESTS_ABANDONED_MINESHAFT)) {

      lp.addEntry(createLootEntry(Alloy.DARK_STEEL.getStackIngot(), 1, 3, 0.05F));
      lp.addEntry(createLootEntry(Items.ENDER_PEARL, 1, 2, 0.10F));
      lp.addEntry(createDarkSteelLootEntry(ModObject.itemDarkSteelSword.getItemNN(), 0.2F));
      lp.addEntry(createLootEntry(Material.GEAR_WOOD.getStack(), 1, 2, 0.5F));
      lp.addEntry(createLootCapacitor(0.15F));
      lp.addEntry(createLootCapacitor(0.05F));
      lp.addEntry(createLootEntry(ModObject.blockExitRail.getItemNN(), 1, 2, 0.15F));

    } else if (evt.getName().equals(LootTableList.CHESTS_NETHER_BRIDGE)) {

      lp.addEntry(createDarkSteelLootEntry(ModObject.itemDarkSteelBoots.getItemNN(), 0.1F));
      lp.addEntry(createLootEntry(Material.GEAR_IRON.getStack(), 1, 2, 0.5F));
      lp.addEntry(createLootCapacitor(0.15F));

    } else if (evt.getName().equals(LootTableList.CHESTS_IGLOO_CHEST)) {

      final CapturedMob polarBear = CapturedMob.create(new ResourceLocation(RL_MINECRAFT, "polar_bear"));
      if (polarBear != null) {
        lp.addEntry(
            new LootEntryItem(ModObject.itemSoulVial.getItemNN(), 1, 1, new LootFunction[] { setCount(1, 1), new SetNBT(NO_CONDITIONS, polarBear.toNbt(null)) },
                new LootCondition[] { new RandomChance(.2F) }, "PolarBearSoulVial"));
      }
      lp.addEntry(createLootEntry(ModObject.itemSoulVial.getItemNN(), 1, 3, 0.5F));
      lp.addEntry(createLootCapacitor(0.05F));

    } else if (evt.getName().equals(LootTableList.CHESTS_JUNGLE_TEMPLE_DISPENSER)) {

      ItemStack bucket = Fluids.FIRE_WATER.getBucket();
      lp.addEntry(new LootEntryItem(bucket.getItem(), 1, 1, new LootFunction[] { setCount(1, 1), setMetadata(bucket.getMetadata()), setNBT(bucket) },
          new LootCondition[] { new RandomChance(.05F) }, bucket.getItem().getUnlocalizedName() + ":" + bucket.getMetadata()));

    } else if (evt.getName().equals(LootTableList.CHESTS_VILLAGE_BLACKSMITH)) {

      lp.addEntry(createLootEntry(Alloy.ELECTRICAL_STEEL.getStackIngot(), 2, 6, 0.20F));
      lp.addEntry(createLootEntry(Alloy.REDSTONE_ALLOY.getStackIngot(), 3, 6, 0.35F));
      lp.addEntry(createLootEntry(Alloy.DARK_STEEL.getStackIngot(), 3, 6, 0.35F));
      lp.addEntry(createLootEntry(Alloy.PULSATING_IRON.getStackIngot(), 1, 2, 0.3F));
      lp.addEntry(createLootEntry(Alloy.VIBRANT_ALLOY.getStackIngot(), 1, 2, 0.2F));
      lp.addEntry(createLootEntry(Material.GEAR_WOOD.getStack(), 1, 2, 0.5F));
      lp.addEntry(createLootEntry(Material.GEAR_STONE.getStack(), 1, 2, 0.4F));
      lp.addEntry(createLootEntry(Material.GEAR_IRON.getStack(), 1, 2, 0.25F));
      lp.addEntry(createLootEntry(Material.GEAR_ENERGIZED.getStack(), 1, 2, 0.125F));
      lp.addEntry(createLootEntry(Material.GEAR_VIBRANT.getStack(), 1, 2, 0.0625F));
      lp.addEntry(createDarkSteelLootEntry(ModObject.itemDarkSteelSword.getItemNN(), 1, 1, 0.25F));
      lp.addEntry(createDarkSteelLootEntry(ModObject.itemDarkSteelBoots.getItemNN(), 1, 1, 0.25F));
      lp.addEntry(createLootCapacitor(0.1F));

    } else if (evt.getName().equals(LootTableList.CHESTS_DESERT_PYRAMID)) {

      lp.addEntry(createDarkSteelLootEntry(ModObject.itemDarkSteelSword.getItemNN(), 0.2F));
      lp.addEntry(createLootEntry(Material.GEAR_VIBRANT.getStack(), 1, 2, 0.0625F));
      lp.addEntry(createLootEntry(itemTravelStaff.getItemNN(), 0.1F));
      lp.addEntry(createLootCapacitor(.5f));

    } else if (evt.getName().equals(LootTableList.CHESTS_JUNGLE_TEMPLE)) {

      lp.addEntry(createDarkSteelLootEntry(ModObject.itemDarkSteelSword.getItemNN(), 1, 1, 0.25F));
      lp.addEntry(createLootEntry(itemTravelStaff.getItemNN(), 1, 1, 0.1F));
      lp.addEntry(createLootCapacitor(0.25F));
      lp.addEntry(createLootCapacitor(0.25F));

    } else if (evt.getName().equals(LootTableList.CHESTS_WOODLAND_MANSION)) {

      lp.addEntry(createDarkSteelLootEntry(ModObject.itemDarkSteelBow.getItemNN(), 1, 1, 0.25F));
      lp.addEntry(createDarkSteelLootEntry(ModObject.itemDarkSteelAxe.getItemNN(), 1, 1, 0.25F));
      lp.addEntry(createLootEntry(Material.GEAR_STONE.getStack(), 1, 2, 0.4F));
      lp.addEntry(createLootCapacitor(0.25F));
      lp.addEntry(createLootEntry(itemTravelStaff.getItemNN(), 1, 1, 0.1F));

    } else if (evt.getName().equals(LootTableList.CHESTS_END_CITY_TREASURE)) {

      final CapturedMob shulker = CapturedMob.create(new ResourceLocation(RL_MINECRAFT, "shulker"));
      if (shulker != null) {
        lp.addEntry(
            new LootEntryItem(ModObject.itemSoulVial.getItemNN(), 1, 1, new LootFunction[] { setCount(1, 1), new SetNBT(NO_CONDITIONS, shulker.toNbt(null)) },
                new LootCondition[] { new RandomChance(.2F) }, "ShulkerSoulVial"));
      }
      lp.addEntry(createLootEntry(ModObject.itemSoulVial.getItemNN(), 1, 3, 0.5F));
      lp.addEntry(createLootEntry(Material.GEAR_ENERGIZED.getStack(), 1, 2, 0.125F));
      lp.addEntry(createLootEntry(Material.GEAR_VIBRANT.getStack(), 1, 2, 0.125F));
      lp.addEntry(createLootCapacitor(0.05F));
      lp.addEntry(createDarkSteelLootEntry(ModObject.itemDarkSteelBow.getItemNN(), 1, 1, 0.25F));

    } else {
      return;
    }

    if (table.isFrozen()) {
      throw new RuntimeException("Some other mod (a list of suspects is printed in the log file) put a frozen loot table into the load event for loot table '"
          + evt.getName() + "'. This is a bug in that other mod. Ender IO is the victim here. Don't blame the victim!");
    }

    table.addPool(lp);
  }

  private static void addTable(@Nonnull LootPool pool, @Nonnull ResourceLocation resourceLocation) {
    pool.addEntry(new LootEntryTable(resourceLocation, 1, 1, NO_CONDITIONS, resourceLocation.toString()));
  }

  private @Nonnull static LootEntry createLootEntry(@Nonnull Item item, float chance) {
    return createLootEntry(item, 1, 1, chance);
  }

  private @Nonnull static LootEntry createLootEntry(@Nonnull Item item, int minSize, int maxSize, float chance) {
    return createLootEntry(item, 0, minSize, maxSize, chance);
  }

  /*
   * All loot entries are given the same weight, the generation probabilities depend on the RandomChance condition.
   */
  private @Nonnull static LootEntry createLootEntry(@Nonnull Item item, int meta, int minStackSize, int maxStackSize, float chance) {
    LootCondition[] chanceCond = new LootCondition[] { new RandomChance(chance) };
    final ResourceLocation registryName = NullHelper.notnull(item.getRegistryName(), ERROR_UNREGISTERED_ITEM);
    if (item.isDamageable()) {
      return new LootEntryItem(item, 1, 1, new LootFunction[] { setCount(minStackSize, maxStackSize), setDamage(item, meta), setEnergy() }, chanceCond,
          registryName.toString() + ":" + meta);
    } else {
      return new LootEntryItem(item, 1, 1, new LootFunction[] { setCount(minStackSize, maxStackSize), setMetadata(meta) }, chanceCond,
          registryName.toString() + ":" + meta);
    }
  }

  private @Nonnull static LootEntry createLootEntry(@Nonnull ItemStack stack, int minStackSize, int maxStackSize, float chance) {
    LootCondition[] chanceCond = new LootCondition[] { new RandomChance(chance) };
    final ResourceLocation registryName = NullHelper.notnull(stack.getItem().getRegistryName(), ERROR_UNREGISTERED_ITEM);
    return new LootEntryItem(stack.getItem(), 1, 1, new LootFunction[] { setCount(minStackSize, maxStackSize), setMetadata(stack.getMetadata()) }, chanceCond,
        registryName.toString() + ":" + stack.getMetadata());
  }

  private @Nonnull static LootEntry createDarkSteelLootEntry(@Nonnull Item item, float chance) {
    return createDarkSteelLootEntry(item, 1, 1, chance);
  }

  private @Nonnull static LootEntry createDarkSteelLootEntry(@Nonnull Item item, int minSize, int maxSize, float chance) {
    return createDarkSteelLootEntry(item, 0, minSize, maxSize, chance);
  }

  private @Nonnull static LootEntry createDarkSteelLootEntry(@Nonnull Item item, int meta, int minStackSize, int maxStackSize, float chance) {
    LootCondition[] chanceCond = new LootCondition[] { new RandomChance(chance) };
    final ResourceLocation registryName = NullHelper.notnull(item.getRegistryName(), ERROR_UNREGISTERED_ITEM);
    return new LootEntryItem(item, 1, 1, new LootFunction[] { setCount(minStackSize, maxStackSize), setDamage(item, meta), setUpgrades(), setEnergy() },
        chanceCond, registryName.toString() + ":" + meta);
  }

  static int capCount = 0; // Each loot entry in a pool must have a unique name

  private @Nonnull static LootEntry createLootCapacitor(float chance) {
    capCount++;
    return new LootEntryItem(itemBasicCapacitor.getItemNN(), 1, 1, new LootFunction[] { ls, setMetadata(3, 4) },
        new LootCondition[] { new RandomChance(chance) }, itemBasicCapacitor.getUnlocalisedName() + capCount);
  }

  private @Nonnull static SetCount setCount(int min, int max) {
    return new SetCount(NO_CONDITIONS, new RandomValueRange(min, min));
  }

  private @Nonnull static SetDamage setDamage(Item item, int damage) {
    return new SetDamage(NO_CONDITIONS, new RandomValueRange(damage > 0 ? damage : 1, damage > 0 ? damage : item.getMaxDamage()));
  }

  private @Nonnull static SetMetadata setMetadata(int metaMin, int metaMax) {
    return new SetMetadata(NO_CONDITIONS, new RandomValueRange(metaMin, metaMax));
  }

  private @Nonnull static SetMetadata setMetadata(int meta) {
    return new SetMetadata(NO_CONDITIONS, new RandomValueRange(meta));
  }

  private @Nonnull static SetRandomEnergy setEnergy() {
    return new SetRandomEnergy(NO_CONDITIONS);
  }

  private @Nonnull static SetRandomDarkUpgrade setUpgrades() {
    return new SetRandomDarkUpgrade(NO_CONDITIONS);
  }

  private @Nonnull static SetNBT setNBT(ItemStack stack) {
    return new SetNBT(NO_CONDITIONS, NullHelper.first(stack.getTagCompound(), new NBTTagCompound()));
  }

  private static final @Nonnull LootSelector ls = new LootSelector(NO_CONDITIONS);

  private static @Nonnull ResourceLocation eio(ResourceLocation mc) {
    return new ResourceLocation(EnderIO.DOMAIN, mc.getResourcePath());
  }
}
