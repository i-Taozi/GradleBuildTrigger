package binnie.core.triggers;

import binnie.core.machines.component.IBuildcraft;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerInternalSided;
import buildcraft.api.statements.ITriggerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

class TriggerProvider implements ITriggerProvider {
	static TriggerProvider instance;
	public static List<BinnieTrigger> triggers;

	@Override
	public void addInternalTriggers(Collection<ITriggerInternal> triggers, IStatementContainer container) {
	}

	@Override
	public void addInternalSidedTriggers(Collection<ITriggerInternalSided> triggers, IStatementContainer container, @Nonnull EnumFacing side) {
	}

	@Override
	public void addExternalTriggers(Collection<ITriggerExternal> triggers, @Nonnull EnumFacing side, TileEntity tile) {
		LinkedList<TriggerData> list = new LinkedList<>();
		if (tile instanceof IBuildcraft.TriggerProvider) {
			((IBuildcraft.TriggerProvider) tile).getTriggers(list);
		}
		for (final TriggerData data : list) {
			if (data.getKey() != null && data.getKey().getUniqueTag() != null) {
				triggers.add(data.getKey());
			}
		}
	}

	public static boolean isTriggerActive(final ITriggerExternal trigger, final TileEntity tile) {
		final LinkedList<TriggerData> list = new LinkedList<>();
		final LinkedList<ITriggerExternal> triggerData = new LinkedList<>();
		if (tile instanceof IBuildcraft.TriggerProvider) {
			((IBuildcraft.TriggerProvider) tile).getTriggers(list);
		}
		for (final TriggerData data : list) {
			if (data.getKey() == trigger) {
				return data.getValue();
			}
		}
		return false;
	}

	static {
		TriggerProvider.instance = new TriggerProvider();
		TriggerProvider.triggers = new ArrayList<>();
	}
}
