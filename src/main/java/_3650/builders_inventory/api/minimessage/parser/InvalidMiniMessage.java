package _3650.builders_inventory.api.minimessage.parser;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("serial")
public class InvalidMiniMessage extends Throwable {
	
	@Nullable
	public final String message;
	public final Type type;
	
	public static InvalidMiniMessage warning(String message) {
		return new InvalidMiniMessage(Type.WARNING, message);
	}
	
	public static InvalidMiniMessage error(String message) {
		return new InvalidMiniMessage(Type.ERROR, message);
	}
	
	private InvalidMiniMessage(Type type, String message) {
		this.type = type;
		this.message = message;
	}
	
	public static enum Type {
		WARNING,
		ERROR
	}
	
}
