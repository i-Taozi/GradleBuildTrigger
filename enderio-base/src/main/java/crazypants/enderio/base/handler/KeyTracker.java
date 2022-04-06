package crazypants.enderio.base.handler;

import java.util.Locale;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import crazypants.enderio.api.tool.IConduitControl;
import crazypants.enderio.api.upgrades.IDarkSteelUpgrade;
import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.conduit.ConduitDisplayMode;
import crazypants.enderio.base.handler.darksteel.DarkSteelController;
import crazypants.enderio.base.handler.darksteel.StateController;
import crazypants.enderio.base.handler.darksteel.gui.PacketOpenDSU;
import crazypants.enderio.base.integration.baubles.BaublesUtil;
import crazypants.enderio.base.integration.thaumcraft.GogglesOfRevealingUpgrade;
import crazypants.enderio.base.integration.top.TheOneProbeUpgrade;
import crazypants.enderio.base.item.conduitprobe.PacketConduitProbeMode;
import crazypants.enderio.base.item.darksteel.upgrade.elytra.ElytraUpgrade;
import crazypants.enderio.base.item.darksteel.upgrade.explosive.ExplosiveUpgrade;
import crazypants.enderio.base.item.darksteel.upgrade.glider.GliderUpgrade;
import crazypants.enderio.base.item.darksteel.upgrade.jump.JumpUpgrade;
import crazypants.enderio.base.item.darksteel.upgrade.nightvision.NightVisionUpgrade;
import crazypants.enderio.base.item.darksteel.upgrade.sound.SoundDetectorUpgrade;
import crazypants.enderio.base.item.darksteel.upgrade.speed.SpeedUpgrade;
import crazypants.enderio.base.item.darksteel.upgrade.stepassist.StepAssistUpgrade;
import crazypants.enderio.base.item.darksteel.upgrade.storage.PacketOpenInventory;
import crazypants.enderio.base.item.magnet.ItemMagnet;
import crazypants.enderio.base.item.magnet.MagnetController;
import crazypants.enderio.base.item.magnet.MagnetController.ActiveMagnet;
import crazypants.enderio.base.item.magnet.PacketMagnetState;
import crazypants.enderio.base.item.magnet.PacketMagnetState.SlotType;
import crazypants.enderio.base.item.yetawrench.PacketYetaWrenchDisplayMode;
import crazypants.enderio.base.network.PacketHandler;
import crazypants.enderio.base.sound.SoundHelper;
import crazypants.enderio.base.sound.SoundRegistry;
import crazypants.enderio.util.Prep;
import crazypants.enderio.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;

import static crazypants.enderio.base.init.ModObject.itemConduitProbe;

@EventBusSubscriber(modid = EnderIO.MODID, value = Side.CLIENT)
public enum KeyTracker {

