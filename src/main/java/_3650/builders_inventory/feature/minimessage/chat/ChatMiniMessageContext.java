package _3650.builders_inventory.feature.minimessage.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.api.minimessage.validator.ChatMiniMessageValidatorRegistry;
import _3650.builders_inventory.config.Config;
import _3650.builders_inventory.feature.minimessage.MiniMessageFeature;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.util.StringUtil;

public class ChatMiniMessageContext {
	
	private static List<ServerCommandEntry> serverList = List.of();
	@Nullable
	private static Pattern currentServer = null;
	@Nullable
	public static String currentServerIP = null;
	public static boolean forceChatMinimessage = false;
	
	public static void loadServerCommandMap() {
		BuildersInventory.LOGGER.info("Reloading per-server commands");
		final List<String> lines = Config.instance().minimessage_perServerCommands;
		final ArrayList<ServerCommandEntry> servers = new ArrayList<>(lines.size());
		for (var line : lines) ServerCommandEntry.fromString(line).ifPresent(servers::add);
		serverList = servers;
		refreshCurrentServer();
	}
	
	public static void onJoinWorld(ClientPacketListener handler, PacketSender sender, Minecraft mc) {
		forceChatMinimessage = Config.instance().minimessage_chatForceDefault;
		currentServerIP = getServerIP(mc);
		MiniMessageFeature.reloadTagAutocomplete(currentServerIP);
		refreshCurrentServer();
	}
	
	private static void refreshCurrentServer() {
		final String ip = currentServerIP;
		currentServer = null;
		for (var entry : serverList) {
			if (StringUtils.endsWithIgnoreCase(ip, entry.server)) {
				currentServer = entry.pattern;
				break;
			}
		}
	}
	
	private static String getServerIP(Minecraft mc) {
		return (mc.getCurrentServer() == null || mc.getCurrentServer().ip == null) ? "localhost" : mc.getCurrentServer().ip;
	}
	
	public static void onQuitWorld(ClientPacketListener handler, Minecraft mc) {
		currentServer = null;
		currentServerIP = null;
	}
	
	public static Optional<String> isValid(Minecraft minecraft, String value) {
		// perform the same exact modifications vanilla makes to chat messages when sent
		value = StringUtil.trimChatMessage(StringUtils.normalizeSpace(value.trim()));
		
		// server-based command matcher
		if (currentServer != null) {
			Matcher matcher = currentServer.matcher(value);
			if (matcher.find()) {
				return Optional.of(value.substring(matcher.end()));
			}
		}
		
		// other mods' tests
		final var genericValid = ChatMiniMessageValidatorRegistry.isValid(minecraft, value);
		if (genericValid.isPresent()) return genericValid;
		
		// force minimessage button
		if (forceChatMinimessage) return Optional.ofNullable(value);
		
		return Optional.empty();
	}
	
	private static class ServerCommandEntry {
		public final String server;
		public final Pattern pattern;
		
		public ServerCommandEntry(String server, String pattern) {
			this.server = server;
			this.pattern = Pattern.compile("^/(?:" + pattern + ")\\s+");
		}
		
		public static Optional<ServerCommandEntry> fromString(String str) {
			final int i = str.indexOf('=');
			if (i < 0) {
				BuildersInventory.LOGGER.error("Expected = in server=pattern format but got {}", str);
				return Optional.empty();
			} else {
				String server = str.substring(0, i);
				String pattern = str.substring(i + 1);
				return Optional.ofNullable(new ServerCommandEntry(server, pattern));
			}
		}
		
	}
	
}
