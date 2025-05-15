package _3650.builders_inventory.api.minimessage.format;

import net.minecraft.network.chat.MutableComponent;

public class ShadowColorFormat extends Format {
	
	private final int shadowColor;
	
	public ShadowColorFormat(String argString, String tag, int shadowColor) {
		super(argString, tag);
		this.shadowColor = shadowColor;
	}
	
	@Override
	public MutableComponent format(MutableComponent component) {
		return component.setStyle(component.getStyle().withShadowColor(this.shadowColor));
	}
	
}
