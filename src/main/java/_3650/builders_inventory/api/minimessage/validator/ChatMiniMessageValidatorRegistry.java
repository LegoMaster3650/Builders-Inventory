package _3650.builders_inventory.api.minimessage.validator;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;

public class ChatMiniMessageValidatorRegistry {
	
	private static final ArrayList<MiniMessageValidator> VALIDATORS = new ArrayList<>();
	
	public static void register(MiniMessageValidator context) {
		VALIDATORS.add(context);
	}
	
	public static Optional<String> isValid(Minecraft minecraft, String value, Consumer<MiniMessageValidator> validatorChanger) {
		for (var context : VALIDATORS) {
			final Optional<String> result = context.isValid(minecraft, value, validatorChanger);
			if (result.isPresent()) {
				validatorChanger.accept(context);
				return result;
			}
		}
		return Optional.empty();
	}
	
}
