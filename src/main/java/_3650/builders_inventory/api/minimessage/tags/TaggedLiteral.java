package _3650.builders_inventory.api.minimessage.tags;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TaggedLiteral extends Node {
	
	public final String tag;
	public final String text;
	
	public TaggedLiteral(String tag, String text) {
		this.tag = '<' + tag + '>';
		this.text = text;
	}
	
	@Override
	public String plainText() {
		return tag;
	}
	
	@Override
	public MutableComponent visit() {
		return Component.literal(text);
	}
	
}
