package binnie.core.gui.database;

import binnie.core.api.genetics.IBreedingSystem;
import binnie.core.api.gui.IArea;
import binnie.core.api.gui.IWidget;
import binnie.core.gui.CraftGUI;
import binnie.core.gui.controls.core.Control;
import binnie.core.gui.geometry.Point;
import binnie.core.gui.renderer.RenderUtil;
import binnie.core.gui.resource.textures.CraftGUITexture;
import binnie.core.gui.resource.textures.CraftGUITextureSheet;
import binnie.core.gui.resource.textures.StandardTexture;
import binnie.core.gui.resource.textures.Texture;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

class ControlBreedingProgress extends Control {
	private static final Texture Progress = new StandardTexture(80, 22, 4, 4, CraftGUITextureSheet.CONTROLS);
	private final float percentage;
	private final int colour;

	public ControlBreedingProgress(final IWidget parent, final int x, final int y, final int width, final int height, final IBreedingSystem system, final float percentage) {
		super(parent, x, y, width, height);
		this.percentage = percentage;
		this.colour = system.getColour();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onRenderBackground(int guiWidth, int guiHeight) {
		CraftGUI.RENDER.texture(CraftGUITexture.PANEL_BLACK, this.getArea());
		final IArea area = this.getArea().inset(1);
		area.setSize(new Point(Math.round(area.size().xPos() * this.percentage), area.size().yPos()));
		RenderUtil.setColour(this.colour);
		CraftGUI.RENDER.texture(ControlBreedingProgress.Progress, area);
	}
}