  inventory(Keyboard.KEY_I, CATEGORY.DARKSTEELARMOR, new InventoryAction()),
  dsu(Keyboard.KEY_NONE, CATEGORY.DARKSTEELARMOR, new DSUAction()),
  glidertoggle(Keyboard.KEY_G, CATEGORY.DARKSTEELARMOR, all( //
      toggleStateAction(DarkSteelController::isGliderUpgradeEquipped, GliderUpgrade.INSTANCE), //
      toggleStateAction(DarkSteelController::isElytraUpgradeEquipped, ElytraUpgrade.INSTANCE) //
  )),
  soundlocator(Keyboard.KEY_NONE, CATEGORY.DARKSTEELARMOR,
      toggleStateAction(DarkSteelController::isSoundDetectorUpgradeEquipped, SoundDetectorUpgrade.INSTANCE)),
  nightvision(Keyboard.KEY_P, CATEGORY.DARKSTEELARMOR, new NightVisionAction()),
  gogglesofrevealing(Keyboard.KEY_NONE, CATEGORY.DARKSTEELARMOR,
      toggleStateAction(GogglesOfRevealingUpgrade::isUpgradeEquipped, GogglesOfRevealingUpgrade.INSTANCE)),
  stepassist(Keyboard.KEY_NONE, CATEGORY.DARKSTEELARMOR, toggleStateAction(StepAssistUpgrade::isEquipped, StepAssistUpgrade.INSTANCE)),
  speed(Keyboard.KEY_NONE, CATEGORY.DARKSTEELARMOR, toggleStateAction(SpeedUpgrade::isEquipped, SpeedUpgrade.SPEED_ONE)),
  jump(Keyboard.KEY_NONE, CATEGORY.DARKSTEELARMOR, toggleStateAction(JumpUpgrade::isEquipped, JumpUpgrade.JUMP_ONE)),
  top(Keyboard.KEY_NONE, CATEGORY.DARKSTEELARMOR, new TopAction()),
  tnt(Keyboard.KEY_NONE, CATEGORY.DARKSTEELARMOR, toggleStateAction(ExplosiveUpgrade::isEquipped, ExplosiveUpgrade.INSTANCE)),
  yetawrenchmode(Keyboard.KEY_Y, CATEGORY.TOOLS, new YetaWrenchAction()),
  magnet(Keyboard.KEY_NONE, CATEGORY.TOOLS, new MagnetAction()),
  fovReset(Keyboard.KEY_NONE, CATEGORY.CAMERA, new FovZoomHandler.FovResetAction()),
  fovUnReset(Keyboard.KEY_NONE, CATEGORY.CAMERA, new FovZoomHandler.FovUnresetAction()),
  fovStore(Keyboard.KEY_NONE, CATEGORY.CAMERA, new FovZoomHandler.FovStoreAction()),
  fovRecall(Keyboard.KEY_NONE, CATEGORY.CAMERA, new FovZoomHandler.FovRecallAction()),
  fovPlus(Keyboard.KEY_NONE, CATEGORY.CAMERA, null),
  fovMinus(Keyboard.KEY_NONE, CATEGORY.CAMERA, null),
  fovPlusFast(Keyboard.KEY_NONE, CATEGORY.CAMERA, null),
  fovMinusFast(Keyboard.KEY_NONE, CATEGORY.CAMERA, null),

  ;

  private static final class CATEGORY {
    private static final @Nonnull String DARKSTEELARMOR = "key.category.darksteelarmor";
    private static final @Nonnull String TOOLS = "key.category.tools";
    private static final @Nonnull String CAMERA = "key.category.camera";
  }

  public interface Action {
    void execute();
  }

  private final @Nonnull KeyBinding binding;
  private final Action action;

  private KeyTracker(int keyCode, @Nonnull String category, @Nullable Action action) {
    this.binding = new KeyBinding("enderio.keybind." + name().toLowerCase(Locale.ENGLISH), keyCode, StringUtil.trim(category));
    ClientRegistry.registerKeyBinding(binding);
    this.action = action;
  }

  public @Nonnull KeyBinding getBinding() {
    return binding;
  }

  @SubscribeEvent
  public static void onKeyInput(KeyInputEvent event) {
    for (KeyTracker tracker : values()) {
      if (tracker.action != null && tracker.binding.isPressed()) {
        tracker.action.execute();
      }
    }
  }

  public static @Nonnull Action toggleStateAction(@Nonnull Predicate<EntityPlayer> condition, @Nonnull IDarkSteelUpgrade type) {
    return () -> {
      if (condition.test(Minecraft.getMinecraft().player)) {
        boolean isActive = !StateController.isActive(Minecraft.getMinecraft().player, type);
        StringUtil.sendEnabledChatMessage(Minecraft.getMinecraft().player, type.getUnlocalizedName(), isActive);
        StateController.setActive(Minecraft.getMinecraft().player, type, isActive);
      }
    };
  }

  public static @Nonnull Action all(@Nonnull Action... actions) {
    return () -> {
      for (Action action : actions) {
        action.execute();
      }
    };
  }

