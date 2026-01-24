package _3650.builders_inventory.api.minimessage.tags;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class HiddenLiteral extends Node {
	
	public final String text;
	
	private HiddenLiteral(String text) {
		this.text = text;
	}
	
	public static HiddenLiteral plain(String text) {
		return new HiddenLiteral(text);
	}
	
	public static HiddenLiteral tag(String tag) {
		return new HiddenLiteral('<' + tag + '>');
	}
	
	@Override
	public String getPlainText() {
		return text;
	}
	
	@Override
	public MutableComponent getFormatted() {
		return Component.empty();
	}
	
}
