package binnie.genetics.machine;

import binnie.core.machines.IMachineType;
import binnie.core.machines.MachinePackage;
import binnie.core.machines.TileEntityMachine;
import binnie.genetics.machine.inoculator.PackageInoculator;
import binnie.genetics.machine.isolator.PackageIsolator;
import binnie.genetics.machine.polymeriser.PackagePolymeriser;
import binnie.genetics.machine.sequencer.PackageSequencer;
import binnie.genetics.modules.ModuleMachine;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.function.Supplier;

public enum GeneticMachine implements IMachineType {
	Isolator(PackageIsolator::new),
	Sequencer(PackageSequencer::new),
	Polymeriser(PackagePolymeriser::new),
	Inoculator(PackageInoculator::new);

	private final Supplier<MachinePackage> supplier;

	GeneticMachine(final Supplier<MachinePackage> supplier) {
		this.supplier = supplier;
	}

	@Override
	public Supplier<MachinePackage> getSupplier() {
		return supplier;
	}

	public ItemStack get(final int i) {
		return new ItemStack(ModuleMachine.getPackageGenetic().getBlock(), i, this.ordinal());
	}

	public abstract static class PackageGeneticBase extends MachinePackage {
		private final int colour;

		protected PackageGeneticBase(final String uid, final int flashColour) {
			super(uid);
			this.colour = flashColour;
		}

		@Override
		public TileEntity createTileEntity() {
			return new TileEntityMachine(this);
		}

	}
}
