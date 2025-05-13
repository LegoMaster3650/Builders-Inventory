package _3650.builders_inventory.api.minimessage.parser;

import java.util.List;

public class ArgData {
	
	public final int size;
	private final List<String> args;
	private int i = 0;
	
	public ArgData(List<String> args) {
		this.size = args.size();
		this.args = args;
	}
	
	public boolean hasNext() {
		return i < size;
	}
	
	public String next() {
		return args.get(i++);
	}
	
	public String peek() {
		return args.get(i);
	}
	
	public String require() throws InvalidMiniMessage {
		if (hasNext()) return next();
		else throw new InvalidMiniMessage();
	}
	
	public String require(String error) throws InvalidMiniMessage {
		if (hasNext()) return next();
		else throw new InvalidMiniMessage(error);
	}
	
}
