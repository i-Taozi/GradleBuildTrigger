package binnie.core.gui.database;

import binnie.core.Binnie;
import binnie.core.api.genetics.IBreedingSystem;
import binnie.core.api.gui.IWidget;
import binnie.core.gui.Attribute;
import binnie.core.gui.ITooltip;
import binnie.core.gui.Tooltip;
import binnie.core.gui.events.EventMouse;
import binnie.core.gui.geometry.Point;
import binnie.core.gui.minecraft.Window;
import binnie.core.gui.minecraft.control.ControlItemDisplay;
import binnie.core.gui.renderer.RenderUtil;
import binnie.core.util.I18N;
import com.mojang.authlib.GameProfile;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IAlleleSpecies;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ISpeciesRoot;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ControlIndividualDisplay extends ControlItemDisplay implements ITooltip {
	private EnumDiscoveryState discovered;
	@Nullable
	private IAlleleSpecies species;

	public ControlIndividualDisplay(final IWidget parent, final int x, final int y) {
		this(parent, x, y, 16);
	}

	public ControlIndividualDisplay(final IWidget parent, final int x, final int y, final int size) {
		super(parent, x, y, size);
		this.species = null;
		this.discovered = EnumDiscoveryState.SHOW;
		this.addSelfEventHandler(EventMouse.Down.class, event -> {
			if (event.getButton() == 0 && ControlIndividualDisplay.this.species != null && EnumDiscoveryState.SHOW == ControlIndividualDisplay.this.discovered) {
				((WindowAbstractDatabase) ControlIndividualDisplay.this.getTopParent()).gotoSpeciesDelayed(ControlIndividualDisplay.this.species);
			}
		});
	}

	public void setSpecies(final IAlleleSpecies species) {
		this.setSpecies(species, EnumDiscoveryState.SHOW);
	}

	public void setSpecies(final IAlleleSpecies species, EnumDiscoveryState state) {
		final ISpeciesRoot speciesRoot = Binnie.GENETICS.getSpeciesRoot(species);
		final IBreedingSystem system = Binnie.GENETICS.getSystem(speciesRoot);
		final IAllele[] template = system.getSpeciesRoot().getTemplate(species);
		final IIndividual ind = system.getSpeciesRoot().templateAsIndividual(template);
		super.setItemStack(system.getSpeciesRoot().getMemberStack(ind, system.getDefaultType()));
		this.species = species;
		final GameProfile username = Window.get(this).getUsername();
		if (state == EnumDiscoveryState.UNDETERMINED) {
			state = (system.isSpeciesDiscovered(species, Window.get(this).getWorld(), username) ? EnumDiscoveryState.DISCOVERED : EnumDiscoveryState.UNDISCOVERED);
		}
		if (Window.get(this) instanceof WindowAbstractDatabase && ((WindowAbstractDatabase) Window.get(this)).isMaster()) {
			state = EnumDiscoveryState.SHOW;
		}
		this.discovered = state;
		this.addAttribute(Attribute.MOUSE_OVER);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onRenderForeground(int guiWidth, int guiHeight) {

		TextureAtlasSprite icon = null;
		if (this.species == null) {
			return;
		}
		IBreedingSystem system = Binnie.GENETICS.getSystem(this.species.getRoot());
		switch (this.discovered) {
			case SHOW: {
				super.onRenderForeground(guiWidth, guiHeight);
				return;
			}
			case DISCOVERED: {
				icon = system.getDiscoveredIcon();
				break;
			}
			case UNDISCOVERED: {
				icon = system.getUndiscoveredIcon();
				break;
			}
		}
		if (icon != null) {
			RenderUtil.drawGuiSprite(Point.ZERO, icon);
		}
	}

	@Override
	public void getTooltip(final Tooltip tooltip, ITooltipFlag tooltipFlag) {
		if (this.species != null) {
			switch (this.discovered) {
				case SHOW: {
					tooltip.add(this.species.getAlleleName());
					break;
				}
				case DISCOVERED: {
					tooltip.add(I18N.localise(DatabaseConstants.DISCOVERED_KEY + ".discovered"));
					break;
				}
				case UNDISCOVERED: {
					tooltip.add(I18N.localise(DatabaseConstants.DISCOVERED_KEY + ".undiscovered"));
					break;
				}
			}
		}
	}

	public void setDiscovered(EnumDiscoveryState discovered) {
		this.discovered = discovered;
	}

	public EnumDiscoveryState getDiscovered() {
		return discovered;
	}
}
