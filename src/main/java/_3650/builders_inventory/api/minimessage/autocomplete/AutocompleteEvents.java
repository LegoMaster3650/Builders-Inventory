package _3650.builders_inventory.api.minimessage.autocomplete;

import org.jetbrains.annotations.Nullable;

import _3650.builders_inventory.api.minimessage.autocomplete.AutocompleteTagLookup.ACBuilder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class AutocompleteEvents {
	
	public static final Event<RegisterTags> REGISTER_TAGS = EventFactory.createArrayBacked(RegisterTags.class, callbacks -> (builder, server) -> {
		for (RegisterTags event : callbacks) {
			event.onRegisterTags(builder, server);
		}
	});
	
	@FunctionalInterface
	public interface RegisterTags {
		public void onRegisterTags(ACBuilder builder, @Nullable String server);
	}
	
}
