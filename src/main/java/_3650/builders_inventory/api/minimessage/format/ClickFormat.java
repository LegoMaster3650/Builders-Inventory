package _3650.builders_inventory.api.minimessage.format;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;

public class ClickFormat extends Format {
	
	public final ClickEvent event;
	
	public ClickFormat(String argString, String tag, ClickEvent event) {
		super(argString, tag);
		this.event = event;
	}
	
	@Override
	public MutableComponent format(MutableComponent component) {
		return component.setStyle(component.getStyle().withClickEvent(event));
	}
	
}
