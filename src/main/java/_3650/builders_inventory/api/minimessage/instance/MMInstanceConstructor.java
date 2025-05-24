package _3650.builders_inventory.api.minimessage.instance;

import _3650.builders_inventory.api.minimessage.instance.MiniMessageInstance.PreviewOptions;
import _3650.builders_inventory.api.minimessage.instance.MiniMessageInstance.SuggestionOptions;
import _3650.builders_inventory.api.minimessage.validator.MiniMessageValidator;
import _3650.builders_inventory.api.minimessage.widgets.wrapper.WrappedTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;

@FunctionalInterface
public interface MMInstanceConstructor {
	public static MMInstanceConstructor standardWidget(Minecraft minecraft, Screen screen, Font font) {
		return (input, listener) -> new MiniMessageInstance(
				minecraft,
				screen,
				font,
				input,
				MiniMessageValidator.ALWAYS,
				listener,
				PreviewOptions.standard(false),
				SuggestionOptions.standard(7));
	}
	
	public MiniMessageInstance construct(WrappedTextField input, LastParseListener listener);
}
