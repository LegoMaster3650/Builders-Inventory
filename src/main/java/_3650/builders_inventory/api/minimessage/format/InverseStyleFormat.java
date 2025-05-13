package _3650.builders_inventory.api.minimessage.format;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

public class InverseStyleFormat extends FormatNonPlaintext {
	
	private final ChatFormatting format;
	
	public InverseStyleFormat(String argString, String tag, ChatFormatting format) {
		super(argString, tag);
		this.format = format;
	}
	
	@Override
	public MutableComponent format(MutableComponent component) {
		return switch (format) {
		default -> component;
		case BOLD -> component.withStyle(component.getStyle().withBold(false));
		case ITALIC -> component.withStyle(component.getStyle().withItalic(false));
		case UNDERLINE -> component.withStyle(component.getStyle().withUnderlined(false));
		case STRIKETHROUGH -> component.withStyle(component.getStyle().withStrikethrough(false));
		case OBFUSCATED -> component.withStyle(component.getStyle().withObfuscated(false));
		};
	}
	
}
