package _3650.builders_inventory.api.minimessage.widgets;

import _3650.builders_inventory.api.minimessage.instance.LastParseListener;
import _3650.builders_inventory.api.minimessage.instance.MiniMessageInstance;
import _3650.builders_inventory.api.minimessage.instance.MiniMessageInstance.PreviewOptions;
import _3650.builders_inventory.api.minimessage.instance.MiniMessageInstance.SuggestionOptions;
import _3650.builders_inventory.api.minimessage.validator.MiniMessageValidator;
import _3650.builders_inventory.api.minimessage.widgets.wrapper.WrappedTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;

@FunctionalInterface
public interface MMWidgetConstructor {
	public static MMWidgetConstructor standardWidget(Minecraft minecraft, Screen screen, Font font) {
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
