package _3650.builders_inventory.api.minimessage.format;

import net.minecraft.network.chat.MutableComponent;

public class QuoteFormat extends Format {
	
	public QuoteFormat(String quote) {
		super(quote, quote);
	}
	
	@Override
	public String plainTextFront() {
		return this.tagName;
	}
	
	@Override
	public String plainTextBack() {
		return this.tagName;
	}
	
	@Override
	public MutableComponent format(MutableComponent component) {
		return component;
	}
	
}
