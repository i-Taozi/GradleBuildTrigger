package crazypants.enderio.base.item.darksteel.upgrade.energy;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.client.handlers.SpecialTooltipHandler;
import com.enderio.core.common.util.NNList;

import crazypants.enderio.api.capacitor.ICapacitorData;
import crazypants.enderio.api.capacitor.ICapacitorKey;
import crazypants.enderio.api.upgrades.IDarkSteelItem;
import crazypants.enderio.api.upgrades.IDarkSteelUpgrade;
import crazypants.enderio.api.upgrades.IRule;
import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.config.config.DarkSteelConfig;
import crazypants.enderio.base.handler.darksteel.AbstractUpgrade;
import crazypants.enderio.base.handler.darksteel.Rules;
import crazypants.enderio.base.item.travelstaff.ItemTravelStaff;
import crazypants.enderio.base.lang.Lang;
import crazypants.enderio.base.lang.LangPower;
import crazypants.enderio.util.Prep;
import info.loenwind.autoconfig.factory.IValue;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(modid = EnderIO.MODID)
public class EnergyUpgrade extends AbstractUpgrade {

  public final class EnergyUpgradeHolder {
    private int energy;
    private final @Nonnull ItemStack stack;
    private final @Nonnull IDarkSteelItem item;

    private EnergyUpgradeHolder(@Nonnull ItemStack stack, @Nonnull IDarkSteelItem item) {
      this.stack = stack;
      this.item = item;
      setEnergy(getOrCreateUpgradeNBT(stack).getInteger(EnergyUpgradeManager.KEY_ENERGY));
    }

    public int getEnergy() {
      return energy;
    }

    public void setEnergy(int energy) {
      this.energy = MathHelper.clamp(energy, 0, getCapacity());
    }

    public int receiveEnergy(int maxRF, boolean simulate) {
      int energyReceived = Math.max(0, Math.min(getCapacity() - energy, Math.min(getMaxInput(), maxRF)));
      if (!simulate) {
        energy += energyReceived;
      }
      return energyReceived;
    }

    public int extractEnergy(int maxExtract, boolean simulate) {
      int energyExtracted = Math.max(0, Math.min(energy, Math.min(getMaxOutput(), maxExtract)));
      if (!simulate) {
        energy -= energyExtracted;
      }
      return energyExtracted;
    }

    public @Nonnull EnergyUpgrade getUpgrade() {
      return EnergyUpgrade.this;
    }

    public void writeToItem() {
      EnergyUpgrade.this.addToItem(stack, item);
      getOrCreateUpgradeNBT(stack).setInteger(EnergyUpgradeManager.KEY_ENERGY, energy);
    }

    public int getCapacity() {
      return item.getEnergyStorageKey(stack).get(capData);
    }

    public int getMaxInput() {
      return item.getEnergyInputKey(stack).get(capData);
    }

    public int getMaxOutput() {
      return item.getEnergyUseKey(stack).get(capData);
    }

    public float getAbsorptionRatio() {
      return item.getAbsorptionRatioKey(stack).getFloat(capData);
    }

    public boolean isAbsorbDamageWithPower() {
      return EnergyUpgradeManager.RANDOM.nextDouble() < getAbsorptionRatio();
    }

  }

  public static final @Nonnull NNList<EnergyUpgrade> UPGRADES = new NNList<>(
      new EnergyUpgrade(EnergyUpgradeManager.UPGRADE_NAME, 0, "enderio.darksteel.upgrade.empowered_one", DarkSteelConfig.energyUpgradeLevelCostEmpowered0),
      new EnergyUpgrade(EnergyUpgradeManager.UPGRADE_NAME, 1, "enderio.darksteel.upgrade.empowered_two", DarkSteelConfig.energyUpgradeLevelCostEmpowered1),
      new EnergyUpgrade(EnergyUpgradeManager.UPGRADE_NAME, 2, "enderio.darksteel.upgrade.empowered_three", DarkSteelConfig.energyUpgradeLevelCostEmpowered2),
      new EnergyUpgrade(EnergyUpgradeManager.UPGRADE_NAME, 3, "enderio.darksteel.upgrade.empowered_four", DarkSteelConfig.energyUpgradeLevelCostEmpowered3),
      new EnergyUpgrade(EnergyUpgradeManager.UPGRADE_NAME, 4, "enderio.darksteel.upgrade.empowered_five", DarkSteelConfig.energyUpgradeLevelCostEmpowered4));

