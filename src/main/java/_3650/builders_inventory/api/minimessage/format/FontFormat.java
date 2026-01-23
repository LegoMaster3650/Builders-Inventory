package _3650.builders_inventory.api.minimessage.format;

import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public class FontFormat extends FormatNonPlaintext {
	
	public final Identifier font;
	
	public FontFormat(String argString, String tag, Identifier font) {
		super(argString, tag);
		this.font = font;
	}
	
	@Override
	public MutableComponent format(MutableComponent component) {
		return component.setStyle(component.getStyle().withFont(new FontDescription.Resource(font)));
	}
	
}
