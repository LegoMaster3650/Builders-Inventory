package _3650.builders_inventory.api.minimessage.format;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class FontFormat extends FormatNonPlaintext {
	
	public final ResourceLocation font;
	
	public FontFormat(String argString, String tag, ResourceLocation font) {
		super(argString, tag);
		this.font = font;
	}
	
	@Override
	public MutableComponent format(MutableComponent component) {
		return component.setStyle(component.getStyle().withFont(font));
	}
	
}
