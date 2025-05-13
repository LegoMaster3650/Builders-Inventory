package _3650.builders_inventory.api.minimessage.validator;

import java.util.ArrayList;
import java.util.Optional;

import net.minecraft.client.Minecraft;

public class ChatMiniMessageValidatorRegistry {
	
	private static final ArrayList<MiniMessageValidator> VALIDATORS = new ArrayList<>();
	
	public static void register(MiniMessageValidator context) {
		VALIDATORS.add(context);
	}
	
	public static Optional<String> isValid(Minecraft minecraft, String value) {
		for (var context : VALIDATORS) {
			final var result = context.isValid(minecraft, value);
			if (result.isPresent()) return result;
		}
		return Optional.empty();
	}
	
}
