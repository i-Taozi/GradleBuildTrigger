package crazypants.enderio.base.network;

import javax.annotation.Nonnull;

import com.enderio.core.common.network.ThreadedNetworkWrapper;

import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.block.charge.PacketExplodeEffect;
import crazypants.enderio.base.capacitor.PacketCapacitorSync;
import crazypants.enderio.base.filter.network.PacketExistingItemFilterSnapshot;
import crazypants.enderio.base.filter.network.PacketFilterUpdate;
import crazypants.enderio.base.filter.network.PacketHeldFilterUpdate;
import crazypants.enderio.base.handler.darksteel.PacketDarkSteelPowerUse;
import crazypants.enderio.base.handler.darksteel.PacketDarkSteelSFX;
import crazypants.enderio.base.handler.darksteel.PacketUpgradeState;
import crazypants.enderio.base.handler.darksteel.gui.PacketOpenDSU;
import crazypants.enderio.base.item.conduitprobe.PacketConduitProbe;
import crazypants.enderio.base.item.conduitprobe.PacketConduitProbeMode;
import crazypants.enderio.base.item.coordselector.PacketUpdateLocationPrintout;
import crazypants.enderio.base.item.darksteel.upgrade.storage.PacketOpenInventory;
import crazypants.enderio.base.item.magnet.PacketMagnetState;
import crazypants.enderio.base.item.travelstaff.PacketDrainStaff;
import crazypants.enderio.base.item.xptransfer.PacketXpTransferEffects;
import crazypants.enderio.base.item.yetawrench.PacketYetaWrenchDisplayMode;
import crazypants.enderio.base.machine.base.network.PacketPowerStorage;
import crazypants.enderio.base.machine.baselegacy.PacketLegacyPowerStorage;
import crazypants.enderio.base.machine.modes.PacketIoMode;
import crazypants.enderio.base.machine.modes.PacketRedstoneMode;
import crazypants.enderio.base.teleport.packet.PacketOpenAuthGui;
import crazypants.enderio.base.teleport.packet.PacketPassword;
import crazypants.enderio.base.teleport.packet.PacketTravelEvent;
import crazypants.enderio.base.transceiver.PacketAddRemoveChannel;
import crazypants.enderio.base.transceiver.PacketChannelList;
import crazypants.enderio.base.xp.PacketExperienceContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

public final class PacketHandler {

  public static final @Nonnull ThreadedNetworkWrapper INSTANCE = new ThreadedNetworkWrapper(EnderIO.DOMAIN);

  private static int ID = 0;

  private PacketHandler() {
  }

  public static int nextID() {
    return ID++;
  }

  public static void sendToAllAround(IMessage message, TileEntity te) {
    INSTANCE.sendToAllAround(message, te);
  }

  public static void sendTo(IMessage message, EntityPlayerMP player) {
    INSTANCE.sendTo(message, player);
  }

  public static void init(FMLInitializationEvent event) {
    INSTANCE.registerMessage(ExecPacket.Handler.class, ExecPacket.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketRedstoneMode.Handler.class, PacketRedstoneMode.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(GuiPacket.Handler.class, GuiPacket.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketExperienceContainer.Handler.class, PacketExperienceContainer.class, nextID(), Side.CLIENT);
    INSTANCE.registerMessage(PacketConduitProbe.Handler.class, PacketConduitProbe.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketConduitProbeMode.Handler.class, PacketConduitProbeMode.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketYetaWrenchDisplayMode.Handler.class, PacketYetaWrenchDisplayMode.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketXpTransferEffects.Handler.class, PacketXpTransferEffects.class, nextID(), Side.CLIENT);
    INSTANCE.registerMessage(PacketTravelEvent.Handler.class, PacketTravelEvent.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketAddRemoveChannel.Handler.class, PacketAddRemoveChannel.class, nextID(), Side.CLIENT);
    INSTANCE.registerMessage(PacketAddRemoveChannel.Handler.class, PacketAddRemoveChannel.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketChannelList.Handler.class, PacketChannelList.class, nextID(), Side.CLIENT);
    INSTANCE.registerMessage(PacketDarkSteelPowerUse.Handler.class, PacketDarkSteelPowerUse.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketUpgradeState.ServerHandler.class, PacketUpgradeState.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketUpgradeState.ClientHandler.class, PacketUpgradeState.class, nextID(), Side.CLIENT);
    INSTANCE.registerMessage(PacketOpenInventory.Handler.class, PacketOpenInventory.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketDrainStaff.Handler.class, PacketDrainStaff.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketIoMode.Handler.class, PacketIoMode.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketLegacyPowerStorage.Handler.class, PacketLegacyPowerStorage.class, nextID(), Side.CLIENT);
    INSTANCE.registerMessage(PacketMagnetState.Handler.class, PacketMagnetState.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketOpenAuthGui.Handler.class, PacketOpenAuthGui.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketUpdateLocationPrintout.Handler.class, PacketUpdateLocationPrintout.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketSpawnParticles.Handler.class, PacketSpawnParticles.class, nextID(), Side.CLIENT);
    INSTANCE.registerMessage(PacketPowerStorage.Handler.class, PacketPowerStorage.class, nextID(), Side.CLIENT);
    INSTANCE.registerMessage(PacketPassword.Handler.class, PacketPassword.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketDarkSteelSFX.ServerHandler.class, PacketDarkSteelSFX.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketDarkSteelSFX.ClientHandler.class, PacketDarkSteelSFX.class, nextID(), Side.CLIENT);
    INSTANCE.registerMessage(PacketFilterUpdate.Handler.class, PacketFilterUpdate.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketExistingItemFilterSnapshot.Handler.class, PacketExistingItemFilterSnapshot.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketHeldFilterUpdate.Handler.class, PacketHeldFilterUpdate.class, nextID(), Side.SERVER);
    INSTANCE.registerMessage(PacketExplodeEffect.Handler.class, PacketExplodeEffect.class, nextID(), Side.CLIENT);
    INSTANCE.registerMessage(PacketCapacitorSync.Handler.class, PacketCapacitorSync.class, nextID(), Side.CLIENT);
    INSTANCE.registerMessage(PacketOpenDSU.Handler.class, PacketOpenDSU.class, nextID(), Side.SERVER);
  }

}
