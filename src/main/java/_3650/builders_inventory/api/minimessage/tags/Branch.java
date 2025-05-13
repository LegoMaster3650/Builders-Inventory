package _3650.builders_inventory.api.minimessage.tags;

import java.util.ArrayList;

import _3650.builders_inventory.api.minimessage.format.Format;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class Branch extends Node {
	
	public final ArrayList<Node> nodes = new ArrayList<>();
	public final Format format;
	private boolean closed = true;
	
	public Branch(Format format) {
		this.format = format;
	}
	
	public void append(Node node) {
		nodes.add(node);
	}
	
	@Override
	public String plainText() {
		StringBuilder result = new StringBuilder();
		result.append(this.format.plainTextFront());
		for (var node : this.nodes) {
			result.append(node.plainText());
		}
		if (closed) result.append(this.format.plainTextBack());
		return result.toString();
	}
	
	@Override
	public MutableComponent visit() {
		MutableComponent result = Component.empty();
		for (var node : this.nodes) {
			result.append(node.visit());
		}
		return format.format(result);
	}
	
	@Override
	public MutableComponent visitPlainText() {
		MutableComponent result = Component.empty();
		result.append(format.formatPlain(Component.literal(format.plainTextFront())));
		MutableComponent content = Component.empty();
		for (var node : this.nodes) {
			content.append(node.visitPlainText());
		}
		result.append(format.formatPlain(content));
		if (closed) result.append(format.formatPlain(Component.literal(format.plainTextBack())));
		return result;
	}
	
	public void setUnclosed() {
		this.closed = false;
	}
	
}
