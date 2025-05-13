package _3650.builders_inventory.api.extended_inventory;

import _3650.builders_inventory.ModKeybinds;
import _3650.builders_inventory.config.Config;

public class ExtendedInventoryKeys {
	
	public static boolean matchesOpen(int keyCode, int scanCode, int modifiers) {
		return Config.instance().extended_inventory_enabled && ModKeybinds.OPEN_EXTENDED_INVENTORY.matches(keyCode, scanCode);
	}
	
	public static boolean isOpenDown() {
		return Config.instance().extended_inventory_enabled && ModKeybinds.OPEN_EXTENDED_INVENTORY.isDown();
	}
	
	public static boolean consumeOpenPress() {
		return Config.instance().extended_inventory_enabled && ModKeybinds.OPEN_EXTENDED_INVENTORY.consumeClick();
	}
	
}
