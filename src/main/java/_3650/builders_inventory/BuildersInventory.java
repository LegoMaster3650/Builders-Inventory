package _3650.builders_inventory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import _3650.builders_inventory.config.Config;
import _3650.builders_inventory.feature.extended_inventory.ExtendedInventory;
import _3650.builders_inventory.feature.hotbar_swapper.HotbarSwapper;

public class BuildersInventory implements ModInitializer, ClientModInitializer {
	
	public static final String MOD_ID = "builders_inventory";
	
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Loading Builder's Inventory!");
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) throw new IllegalStateException("Do not run this on a dedicated server you fool");
		boolean configLoad = Config.HANDLER.load();
		if (configLoad) LOGGER.info("Config loaded successfully.");
		else LOGGER.error("Config load failed!");
		ExtendedInventory.refresh();
		HotbarSwapper.refresh();
	}
	
	@Override
	public void onInitializeClient() {
		LOGGER.info("Loading Builder's Inventory Client!");
		ModKeybinds.register();
		ClientTickEvents.END_CLIENT_TICK.register(ExtendedInventory::clientTick);
		ClientPlayConnectionEvents.JOIN.register(ExtendedInventory::onJoinWorld);
		ClientPlayConnectionEvents.DISCONNECT.register(ExtendedInventory::onQuitWorld);
		ClientTickEvents.END_CLIENT_TICK.register(HotbarSwapper::clientTick);
	}
	
}
