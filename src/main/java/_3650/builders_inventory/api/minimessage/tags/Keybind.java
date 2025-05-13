package _3650.builders_inventory.api.minimessage.tags;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class Keybind extends Node {
	
	public final String tag;
	public final String key;
	
	public Keybind(String tag, String key) {
		this.tag = '<' + tag + '>';
		this.key = key;
	}
	
	@Override
	public String plainText() {
		return tag;
	}
	
	@Override
	public MutableComponent visit() {
		return Component.keybind(key);
	}
	
}
