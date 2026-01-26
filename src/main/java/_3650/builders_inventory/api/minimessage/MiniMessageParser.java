package _3650.builders_inventory.api.minimessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.DynamicOps;

import _3650.builders_inventory.api.minimessage.format.Format;
import _3650.builders_inventory.api.minimessage.format.QuoteFormat;
import _3650.builders_inventory.api.minimessage.parser.ArgData;
import _3650.builders_inventory.api.minimessage.parser.InvalidMiniMessage;
import _3650.builders_inventory.api.minimessage.parser.MiniMessageParserRegistry;
import _3650.builders_inventory.api.minimessage.parser.MiniMessageTagOutput;
import _3650.builders_inventory.api.minimessage.tags.Branch;
import _3650.builders_inventory.api.minimessage.tags.HiddenLiteral;
import _3650.builders_inventory.api.minimessage.tags.Literal;
import _3650.builders_inventory.api.minimessage.tags.Node;
import _3650.builders_inventory.api.util.ArrayStack;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextColor;

public class MiniMessageParser {
	
	public final Optional<HolderLookup.Provider> registryAccess;
	public final Optional<DynamicOps<Tag>> registryOps;
	@Nullable
	private final String server;
	private MiniMessageTagOutput tagOutput;
	
	private final String input;
	private final int tMax;
	public final Branch root;
	public Branch ctx;
	private ArrayStack<Branch> stack;
	private ArrayList<String> args;
	private Object2IntOpenHashMap<String> nameCounter;
	
	private final ArrayList<String> warnings = new ArrayList<>();
	private final ArrayList<String> errors = new ArrayList<>();
	
	private int head = 0;
	private int tail = -1;
	
	public static MiniMessageResult parseNoRegistry(String str) {
		return parse(str, Optional.empty(), null);
	}
	
	public static MiniMessageResult parse(String input, HolderLookup.Provider registryAccess, @Nullable String server) {
		return parse(input, Optional.ofNullable(registryAccess), server);
	}
	
	public static MiniMessageResult parse(String input, Optional<HolderLookup.Provider> registryAccess, @Nullable String server) {
		return parse(input, Format.PLAIN, registryAccess, server);
	}
	
	static MiniMessageResult parse(String input, Format rootFormat, Optional<HolderLookup.Provider> registryAccess, @Nullable String server) {
		return new MiniMessageParser(input, rootFormat, registryAccess, registryAccess.map(access -> access.createSerializationContext(NbtOps.INSTANCE)), server).parseContent();
	}
	
	private MiniMessageResult subParse(String str, Format rootFormat) {
		return new MiniMessageParser(str, rootFormat, this.registryAccess, this.registryOps, this.server).parseContent();
	}
	
	private MiniMessageParser(String input, Format rootFormat, Optional<HolderLookup.Provider> registryAccess, Optional<DynamicOps<Tag>> registryOps, @Nullable String server) {
		this.input = input;
		this.tMax = input.length() - 1;
		this.root = new Branch(rootFormat);
		this.ctx = root;
		this.registryAccess = registryAccess;
		this.registryOps = registryOps;
		this.server = null;
		this.tagOutput = new MiniMessageTagOutput() {
			@Override
			public void append(Node node) {
				MiniMessageParser.this.ctx.append(node);
			}
			@Override
			public void push(Format format) {
				MiniMessageParser.this.pushCtx(format);
			}
			@Override
			public Branch pop() {
				return MiniMessageParser.this.popCtx();
			}
			@Override
			public Branch popUnclosed() {
				return MiniMessageParser.this.popUnclosed();
			}
		};
	}
	
