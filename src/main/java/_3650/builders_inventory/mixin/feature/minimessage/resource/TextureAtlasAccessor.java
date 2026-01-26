package _3650.builders_inventory.mixin.feature.minimessage.resource;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

@Mixin(TextureAtlas.class)
public interface TextureAtlasAccessor {
	
	@Accessor("texturesByName")
	public Map<Identifier, TextureAtlasSprite> getTexturesByName();
	
}
