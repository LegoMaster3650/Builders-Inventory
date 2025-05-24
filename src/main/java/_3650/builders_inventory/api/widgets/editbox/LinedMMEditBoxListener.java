package _3650.builders_inventory.api.widgets.editbox;

@FunctionalInterface
public interface LinedMMEditBoxListener {
	
	public static final LinedMMEditBoxListener IGNORE = (value, start, end) -> {};
	
	public void onInsert(String value, int start, int end);
	
}
