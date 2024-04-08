package _3650.builders_inventory.config;

import com.google.gson.GsonBuilder;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.feature.extended_inventory.ExtendedInventoryClearBehavior;
import _3650.builders_inventory.feature.hotbar_swapper.HotbarSwapperBehavior;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.AutoGen;
import dev.isxander.yacl3.config.v2.api.autogen.Boolean;
import dev.isxander.yacl3.config.v2.api.autogen.Boolean.Formatter;
import dev.isxander.yacl3.config.v2.api.autogen.EnumCycler;
import dev.isxander.yacl3.config.v2.api.autogen.IntSlider;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

public class Config {
	
	public static final ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
			.id(new ResourceLocation(BuildersInventory.MOD_ID, "config"))
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
