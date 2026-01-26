package _3650.builders_inventory.api.minimessage.autocomplete;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import _3650.builders_inventory.api.minimessage.autocomplete.AutocompleteTagLookup.Suggestor;
import _3650.builders_inventory.mixin.feature.minimessage.resource.TextureAtlasAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.IdentifierException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.resources.Identifier;

public class AtlasSpriteSuggestor implements Suggestor {
	
	public static final AtlasSpriteSuggestor INSTANCE = new AtlasSpriteSuggestor();
	
	private Object2ObjectOpenHashMap<Identifier, AutocompleteArg> cache = new Object2ObjectOpenHashMap<>();
	
	@Override
	public List<String> suggest(@Nullable String prev, @NotNull String input) {
		Optional<AutocompleteArg> arg = this.getAtlasArg(prev);
		if (arg.isPresent()) return arg.get().findNonMatch(input);
		else return List.of();
	}
	
	public Optional<AutocompleteArg> getAtlasArg(String id) {
		if (id == null || id.isEmpty()) return Optional.empty();
		
		Identifier atlasId;
		try {
			atlasId = Identifier.withDefaultNamespace(id);
		} catch (IdentifierException e) {
			return Optional.empty();
		}
		if (cache.containsKey(atlasId)) return Optional.of(cache.get(atlasId));
		else return this.getAtlasArg(atlasId);
	}
	
	public Optional<AutocompleteArg> getAtlasArg(Identifier atlasId) {
		Minecraft mc = Minecraft.getInstance();
		AtlasManager atlases = mc.getAtlasManager();
		TextureAtlas atlas;
		try {
			atlas = atlases.getAtlasOrThrow(atlasId);
		} catch (Exception e) {
			return Optional.empty();
		}
		Map<Identifier, TextureAtlasSprite> texturesByName = ((TextureAtlasAccessor)atlas).getTexturesByName();
		ArrayList<String> spriteIds = texturesByName.keySet().stream()
				.map(Identifier::getPath)
				.sorted()
				.collect(Collectors.toCollection(() -> new ArrayList<>()));
		SimpleStringArg arg = SimpleStringArg.of(spriteIds);
		cache.put(atlasId, arg);
		return Optional.of(arg);
	}
	
	public void clearCache() {
		this.cache.clear();
	}
	
}
