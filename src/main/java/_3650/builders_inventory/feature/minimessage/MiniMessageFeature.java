package _3650.builders_inventory.feature.minimessage;

import org.jetbrains.annotations.Nullable;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.api.minimessage.autocomplete.AutocompleteEvents;
import _3650.builders_inventory.api.minimessage.autocomplete.AutocompleteTagLookup;
import _3650.builders_inventory.api.minimessage.parser.MiniMessageParserRegistry;
import net.minecraft.client.Minecraft;

public class MiniMessageFeature {
	
	public static final AutocompleteTagLookup TAG_LOOKUP = new AutocompleteTagLookup();
	
	public static void init() {
		// register parser
		MiniMessageParserRegistry.register(new StandardMiniMessageParser());
		
		// register tags
		AutocompleteEvents.REGISTER_TAGS.register(StandardMiniMessageTags::onRegisterTags);
	}
	
	public static void onClientStarted(Minecraft mc) {
		reloadTagAutocomplete(null);
	}
	
	public static void reloadTagAutocomplete(@Nullable String server) {
		BuildersInventory.LOGGER.info("Loading Tag Autocomplete");
		// build tag lookups
		var builder = TAG_LOOKUP.builder();
		AutocompleteEvents.REGISTER_TAGS.invoker().onRegisterTags(builder, server);
		builder.end();
	}
	
}
