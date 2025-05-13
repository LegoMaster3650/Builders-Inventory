package _3650.builders_inventory.api.minimessage.parser;

import _3650.builders_inventory.api.minimessage.format.Format;
import _3650.builders_inventory.api.minimessage.tags.Branch;
import _3650.builders_inventory.api.minimessage.tags.Node;

public interface MiniMessageTagOutput {
	
	public static final MiniMessageTagOutput SINK = new MiniMessageTagOutput() {
		@Override
		public void append(Node node) {
		}
		@Override
		public void push(Format format) {
		}
		@Override
		public Branch pop() {
			return new Branch(Format.PLAIN);
		}
		@Override
		public Branch popUnclosed() {
			return new Branch(Format.PLAIN);
		}
	};
	
	public void append(Node node);
	
	public void push(Format format);
	
	public Branch pop();
	
	public Branch popUnclosed();
	
}