	// heavily based on the actual minimessage parser but with only 1 pass because i hate fun
	private MiniMessageResult parseContent() {
		stack = new ArrayStack<>();
		args = new ArrayList<>();
		nameCounter = new Object2IntOpenHashMap<>();
		
		ParseState state = ParseState.NORMAL;
		ParseState exitState = ParseState.NORMAL;
		boolean escaped = false;
		char quote = 0;
		
		int tagHead = 0;
		int argHead = 0;
		
		while (hasNext()) {
			char c = next();
			
			if (!escaped) {
				if (c == '\\' && hasNext()) {
					char next = input.charAt(tail + 1);
					switch (state) {
					case NORMAL:
						escaped = next == '<' || next == '\\';
						break;
					case STRING:
						escaped = next == quote || next == '\\';
						break;
					case TAG:
						if (next == '<') {
							head = tagHead;
							terminate();
							escaped = true;
							state = ParseState.NORMAL;
							exitState = ParseState.NORMAL;
						}
						break;
					}
				}
				
				if (escaped) {
					switch (state) {
					case NORMAL:
						terminate();
						++head;
						ctx.append(HiddenLiteral.plain("\\"));
						break;
					case STRING:
						break;
					case TAG:
						break;
					}
					continue;
				}
			} else {
				escaped = false;
				continue;
			}
			
			switch (state) {
			case NORMAL:
				if (c == '<') {
					terminate();
					tagHead = head;
					++head;
					argHead = head;
					state = ParseState.TAG;
					exitState = ParseState.TAG;
				}
				break;
			case TAG:
				if (c == '<') {
					head = tagHead;
					terminate();
					tagHead = tail;
					++head;
					argHead = head;
					state = ParseState.TAG;
					exitState = ParseState.TAG;
				} else if (c == ':') {
					if (tail + 2 < input.length() && input.charAt(tail + 1) == '/' && input.charAt(tail + 2) == '/') {
					} else {
						finishArg();
						argHead = head;
					}
				} else if (c == '>') {
					finishArg();
					argHead = head;
					if (args.size() > 0) {
						String argString = input.substring(tagHead + 1, tail);
						String name = args.get(0);
						ArgData argsData = new ArgData(args.size() > 1 ? args.subList(1, args.size()) : List.of());
						if (!parseTag(argString, name, argsData)) {
							head = tagHead;
						}
						args.clear();
					}
					state = ParseState.NORMAL;
					exitState = ParseState.NORMAL;
				} else if (c == '\'' || c == '"') {
					quote = c;
					if (input.indexOf(c, tail + 1) != -1) {
						state = ParseState.STRING;
						exitState = ParseState.STRING;
					}
				}
				break;
			case STRING:
				if (c == quote) {
					state = ParseState.TAG;
					exitState = ParseState.TAG;
				}
				break;
			}
			
			if (!hasNext() && state == ParseState.TAG) {
				head = tagHead;
				tail = head;
				state = ParseState.NORMAL;
				exitState = ParseState.TAG;
			}
		}
		
		String trailingText = null;
		ArrayList<String> trailingArgs = new ArrayList<>();
		ArrayList<String> unclosedTags = new ArrayList<>();
		
		// clean up remaining content
		++tail;
		if (exitState == ParseState.TAG) {
			head = argHead;
			finishArg();
			head = tagHead;
		}
		if (head < tail) ctx.append(new Literal(input.substring(head, tail)));
		
		// read trailing args
		for (String arg : args) {
			trailingArgs.add(arg);
		}
		if (!trailingArgs.isEmpty()) trailingText = trailingArgs.remove(trailingArgs.size() - 1);
		
		// close tags
		while (ctx != root) {
			Branch b = popUnclosed();
			ctx.append(b);
			unclosedTags.add(b.format.tagName);
		}
		
		if (exitState == ParseState.TAG && args.size() > 0) {
			String argString = input.substring(tagHead + 1, tail);
			String name = args.get(0);
			ArgData argsData = new ArgData(args.size() > 1 ? args.subList(1, args.size()) : List.of());
			this.tagOutput = MiniMessageTagOutput.SINK;
			this.parseTag(argString, name, argsData);
		}
		
		return new MiniMessageResult(root, trailingText, trailingArgs, unclosedTags, warnings, errors);
	}
	
	private boolean parseTag(String argString, String name, ArgData args) {
		if (name.isEmpty()) return false;
		try {
			return name.charAt(0) == '/' ? parseCloseTag(argString, name, args) : parseTagUnsafe(argString, name, args);
		} catch (InvalidMiniMessage e) {
			if (e.message != null) {
				switch (e.type) {
				case WARNING:
					warnings.add(e.message);
					break;
				case ERROR:
					errors.add(e.message);
					break;
				}
			}
			return false;
		}
	}
	
	private boolean parseCloseTag(String argString, String name, ArgData args) throws InvalidMiniMessage {
		String nameLower = name.substring(1).toLowerCase(Locale.ROOT);
		if (nameCounter.getInt(nameLower) > 0) {
			// traverse non-matching tags
			while (ctx != root && !ctx.format.tagName.toLowerCase(Locale.ROOT).equals(nameLower)) {
				Branch b = popUnclosed();
				ctx.append(b);
			}
			// if not root...
			if (ctx == root) return false;
			// pop tag
			Branch b = popCtx();
			ctx.append(b);
			return true;
		}
		return false;
	}
	
	private boolean parseTagUnsafe(String argString, String name, ArgData args) throws InvalidMiniMessage {
		return MiniMessageParserRegistry.forEach(parser -> {
			return parser.parseTag(this.tagOutput, this, argString, name, args, this.server);
		});
	}
	
	public char next() {
		return tail < tMax ? input.charAt(++tail) : 0;
	}
	
	public boolean hasNext() {
		return tail < tMax;
	}
	
//	public char peek() {
//		return tail < tMax ? s.charAt(tail + 1) : 0;
//	}
	
//	public char prev()  {
//		return tail > 0 && tail - 1 < s.length() ? s.charAt(tail - 1) : 0;
//	}
	
	public boolean finishArg() {
		String str = head <= tail ? input.substring(head, tail) : null;
		if (str != null) args.add(str);
		head = tail + 1;
		return str != null;
	}
	
