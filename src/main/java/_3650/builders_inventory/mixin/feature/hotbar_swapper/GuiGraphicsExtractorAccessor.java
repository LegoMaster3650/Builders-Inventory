package _3650.builders_inventory.mixin.feature.hotbar_swapper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlas;

@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorAccessor {
	
	@Accessor("guiSprites")
	public TextureAtlas getGuiSprites();
	
}
