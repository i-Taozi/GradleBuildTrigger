package crazypants.enderio.conduits.gui;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.gui.ITabPanel;
import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.client.gui.button.CheckBox;
import com.enderio.core.client.gui.button.IconButton;
import com.enderio.core.client.gui.widget.GuiToolTip;
import com.enderio.core.client.render.ColorUtil;

import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.conduit.ConnectionMode;
import crazypants.enderio.base.conduit.IClientConduit;
import crazypants.enderio.base.conduit.IGuiExternalConnection;
import crazypants.enderio.base.filter.network.IOpenFilterRemoteExec;
import crazypants.enderio.base.gui.IconEIO;
import crazypants.enderio.base.network.PacketHandler;
import crazypants.enderio.conduits.lang.Lang;
import crazypants.enderio.conduits.network.PacketConnectionMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

public class BaseSettingsPanel extends Gui implements ITabPanel, IOpenFilterRemoteExec.GUI {

  static final int ID_INSERT_ENABLED = 327;
  static final int ID_EXTRACT_ENABLED = 328;
  protected static final int ID_INSERT_FILTER_OPTIONS = 329;
  protected static final int ID_EXTRACT_FILTER_OPTIONS = 330;
  protected final int ID_ENABLED = 331;

  protected final @Nonnull IWidgetIcon icon;
  protected final @Nonnull IGuiExternalConnection gui;
  protected @Nonnull IClientConduit con;
  protected final @Nonnull String typeName;
  protected final @Nonnull ResourceLocation texture;

  protected ConnectionMode oldConnectionMode;

  private @Nonnull String inputHeading;
  private @Nonnull String outputHeading;

  private boolean insertEnabled = false;
  private boolean extractEnabled = false;

  private boolean enabled = false;

  private final @Nonnull CheckBox extractEnabledB;
  private final @Nonnull CheckBox insertEnabledB;

  private final @Nonnull CheckBox enabledB;

  private @Nonnull IconButton insertFilterOptionsB;
  private @Nonnull IconButton extractFilterOptionsB;
  private @Nonnull FakeButton functionUpgradeOptionsB;

  protected int left = 0;
  protected int top = 0;
  protected int width = 0;
  protected int height = 0;
  protected int rightColumn = 112;
  protected int leftColumn = 22;

  protected int gap = 5;

  protected int customTop = 0;

  private final @Nonnull GuiToolTip functionUpgradeTooltip;
  protected @Nonnull GuiToolTip filterExtractUpgradeTooltip;
  protected @Nonnull GuiToolTip filterInsertUpgradeTooltip;

