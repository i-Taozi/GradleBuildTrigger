package crazypants.enderio.machines.machine.teleport.telepad.gui;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.client.gui.button.IconButton;
import com.enderio.core.client.gui.widget.GuiToolTip;
import com.enderio.core.client.gui.widget.TextFieldEnder;
import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.util.NullHelper;
import com.enderio.core.common.util.Util;
import com.google.common.collect.Lists;

import crazypants.enderio.base.gui.GuiContainerBaseEIO;
import crazypants.enderio.base.gui.IconEIO;
import crazypants.enderio.base.lang.LangFluid;
import crazypants.enderio.base.lang.LangPower;
import crazypants.enderio.base.machine.gui.PowerBar;
import crazypants.enderio.machines.config.config.TelePadConfig;
import crazypants.enderio.machines.lang.Lang;
import crazypants.enderio.machines.machine.teleport.telepad.BlockTelePad;
import crazypants.enderio.machines.machine.teleport.telepad.TileTelePad;
import crazypants.enderio.machines.machine.teleport.telepad.packet.PacketOpenServerGui;
import crazypants.enderio.machines.machine.teleport.telepad.packet.PacketSetTarget;
import crazypants.enderio.machines.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.math.BlockPos;

public class GuiTelePad extends GuiContainerBaseEIO<TileTelePad> {

  private static final int ID_TELEPORT_BUTTON = 96;
  private static final int ID_TRAVEL_SETTINGS_BUTTON = 97;

  GuiButton switchButton;
  GuiButton teleportButton;
  IconButton travelSettingsButton;

  private final @Nonnull TextFieldEnder xTF, yTF, zTF, dimTF;

  private static final int powerX = 8;
  private static final int powerY = 9;

  private int getPowerScale() {
    return TelePadConfig.telepadFluidUse.get() > 0 ? 57 : 120;
  }

  private static final int progressX = 26;
  private static final int progressY = 110;
  private static final int progressScale = 124;

  private static final int fluidX = 8;
  private static final int fluidY = 71;
  private static final int fluidScale = 58;
  private static final @Nonnull Rectangle RECTANGLE_TANK = new Rectangle(fluidX, fluidY, 10, fluidScale);

  public GuiTelePad(@Nonnull InventoryPlayer playerInv, final @Nonnull TileTelePad te) {
    super(te, new ContainerTelePad(playerInv, te), "tele_pad");
    ySize = 220;

    int settingsBX = guiLeft + xSize - (7 + 16);
    int settingsBY = guiTop + 10;

    travelSettingsButton = new IconButton(this, ID_TRAVEL_SETTINGS_BUTTON, settingsBX, settingsBY, IconEIO.GEAR_LIGHT);
    travelSettingsButton.setToolTip(Lang.GUI_TELEPAD_TO_TRAVEL.get());

    addToolTip(new GuiToolTip(new Rectangle(progressX, progressY, progressScale, 10), "") {
      @Override
      protected void updateText() {
        text.clear();
        text.add(Math.round(GuiTelePad.this.getOwner().getProgress() * 100) + "%");
      }
    });

    if (TelePadConfig.telepadFluidUse.get() > 0) {
      addToolTip(new GuiToolTip(RECTANGLE_TANK, "") {
        @Override
        protected void updateText() {
          text.clear();
          text.add(Lang.GUI_TELEPAD_TANK.get());
          text.add(LangFluid.MB(te.getTank()));
        }
      });
    }

    FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

    int x = 48;
    int y = 24;
    int tfHeight = 12;
    int tfWidth = xSize - x * 2;
    xTF = new TextFieldEnder(fr, x, y, tfWidth, tfHeight, TextFieldEnder.FILTER_NUMERIC);
    yTF = new TextFieldEnder(fr, x, y + xTF.height + 2, tfWidth, tfHeight, TextFieldEnder.FILTER_NUMERIC);
    zTF = new TextFieldEnder(fr, x, y + (xTF.height * 2) + 4, tfWidth, tfHeight, TextFieldEnder.FILTER_NUMERIC);
    dimTF = new TextFieldEnder(fr, x, y + (xTF.height * 3) + 6, tfWidth, tfHeight, TextFieldEnder.FILTER_NUMERIC);

    xTF.setText(Integer.toString(te.getX()));
    yTF.setText(Integer.toString(te.getY()));
    zTF.setText(Integer.toString(te.getZ()));
    dimTF.setText(Integer.toString(te.getTargetDim()));

    xTF.setCanLoseFocus(!TelePadConfig.telepadLockCoords.get());
    yTF.setCanLoseFocus(!TelePadConfig.telepadLockCoords.get());
    zTF.setCanLoseFocus(!TelePadConfig.telepadLockCoords.get());
    dimTF.setCanLoseFocus(!TelePadConfig.telepadLockDimension.get());

    textFields.addAll(Lists.newArrayList(xTF, yTF, zTF, dimTF));

    addDrawingElement(new PowerBar(te.getEnergy(), this, powerX, powerY, 10, getPowerScale()));
  }

  @Override
  @Nullable
  public Object getIngredientUnderMouse(int mouseX, int mouseY) {
    if (RECTANGLE_TANK.contains(mouseX, mouseY)) {
      return getOwner().getTank().getFluid();
    }
    return super.getIngredientUnderMouse(mouseX, mouseY);
  }

  protected int getPowerOutputValue() {
    return getOwner().getUsage();
  }

