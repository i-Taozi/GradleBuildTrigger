package binnie.core.gui.controls.core;

import binnie.core.api.gui.IArea;
import binnie.core.api.gui.IWidget;
import binnie.core.gui.Attribute;
import binnie.core.gui.ITooltip;
import binnie.core.gui.ITooltipHelp;
import binnie.core.gui.Tooltip;
import binnie.core.gui.Widget;
import binnie.core.gui.geometry.Point;
import binnie.core.gui.minecraft.Window;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class Control extends Widget implements ITooltipHelp, ITooltip {
	private final List<String> helpStrings;
	private final List<String> tooltipStrings;

	public Control(IWidget parent, int xPos, int yPos, int width, int height) {
		super(parent);
		this.helpStrings = new ArrayList<>();
		this.tooltipStrings = new ArrayList<>();
		this.position = new Point(xPos, yPos);
		this.size = new Point(width, height);
	}

	public Control(IWidget parent, IArea area) {
		this(parent, area.xPos(), area.yPos(), area.width(), area.height());
	}

	public void addHelp(final String string) {
		this.helpStrings.add(string);
	}

	public void addHelp(final String[] strings) {
		for (final String string : strings) {
			this.addHelp(string);
		}
	}

	public void addTooltip(final String string) {
		this.addAttribute(Attribute.MOUSE_OVER);
		this.tooltipStrings.add(string);
	}

	public void addTooltip(final String[] strings) {
		for (final String string : strings) {
			this.addTooltip(string);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getHelpTooltip(final Tooltip tooltip, ITooltipFlag tooltipFlag) {
		tooltip.add(this.helpStrings);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getTooltip(final Tooltip tooltip, ITooltipFlag tooltipFlag) {
		tooltip.add(this.tooltipStrings);
	}

	public Window getWindow() {
		return (Window) this.getTopParent();
	}
}