  protected BaseSettingsPanel(@Nonnull IWidgetIcon icon, @Nonnull String typeName, @Nonnull IGuiExternalConnection gui, @Nonnull IClientConduit con,
      @Nonnull String texture) {
    this.icon = icon;
    this.typeName = typeName;
    this.gui = gui;
    this.con = con;
    this.texture = EnderIO.proxy.getGuiTexture(texture);

    inputHeading = getInputHeading();
    outputHeading = getOutputHeading();

    FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

    customTop = top + gap * 5 + fr.FONT_HEIGHT * 2;
    customTop -= 16;

    int x = leftColumn;
    int y = 6;

    insertEnabledB = new CheckBox(gui, ID_INSERT_ENABLED, x, y);

    enabledB = new CheckBox(gui, ID_ENABLED, x, y);

    x = rightColumn;

    extractEnabledB = new CheckBox(gui, ID_EXTRACT_ENABLED, x, y);

    x = leftColumn;
    y = 92;

    insertFilterOptionsB = new IconButton(gui, ID_INSERT_FILTER_OPTIONS, x, y, IconEIO.GEAR_LIGHT);
    insertFilterOptionsB.setToolTip(crazypants.enderio.base.lang.Lang.GUI_EDIT_ITEM_FILTER.get());

    x = rightColumn;

    extractFilterOptionsB = new IconButton(gui, ID_EXTRACT_FILTER_OPTIONS, x, y, IconEIO.GEAR_LIGHT);
    extractFilterOptionsB.setToolTip(crazypants.enderio.base.lang.Lang.GUI_EDIT_ITEM_FILTER.get());

    x = rightColumn + 18 + 1;

    functionUpgradeOptionsB = new FakeButton(gui, x, y, IconEIO.INFO) {
      @Override
      public boolean isVisible() {
        return hasUpgrades() && super.isVisible();
      }
    };
    functionUpgradeOptionsB.setToolTip(new GuiToolTip(new Rectangle(x, y, 18, 18), "") {
      @Override
      public boolean shouldDraw() {
        return hasUpgrades() && super.shouldDraw();
      }

      @Override
      public @Nonnull List<String> getToolTipText() {
        return gui.getContainer().getFunctionUpgradeToolTipText();
      }
    });

    filterExtractUpgradeTooltip = new GuiToolTip(new Rectangle(rightColumn, 70, 18, 18), Lang.GUI_ITEM_FILTER_UPGRADE.get()) {
      @Override
      public boolean shouldDraw() {
        return !gui.getContainer().hasFilter(false) && super.shouldDraw();
      }
    };

    filterInsertUpgradeTooltip = new GuiToolTip(new Rectangle(leftColumn, 70, 18, 18), Lang.GUI_ITEM_FILTER_UPGRADE.get()) {
      @Override
      public boolean shouldDraw() {
        return !gui.getContainer().hasFilter(true) && super.shouldDraw();
      }
    };

    functionUpgradeTooltip = new GuiToolTip(new Rectangle(rightColumn + 18, customTop + 43, 18, 18), Lang.GUI_ITEM_FUNCTION_UPGRADE.get(),
        Lang.GUI_ITEM_FUNCTION_UPGRADE_2.get(), Lang.GUI_ITEM_FUNCTION_UPGRADE_3.get()) {
      @Override
      public boolean shouldDraw() {
        return !gui.getContainer().hasFunctionUpgrade() && super.shouldDraw();
      }
    };

    gui.getContainer().setInOutSlotsVisible(false, false, con);

  }

  protected void updateFilterButtons() {
    if (gui.getContainer().hasFilter(true) && hasFilterGui(true)) {
      insertFilterOptionsB.setIsVisible(true);
    } else {
      insertFilterOptionsB.setIsVisible(false);
    }

    if (gui.getContainer().hasFilter(false) && hasFilterGui(false)) {
      extractFilterOptionsB.setIsVisible(true);
    } else {
      extractFilterOptionsB.setIsVisible(false);
    }
  }

  protected boolean hasFilterGui(boolean input) {
    return true;
  }

  public boolean updateConduit(@Nonnull IClientConduit conduit) {
    this.con = conduit;
    if (oldConnectionMode != con.getConnectionMode(gui.getDir())) {
      connectionModeChanged(con.getConnectionMode(gui.getDir()));
    }
    return true;
  }

  @Override
  public void onGuiInit(int leftIn, int topIn, int widthIn, int heightIn) {
    this.left = leftIn;
    this.top = topIn;
    this.width = widthIn;
    this.height = heightIn;

    updateConduit(con);

    if (hasInOutModes()) {
      insertEnabledB.onGuiInit();
      extractEnabledB.onGuiInit();

      insertEnabledB.setSelected(insertEnabled);
      extractEnabledB.setSelected(extractEnabled);
    } else {
      enabledB.onGuiInit();
      enabledB.setSelected(enabled);
    }

    if (hasFilters()) {
      gui.addToolTip(filterExtractUpgradeTooltip);
      gui.addToolTip(filterInsertUpgradeTooltip);
      insertFilterOptionsB.onGuiInit();
      extractFilterOptionsB.onGuiInit();
      insertFilterOptionsB.setIsVisible(false);
      extractFilterOptionsB.setIsVisible(false);
    }
    if (hasUpgrades()) {
      gui.addToolTip(functionUpgradeTooltip);
      functionUpgradeOptionsB.onGuiInit();
    }

    initCustomOptions();
  }