  protected void updatePowerBarTooltip(List<String> text) {
    text.add(Lang.GUI_TELEPAD_MAX.get(LangPower.RFt(getPowerOutputValue())));
    text.add(LangPower.RF(getOwner().getEnergy().getEnergyStored(), getOwner().getEnergy().getMaxEnergyStored()));
  }

  @Override
  public void initGui() {
    super.initGui();

    String text = Lang.GUI_TELEPAD_TELEPORT.get();
    int textWidth = getFontRenderer().getStringWidth(text) + 10;

    int x = guiLeft + (xSize / 2) - (textWidth / 2);
    int y = guiTop + 83;

    teleportButton = new GuiButton(ID_TELEPORT_BUTTON, x, y, textWidth, 20, text);
    addButton(teleportButton);

    travelSettingsButton.onGuiInit();

    ((ContainerTelePad) inventorySlots).createGhostSlots(getGhostSlotHandler().getGhostSlots());
  }

  @Override
  public void updateScreen() {
    super.updateScreen();

    if (!xTF.isFocused()) {
      xTF.setText(Integer.toString(getOwner().getX()));
    }
    if (!yTF.isFocused()) {
      yTF.setText(Integer.toString(getOwner().getY()));
    }
    if (!zTF.isFocused()) {
      zTF.setText(Integer.toString(getOwner().getZ()));
    }
    if (!dimTF.isFocused()) {
      dimTF.setText(Integer.toString(getOwner().getTargetDim()));
    }
  }

  @Override
  protected void keyTyped(char par1, int par2) throws IOException {
    super.keyTyped(par1, par2);
    updateCoords();
  }

  private void updateCoords() {
    BlockPos pos = new BlockPos(getIntFromTextBox(xTF), getIntFromTextBox(yTF), getIntFromTextBox(zTF));
    int targetDim = getIntFromTextBox(dimTF);
    if (!pos.equals(getOwner().getTarget().getLocation()) || targetDim != getOwner().getTargetDim()) {
      getOwner().setCoords(pos);
      getOwner().setTargetDim(targetDim);
      PacketHandler.INSTANCE.sendToServer(new PacketSetTarget(getOwner(), getOwner().getTarget()));
    }
  }

  private int getIntFromTextBox(TextFieldEnder tf) {
    String text = tf.getText();
    if ("".equals(text) || "-".equals(text)) {
      return 0;
    }
    return Integer.parseInt(text);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    bindGuiTexture();
    int sx = (width - xSize) / 2;
    int sy = (height - ySize) / 2;

    drawTexturedModalRect(sx, sy, 0, 0, this.xSize, this.ySize);

    // draw power / fluid background
    int u = TelePadConfig.telepadFluidUse.get() > 0 ? 200 : 187;
    int v = 0;
    drawTexturedModalRect(sx + powerX - 1, sy + powerY - 1, u, v, 12, 122);

    if (TelePadConfig.telepadFluidUse.get() > 0 && getOwner().getFluidAmount() > 0) {
      RenderUtil.renderGuiTank(getOwner().getTank(), sx + fluidX, sy + fluidY, 0, 10, fluidScale);
      bindGuiTexture();
      drawTexturedModalRect(sx + fluidX, sy + fluidY, 213, v, 10, fluidScale);
    }

    int progressScaled = Util.getProgressScaled(progressScale, getOwner());
    drawTexturedModalRect(sx + progressX, sy + progressY, 0, ySize, progressScaled, 10);

    FontRenderer fnt = getFontRenderer();

    String[] text = { "X", "Y", "Z", "DIM" };
    for (int i = 0; i < text.length; i++) {
      TextFieldEnder f = textFields.get(i);
      fnt.drawString(NullHelper.first(text[i]), f.x - (fnt.getStringWidth(NullHelper.first(text[i], "")) / 2) - 10,
          f.y + ((f.height - fnt.FONT_HEIGHT) / 2) + 1, 0x000000);
      if (!f.getCanLoseFocus()) {
        IconEIO.map.render(IconEIO.LOCK_LOCKED, f.x + f.width - 2, f.y - 2, true);
      }
    }

    Entity e = getOwner().getCurrentTarget();
    if (e != null) {
      String name = e.getName();
      fnt.drawString(name, sx + xSize / 2 - fnt.getStringWidth(name) / 2, sy + progressY + fnt.FONT_HEIGHT + 6, 0x000000);
    } else if (getOwner().wasBlocked()) {
      String s = Lang.GUI_TELEPAD_ERROR_BLOCKED.get();
      fnt.drawString(s, sx + xSize / 2 - fnt.getStringWidth(s) / 2, sy + progressY + fnt.FONT_HEIGHT + 6, 0xAA0000);
    }

    String name = getOwner().getTarget().getName();
    fnt.drawStringWithShadow(name, sx + xSize / 2 - fnt.getStringWidth(name) / 2, getGuiTop() + 10, 0xffffff);

    super.drawGuiContainerBackgroundLayer(p_146976_1_, p_146976_2_, p_146976_3_);
  }

  @Override
  protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
    super.actionPerformed(button);
    if (button.id == ID_TELEPORT_BUTTON) {
      getOwner().teleportAll();
    } else if (button.id == ID_TRAVEL_SETTINGS_BUTTON) {
      PacketHandler.INSTANCE.sendToServer(new PacketOpenServerGui(getOwner(), BlockTelePad.GUI_ID_TELEPAD_TRAVEL));
    }
  }
}
