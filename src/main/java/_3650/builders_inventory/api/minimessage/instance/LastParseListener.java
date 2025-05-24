package _3650.builders_inventory.api.minimessage.instance;

import org.jetbrains.annotations.Nullable;

import _3650.builders_inventory.api.minimessage.MiniMessageResult;

@FunctionalInterface
public interface LastParseListener {
	
	public static final LastParseListener IGNORE = lastParse -> {};
	
	public void onParseChange(@Nullable MiniMessageResult lastParse);
	
}
