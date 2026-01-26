package _3650.builders_inventory.api.minimessage.tags;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.objects.ObjectInfo;

public class ObjectTag extends Node {
	
	public final String tag;
	public final ObjectInfo contents;
	
	public ObjectTag(String tag, ObjectInfo contents) {
		this.tag = '<' + tag + '>';
		this.contents = contents;
	}
	
	@Override
	public String getPlainText() {
		return tag;
	}
	
	@Override
	public MutableComponent getFormatted() {
		return Component.object(contents);
	}
	
}
