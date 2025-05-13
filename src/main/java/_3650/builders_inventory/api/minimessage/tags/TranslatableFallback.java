package _3650.builders_inventory.api.minimessage.tags;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TranslatableFallback extends Node {
	
	public final String tag;
	public final String key;
	public final String fallback;
	public final List<Node> args;
	
	public TranslatableFallback(String tag, String key, String fallback, List<Node> args) {
		this.tag = '<' + tag + '>';
		this.key = key;
		this.fallback = fallback;
		this.args = args;
	}
	
	@Override
	public String plainText() {
		return key;
	}
	@Override
	public MutableComponent visit() {
		return args.isEmpty() ? Component.translatableWithFallback(key, fallback) : Component.translatableWithFallback(key, fallback, args.stream().map(Node::visit).toArray());
	}
	
}
