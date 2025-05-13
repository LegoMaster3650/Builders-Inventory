package _3650.builders_inventory.api.minimessage.tags;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class Translatable extends Node {
	
	public final String tag;
	public final String key;
	public final List<Node> args;
	
	public Translatable(String tag, String key, List<Node> args) {
		this.tag = '<' + tag + '>';
		this.key = key;
		this.args = args;
	}
	
	@Override
	public String plainText() {
		return tag;
	}
	
	@Override
	public MutableComponent visit() {
		return args.isEmpty() ? Component.translatable(key) : Component.translatable(key, args.stream().map(Node::visit).toArray());
	}
	
//	@Override
//	public MutableComponent visitPlainText() {
//		if (args.isEmpty()) return super.visitPlainText();
//		MutableComponent result = Component.empty();
//		result.append(Component.literal(key));
//		for (var arg : args) {
//			result.append(arg.visitPlainText());
//		}
//		return result.append(Component.literal(">"));
//	}
	
}