  protected boolean hasUpgrades() {
    return false;
  }

  protected void initCustomOptions() {
  }

  @Override
  public void deactivate() {
    insertEnabledB.detach();
    extractEnabledB.detach();
    insertFilterOptionsB.detach();
    extractFilterOptionsB.detach();
    functionUpgradeOptionsB.detach();

    gui.removeToolTip(functionUpgradeTooltip);
    gui.removeToolTip(filterExtractUpgradeTooltip);
    gui.removeToolTip(filterInsertUpgradeTooltip);
  }

  @Override
  public void mouseClicked(int x, int y, int par3) {
  }

  @Override
  public void keyTyped(char par1, int par2) {
  }

  @Override
  public void updateScreen() {
  }

  @Override
  @Nonnull
  public IWidgetIcon getIcon() {
    return icon;
  }

  @Override
  @Nonnull
  public ResourceLocation getTexture() {
    return texture;
  }

  private void updateConnectionMode() {
    ConnectionMode mode = ConnectionMode.DISABLED;
    if (insertEnabled && extractEnabled || enabled) {
      mode = ConnectionMode.IN_OUT;
    } else if (insertEnabled) {
      mode = ConnectionMode.OUTPUT;
    } else if (extractEnabled) {
      mode = ConnectionMode.INPUT;
    }
    PacketHandler.INSTANCE.sendToServer(new PacketConnectionMode(con, gui.getDir(), mode));
  }

  @Override
  public void actionPerformed(@Nonnull GuiButton guiButton) {
    if (guiButton.id == ID_INSERT_ENABLED) {
      insertEnabled = !insertEnabled;
      updateConnectionMode();
    } else if (guiButton.id == ID_EXTRACT_ENABLED) {
      extractEnabled = !extractEnabled;
      updateConnectionMode();
    } else if (guiButton.id == ID_ENABLED) {
      enabled = !enabled;
      updateConnectionMode();
    }
  }

  protected void connectionModeChanged(@Nonnull ConnectionMode mode) {
    oldConnectionMode = mode;
    if (hasInOutModes()) {
      insertEnabled = mode.acceptsOutput();
      extractEnabled = mode.acceptsInput();
    } else {
      enabled = mode == ConnectionMode.IN_OUT;
    }
  }

  @Override
  public void render(float par1, int par2, int par3) {
    updateFilterButtons();

    FontRenderer fr = gui.getFontRenderer();

    int rgb = ColorUtil.getRGB(Color.darkGray);
    int x = left + 32;
    int y = gui.getGuiTop() + 10;

    if (hasInOutModes()) {
      fr.drawString(inputHeading, x, y, rgb);
      x += 92;
      fr.drawString(outputHeading, x, y, rgb);
    } else {
      String heading = enabled ? getEnabledHeading() : getDisabledHeading();
      fr.drawString(heading, x, y, rgb);
    }
    renderCustomOptions(y + gap + fr.FONT_HEIGHT + gap, par1, par2, par3);
  }

  protected void renderCustomOptions(int topIn, float par1, int par2, int par3) {

  }

  protected @Nonnull String getTypeName() {
    return typeName;
  }

  protected boolean hasFilters() {
    return false;
  }

  @Override
  public void setGuiID(int id) {
    gui.setGuiID(id);
  }

  @Override
  public int getGuiID() {
    return gui.getGuiID();
  }

  @Nonnull
  protected String getInputHeading() {
    return Lang.GUI_CONDUIT_INSERT_MODE.get();
  }

  @Nonnull
  protected String getOutputHeading() {
    return Lang.GUI_CONDUIT_EXTRACT_MODE.get();
  }

  @Nonnull
  protected String getEnabledHeading() {
    return Lang.GUI_CONDUIT_ENABLED_MODE.get();
  }

  @Nonnull
  protected String getDisabledHeading() {
    return Lang.GUI_CONDUIT_DISABLED_MODE.get();
  }

  protected boolean hasInOutModes() {
    return true;
  }

}
