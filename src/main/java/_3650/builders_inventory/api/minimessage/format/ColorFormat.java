package _3650.builders_inventory.api.minimessage.format;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

public class ColorFormat extends Format {
	
	private final TextColor color;
	
	public ColorFormat(String argString, String tag, TextColor color) {
		super(argString, tag);
		this.color = color;
	}
	
	@Override
	public MutableComponent format(MutableComponent component) {
		return component.withColor(this.color.getValue());
	}
	
}
