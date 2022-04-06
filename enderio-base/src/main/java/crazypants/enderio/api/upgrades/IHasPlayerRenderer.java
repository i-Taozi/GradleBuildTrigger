package crazypants.enderio.api.upgrades;

import javax.annotation.Nonnull;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This interface allows {@link IDarkSteelUpgrade}s and items that are in {@link EntityEquipmentSlot}s to contribute to the rendering of players.
 * <p>
 * The rendering happens as part of Ender IO's {@link LayerRenderer}&lt;{@link AbstractClientPlayer}&gt;.
 * 
 * @author Henry Loenwind
 *
 */
public interface IHasPlayerRenderer {

  /**
   * Returns a renderer that will be used to render the player model. The calling code will only use this renderer once and will ask any time it needs one. The
   * implementing code should cache it---using a single instance and configuring it for the specific item and player is fine.
   */
  @SideOnly(Side.CLIENT)
  default @Nonnull IRenderUpgrade getRender(@Nonnull AbstractClientPlayer player) {
    return RenderUpgradeHelper.NULL_RENDERER;
  }

}