  private static class MagnetAction implements Action {
    @Override
    public void execute() {
      EntityPlayerSP player = Minecraft.getMinecraft().player;
      ActiveMagnet activeMagnet = MagnetController.getMagnet(player, false);
      if (activeMagnet != null) {
        boolean isActive = !ItemMagnet.isActive(activeMagnet.getItem());
        PacketHandler.INSTANCE.sendToServer(new PacketMagnetState(SlotType.INVENTORY, activeMagnet.getSlot(), isActive));
        return;
      }

      IInventory baubles = BaublesUtil.instance().getBaubles(player);
      if (baubles != null) {
        for (int i = 0; i < baubles.getSizeInventory(); i++) {
          ItemStack stack = baubles.getStackInSlot(i);
          if (ItemMagnet.isMagnet(stack)) {
            boolean isActive = !ItemMagnet.isActive(stack);
            PacketHandler.INSTANCE.sendToServer(new PacketMagnetState(SlotType.BAUBLES, i, isActive));
            return;
          }
        }
      }
    }
  }

  private static class InventoryAction implements Action {
    @Override
    public void execute() {
      PacketHandler.INSTANCE.sendToServer(new PacketOpenInventory());
    }
  }

  private static class DSUAction implements Action {
    @Override
    public void execute() {
      PacketHandler.INSTANCE.sendToServer(new PacketOpenDSU());
    }
  }

  private static class YetaWrenchAction implements Action {
    @Override
    public void execute() {
      EntityPlayerSP player = Minecraft.getMinecraft().player;
      ItemStack equipped = player.getHeldItemMainhand();
      if (Prep.isInvalid(equipped)) {
        return;
      }
      if (equipped.getItem() instanceof IConduitControl) {
        ConduitDisplayMode curMode = ConduitDisplayMode.getDisplayMode(equipped);
        ConduitDisplayMode newMode = player.isSneaking() ? curMode.previous() : curMode.next();
        ConduitDisplayMode.setDisplayMode(equipped, newMode);
        PacketHandler.INSTANCE.sendToServer(new PacketYetaWrenchDisplayMode(player.inventory.currentItem, newMode));
      } else if (equipped.getItem() == itemConduitProbe.getItemNN()) {
        int newMeta = equipped.getItemDamage() == 0 ? 1 : 0;
        equipped.setItemDamage(newMeta);
        PacketHandler.INSTANCE.sendToServer(new PacketConduitProbeMode());
        player.swingArm(EnumHand.MAIN_HAND);
      }
    }
  }

  private static class NightVisionAction implements Action {
    @Override
    public void execute() {
      EntityPlayer player = Minecraft.getMinecraft().player;
      if (DarkSteelController.isNightVisionUpgradeEquipped(player)) {
        boolean isActive = !StateController.isActive(player, NightVisionUpgrade.INSTANCE);
        if (isActive) {
          SoundHelper.playSound(player.world, player, SoundRegistry.NIGHTVISION_ON, 0.1f, player.world.rand.nextFloat() * 0.4f - 0.2f + 1.0f);
        } else {
          SoundHelper.playSound(player.world, player, SoundRegistry.NIGHTVISION_OFF, 0.1f, 1.0f);
        }
        StateController.setActive(player, NightVisionUpgrade.INSTANCE, isActive);
        StringUtil.sendEnabledChatMessage(Minecraft.getMinecraft().player, NightVisionUpgrade.INSTANCE.getUnlocalizedName(), isActive);
      }
    }
  }

  private static class TopAction implements Action {
    @Override
    public void execute() {
      EntityPlayer player = Minecraft.getMinecraft().player;
      if (DarkSteelController.isTopUpgradeEquipped(player)) {
        boolean isActive = !DarkSteelController.isTopActive(player);
        DarkSteelController.setTopActive(player, isActive);
        StringUtil.sendEnabledChatMessage(Minecraft.getMinecraft().player, TheOneProbeUpgrade.INSTANCE.getUnlocalizedName(), isActive);
      }
    }
  }

}
