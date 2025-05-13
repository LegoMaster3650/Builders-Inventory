package _3650.builders_inventory.api.minimessage.parser;

import org.jetbrains.annotations.Nullable;

import _3650.builders_inventory.api.minimessage.MiniMessageParser;

public interface MiniMessageTagParser {
	
	public boolean parseTag(MiniMessageTagOutput output, MiniMessageParser parser, String argString, String name, ArgData args, @Nullable String server) throws InvalidMiniMessage;
	
}
