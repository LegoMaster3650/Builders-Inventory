package _3650.builders_inventory.api.minimessage.format;

import net.minecraft.network.chat.MutableComponent;

public abstract class Format {
	
	private final String plainFront;
	private final String plainBack;
	public final String tagName;
	
	public Format(String argString, String tag) {
		this.plainFront = '<' + argString + '>';
		this.plainBack = "</" + tag + '>';
		this.tagName = tag;
	}
	
	// plain format, the front and back strings are unused in this which is why they have those names
	public static final Format PLAIN = new Format("error", "please-report-this") {
		@Override
		public String plainTextFront() {
			return "";
		}
		@Override
		public String plainTextBack() {
			return "";
		}
		@Override
		public MutableComponent format(MutableComponent component) {
			return component;
		}
	};
	
	public String plainTextFront() {
		return this.plainFront;
	}
	
	public String plainTextBack() {
		return this.plainBack;
	}
	
	public abstract MutableComponent format(MutableComponent component);
	
	public MutableComponent formatPlain(MutableComponent component) {
		return format(component);
	}
	
}
