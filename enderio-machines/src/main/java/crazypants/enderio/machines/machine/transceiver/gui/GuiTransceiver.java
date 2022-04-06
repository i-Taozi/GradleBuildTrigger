package crazypants.enderio.machines.machine.transceiver.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.gui.IGuiOverlay;
import com.enderio.core.api.client.gui.ITabPanel;

import crazypants.enderio.base.machine.gui.GuiInventoryMachineBase;
import crazypants.enderio.base.transceiver.ChannelType;
import crazypants.enderio.machines.machine.transceiver.TileTransceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiTransceiver extends GuiInventoryMachineBase<TileTransceiver> implements ITransceiverRemoteExec.GUI {

  private int activeTab = 0;
  private final List<ITabPanel> tabs = new ArrayList<ITabPanel>();
  GeneralTab generalTab;

  public GuiTransceiver(@Nonnull InventoryPlayer par1InventoryPlayer, @Nonnull TileTransceiver te) {
    super(te, new ContainerTransceiver(par1InventoryPlayer, te), "transceiver_general", "itemFilter");

    generalTab = new GeneralTab(this);
    tabs.add(generalTab);
    // FilterTab filterTab = new FilterTab(this);
    // tabs.add(filterTab);
    tabs.add(new ChannelTab(this, ChannelType.POWER));
    tabs.add(new ChannelTab(this, ChannelType.ITEM));
    tabs.add(new ChannelTab(this, ChannelType.FLUID));
  }

  @Override
  protected boolean showRecipeButton() {
    return false;
  }

  @Override
  public int getXSize() {
    return ContainerTransceiver.GUI_WIDTH;
  }

  @Override
  public void updateScreen() {
    for (int i = 0; i < tabs.size(); i++) {
      if (i == activeTab) {
        tabs.get(i).updateScreen();
        return;
      }
    }
  }

  @Override
  protected void keyTyped(char par1, int par2) {
    if (par2 == 1) {
      for (IGuiOverlay overlay : overlays) {
        if (overlay.isVisible()) {
          overlay.setIsVisible(false);
          return;
        }
      }
      mc.player.closeScreen();
    }

    for (int i = 0; i < tabs.size(); i++) {
      if (i == activeTab) {
        tabs.get(i).keyTyped(par1, par2);
        return;
      }
    }
  }

  void doDefaultKeyTyped(char par1, int par2) {
    try {
      super.keyTyped(par1, par2);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void initGui() {
    super.initGui();
    for (int i = 0; i < tabs.size(); i++) {
      if (i != activeTab) {
        tabs.get(i).deactivate();
      }
    }
    getGhostSlotHandler().getGhostSlots().clear();
    for (int i = 0; i < tabs.size(); i++) {
      if (i == activeTab) {
        tabs.get(i).onGuiInit(guiLeft + 10, guiTop, xSize - 20, ySize - 20);
      }
    }
    configB.visible = activeTab == 0;
    redstoneButton.visible = activeTab == 0;
  }

  protected boolean renderPowerBar() {
    return activeTab == 0;
  }

  @Override
  protected void mouseClicked(int x, int y, int par3) throws IOException {
    super.mouseClicked(x, y, par3);

    x = (x - guiLeft);
    y = (y - guiTop);

    tabs.get(activeTab).mouseClicked(x, y, par3);
  }

  @Override
  protected boolean doSwitchTab(int tab) {
    if (tab != activeTab) {
      activeTab = tab;
      hideOverlays();
      initGui();
      return true;
    }
    return super.doSwitchTab(tab);
  }

  @Override
  protected void actionPerformed(@Nonnull GuiButton guiButton) throws IOException {
    super.actionPerformed(guiButton);
    tabs.get(activeTab).actionPerformed(guiButton);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

    int sx = (width - xSize) / 2;
    int sy = (height - ySize) / 2;

    ITabPanel tab = tabs.get(activeTab);

    if (tab != null) {
      Minecraft.getMinecraft().getTextureManager().bindTexture(tab.getTexture());
      drawTexturedModalRect(sx, sy, 0, 0, xSize, ySize);
    } else {
      Minecraft.getMinecraft().player.closeScreen();
      return;
    }

    startTabs();
    for (int i = 0; i < tabs.size(); i++) {
      renderStdTab(sx, sy, i, tabs.get(i).getIcon(), i == activeTab);
    }

    tabs.get(activeTab).render(par1, par2, par3);

    super.drawGuiContainerBackgroundLayer(par1, par2, par3);
  }

  public @Nonnull TileTransceiver getTransciever() {
    return getTileEntity();
  }

  public @Nonnull ContainerTransceiver getContainer() {
    return (ContainerTransceiver) inventorySlots;
  }

  @Override
  protected @Nonnull ResourceLocation getGuiTexture() {
    return tabs.get(activeTab).getTexture();
  }

  @Override
  public int getOverlayOffsetXLeft() {
    return 38;
  }

  @Override
  public int getOverlayOffsetXRight() {
    return 78;
  }

  // @Override
  // @Optional.Method(modid = "NotEnoughItems")
  // public boolean hideItemPanelSlot(GuiContainer gc, int x, int y, int w, int h) {
  // if(tabs.size() > 0) {
  // int sx = (width - xSize) / 2;
  // int sy = (height - ySize) / 2;
  // int tabX = sx + xSize - 3;
  // int tabY = sy + tabYOffset;
  //
  // return (x+w) >= tabX && x < (tabX + 14) && (y+h) >= tabY && y < (tabY + tabs.size()*TAB_HEIGHT);
  // }
  // return false;
  // }
}
