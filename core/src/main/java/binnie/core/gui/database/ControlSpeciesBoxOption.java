package binnie.core.gui.database;

import binnie.core.gui.Attribute;
import binnie.core.gui.CraftGUI;
import binnie.core.gui.controls.listbox.ControlList;
import binnie.core.gui.controls.listbox.ControlTextOption;
import binnie.core.gui.geometry.CraftGUIUtil;
import binnie.core.gui.geometry.Point;
import forestry.api.genetics.IAlleleSpecies;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
class ControlSpeciesBoxOption extends ControlTextOption<IAlleleSpecies> {
	private final ControlIndividualDisplay controlBee;

	public ControlSpeciesBoxOption(final ControlList<IAlleleSpecies> controlList, final IAlleleSpecies option, final int y) {
		super(controlList, option, option.getAlleleName(), y);
		this.setSize(new Point(this.getSize().xPos(), 20));
		(this.controlBee = new ControlIndividualDisplay(this, 2, 2)).setSpecies(this.getValue(), EnumDiscoveryState.UNDETERMINED);
		EnumDiscoveryState discovered = controlBee.getDiscovered();
		if (discovered == EnumDiscoveryState.DISCOVERED) {
			this.controlBee.setDiscovered(discovered = EnumDiscoveryState.SHOW);
		}
		this.textWidget.setValue((discovered == EnumDiscoveryState.SHOW) ? option.getAlleleName() : DatabaseConstants.CONTROL_KEY + ".undiscovered");
		if (discovered == EnumDiscoveryState.SHOW) {
			this.addAttribute(Attribute.MOUSE_OVER);
		}
		CraftGUIUtil.moveWidget(this.textWidget, new Point(22, 0));
		this.textWidget.setSize(this.textWidget.getSize().sub(new Point(24, 0)));
		final int th = CraftGUI.RENDER.textHeight(this.textWidget.getValue(), this.textWidget.getSize().xPos());
		final int height = Math.max(20, th + 6);
		this.setSize(new Point(this.getSize().xPos(), height));
		this.textWidget.setSize(new Point(this.textWidget.getSize().xPos(), height));
		this.controlBee.setPosition(new Point(controlBee.getPosition().xPos(), (height - 18) / 2));
	}
}
