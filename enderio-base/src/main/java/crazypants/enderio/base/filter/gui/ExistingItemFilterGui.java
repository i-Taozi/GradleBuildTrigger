package crazypants.enderio.base.filter.gui;

import java.awt.Rectangle;
import java.io.IOException;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.gui.IGuiOverlay;
import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.client.gui.button.IconButton;
import com.enderio.core.client.gui.button.ToggleButton;
import com.enderio.core.client.gui.button.TooltipButton;
import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.util.NNList;
import com.enderio.core.common.vecmath.Vector4f;

import crazypants.enderio.base.filter.item.ExistingItemFilter;
import crazypants.enderio.base.filter.item.IItemFilter;
import crazypants.enderio.base.filter.network.PacketExistingItemFilterSnapshot;
import crazypants.enderio.base.gui.IconEIO;
import crazypants.enderio.base.lang.Lang;
import crazypants.enderio.base.network.PacketHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class ExistingItemFilterGui extends AbstractFilterGui {

  private static final int ID_NBT = FilterGuiUtil.nextButtonId();
  private static final int ID_META = FilterGuiUtil.nextButtonId();
  private static final int ID_ORE_DICT = FilterGuiUtil.nextButtonId();
  private static final int ID_STICKY = FilterGuiUtil.nextButtonId();

  private static final int ID_SNAPSHOT = FilterGuiUtil.nextButtonId();
  private static final int ID_CLEAR = FilterGuiUtil.nextButtonId();
  private static final int ID_SHOW = FilterGuiUtil.nextButtonId();
  private static final int ID_MERGE = FilterGuiUtil.nextButtonId();
  private static final int ID_WHITELIST = FilterGuiUtil.nextButtonId();

  private @Nonnull ToggleButton useMetaB;
  private @Nonnull ToggleButton useNbtB;
  private @Nonnull ToggleButton useOreDictB;
  private @Nonnull ToggleButton stickyB;

  private final @Nonnull IconButton whiteListB;

  private @Nonnull TooltipButton snapshotB;
  private @Nonnull TooltipButton clearB;
  private @Nonnull TooltipButton showB;
  private @Nonnull TooltipButton mergeB;
  private @Nonnull SnapshotOverlay snapshotOverlay;

  private @Nonnull ExistingItemFilter filter;

  public ExistingItemFilterGui(@Nonnull InventoryPlayer playerInv, @Nonnull ContainerFilter filterContainer, TileEntity te, @Nonnull IItemFilter filterIn) {
    super(playerInv, filterContainer, te, filterIn);

    filter = (ExistingItemFilter) filterIn;

    int butLeft = 20;
    int x = getGuiLeft() + butLeft;
    int y = getGuiTop() + 36;

    useMetaB = new ToggleButton(this, ID_META, x, y, IconEIO.FILTER_META_OFF, IconEIO.FILTER_META);
    useMetaB.setSelectedToolTip(Lang.GUI_ITEM_FILTER_MATCH_META.get());
    useMetaB.setUnselectedToolTip(Lang.GUI_ITEM_FILTER_IGNORE_META.get());
    useMetaB.setPaintSelectedBorder(false);

    x += 20;
    whiteListB = new IconButton(this, ID_WHITELIST, x, y, IconEIO.FILTER_WHITELIST);
    whiteListB.setToolTip(Lang.GUI_ITEM_FILTER_WHITELIST.get());

    x += 20;
    useNbtB = new ToggleButton(this, ID_NBT, x, y, IconEIO.FILTER_NBT_OFF, IconEIO.FILTER_NBT);
    useNbtB.setSelectedToolTip(Lang.GUI_ITEM_FILTER_MATCH_NBT.get());
    useNbtB.setUnselectedToolTip(Lang.GUI_ITEM_FILTER_IGNORE_NBT.get());
    useNbtB.setPaintSelectedBorder(false);

    x += 20;
    useOreDictB = new ToggleButton(this, ID_ORE_DICT, x, y, IconEIO.FILTER_ORE_DICT_OFF, IconEIO.FILTER_ORE_DICT);
    useOreDictB.setSelectedToolTip(Lang.GUI_ITEM_FILTER_ORE_DIC_ENABLED.get());
    useOreDictB.setUnselectedToolTip(Lang.GUI_ITEM_FILTER_ORE_DIC_DISABLED.get());
    useOreDictB.setPaintSelectedBorder(false);

    x += 20;
    stickyB = new ToggleButton(this, ID_STICKY, x, y, IconEIO.FILTER_STICKY_OFF, IconEIO.FILTER_STICKY);
    stickyB.setSelectedToolTip(Lang.GUI_ITEM_FILTER_STICKY_ENABLED.get(), Lang.GUI_ITEM_FILTER_STICKY_ENABLED_2.get());
    stickyB.setUnselectedToolTip(Lang.GUI_ITEM_FILTER_STICKY_DISABLED.get());
    stickyB.setPaintSelectedBorder(false);

    x = butLeft;
    y += 24;

    snapshotB = new TooltipButton(this, ID_SNAPSHOT, x, y, 60, 20, Lang.GUI_EXISTING_ITEM_FILTER_SNAPSHOT.get());

    x += 64;

    mergeB = new TooltipButton(this, ID_MERGE, x, y, 40, 20, Lang.GUI_EXISTING_ITEM_FILTER_MERGE.get());

    x -= 64;
    y += 24;

    clearB = new TooltipButton(this, ID_CLEAR, x, y, 60, 20, Lang.GUI_EXISTING_ITEM_FILTER_CLEAR.get());

    x += 64;
    showB = new TooltipButton(this, ID_SHOW, x, y, 40, 20, Lang.GUI_EXISTING_ITEM_FILTER_SHOW.get());

    snapshotB.setToolTip(Lang.GUI_EXISTING_ITEM_FILTER_SNAPSHOT_2.get());
    mergeB.setToolTip(Lang.GUI_EXISTING_ITEM_FILTER_MERGE_2.get());
    clearB.setToolTip(Lang.GUI_EXISTING_ITEM_FILTER_CLEAR_2.get());
    showB.setToolTip(Lang.GUI_EXISTING_ITEM_FILTER_SHOW_2.get());

    snapshotOverlay = new SnapshotOverlay();
    addOverlay(snapshotOverlay);

  }

  @Override
  public void updateButtons() {
    super.updateButtons();

    useNbtB.onGuiInit();
    useNbtB.setSelected(filter.isMatchNBT());

    useOreDictB.onGuiInit();
    useOreDictB.setSelected(filter.isUseOreDict());

    if (isStickyModeAvailable) {
      stickyB.onGuiInit();
      stickyB.setSelected(filter.isSticky());
    }

    useMetaB.onGuiInit();
    useMetaB.setSelected(filter.isMatchMeta());

    whiteListB.onGuiInit();
    if (filter.isBlacklist()) {
      whiteListB.setIcon(IconEIO.FILTER_BLACKLIST);
      whiteListB.setToolTip(Lang.GUI_ITEM_FILTER_BLACKLIST.get());
    } else {
      whiteListB.setIcon(IconEIO.FILTER_WHITELIST);
      whiteListB.setToolTip(Lang.GUI_ITEM_FILTER_WHITELIST.get());
    }

    snapshotB.onGuiInit();
    clearB.onGuiInit();
    mergeB.onGuiInit();
    showB.onGuiInit();

    clearB.setEnabled(filter.getSnapshot() != null);
    mergeB.setEnabled(clearB.isEnabled());
    showB.setEnabled(clearB.isEnabled());
  }

  @Override
  public void actionPerformed(@Nonnull GuiButton guiButton) throws IOException {
    super.actionPerformed(guiButton);
    if (guiButton.id == ID_META) {
      filter.setMatchMeta(useMetaB.isSelected());
      sendFilterChange();
    } else if (guiButton.id == ID_NBT) {
      filter.setMatchNBT(useNbtB.isSelected());
      sendFilterChange();
    } else if (guiButton.id == ID_STICKY) {
      filter.setSticky(stickyB.isSelected());
      sendFilterChange();
    } else if (guiButton.id == ID_ORE_DICT) {
      filter.setUseOreDict(useOreDictB.isSelected());
      sendFilterChange();
    } else if (guiButton.id == ID_SNAPSHOT) {
      sendSnapshotPacket(PacketExistingItemFilterSnapshot.Opcode.SET);
    } else if (guiButton.id == ID_CLEAR) {
      sendSnapshotPacket(PacketExistingItemFilterSnapshot.Opcode.CLEAR);
    } else if (guiButton.id == ID_MERGE) {
      sendSnapshotPacket(PacketExistingItemFilterSnapshot.Opcode.MERGE);
    } else if (guiButton.id == ID_SHOW) {
      showSnapshotOverlay();
    } else if (guiButton.id == ID_WHITELIST) {
      filter.setBlacklist(!filter.isBlacklist());
      sendFilterChange();
    }
  }

  @Override
  public void updateScreen() {
    updateSnapshotButtons();
    super.updateScreen();
  }

  private void updateSnapshotButtons() {
    // TODO Make this a callback based thing, current implementation does not sync well between server and client
    // filter = (ExistingItemFilter) filterContainer.getItemFilter();
    clearB.setEnabled(filter.getSnapshot() != null);
    mergeB.setEnabled(clearB.isEnabled());
    showB.setEnabled(clearB.isEnabled());
  }

  private void showSnapshotOverlay() {
    snapshotOverlay.setIsVisible(true);
  }

  private void sendSnapshotPacket(@Nonnull PacketExistingItemFilterSnapshot.Opcode opcode) {
    TileEntity te = filterContainer.getTileEntity();
    if (te != null) {
      PacketHandler.INSTANCE
          .sendToServer(new PacketExistingItemFilterSnapshot(te, filter, filterContainer.getFilterIndex(), filterContainer.getParam1(), opcode));
    }
  }

  class SnapshotOverlay implements IGuiOverlay {

    boolean visible;

    @Override
    public void init(@Nonnull IGuiScreen screen) {
    }

    @Override
    public @Nonnull Rectangle getBounds() {
      return new Rectangle(0, 0, xSize, ySize);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTick) {
      RenderHelper.enableGUIStandardItemLighting();
      GlStateManager.enableBlend();

      RenderUtil.renderQuad2D(8, 8, 0, getXSize() - 11, getYSize() - 11, new Vector4f(0, 0, 0, 1));
      RenderUtil.renderQuad2D(10, 10, 0, getXSize() - 15, getYSize() - 15, new Vector4f(0.6, 0.6, 0.6, 1));

      RenderItem itemRenderer = mc.getRenderItem();
      GlStateManager.enableDepth();

      NNList<ItemStack> snapshot = filter.getSnapshot();
      int x = 15;
      int y = 10;
      int count = 0;
      for (ItemStack st : snapshot) {
        if (!st.isEmpty()) {
          itemRenderer.renderItemAndEffectIntoGUI(st, x, y);
        }
        x += 20;
        count++;
        if (count % 9 == 0) {
          x = 15;
          y += 20;
        }
      }
    }

    @Override
    public void setIsVisible(boolean visible) {
      this.visible = visible;
    }

    @Override
    public boolean isVisible() {
      return visible;
    }

    @Override
    public boolean handleMouseInput(int x, int y, int b) {
      return true;
    }

    @Override
    public boolean isMouseInBounds(int mouseX, int mouseY) {
      return getBounds().contains(mouseX - guiLeft, mouseY - guiTop);
    }

    @Override
    public void guiClosed() {
    }

  }

  @Override
  @Nonnull
  protected String getUnlocalisedNameForHeading() {
    return Lang.GUI_EXISTING_ITEM_FILTER.get();
  }
}
