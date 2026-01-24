package _3650.builders_inventory.feature.minimessage.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.Nullable;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.api.minimessage.instance.HighlightedTextInput;
import _3650.builders_inventory.api.minimessage.validator.ChatMiniMessageValidatorRegistry;
import _3650.builders_inventory.api.minimessage.validator.MiniMessageValidator;
import _3650.builders_inventory.config.Config;
import _3650.builders_inventory.feature.minimessage.MiniMessageFeature;
import _3650.builders_inventory.feature.minimessage.chat.ChatDiff.ChatSwap;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringUtil;

public class ChatMiniMessageContext implements MiniMessageValidator {
	
	public static final ChatMiniMessageContext INSTANCE = new ChatMiniMessageContext();
	
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
			if (Strings.CI.endsWith(ip, entry.server)) {
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
	
	@Override
	public Optional<String> isValid(Minecraft minecraft, String value, Consumer<MiniMessageValidator> validatorChanger) {
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
		final Optional<String> customValidator = ChatMiniMessageValidatorRegistry.isValid(minecraft, value, validatorChanger);
		if (customValidator.isPresent()) return customValidator;
		
		// force minimessage button
		if (forceChatMinimessage) return Optional.ofNullable(value);
		
		return Optional.empty();
	}
	
	private static final Style LITERAL_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);
	
	@Override
	public void rebuildText(String original, String modified, MutableComponent highlighted, HighlightedTextInput.Builder output) {
		rebuildChatText(original, modified, highlighted, output, LITERAL_STYLE);
	}
	
	public static void rebuildChatText(String original, String modified, MutableComponent highlighted, HighlightedTextInput.Builder output, Style prefixStyle) {
		// use my less evil chat-specific diff checker that shouldn't break
		final var chatDiff = ChatDiff.calculate(original, modified);
		final var swaps = chatDiff.swaps;
		
		// add prefix
		if (!chatDiff.prefix.isEmpty()) {
			output.append(chatDiff.prefix, prefixStyle);
		}
		
		// end things the easy way if there are no modified parts
		if (swaps.size() == 0) {
			output.visit(highlighted, Style.EMPTY);
			if (!chatDiff.trailing.isEmpty()) output.append(chatDiff.trailing, Style.EMPTY);
			return;
		}
		
		// otherwise continue to prepare
		final var swapIter = swaps.listIterator(swaps.size());
		
		// evil string reconstructor mk II slightly less evil edition
		final var swapVisitor = new FormattedText.StyledContentConsumer<Integer>() {
			private int index = 0;
			private ChatSwap swap = swapIter.previous();
			
			@Override
			public Optional<Integer> accept(Style style, String string) {
				// if all swaps are accepted, proceed asap
				if (swap == null) {
					output.append(string, style);
					return Optional.empty();
				}
				if (index + string.length() > swap.index && swap.index >= index) {
					final int i = swap.index - index;
					
					if (i > 0) {
						final String before = string.substring(0, i);
						output.append(before, style);
						index += before.length();
					}
					
					output.append(swap.value, style);
					index += 1;
					
					swap = swapIter.hasPrevious() ? swapIter.previous() : null;
					
					if (i + 1 < string.length()) return accept(style, string.substring(i + 1));
				} else {
					output.append(string, style);
					index += string.length();
				}
				return Optional.empty();
			}
			
		};
		
		// execute order 67
		highlighted.visit(swapVisitor, Style.EMPTY);
		
		// add trailing whitespace
		if (!chatDiff.trailing.isEmpty()) output.append(chatDiff.trailing, Style.EMPTY);
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
