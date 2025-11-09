package _3650.builders_inventory.mixin.feature.hotbar_swapper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsAccessor {
	
	@Accessor("guiSprites")
	public TextureAtlas getGuiSprites();
	
}
