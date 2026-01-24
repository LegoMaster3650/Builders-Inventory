package _3650.builders_inventory.api.minimessage.tags;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class Node {
	
	public abstract String getPlainText();
	
	public abstract MutableComponent getFormatted();
	
	public MutableComponent getFormattedPlainText() {
		return Component.literal(getPlainText());
	}
	
}
