package crazypants.enderio.base.filter.gui;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.enderio.core.client.gui.button.CycleButton;
import com.enderio.core.client.gui.button.IconButton;
import com.enderio.core.client.gui.button.ToggleButton;

import crazypants.enderio.base.filter.item.IItemFilter;
import crazypants.enderio.base.filter.item.ItemFilter;
import crazypants.enderio.base.gui.IconEIO;
import crazypants.enderio.base.lang.Lang;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;

public class BasicItemFilterGui extends AbstractFilterGui {

  private static final int ID_WHITELIST = FilterGuiUtil.nextButtonId();
  private static final int ID_NBT = FilterGuiUtil.nextButtonId();
  private static final int ID_META = FilterGuiUtil.nextButtonId();
  private static final int ID_ORE_DICT = FilterGuiUtil.nextButtonId();
  private static final int ID_STICKY = FilterGuiUtil.nextButtonId();
  private static final int ID_DAMAGE = FilterGuiUtil.nextButtonId();

  private final ToggleButton useMetaB;
  private final ToggleButton useNbtB;
  private final IconButton whiteListB;
  private final ToggleButton useOreDictB;
  private final ToggleButton stickyB;
  private final CycleButton<DamageModeIconHolder> damageB;

  final boolean isAdvanced, isLimited, isBig;

  private final @Nonnull ItemFilter filter;

  private int xOffset;
  private int yOffset;

  public BasicItemFilterGui(@Nonnull InventoryPlayer playerInv, @Nonnull ContainerFilter filterContainer, TileEntity te, @Nonnull IItemFilter filter) {
    this(playerInv, filterContainer, 13, 34, te, filter);
  }

  public BasicItemFilterGui(@Nonnull InventoryPlayer playerInv, @Nonnull ContainerFilter filterContainer, int xOffset, int yOffset, TileEntity te,
      @Nonnull IItemFilter filterIn) {
    super(playerInv, filterContainer, te, filterIn, "basic_item_filter", "advanced_item_filter", "big_item_filter");
    this.xOffset = xOffset;
    this.yOffset = yOffset;

    filter = (ItemFilter) filterIn;

    isAdvanced = filter.isAdvanced();
    isLimited = filter.isLimited();
    isBig = filter.isBig();

    int butLeft = xOffset + 98;
    int x = butLeft;
    int y = yOffset + 1;

    if (isBig) {
      y = 13;
      x = isAdvanced ? x - 53 : x + 27;
    }
    whiteListB = new IconButton(this, ID_WHITELIST, x, y, IconEIO.FILTER_WHITELIST);
    whiteListB.setToolTip(Lang.GUI_ITEM_FILTER_WHITELIST.get());

    x += 20;
    useMetaB = new ToggleButton(this, ID_META, x, y, IconEIO.FILTER_META_OFF, IconEIO.FILTER_META);
    useMetaB.setSelectedToolTip(Lang.GUI_ITEM_FILTER_MATCH_META.get());
    useMetaB.setUnselectedToolTip(Lang.GUI_ITEM_FILTER_IGNORE_META.get());
    useMetaB.setPaintSelectedBorder(false);

    x += 20;
    stickyB = new ToggleButton(this, ID_STICKY, x, y, IconEIO.FILTER_STICKY_OFF, IconEIO.FILTER_STICKY);
    stickyB.setSelectedToolTip(Lang.GUI_ITEM_FILTER_STICKY_ENABLED.get(), Lang.GUI_ITEM_FILTER_STICKY_ENABLED_2.get());
    stickyB.setUnselectedToolTip(Lang.GUI_ITEM_FILTER_STICKY_DISABLED.get());
    stickyB.setPaintSelectedBorder(false);

    if (!isBig) {
      y += 20;
      x = butLeft;
    } else {
      x += 20;
    }

    useOreDictB = new ToggleButton(this, ID_ORE_DICT, x, y, IconEIO.FILTER_ORE_DICT_OFF, IconEIO.FILTER_ORE_DICT);
    useOreDictB.setSelectedToolTip(Lang.GUI_ITEM_FILTER_ORE_DIC_ENABLED.get());
    useOreDictB.setUnselectedToolTip(Lang.GUI_ITEM_FILTER_ORE_DIC_DISABLED.get());
    useOreDictB.setPaintSelectedBorder(false);

    x += 20;
    useNbtB = new ToggleButton(this, ID_NBT, x, y, IconEIO.FILTER_NBT_OFF, IconEIO.FILTER_NBT);
    useNbtB.setSelectedToolTip(Lang.GUI_ITEM_FILTER_MATCH_NBT.get());
    useNbtB.setUnselectedToolTip(Lang.GUI_ITEM_FILTER_IGNORE_NBT.get());
    useNbtB.setPaintSelectedBorder(false);

    x += 20;
    damageB = new CycleButton<DamageModeIconHolder>(this, ID_DAMAGE, x, y, DamageModeIconHolder.class);
  }

