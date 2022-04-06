package binnie.core.gui.minecraft.control;

import binnie.core.api.gui.Alignment;
import binnie.core.api.gui.IWidget;
import binnie.core.gui.resource.textures.Texture;

public class ControlMachineProgress extends ControlProgress {
	public ControlMachineProgress(final IWidget parent, final int x, final int y, final Texture base, final Texture progress, final Alignment dir) {
		super(parent, x, y, base, progress, dir);
	}
}
