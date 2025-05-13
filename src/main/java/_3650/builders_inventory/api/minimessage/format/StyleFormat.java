package _3650.builders_inventory.api.minimessage.format;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

public class StyleFormat extends FormatNonPlaintext {
	
	private final ChatFormatting format;
	
	public StyleFormat(String argString, String tag, ChatFormatting format) {
		super(argString, tag);
		this.format = format;
	}
	
	@Override
	public MutableComponent format(MutableComponent component) {
		return component.withStyle(format);
	}
	
}
