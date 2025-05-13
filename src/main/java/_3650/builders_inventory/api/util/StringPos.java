package _3650.builders_inventory.api.util;

public class StringPos {
	
	public static final StringPos EMPTY = new StringPos(0, 0);
	
	public final int beginIndex;
	public final int endIndex;
	
	public StringPos(int beginIndex, int endIndex) {
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
	}
	
	public StringPos shift(int amount) {
		return new StringPos(this.beginIndex + amount, this.endIndex + amount);
	}
	
}