  public static final @Nonnull IRule.Prerequisite HAS_ANY = new IRule.Prerequisite() {

    @Override
    @Nonnull
    public CheckResult check(@Nonnull ItemStack stack, @Nonnull IDarkSteelItem item) {
      for (EnergyUpgrade energyUpgrade : UPGRADES) {
        if (energyUpgrade.hasUpgrade(stack, item)) {
          return CheckResult.PASS;
        }
      }

      return new CheckResult(Lang.DSU_CHECK_PREREQ_ENERGY.toChatServer());
    }

    @Override
    @Nonnull
    public IDarkSteelUpgrade getPrerequisite() {
      return UPGRADES.get(0);
    }

  };

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void registerDarkSteelUpgrades(@Nonnull RegistryEvent.Register<IDarkSteelUpgrade> event) {
    final IForgeRegistry<IDarkSteelUpgrade> registry = event.getRegistry();
    for (EnergyUpgrade energyUpgrade : UPGRADES) {
      registry.register(energyUpgrade);
    }
  }

  public static EnergyUpgrade loadAnyFromItem(@Nonnull ItemStack stack) {
    for (EnergyUpgrade energyUpgrade : UPGRADES) {
      if (energyUpgrade.hasUpgrade(stack)) {
        return energyUpgrade;
      }
    }
    if (stack.getItem() instanceof ItemTravelStaff) {
      return UPGRADES.get(0);
    }
    return null;
  }

  public static EnergyUpgrade next(EnergyUpgrade upgrade) {
    int next = upgrade == null ? 0 : (upgrade.level + 1);
    return next >= UPGRADES.size() ? null : UPGRADES.get(next);
  }

  public static EnergyUpgrade prev(EnergyUpgrade upgrade) {
    int next = upgrade == null ? 0 : (upgrade.level - 1);
    return next < 0 ? null : UPGRADES.get(next);
  }

  protected final int level;
  protected final @Nonnull ICapacitorData capData;

  public EnergyUpgrade(@Nonnull String id, int level, @Nonnull String name, @Nonnull IValue<Integer> levels) {
    super(id, level, name, levels);
    this.level = level;
    this.capData = new ICapacitorData() {

      @Override
      public float getUnscaledValue(@Nonnull ICapacitorKey key) {
        return level + 1; // 1...5
      }

      @Override
      @Nonnull
      public String getUnlocalizedName() {
        return name;
      }

      @Override
      @Nonnull
      public String getLocalizedName() {
        return EnderIO.lang.localize(getUnlocalizedName());
      }
    };
  }

  @Override
  @Nonnull
  public List<IRule> getRules() {
    return new NNList<>(Rules.withLevels(level, UPGRADES),
        // static check for slots:
        Rules.staticCheck(item -> item.getMaxEmpoweredLevel(Prep.getEmpty()) >= level),
        // dynamic check for applying:
        (stack, item) -> item.getMaxEmpoweredLevel(stack) >= level ? IRule.CheckResult.PASS : IRule.CheckResult.SILENT_FAIL);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void addDetailedEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer, @Nonnull List<String> list, boolean flag) {
    List<String> upgradeStr = new ArrayList<String>();
    upgradeStr.add(TextFormatting.DARK_AQUA + getDisplayName());
    if (itemstack.isItemStackDamageable() && itemstack.getItem() instanceof IDarkSteelItem) {
      SpecialTooltipHandler.addDetailedTooltipFromResources(upgradeStr, getUnlocalizedName());

      EnergyUpgradeHolder eu = getEnergyUpgradeHolder(itemstack, (IDarkSteelItem) itemstack.getItem());
      String percDamage = Math.round(eu.getAbsorptionRatio() * 100) + "";
      String capString = LangPower.RF(eu.getCapacity());
      for (int i = 0; i < upgradeStr.size(); i++) {
        String str = upgradeStr.get(i);
        str = str.replaceAll("\\$P", capString);
        str = str.replaceAll("\\$D", percDamage);
        upgradeStr.set(i, str);
      }
    }
    list.addAll(upgradeStr);
  }

  public int getLevel() {
    return level;
  }

  public @Nonnull EnergyUpgradeHolder getEnergyUpgradeHolder(@Nonnull ItemStack stack, @Nonnull IDarkSteelItem item) {
    return new EnergyUpgradeHolder(stack, item);
  }

}
