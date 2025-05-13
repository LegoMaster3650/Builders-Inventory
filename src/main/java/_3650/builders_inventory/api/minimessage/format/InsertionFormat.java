package _3650.builders_inventory.api.minimessage.format;

import net.minecraft.network.chat.MutableComponent;

public class InsertionFormat extends Format {
	
	public final String text;
	
	public InsertionFormat(String argString, String tag, String text) {
		super(argString, tag);
		this.text = text;
	}
	
	@Override
	public MutableComponent format(MutableComponent component) {
		return component.setStyle(component.getStyle().withInsertion(text));
	}
	
}
