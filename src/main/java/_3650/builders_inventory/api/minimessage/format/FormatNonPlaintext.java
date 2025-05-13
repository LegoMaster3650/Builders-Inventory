package _3650.builders_inventory.api.minimessage.format;

import net.minecraft.network.chat.MutableComponent;

public abstract class FormatNonPlaintext extends Format {
	
	public FormatNonPlaintext(String argString, String tag) {
		super(argString, tag);
	}
	
	@Override
	public MutableComponent formatPlain(MutableComponent component) {
		return component;
	}
	
}