  public void createFilterSlots() {
    filter.createGhostSlots(getGhostSlotHandler().getGhostSlots(), xOffset + 1, yOffset + 1, new Runnable() {
      @Override
      public void run() {
        sendFilterChange();
      }
    });
  }

  @Override
  public void initGui() {
    createFilterSlots();
    super.initGui();
  }

  @Override
  public void updateButtons() {
    super.updateButtons();
    ItemFilter activeFilter = filter;

    if (isAdvanced) {
      useNbtB.onGuiInit();
      useNbtB.setSelected(activeFilter.isMatchNBT());

      useOreDictB.onGuiInit();
      useOreDictB.setSelected(activeFilter.isUseOreDict());

      if (isStickyModeAvailable) {
        stickyB.onGuiInit();
        stickyB.setSelected(activeFilter.isSticky());
      }

      damageB.onGuiInit();
      damageB.setMode(DamageModeIconHolder.getFromMode(activeFilter.getDamageMode()));
    }

    useMetaB.onGuiInit();
    useMetaB.setSelected(activeFilter.isMatchMeta());

    if (!isLimited) {
      whiteListB.onGuiInit();
      if (activeFilter.isBlacklist()) {
        whiteListB.setIcon(IconEIO.FILTER_BLACKLIST);
        whiteListB.setToolTip(Lang.GUI_ITEM_FILTER_BLACKLIST.get());
      } else {
        whiteListB.setIcon(IconEIO.FILTER_WHITELIST);
        whiteListB.setToolTip(Lang.GUI_ITEM_FILTER_WHITELIST.get());
      }
    }

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
    } else if (guiButton.id == ID_DAMAGE) {
      filter.setDamageMode(damageB.getMode().getMode());
      sendFilterChange();
    } else if (guiButton.id == ID_WHITELIST) {
      filter.setBlacklist(!filter.isBlacklist());
      sendFilterChange();
    }
  }

  @Override
  public void bindGuiTexture() {
    super.bindGuiTexture(isBig ? 2 : (isAdvanced ? 1 : 0));
  }

  @Override
  @Nonnull
  protected String getUnlocalisedNameForHeading() {
    if (filter.isBig()) {
      return filter.isAdvanced() ? Lang.GUI_BIG_ADVANCED_ITEM_FILTER.get() : Lang.GUI_BIG_ITEM_FILTER.get();
    } else if (filter.isLimited()) {
      return Lang.GUI_LIMITED_ITEM_FILTER.get();
    } else if (filter.isAdvanced()) {
      return Lang.GUI_ADVANCED_ITEM_FILTER.get();
    } else {
      return Lang.GUI_BASIC_ITEM_FILTER.get();
    }
  }

  @Override
  @Nonnull
  protected String getDocumentationPage() {
    return super.getDocumentationPage() + (isAdvanced ? "_advanced" : "") + (isLimited ? "_limited" : "") + (isBig ? "_big" : "");
  }
}
