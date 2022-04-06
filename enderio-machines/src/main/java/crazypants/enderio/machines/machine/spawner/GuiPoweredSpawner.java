package crazypants.enderio.machines.machine.spawner;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import com.enderio.core.client.gui.button.MultiIconButton;
import com.enderio.core.client.gui.button.ToggleButton;
import com.enderio.core.client.gui.widget.GuiToolTip;
import com.enderio.core.client.render.ColorUtil;
import com.google.common.collect.Lists;

import crazypants.enderio.base.gui.IconEIO;
import crazypants.enderio.base.machine.gui.GuiInventoryMachineBase;
import crazypants.enderio.base.machine.gui.PowerBar;
import crazypants.enderio.machines.lang.Lang;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class GuiPoweredSpawner extends GuiInventoryMachineBase<TilePoweredSpawner> implements IPoweredSpawnerRemoteExec.GUI {

  private final @Nonnull MultiIconButton modeB;
  private final @Nonnull Rectangle progressTooltipRect;
  private boolean wasSpawnMode;
  private @Nonnull String header = "";
  private final @Nonnull ToggleButton showRangeB;

  public GuiPoweredSpawner(@Nonnull InventoryPlayer par1InventoryPlayer, @Nonnull TilePoweredSpawner te) {
    super(te, new ContainerPoweredSpawner(par1InventoryPlayer, te), "powered_spawner_spawn", "powered_spawner_capture");

    modeB = MultiIconButton.createRightArrowButton(this, 8888, 115, 10);
    modeB.setSize(10, 16);

    addProgressTooltip(80, 34, 14, 14);
    progressTooltipRect = progressTooltips.get(0).getBounds();

    updateSpawnMode(te.isSpawnMode());

    int x = getXSize() - 5 - BUTTON_SIZE;
    showRangeB = new ToggleButton(this, -1, x, 44, IconEIO.SHOW_RANGE, IconEIO.HIDE_RANGE);
    showRangeB.setSize(BUTTON_SIZE, BUTTON_SIZE);
    addToolTip(new GuiToolTip(showRangeB.getBounds(), "null") {
      @Override
      public @Nonnull List<String> getToolTipText() {
        return Lists.newArrayList((showRangeB.isSelected() ? Lang.GUI_HIDE_RANGE : Lang.GUI_SHOW_RANGE).get());
      }
    });

    addDrawingElement(new PowerBar(te, this));
  }

  @Override
  public void initGui() {
    super.initGui();
    modeB.onGuiInit();
    showRangeB.onGuiInit();
    showRangeB.setSelected(getTileEntity().isShowingRange());
  }

  @Override
  protected void actionPerformed(@Nonnull GuiButton par1GuiButton) throws IOException {
    if (par1GuiButton == modeB) {
      getTileEntity().setSpawnMode(!getTileEntity().isSpawnMode());
      doSetSpawnMode(getTileEntity().isSpawnMode());
    } else if (par1GuiButton == showRangeB) {
      getTileEntity().setShowRange(showRangeB.isSelected());
    } else {
      super.actionPerformed(par1GuiButton);
    }
  }

  private void updateSpawnMode(boolean spawnMode) {
    wasSpawnMode = spawnMode;
    ((ContainerPoweredSpawner) inventorySlots).setSlotVisibility(!spawnMode);

    if (spawnMode) {
      getGhostSlotHandler().getGhostSlots().clear();
      header = Lang.GUI_SPAWNER_SPAWN.get();
      progressTooltipRect.x = 80;
      progressTooltipRect.y = 34;
      progressTooltipRect.width = 14;
      progressTooltipRect.height = 14;
    } else {
      ((ContainerPoweredSpawner) inventorySlots).createGhostSlots(getGhostSlotHandler().getGhostSlots());
      header = Lang.GUI_SPAWNER_CAPTURE.get();
      progressTooltipRect.x = 52;
      progressTooltipRect.y = 40;
      progressTooltipRect.width = 72;
      progressTooltipRect.height = 21;
    }
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
    GlStateManager.color(1, 1, 1);
    int sx = (width - xSize) / 2;
    int sy = (height - ySize) / 2;

    boolean spawnMode = getTileEntity().isSpawnMode();
    if (spawnMode != wasSpawnMode) {
      updateSpawnMode(spawnMode);
    }

    bindGuiTexture(spawnMode ? 0 : 1);
    drawTexturedModalRect(sx, sy, 0, 0, xSize, ySize);

    if (shouldRenderProgress()) {
      if (spawnMode) {
        int scaled = getProgressScaled(14) + 1;
        drawTexturedModalRect(sx + 81, sy + 34 + 14 - scaled + 3, 176, 14 - scaled, 14, scaled);
      } else {
        int scaled = getProgressScaled(24);
        drawTexturedModalRect(sx + 76, sy + 43, 176, 14, scaled + 1, 16);
      }
    }

    FontRenderer fr = getFontRenderer();
    int x = sx + xSize / 2 - fr.getStringWidth(header) / 2;
    int y = sy + fr.FONT_HEIGHT + 6;
    fr.drawStringWithShadow(header, x, y, ColorUtil.getRGB(Color.WHITE));

    String name = TextFormatting.ITALIC + (getTileEntity().getEntity() != null ? getTileEntity().getEntity().getDisplayName() : "");
    x = sx + xSize / 2 - fr.getStringWidth(name) / 2;
    y = sy + 43 + 16 + 2;
    fr.drawStringWithShadow(name, x, y, ColorUtil.getRGB(Color.WHITE));
    GlStateManager.color(1, 1, 1);
    bindGuiTexture(spawnMode ? 0 : 1);

    super.drawGuiContainerBackgroundLayer(par1, par2, par3);
  }

  @Override
  protected boolean showRecipeButton() {
    return false;
  }

  @Override
  protected @Nonnull ResourceLocation getGuiTexture() {
    return super.getGuiTexture(getTileEntity().isSpawnMode() ? 0 : 1);
  }
}
