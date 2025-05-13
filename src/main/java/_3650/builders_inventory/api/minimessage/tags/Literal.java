package _3650.builders_inventory.api.minimessage.tags;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class Literal extends Node {
	
	public final String text;
	
	public Literal(String text) {
		this.text = text;
	}
	
	@Override
	public String plainText() {
		return text;
	}
	
	@Override
	public MutableComponent visit() {
		return Component.literal(text);
	}
	
}
