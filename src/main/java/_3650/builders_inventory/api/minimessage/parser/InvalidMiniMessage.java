package _3650.builders_inventory.api.minimessage.parser;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("serial")
public class InvalidMiniMessage extends Exception {
	
	@Nullable
	public final String error;
	
	public InvalidMiniMessage() {
		super();
		this.error = null;
	}
	
	public InvalidMiniMessage(String error) {
		super(error);
		this.error = error;
	}
	
}
