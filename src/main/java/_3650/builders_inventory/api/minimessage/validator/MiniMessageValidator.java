package _3650.builders_inventory.api.minimessage.validator;

import java.util.Optional;

import net.minecraft.client.Minecraft;

/**
 * Determines whether minimessage input should proceed and can modify the input text
 */
public interface MiniMessageValidator {
	
	public static final MiniMessageValidator ALWAYS = (minecraft, value) -> Optional.ofNullable(value);
	
	public Optional<String> isValid(Minecraft minecraft, String value);
	
}
