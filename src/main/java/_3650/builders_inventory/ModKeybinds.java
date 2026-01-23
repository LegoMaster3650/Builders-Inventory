package _3650.builders_inventory;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class ModKeybinds {
	
	private static final ArrayList<KeyMapping> KEYS = new ArrayList<>();
	private static final KeyMapping.Category KEYBIND_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(BuildersInventory.MOD_ID, "main"));
	
	public static final KeyMapping HOTBAR_SCROLL = create(
			"hotbar_swapper.hotbar_scroll",
			InputConstants.Type.MOUSE,
			GLFW.GLFW_MOUSE_BUTTON_5);
	public static final KeyMapping COLUMN_SCROLL = create(
			"hotbar_swapper.column_scroll",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_LEFT_ALT);
	
	public static final KeyMapping OPEN_EXTENDED_INVENTORY = create(
			"extended_inventory.open",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_R);
	
	private static KeyMapping create(String name, InputConstants.Type type, int keycode) {
		var mapping = new KeyMapping("key." + BuildersInventory.MOD_ID + "." + name, type, keycode, KEYBIND_CATEGORY);
		KEYS.add(mapping);
		return mapping;
	}
	
	static void register() {
		KEYS.forEach(KeyBindingHelper::registerKeyBinding);
	}
	
}
