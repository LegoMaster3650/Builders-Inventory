package _3650.builders_inventory.api.minimessage.validator;

import java.util.Optional;
import java.util.function.Consumer;

import _3650.builders_inventory.api.minimessage.instance.HighlightedTextInput;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/**
 * Determines whether minimessage input should proceed and can modify the input text<br>
 * Also gets to fix any changes it made to the original text so the input displays properly :D
 */
public interface MiniMessageValidator {
	public static final MiniMessageValidator PASSTHROUGH = new UnmodifiedMMValidator();
	
	public Optional<String> isValid(Minecraft minecraft, String value, Consumer<MiniMessageValidator> validatorChanger);
	
	public void applyFormattedInput(String original, String modified, MutableComponent highlighted, HighlightedTextInput.Builder output);
	
	/*
	 * MiniMessage validator base type that doesn't modify the input in any way<br>
	 * Not default in the interface because I don't want any validator that DOES modify the input to fail to implement rebuildText
	 */
	public static class UnmodifiedMMValidator implements MiniMessageValidator {
		
		@Override
		public Optional<String> isValid(Minecraft minecraft, String value, Consumer<MiniMessageValidator> validatorChanger) {
			return Optional.ofNullable(value);
		}
		
		@Override
		public void applyFormattedInput(String original, String modified, MutableComponent highlighted, HighlightedTextInput.Builder output) {
			output.visit(highlighted, Style.EMPTY);
		}
		
	}
	
	
}
