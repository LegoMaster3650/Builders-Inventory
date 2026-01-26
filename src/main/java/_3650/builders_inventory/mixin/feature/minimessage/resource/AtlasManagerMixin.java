package _3650.builders_inventory.mixin.feature.minimessage.resource;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import _3650.builders_inventory.api.minimessage.autocomplete.AtlasSpriteSuggestor;
import _3650.builders_inventory.api.minimessage.autocomplete.ReloadableResourceArg;
import _3650.builders_inventory.config.Config;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.resources.ResourceLocation;

@Mixin(AtlasManager.class)
public abstract class AtlasManagerMixin {
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void builders_inventory_captureAtlases(TextureManager textureManager, int maxMipmapLevels, CallbackInfo ci) {
		ArrayList<ResourceLocation> atlasIds = new ArrayList<>();
		((AtlasManager)(Object)this).forEach((atlasId, atlas) -> atlasIds.add(atlasId));
		ArrayList<String> atlases = atlasIds.stream()
				.map(ResourceLocation::getPath)
				.sorted()
				.collect(Collectors.toCollection(() -> new ArrayList<>()));
		ReloadableResourceArg.ATLASES.loadStr(atlases);
		
		AtlasSpriteSuggestor.INSTANCE.clearCache();
		if (Config.instance().minimessage_preloadAtlasData) {
			for (ResourceLocation atlasId : atlasIds) {
				AtlasSpriteSuggestor.INSTANCE.getAtlasArg(atlasId);
			}
		}
	}
	
}
