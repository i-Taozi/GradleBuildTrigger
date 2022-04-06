package crazypants.enderio.machines.machine.generator.zombie;

import javax.annotation.Nonnull;

import crazypants.enderio.base.machine.baselegacy.AbstractInventoryMachineEntity;
import crazypants.enderio.base.machine.gui.AbstractMachineContainer;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerZombieGenerator extends AbstractMachineContainer<AbstractInventoryMachineEntity> {

  public ContainerZombieGenerator(@Nonnull InventoryPlayer playerInv, @Nonnull AbstractInventoryMachineEntity te) {
    super(playerInv, te);
  }

  @Override
  protected void addMachineSlots(@Nonnull InventoryPlayer playerInv) {
  }

}
