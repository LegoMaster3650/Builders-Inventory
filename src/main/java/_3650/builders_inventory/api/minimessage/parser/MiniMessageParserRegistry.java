package _3650.builders_inventory.api.minimessage.parser;

import java.util.ArrayList;

public class MiniMessageParserRegistry {
	
	private static final ArrayList<MiniMessageTagParser> PARSERS = new ArrayList<>();
	
	public static void register(MiniMessageTagParser parser) {
		PARSERS.add(parser);
	}
	
	public static boolean forEach(MMParserForEachFunction func) throws InvalidMiniMessage {
		for (var parser : PARSERS) {
			if (func.parse(parser)) return true;
		}
		return false;
	}
	
	@FunctionalInterface
	public static interface MMParserForEachFunction {
		
		public boolean parse(MiniMessageTagParser parser) throws InvalidMiniMessage;
		
	}
	
}
