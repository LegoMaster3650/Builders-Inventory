package _3650.builders_inventory.api.util;

public class ThemeUtil {
	
	public static int visibleColor(int color) {
		return color > 0xFFFFFF ? color : (color | 0xFF000000);
	}
	
}
