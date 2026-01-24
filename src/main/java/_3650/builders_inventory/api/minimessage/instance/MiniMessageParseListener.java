package _3650.builders_inventory.api.minimessage.instance;

import org.jetbrains.annotations.Nullable;

import _3650.builders_inventory.api.minimessage.MiniMessageResult;

@FunctionalInterface
public interface MiniMessageParseListener {
	
	public static final MiniMessageParseListener IGNORE = parseResult -> {};
	
	public void onParseChange(@Nullable MiniMessageResult parseResult);
	
}