	public void terminate() {
		args.clear();
		String str = token();
		if (str != null && !str.isEmpty()) ctx.append(new Literal(str));
		head = tail;
	}
	
	public String token() {
		return head < tail && tail <= input.length() ? input.substring(head, tail) : null;
	}
	
	
	
	private Branch pushCtx(Format format) {
		stack.push(ctx);
		ctx = new Branch(format);
		String formatName = format.tagName.toLowerCase(Locale.ROOT);
		nameCounter.put(formatName, nameCounter.getInt(formatName) + 1);
		return ctx;
	}
	
	private Branch popCtx() {
		Branch b = ctx;
		String formatName = b.format.tagName.toLowerCase(Locale.ROOT);
		nameCounter.put(formatName, nameCounter.getInt(formatName) - 1);
		ctx = stack.pop();
		return b;
	}
	
	private Branch popUnclosed() {
		ctx.setUnclosed();
		return popCtx();
	}
	
	private enum ParseState {
		NORMAL,
		STRING,
		TAG,
	}
	
	public static String quoteArg(String arg) {
		if (arg.isEmpty() || arg.length() < 2) return arg;
		int min = 0;
		int max = arg.length();
		char first = arg.charAt(0);
		if (first == '\'' || first == '"') {
			++min;
			// note to self: yes, this means unclosed quotes WILL be eaten. yes, this is how it works in regular minimessage
			if (arg.charAt(max - 1) == first) --max;
			return arg.substring(min, max).replaceAll("\\\\" + first, String.valueOf(first));
		}
		return arg;
	}
	
	public Node nodeArg(String arg) throws InvalidMiniMessage {
		if (arg.isEmpty() || arg.length() < 2) return new Literal(arg);
		int min = 0;
		int max = arg.length();
		char first = arg.charAt(0);
		if (first == '\'' || first == '"') {
			++min;
			if (arg.charAt(max - 1) == first) --max;
			return this.subParse(arg.substring(min, max).replaceAll("\\\\" + first, String.valueOf(first)), new QuoteFormat(String.valueOf(first))).root;
		}
		return new Literal(arg);
	}
	
	public static Optional<TextColor> parseColor(String color) {
		if (color.startsWith("#")) {
			try {
				int i = Integer.parseInt(color.substring(1), 16);
				return i >= 0 && i <= 0xFFFFFF ? Optional.of(TextColor.fromRgb(i)) : Optional.empty();
			} catch (NumberFormatException e) {
				return Optional.empty();
			}
		} else {
			return parseNamedColor(color);
		}
	}
	
	public static Optional<TextColor> parseNamedColor(String color) {
		switch (color.toLowerCase(Locale.ROOT)) {
		case "black":
			return Optional.of(TextColor.fromLegacyFormat(ChatFormatting.BLACK));
		case "dark_blue":
			return Optional.of(TextColor.fromLegacyFormat(ChatFormatting.DARK_BLUE));
		case "dark_green":
			return Optional.of(TextColor.fromLegacyFormat(ChatFormatting.DARK_GREEN));
		case "dark_aqua":
			return Optional.of(TextColor.fromLegacyFormat(ChatFormatting.DARK_AQUA));
		case "dark_red":
			return Optional.of(TextColor.fromLegacyFormat(ChatFormatting.DARK_RED));
		case "dark_purple":
			return Optional.of(TextColor.fromLegacyFormat(ChatFormatting.DARK_PURPLE));
		case "gold":
			return Optional.of(TextColor.fromLegacyFormat(ChatFormatting.GOLD));
		case "gray":
		case "grey":
			return Optional.of(TextColor.fromLegacyFormat(ChatFormatting.GRAY));
		case "dark_gray":
		case "dark_grey":
			return Optional.of(TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY));
		case "blue":
			return Optional.of(TextColor.fromLegacyFormat(ChatFormatting.BLUE));
		case "green":
			return Optional.of(TextColor.fromLegacyFormat(ChatFormatting.GREEN));
		case "aqua":
			return Optional.of(TextColor.fromLegacyFormat(ChatFormatting.AQUA));
		case "red":
			return Optional.of(TextColor.fromLegacyFormat(ChatFormatting.RED));
		case "light_purple":
			return Optional.of(TextColor.fromLegacyFormat(ChatFormatting.LIGHT_PURPLE));
		case "yellow":
			return Optional.of(TextColor.fromLegacyFormat(ChatFormatting.YELLOW));
		case "white":
			return Optional.of(TextColor.fromLegacyFormat(ChatFormatting.WHITE));
		}
		return Optional.empty();
	}
	
	public <T> boolean parseComponent(DataComponentType<T> type, Tag tag, DataComponentPatch.Builder components) {
		if (this.registryOps.isEmpty()) return false;
		Optional<T> component = type.codecOrThrow().parse(this.registryOps.get(), tag).resultOrPartial();
		if (component.isEmpty()) return false;
		components.set(type, component.get());
		return true;
	}
	
}
