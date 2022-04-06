package crazypants.enderio.base.machine.base.block;

import javax.annotation.Nonnull;

import com.enderio.core.common.inventory.InventorySlot;

import crazypants.enderio.api.IModObject;
import crazypants.enderio.base.capability.ItemTools;
import crazypants.enderio.base.machine.base.te.AbstractCapabilityMachineEntity;
import crazypants.enderio.base.machine.base.te.EnergyLogic;
import crazypants.enderio.base.power.forge.item.PoweredBlockItem;
import crazypants.enderio.util.Prep;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractCapabilityPoweredMachineBlock<T extends AbstractCapabilityMachineEntity> extends AbstractCapabilityMachineBlock<T> {

  protected AbstractCapabilityPoweredMachineBlock(@Nonnull IModObject mo) {
    super(mo);
  }

  public AbstractCapabilityPoweredMachineBlock(@Nonnull IModObject mo, @Nonnull Material mat) {
    super(mo, mat);
  }

  @Override
  public boolean onBlockActivated(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer entityPlayer,
      @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
    T machine = getTileEntity(world, pos);
    ItemStack heldItem = entityPlayer.getHeldItem(hand);
    if (Prep.isValid(heldItem) && machine != null && machine.isValidUpgrade(heldItem)) {
      InventorySlot upgradeSlot = machine.getInventory().getSlot(EnergyLogic.CAPSLOT);

      ItemStack currentCap = upgradeSlot.get();
      if (Prep.isInvalid(currentCap)) {
        upgradeSlot.set(ItemTools.oneOf(entityPlayer, heldItem));
        return true;
      } else if (!ItemStack.areItemsEqual(heldItem, currentCap)) {
        upgradeSlot.set(ItemTools.oneOf(entityPlayer, heldItem));
        if (!entityPlayer.inventory.addItemStackToInventory(currentCap)) {
          entityPlayer.dropItem(currentCap, true);
        }
        return true;
      }

    }

    return super.onBlockActivated(world, pos, state, entityPlayer, hand, side, hitX, hitY, hitZ);
  }

  @Override
  public PoweredBlockItem createBlockItem(@Nonnull IModObject modObject) {
    return modObject.apply(new PoweredBlockItem(this));
  }

}
