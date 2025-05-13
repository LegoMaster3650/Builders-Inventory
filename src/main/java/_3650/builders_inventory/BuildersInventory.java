package _3650.builders_inventory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import _3650.builders_inventory.config.Config;
import _3650.builders_inventory.feature.extended_inventory.ExtendedInventory;
import _3650.builders_inventory.feature.hotbar_swapper.HotbarSwapper;
import _3650.builders_inventory.feature.minimessage.MiniMessageFeature;
import _3650.builders_inventory.feature.minimessage.chat.ChatMiniMessageContext;

public class BuildersInventory implements ModInitializer, ClientModInitializer {
	
	public static final String MOD_ID = "builders_inventory";
	
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
	@Override
	public void onInitialize() {
		LOGGER.info("Loading Builder's Inventory!");
		
		// ensure on client
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) throw new IllegalStateException("Do not run this on a dedicated server you fool");
		
		// load config
		boolean configLoad = Config.HANDLER.load();
		if (configLoad) LOGGER.info("Config loaded successfully.");
		else LOGGER.error("Config load failed!");
	}
	
	@Override
	public void onInitializeClient() {
		LOGGER.info("Loading Builder's Inventory Client!");
		
		// register events
		ClientTickEvents.END_CLIENT_TICK.register(ExtendedInventory::clientTick);
		ClientPlayConnectionEvents.JOIN.register(ExtendedInventory::onJoinWorld);
		ClientTickEvents.END_CLIENT_TICK.register(HotbarSwapper::clientTick);
		ClientLifecycleEvents.CLIENT_STARTED.register(MiniMessageFeature::onClientStarted);
		ClientPlayConnectionEvents.JOIN.register(ChatMiniMessageContext::onJoinWorld);
		ClientPlayConnectionEvents.DISCONNECT.register(ChatMiniMessageContext::onQuitWorld);
		
		// register keybinds
		ModKeybinds.register();
		
		// load features
		ExtendedInventory.refresh();
		HotbarSwapper.refresh();
		MiniMessageFeature.init();
		ChatMiniMessageContext.loadServerCommandMap();
	}
	
	public static ResourceLocation modLoc(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
	
}
