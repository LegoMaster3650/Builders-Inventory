package _3650.builders_inventory.config;

import java.util.List;

import com.google.gson.GsonBuilder;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.feature.extended_inventory.ExtendedInventoryClearBehavior;
import _3650.builders_inventory.feature.hotbar_swapper.HotbarSwapperBehavior;
import _3650.builders_inventory.feature.minimessage.chat.ChatMiniMessageButtonDisplay;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.AutoGen;
import dev.isxander.yacl3.config.v2.api.autogen.Boolean;
import dev.isxander.yacl3.config.v2.api.autogen.Boolean.Formatter;
import dev.isxander.yacl3.config.v2.api.autogen.EnumCycler;
import dev.isxander.yacl3.config.v2.api.autogen.IntSlider;
import dev.isxander.yacl3.config.v2.api.autogen.ListGroup;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;

public class Config {
	
	public static final ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
			.id(BuildersInventory.modLoc("config"))
			.serializer(config -> GsonConfigSerializerBuilder.create(config)
					.setPath(FabricLoader.getInstance().getConfigDir().resolve(BuildersInventory.MOD_ID + ".json5"))
					.appendGsonBuilder(GsonBuilder::setPrettyPrinting)
					.setJson5(true)
					.build())
			.build();
	
	public static Config instance() {
		return HANDLER.instance();
	}
	
	/*
	 * Hotbar Swapper
	 */
	
	// Behavior
	@SerialEntry(
			value = "hotbar_swapper.behavior",
			comment = "Whether the hotbar swapper should only accept one button or both\n"
					+ "Values:\n"
					+ "- PREFER_FULL - Requires full key to swap at all, column is modifier\n"
					+ "- PREFER_COLUMN - Requires column key to swap at all, full is modifier\n"
					+ "- SEPERATE - Full and column keys function independently")
	@AutoGen(category = "hotbar_swapper")
	@EnumCycler
	public HotbarSwapperBehavior hotbar_swapper_behavior = HotbarSwapperBehavior.PREFER_FULL;
	
	@SerialEntry(
			value = "hotbar_swapper.useHotbarHotkeys",
			comment = "Whether to allow pressing hotbar slot keys to quickly pick a hotbar row")
	@AutoGen(category = "hotbar_swapper")
	@Boolean(colored = false, formatter = Formatter.ON_OFF)
	public boolean hotbar_swapper_useHotbarHotkeys = true;
	
	@SerialEntry(
			value = "hotbar_swapper.hotkeyDelay",
			comment = "Delay (in client ticks) between pressing a number key and swapping that row\n"
					+ "Provides visual feedback for hitting the hotkeys")
	@AutoGen(category = "hotbar_swapper")
	@IntSlider(min = 0, max = 3, step = 1)
	public int hotbar_swapper_hotkeyDelay = 1;
	
	@SerialEntry(
			value = "hotbar_swapper.stickyModifiers",
			comment = "Whether the modifiers for Prefer Full or Column will stick\n"
					+ "- False/Off - Default behavior, modifiers constantly update\n"
					+ "- True/On - Modifiers are only determined when the main key is pressed")
	@AutoGen(category = "hotbar_swapper")
	@Boolean(colored = false, formatter = Formatter.ON_OFF)
	public boolean hotbar_swapper_stickyModifiers = false;
	
	@SerialEntry(
			value = "hotbar_swapper.includeExtendedInventory",
			comment = "Whether the hotbar swapper should allow swapping with the extended inventory (if enabled)")
	@AutoGen(category = "hotbar_swapper")
	@Boolean(colored = false, formatter = Formatter.ON_OFF)
	public boolean hotbar_swapper_includeExtendedInventory = true;
	
	@SerialEntry(
			value = "hotbar_swapper.extendedInventoryOffset",
			comment = "How much to shift up hotbar rows belonging to the extended inventory")
	@AutoGen(category = "hotbar_swapper")
	@IntSlider(min = 0, max = 20, step = 1)
	public int hotbar_swapper_extendedInventoryOffset = 2;
	
	/*
	 * Extended Inventory
	 */
	
	@SerialEntry(
			value = "extended_inventory.enabled",
			comment = "Whether to enable the extended inventory menu at all")
	@AutoGen(category = "extended_inventory")
	@Boolean(colored = true, formatter = Formatter.ON_OFF)
	public boolean extended_inventory_enabled = true;
	
	@SerialEntry(
			value = "extended_inventory.open_button_enabled",
			comment = "Whether the creative inventory has a button to open the extended inventory")
	@AutoGen(category = "extended_inventory")
	@Boolean(colored = true, formatter = Formatter.ON_OFF)
	public boolean extended_inventory_open_button_enabled = true;
	
	@SerialEntry(
			value = "extended_inventory.clear_behavior",
			comment = "How the clear items button should behave in the extended inventory when shift clicked.\n"
					+ "(Note: Only applies to clear button in extended inventory, creative inventory is unchanged)"
					+ "Values:\n"
					+ "- NONE - Does absolutely nothing\n"
					+ "- CLEAR_PLAYER - Clears just the player inventory, like vanilla minecraft (DEFAULT)\n"
					+ "- CLEAR_EXTENDED - Clears just the current extended inventory tab\n"
					+ "- CLEAR_ALL - Clears both the player inventory and current extended inventory tab")
	@AutoGen(category = "extended_inventory")
	@EnumCycler
	public ExtendedInventoryClearBehavior extended_inventory_clear_behavior = ExtendedInventoryClearBehavior.CLEAR_PLAYER;
	@SerialEntry(
			value = "extended_inventory.save_delay",
			comment = "Delay in seconds between modifying extended inventory and saving\n"
					+ "Lower delays are safer against crashes but write files more often")
	@AutoGen(category = "extended_inventory")
	@IntSlider(min = 0, max = 120, step = 10)
	public int extended_inventory_save_delay = 80;
	
