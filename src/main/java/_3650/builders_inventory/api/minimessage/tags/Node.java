package _3650.builders_inventory.api.minimessage.tags;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class Node {
	
	public abstract String plainText();
	
	public abstract MutableComponent visit();
	
	public MutableComponent visitPlainText() {
		return Component.literal(plainText());
	}
	
}
