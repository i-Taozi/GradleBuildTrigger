package crazypants.enderio.base.item.darksteel.upgrade.storage;

import java.awt.Point;

import javax.annotation.Nonnull;

import com.enderio.core.common.ContainerEnderCap;

import crazypants.enderio.util.EIOCombinedInvWrapper;
import info.loenwind.processor.RemoteCall;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

@RemoteCall
public class StorageContainer extends ContainerEnderCap<EIOCombinedInvWrapper<StorageCap>, TileEntity> {

  private static final int X0 = 8;
  private static final int Y0 = 10;

  private final @Nonnull StorageCap feet, legs, body, head;

  protected @Nonnull EntityEquipmentSlot activeTab = EntityEquipmentSlot.CHEST;

  public StorageContainer(@Nonnull InventoryPlayer playerInv, @Nonnull StorageCap feet, @Nonnull StorageCap legs, @Nonnull StorageCap body,
      @Nonnull StorageCap head) {
    super(playerInv, new EIOCombinedInvWrapper<>(feet, legs, body, head), null);
    this.feet = feet;
    this.legs = legs;
    this.body = body;
    this.head = head;
  }

  @Override
  protected void addSlots() {
    int xoff = 0, x = 0, y = 0;
    EntityEquipmentSlot last = null;
    for (int i = 0; i < getItemHandler().getSlots(); i++) {
      EntityEquipmentSlot current = getItemHandler().getHandlerFromSlot(i).getEquipmentSlot();
      if (current != last) {
        xoff = (9 - StorageUpgrade.cols(current)) / 2;
        x = 0;
        y = 0;
        last = current;
      }
      addSlotToContainer(new BaseSlotItemHandler(getItemHandler(), i, X0 + 18 * (x + xoff), Y0 + 18 * y) {
        @Override
        public boolean isEnabled() {
          return activeTab == ((EIOCombinedInvWrapper<StorageCap>) getItemHandler()).getHandlerFromSlot(getSlotIndex()).getEquipmentSlot();
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
          // stops shift-clicking items in. at least while activeTab is in sync between client and server
          return isEnabled() && super.isItemValid(stack);
        }
      });
      x++;
      if (x >= StorageUpgrade.cols(current)) {
        x = 0;
        y++;
      }
    }
  }

  @Override
  public @Nonnull Point getPlayerInventoryOffset() {
    Point p = super.getPlayerInventoryOffset();
    p.translate(8, 70);
    return p;
  }

  @Override
  public void onContainerClosed(@Nonnull EntityPlayer playerIn) {
    feet.onContentsChanged(0);
    legs.onContentsChanged(0);
    body.onContentsChanged(0);
    head.onContentsChanged(0);
    super.onContainerClosed(playerIn);
  }

  @RemoteCall
  public IMessage setTab(@Nonnull EntityEquipmentSlot tab) {
    activeTab = tab;
    return null;
  }

  @Override
  public boolean canInteractWith(@Nonnull EntityPlayer player) {
    return feet.isStillConnectedToPlayer() && legs.isStillConnectedToPlayer() && body.isStillConnectedToPlayer() && head.isStillConnectedToPlayer();
  }

}