	/*
	 * MiniMessage
	 */
	
	@SerialEntry(
			value = "minimessage.syntaxHighlighting",
			comment = "Whether minimessage inputs will be formatted to resemble the output\n"
					+ "Does not copy style or font changes to maintain clarity")
	@AutoGen(category = "minimessage")
	@Boolean(colored = true, formatter = Formatter.ON_OFF)
	public boolean minimessage_syntaxHighlighting = true;
	
	@SerialEntry(
			value = "minimessage.messagePreview",
			comment = "Whether the formatted minimessage preview displays\n"
					+ "This only affects valid previews, errors will always display")
	@AutoGen(category = "minimessage")
	@Boolean(colored = true, formatter = Formatter.ON_OFF)
	public boolean minimessage_messagePreview = true;
	
	@SerialEntry(
			value = "minimessage.suggestions",
			comment = "Whether suggestions for tags will be shown when typing in a minimessage input")
	@AutoGen(category = "minimessage")
	@Boolean(colored = true, formatter = Formatter.ON_OFF)
	public boolean minimessage_suggestions = true;
	
	@SerialEntry(
			value = "minimessage.updateDelay",
			comment = "Number of ticks you must go without typing to update the minimessage formatter\n"
					+ "Not recommended unless typing in a minimessage input slows your game")
	@AutoGen(category = "minimessage")
	@IntSlider(min = 0, max = 50, step = 1)
	public int minimessage_updateDelay = 0;
	
	@SerialEntry(
			value = "minimessage.enabledChat",
			comment = "Whether minimessage features are enabled for chat")
	@AutoGen(category = "minimessage")
	@Boolean(colored = true, formatter = Formatter.ON_OFF)
	public boolean minimessage_enabledChat = true;
	
	@SerialEntry(
			value = "minimessage.chatPreviewHeight",
			comment = "How high above the chat input the message preview is")
	@AutoGen(category = "minimessage")
	@IntSlider(min = 0, max = 100, step = 1)
	public int minimessage_chatPreviewHeight = 4;
	
	@SerialEntry(
			value = "minimessage.previewOffsetsChat",
			comment = "Whether the chat minimessage preview will shift the chat itself away from itself")
	@AutoGen(category = "minimessage")
	@Boolean(colored = true, formatter = Formatter.ON_OFF)
	public boolean minimessage_previewOffsetsChat = true;
	
	@SerialEntry(
			value = "minimessage.previewOffsetIgnored",
			comment = "How many messages to ignore before shifting the chat UI if previewOffsetsChat is enabled")
	@AutoGen(category = "minimessage")
	@IntSlider(min = 0, max = 10, step = 1)
	public int minimessage_previewOffsetIgnored = 2;
	
	@SerialEntry(
			value = "minimessage.chatForceButtonPos",
			comment = "Where on the chat bar to show the force minimessage formatting button\n"
					+ "Values:\n"
					+ "- NONE - Button hidden, normal chat\n"
					+ "- LEFT - Button on left edge of chat\n"
					+ "- RIGHT - Button on right edge of chat")
	@AutoGen(category = "minimessage")
	@EnumCycler
	public ChatMiniMessageButtonDisplay minimessage_chatForceButtonDisplay = ChatMiniMessageButtonDisplay.RIGHT;
	
	@SerialEntry(
			value = "minimessage.chatForceDefault",
			comment = "Whether the force minimessage formatting toggle is always on by default in chat\n"
					+ "This is the value that the toggle will be set to when joining any world or server")
	@AutoGen(category = "minimessage")
	@Boolean(colored = true, formatter = Formatter.ON_OFF)
	public boolean minimessage_chatForceDefault = false;
	
	@SerialEntry(
			value = "minimessage.perServerCommands",
			comment = "Map of server ips to regular expressions (regexes) that match commands to auto format\n"
					+ "USAGE: <server ip>=<command pattern>\n"
					+ "server ip - The end of the server's ip address to match\n"
					+ "command pattern - A regex that matches the commands for that server\n"
					+ "The final regex will actually be ^/(?:<YOUR REGEX>)\\\\s+\n"
					+ "To apply changes, restart the game or Ctrl+Shift+Click the force minimessage toggle button")
	@AutoGen(category = "minimessage")
	@ListGroup(addEntriesToBottom = false, controllerFactory = StringListOptionFactory.class, valueFactory = StringListOptionFactory.class)
	public List<String> minimessage_perServerCommands = List.of();
	
	/*
	 * Force Operator Items
	 */
	
	@SerialEntry(
			value = "force_operator_items",
			comment = "Force the creative mode operator items tab to appear, even if you don't have OP\n"
					+ "(Useful for most players on diamondfire who lack operator permissions)")
	@AutoGen(category = "operator_items")
	@Boolean(colored = true, formatter = Formatter.ON_OFF)
	public boolean operator_items_force = true;
	
}
