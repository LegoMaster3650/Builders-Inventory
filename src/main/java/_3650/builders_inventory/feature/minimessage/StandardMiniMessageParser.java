package _3650.builders_inventory.feature.minimessage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;

import _3650.builders_inventory.api.minimessage.MiniMessageParser;
import _3650.builders_inventory.api.minimessage.color.PrideFlagGradients;
import _3650.builders_inventory.api.minimessage.format.ClickFormat;
import _3650.builders_inventory.api.minimessage.format.ColorFormat;
import _3650.builders_inventory.api.minimessage.format.FontFormat;
import _3650.builders_inventory.api.minimessage.format.GradientFormat;
import _3650.builders_inventory.api.minimessage.format.HoverFormat;
import _3650.builders_inventory.api.minimessage.format.InsertionFormat;
import _3650.builders_inventory.api.minimessage.format.InverseStyleFormat;
import _3650.builders_inventory.api.minimessage.format.RainbowFormat;
import _3650.builders_inventory.api.minimessage.format.ShadowColorFormat;
import _3650.builders_inventory.api.minimessage.format.StyleFormat;
import _3650.builders_inventory.api.minimessage.format.TransitionFormat;
import _3650.builders_inventory.api.minimessage.parser.ArgData;
import _3650.builders_inventory.api.minimessage.parser.MiniMessageTagParser;
import _3650.builders_inventory.api.minimessage.parser.InvalidMiniMessage;
import _3650.builders_inventory.api.minimessage.parser.MiniMessageTagOutput;
import _3650.builders_inventory.api.minimessage.tags.Branch;
import _3650.builders_inventory.api.minimessage.tags.HiddenLiteral;
import _3650.builders_inventory.api.minimessage.tags.Keybind;
import _3650.builders_inventory.api.minimessage.tags.Node;
import _3650.builders_inventory.api.minimessage.tags.ObjectTag;
import _3650.builders_inventory.api.minimessage.tags.TaggedLiteral;
import _3650.builders_inventory.api.minimessage.tags.Translatable;
import _3650.builders_inventory.api.minimessage.tags.TranslatableFallback;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ResolvableProfile;

public class StandardMiniMessageParser implements MiniMessageTagParser {
	
	@Override
	public boolean parseTag(MiniMessageTagOutput output, MiniMessageParser parser, String argString, String name, ArgData args, @Nullable String server) throws InvalidMiniMessage {
		if (name.charAt(0) == '#') {
			// hex color tag
			try {
				int color = Integer.parseInt(name.substring(1), 16);
				if (color > 0xFFFFFF) throw error("Color %s must be less than #FFFFFF", name);
				output.push(new ColorFormat(argString, name, TextColor.fromRgb(color)));
				return true;
			} catch (NumberFormatException e) {
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw error("%s is not a valid hex color", name);
			} catch (IndexOutOfBoundsException e) {
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw error("Color cannot be empty");
			}
		}
		switch (name.toLowerCase()) {
		case "color":
		case "colour":
		case "c":
		{
			String colorName = args.requireWarn("Color tag requires a color");
			if (colorName.isEmpty()) {
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw error("Color name cannot be empty");
			}
			var color = MiniMessageParser.parseColor(colorName);
			if (color.isEmpty()) {
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw error("%s is not a valid color", colorName);
			}
			output.push(new ColorFormat(argString, name, color.get()));
			return true;
		}
		case "shadow":
		{
			String colName = args.requireQuiet();
			if (colName.isEmpty()) {
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw invalid("Color name cannot be empty");
			}
			if (colName.charAt(0) == '#' && colName.length() == 9) {
				// RRGGBBAA
				try {
					int color = Integer.parseInt(colName.substring(1, 7), 16);
					int alpha = Integer.parseInt(colName.substring(7, 9), 16);
					output.push(new ShadowColorFormat(argString, name, color | (alpha << 24)));
					return true;
				} catch (NumberFormatException e) {
					if (output == MiniMessageTagOutput.SINK) return false;
					else throw invalid("%s is not a valid hex color", name);
				}
			}
			var color = MiniMessageParser.parseColor(colName);
			if (color.isEmpty()) {
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw invalid("%s is not a valid color", colName);
			}
			int alpha = 0x3F;
			if (args.hasNext()) {
				String alphaStr = args.next();
				try {
					alpha = (int) (Double.parseDouble(alphaStr) * 0xFF);
					if (alpha < 0 || alpha > 0xFF) throw invalid("Alpha %s must be between 0 and 1 (including those numbers)", alphaStr);
				} catch (NumberFormatException e) {
					if (output == MiniMessageTagOutput.SINK) return false;
					else throw invalid("%s is not a valid number", alphaStr);
				}
			}
			output.push(new ShadowColorFormat(argString, name, color.get().getValue() | (alpha << 24)));
			return true;
		}
		case "black":
		case "dark_blue":
		case "dark_green":
		case "dark_aqua":
		case "dark_red":
		case "dark_purple":
		case "gold":
		case "gray":
		case "grey":
		case "dark_gray":
		case "dark_grey":
		case "blue":
		case "green":
		case "aqua":
		case "red":
		case "light_purple":
		case "yellow":
		case "white":
		{
			var color = MiniMessageParser.parseNamedColor(name);
			if (color.isEmpty()) {
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw error("%s is not a valid color, but should be! Please report this bug!", name);
			}
			output.push(new ColorFormat(argString, name, color.get()));
			return true;
		}
		case "bold":
		case "b":
			output.push(new StyleFormat(argString, name, ChatFormatting.BOLD));
			return true;
		case "!bold":
		case "!b":
			output.push(new InverseStyleFormat(argString, name, ChatFormatting.BOLD));
			return true;
		case "italic":
		case "em":
		case "i":
			output.push(new StyleFormat(argString, name, ChatFormatting.ITALIC));
			return true;
		case "!italic":
		case "!em":
		case "!i":
			output.push(new InverseStyleFormat(argString, name, ChatFormatting.ITALIC));
			return true;
		case "underlined":
		case "u":
			output.push(new StyleFormat(argString, name, ChatFormatting.UNDERLINE));
			return true;
		case "!underlined":
		case "!u":
			output.push(new InverseStyleFormat(argString, name, ChatFormatting.UNDERLINE));
			return true;
		case "strikethrough":
		case "st":
			output.push(new StyleFormat(argString, name, ChatFormatting.STRIKETHROUGH));
			return true;
		case "!strikethrough":
		case "!st":
			output.push(new InverseStyleFormat(argString, name, ChatFormatting.STRIKETHROUGH));
			return true;
		case "obfuscated":
		case "obf":
			output.push(new StyleFormat(argString, name, ChatFormatting.OBFUSCATED));
			return true;
		case "!obfuscated":
		case "!obf":
			output.push(new InverseStyleFormat(argString, name, ChatFormatting.OBFUSCATED));
			return true;
		case "reset":
			if (args.size > 0) throw error("Tag %s does not accept arguments", name);
			while (parser.ctx != parser.root) {
				Branch b = output.popUnclosed();
				output.append(b);
			}
			output.append(HiddenLiteral.tag(name));
			return true;
		case "click":
		{
			String action = args.requireWarn("Click tag requires an action to be specified");
			String actLower = action.toLowerCase();
			for (var act : ClickEvent.Action.values()) {
				if (act.getSerializedName().equals(actLower)) {
					if (!act.isAllowedFromServer()) throw error("Click action %s is not allowed", actLower);
					switch (act) {
					case OPEN_URL:
					{
						String uriArg = MiniMessageParser.quoteArg(args.requireWarn("Open URL click event requires a URL"));
						URI uri;
						try {
							uri = Util.parseAndValidateUntrustedUri(uriArg);
						} catch (URISyntaxException e) {
							if (output == MiniMessageTagOutput.SINK) return false;
							else throw error("%s is not a valid link", uriArg);
						}
						output.push(new ClickFormat(argString, name, ClickFormat.openUrl(uri)));
						return true;
					}
					case RUN_COMMAND:
					{
						String command = MiniMessageParser.quoteArg(args.requireWarn("Run Command click event requires a command"));
						for (int i = 0; i < command.length(); i++) {
							char c = command.charAt(i);
							if (!StringUtil.isAllowedChatCharacter(c)) {
								if (output == MiniMessageTagOutput.SINK) return false;
								else throw error("Character %s not allowed in command", String.valueOf(c));
							}
						}
						output.push(new ClickFormat(argString, name, ClickFormat.runCommand(command)));
						return true;
					}
					case SUGGEST_COMMAND:
					{
						String command = MiniMessageParser.quoteArg(args.requireWarn("Suggest Command click event requires a command"));
						for (int i = 0; i < command.length(); i++) {
							char c = command.charAt(i);
							if (!StringUtil.isAllowedChatCharacter(c)) {
								if (output == MiniMessageTagOutput.SINK) return false;
								else throw error("Character %s not allowed in command", String.valueOf(c));
							}
						}
						output.push(new ClickFormat(argString, name, ClickFormat.suggestCommand(command)));
						return true;
					}
					case SHOW_DIALOG:
					{
						throw invalid("Dialog click action is not implemented");
					}
					case CHANGE_PAGE:
					{
						String pageArg = args.requireWarn("Change Page click event requires a target page");
						int page;
						try {
							page = Integer.parseInt(pageArg);
						} catch (NumberFormatException e) {
							if (output == MiniMessageTagOutput.SINK) return false;
							else throw error("%s is not a valid number", pageArg);
						}
						if (page < 1) {
							throw error("%s must be 1 or greater", pageArg);
						}
						output.push(new ClickFormat(argString, name, ClickFormat.changePage(page)));
						return true;
					}
					case COPY_TO_CLIPBOARD:
					{
						String value = MiniMessageParser.quoteArg(args.requireWarn("Copy to Clipboard click event requires a value"));
						output.push(new ClickFormat(argString, name, ClickFormat.copyToClipboard(value)));
						return true;
					}
					case CUSTOM:
					{
						ResourceLocation id;
						try {
							id = ResourceLocation.parse(MiniMessageParser.quoteArg(args.requireQuiet()));
						} catch (ResourceLocationException e) {
							if (output == MiniMessageTagOutput.SINK && !args.hasNext()) return false;
							else throw invalid(e.getMessage());
						}
						if (!args.hasNext()) {
							output.push(new ClickFormat(argString, name, ClickFormat.custom(id, Optional.empty())));
						}
						String payloadArg = args.next();
						Tag tag;
						try {
							tag = parser.tagParser.parseFully(payloadArg);
						} catch (CommandSyntaxException e) {
							throw invalid("Invalid custom click event payload NBT for %s: %s", id, payloadArg);
						}
						output.push(new ClickFormat(argString, name, ClickFormat.custom(id, Optional.ofNullable(tag))));
						return true;
					}
					default:
						if (output == MiniMessageTagOutput.SINK) return false;
						else throw error("Click action %s not implemented. Report this bug to the mod author.", actLower);
					}
				}
			}
			if (output == MiniMessageTagOutput.SINK) return false;
			else throw warningOrError(args.hasNext(), "%s is not a valid click event", action);
		}
		case "hover":
		{
			String action = args.requireWarn("Hover tag requires an action to be specified");
			switch (action.toLowerCase(Locale.ROOT)) {
			case "show_text":
			{
				String arg = args.requireWarn("Show Text hover event requires text to show");
				output.push(new HoverFormat(argString, name, HoverFormat.text(
						parser.nodeArg(arg)
						)));
				return true;
			}
			case "show_item":
			{
				ResourceLocation id;
				try {
					id = ResourceLocation.parse(MiniMessageParser.quoteArg(args.requireWarn("Show Item hover event requires an item to show")));
				} catch (ResourceLocationException e) {
					if (output == MiniMessageTagOutput.SINK && !args.hasNext()) return false;
					else throw error(e.getMessage());
				}
				Item item = BuiltInRegistries.ITEM.getOptional(id).orElseThrow(invalidSup(output, args, "%s is not a valid item ID", id.toString()));
				if (!args.hasNext()) {
					output.push(new HoverFormat(argString, name, HoverFormat.item(
							item,
							1,
							DataComponentPatch.EMPTY
							)));
					return true;
				}
				
				String countArg = args.next();
				int count;
				try {
					count = Integer.parseInt(countArg);
				} catch (NumberFormatException e) {
					if (output == MiniMessageTagOutput.SINK && !args.hasNext()) return false;
					else throw error("%s is not a valid item count", countArg);
				}
				if (!args.hasNext()) {
					output.push(new HoverFormat(argString, name, HoverFormat.item(
							item,
							count,
							DataComponentPatch.EMPTY
							)));
					return true;
				}
				
				final String fval = args.peek();
				if (fval.startsWith("{")) {
					args.next();
					// ignore it, it's legacy nbt that does nothing but is valid minimessage sooo
					output.push(new HoverFormat(argString, name, HoverFormat.item(
							item,
							count,
							DataComponentPatch.EMPTY
							)));
					return true;
				}
				
				if (parser.registryOps.isEmpty()) throw error("Item components need a world open to resolve");
				
				final DataComponentPatch.Builder components = DataComponentPatch.builder();
				final ReferenceArraySet<DataComponentType<?>> keys = new ReferenceArraySet<>();
				try {
					while (args.hasNext()) {
						String key = MiniMessageParser.quoteArg(args.next());
						if (key.isEmpty()) {
							if (output == MiniMessageTagOutput.SINK && !args.hasNext()) break;
							else throw error("Cannot have empty component type");
						}
						if (key.charAt(0) == '!') {
							// remove component
							String rmKey = key.substring(1);
							if (rmKey.isEmpty()) throw warning("Removed item component must be specified");
							
							ResourceLocation loc = ResourceLocation.parse(rmKey);
							DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(loc);
							if (type == null) {
								if (output == MiniMessageTagOutput.SINK && !args.hasNext()) break;
								else throw error("Invalid component type %s", key);
							}
							if (type.isTransient()) throw error("Component type %s is client-only", rmKey);
							if (!keys.add(type)) throw error("Component %s is already specified in this tag", rmKey);
							
							components.remove(type);
						} else {
							// component
							ResourceLocation loc = ResourceLocation.parse(key);
							DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(loc);
							if (type == null) {
								if (output == MiniMessageTagOutput.SINK && !args.hasNext()) break;
								else throw error("Invalid component type %s", key);
							}
							if (type.isTransient()) throw error("Component type %s is client-only", key);
							if (!keys.add(type)) throw error("Component %s is already specified in this tag", key);
							
							String value = args.requireWarn("Item components require a following value");
							Tag tag;
							try {
								tag = parser.tagParser.parseFully(value);
							} catch (CommandSyntaxException e) {
								throw error("Invalid component NBT for %s: %s", key, value);
							}
							if (!parser.parseComponent(type, tag, components)) throw error("Invalid component value for %s: %s", key, value);
						}
					}
				} catch (ResourceLocationException e) {
					if (output == MiniMessageTagOutput.SINK && !args.hasNext()) return false;
					else throw error(e.getMessage());
				}
				output.push(new HoverFormat(argString, name, HoverFormat.item(
						item,
						count,
						components.build()
						)));
				return true;
				
//				CompoundTag tag;
//				try {
//					tag = TagParser.parseTag(quoteArg(args.next()));
//				} catch (CommandSyntaxException e) {
//					return false;
//				}
//				parser.push(new HoverFormat(argString, name, HoverFormat.item(
//						item,
//						count,
//						DataComponentPatch.EMPTY
//						Optional.of(tag)
//						)));
//				return true;
			}
			case "show_entity":
			{
				ResourceLocation id;
				try {
					id = ResourceLocation.parse(MiniMessageParser.quoteArg(args.requireWarn("Show Entity hover event requires an entity to show")));
				} catch (ResourceLocationException e) {
					if (output == MiniMessageTagOutput.SINK && !args.hasNext()) return false;
					else throw error(e.getMessage());
				}
				EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElseThrow(invalidSup(output, args, "%s is not a valid entity ID", id.toString()));
				
				String uuidStr = args.requireWarn("Show Entity hover event requires an entity UUID");
				UUID uuid;
				try {
					uuid = UUID.fromString(uuidStr);
				} catch (IllegalArgumentException e) {
					if (output == MiniMessageTagOutput.SINK && !args.hasNext()) return false;
					else throw error("%s is not a valid UUID", uuidStr);
				}
				if (!args.hasNext()) {
					output.push(new HoverFormat(argString, name, HoverFormat.entity(type, uuid)));
					return true;
				}
				
				output.push(new HoverFormat(argString, name, HoverFormat.entity(type, uuid, parser.nodeArg(args.next()))));
				return true;
			}
			default:
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw warningOrError(args.hasNext(), "%s is not a valid hover event", action);
			}
		}
		case "key":
		{
			String key = args.requireWarn("Key tag requires a keybind");
			output.append(new Keybind(argString, key));
			return true;
		}
		case "lang":
		case "translate":
		case "tr":
		{
			String key = args.requireWarn("Translate tag requires a translation key");
			if (args.size > 1) {
				ArrayList<Node> trargs = new ArrayList<>(args.size);
				while (args.hasNext()) trargs.add(parser.nodeArg(args.next()));
//				for (Node n : args) trargs.add(n instanceof QuoteLiteral quote ? parse(quote.text, new QuoteFormat(quote.quote), quote.quoteState).root : n);
				output.append(new Translatable(argString, key, trargs));
			} else {
				output.append(new Translatable(argString, key, List.of()));
			}
			return true;
		}
		case "lang_or":
		case "translate_or":
		case "tr_or":
		{
			String key = args.requireWarn("Translate tag requires a translation key");
			String fallback = args.requireWarn("Translate with fallback tag requires a fallback");
			if (args.size > 2) {
				ArrayList<Node> trargs = new ArrayList<>(args.size);
				while (args.hasNext()) trargs.add(parser.nodeArg(args.next()));
				output.append(new TranslatableFallback(argString, key, fallback, trargs));
			} else {
				output.append(new TranslatableFallback(argString, key, fallback, List.of()));
			}
			return true;
		}
		case "insert":
		{
			String text = args.requireWarn("Insert tag requires text to insert");
			output.push(new InsertionFormat(argString, name, text));
			return true;
		}
		case "rainbow":
		{
			if (args.size == 0) {
				output.push(new RainbowFormat(argString, name, false, 0));
				return true;
			}
			String arg = args.next();
			if (arg.isEmpty()) {
				output.push(new RainbowFormat(argString, name, false, 0));
				return true;
			}
			boolean invert = arg.charAt(0) == '!';
			if (invert) arg = arg.substring(1);
			int phase = 0;
			if (arg.length() > 0) {
				try {
					phase = Integer.valueOf(arg);
				} catch (NumberFormatException e) {
					throw error("%s is not a valid number", arg);
				}
			}
			output.push(new RainbowFormat(argString, name, invert, phase));
			return true;
		}
		case "gradient":
		{
			ArrayList<TextColor> colors = new ArrayList<>();
			double phase = 0;
			while (args.hasNext()) {
				String arg = args.next();
				
				var color = MiniMessageParser.parseColor(arg);
				if (color.isPresent()) colors.add(color.get());
				else {
					if (!args.hasNext()) {
						try {
							phase = Double.parseDouble(arg);
							if (phase < -1.0 || phase > 1.0) throw error("Phase %s must be between -1 and 1 (including those numbers)", phase);
						} catch (NumberFormatException e) {
							if (output == MiniMessageTagOutput.SINK && !args.hasNext()) return false;
							else throw error("%s is not a valid color", arg);
						}
					}
				}
			}
			if (colors.size() == 1) throw error("Gradient must have more than 1 color, but only has %s", colors.get(0));
			output.push(new GradientFormat(argString, name, colors, phase));
			return true;
		}
		case "transition":
		{
			ArrayList<TextColor> colors = new ArrayList<>();
			double phase = 0;
			while (args.hasNext()) {
				String arg = args.next();
				
				var color = MiniMessageParser.parseColor(arg);
				if (color.isPresent()) colors.add(color.get());
				else {
					if (!args.hasNext()) {
						try {
							phase = Double.parseDouble(arg);
							if (phase < -1.0 || phase > 1.0) throw error("Phase %s must be between -1 and 1 (including those numbers)", phase);
						} catch (NumberFormatException e) {
							if (output == MiniMessageTagOutput.SINK && !args.hasNext()) return false;
							else throw error("%s is not a valid color", arg);
						}
					}
				}
			}
			if (colors.size() == 1) {
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw error("Transition must have more than 1 color, but only has %s", colors.get(0));
			}
			output.push(new TransitionFormat(argString, name, colors, phase));
			return true;
		}
		case "pride":
		{
			var flag = PrideFlagGradients.pride;
			double phase = 0;
			if (args.hasNext()) {
				String arg = args.next();
				var flagQuery = PrideFlagGradients.byName(arg);
				if (flagQuery.isPresent()) flag = flagQuery.get();
				else if (!arg.isEmpty()) {
					try {
						phase = Double.parseDouble(arg);
						if (phase < -1.0 || phase > 1.0) throw error("Phase %s must be between -1 and 1 (including those numbers)", phase);
					} catch (NumberFormatException e) {
						if (output == MiniMessageTagOutput.SINK) return false;
						else throw error("%s is not an available pride flag gradient"); // "_ is not a valid pride flag" probably wouldn't be the best way to word it
					}
				}
			}
			output.push(new GradientFormat(argString, name, flag.colors, phase));
			return true;
		}
		case "font":
		{
			try {
				String arg1 = MiniMessageParser.quoteArg(args.requireWarn("Font tag requires a font"));
				if (args.hasNext()) {
					String arg2 = args.next();
					ResourceLocation font = ResourceLocation.fromNamespaceAndPath(arg1, arg2);
					output.push(new FontFormat(argString, name, font));
					return true;
				} else {
					ResourceLocation font = ResourceLocation.parse(arg1);
					output.push(new FontFormat(argString, name, font));
					return true;
				}
			} catch (ResourceLocationException e) {
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw error(e.getMessage());
			}
		}
		case "sprite":
		{
			try {
				String arg1 = MiniMessageParser.quoteArg(args.requireQuiet());
				if (args.hasNext()) {
					String arg2 = args.next();
					ResourceLocation atlas = ResourceLocation.parse(arg1);
					ResourceLocation sprite = ResourceLocation.parse(arg2);
					AtlasSprite contents = new AtlasSprite(atlas, sprite);
					output.append(new ObjectTag(argString, contents));
					return true;
				} else {
					ResourceLocation sprite = ResourceLocation.parse(arg1);
					AtlasSprite contents = new AtlasSprite(AtlasSprite.DEFAULT_ATLAS, sprite);
					output.append(new ObjectTag(argString, contents));
					return true;
				}
			} catch (ResourceLocationException e) {
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw invalid(e.getMessage());
			}
		}
		case "head":
		{
			String arg = args.requireQuiet();
			ResolvableProfile profile = null;
			try {
				UUID uuid = UUID.fromString(arg);
				profile = ResolvableProfile.createUnresolved(uuid);
			} catch (IllegalArgumentException e) {}
			if (profile == null && arg.contains("/")) try {
				ResourceLocation texture = ResourceLocation.parse(arg);
				JsonObject hackyEvilJson = new JsonObject();
				hackyEvilJson.addProperty("texture", texture.toString());
				profile = ResolvableProfile.CODEC.parse(JsonOps.INSTANCE, hackyEvilJson)
						.resultOrPartial()
						.orElseThrow(invalidSup());
			} catch (ResourceLocationException e) {}
			if (profile == null) profile = ResolvableProfile.createUnresolved(arg);
			
			boolean outer_layer = true;
			if (args.hasNext()) {
				String hat = args.next();
				switch (hat.toLowerCase(Locale.ROOT)) {
				case "true":
				case "on":
					outer_layer = true;
					break;
				case "false":
				case "off":
					outer_layer = false;
					break;
				}
			}
			PlayerSprite contents = new PlayerSprite(profile, outer_layer);
			output.append(new ObjectTag(argString, contents));
			return true;
		}
		case "newline":
		case "br":
			output.append(new TaggedLiteral(argString, "\n"));
			return true;
		default:
			return false; // the only return false permitted outside of sunk tags
		}
	}
	
	private static InvalidMiniMessage warningOrError(boolean isError, String message) {
		return isError ? InvalidMiniMessage.error(message) : InvalidMiniMessage.warning(message);
	}
	
	private static InvalidMiniMessage warningOrError(boolean isError, String message, Object... formatArgs) {
		return warningOrError(isError, String.format(message, formatArgs));
	}
	
	private static InvalidMiniMessage warning(String message) {
		return InvalidMiniMessage.warning(message);
	}
	
//	private static InvalidMiniMessage warning(String message, Object... formatArgs) {
//		return InvalidMiniMessage.warning(String.format(message, formatArgs));
//	}
	
	private static InvalidMiniMessage error(String message) {
		return InvalidMiniMessage.error(message);
	}
	
	private static InvalidMiniMessage error(String message, Object... formatArgs) {
		return InvalidMiniMessage.error(String.format(message, formatArgs));
	}
	
	private static Supplier<InvalidMiniMessage> warningSupplier(String message) {
		return () -> InvalidMiniMessage.warning(message);
	}
	
	private static Supplier<InvalidMiniMessage> errorSupplier(String message) {
		return () -> InvalidMiniMessage.error(message);
	}
	
	private static Supplier<InvalidMiniMessage> invalidSup(MiniMessageTagOutput output, ArgData args, String message, Object... formatArgs) {
		final String formattedMessage = String.format(message, formatArgs);
		return output == MiniMessageTagOutput.SINK && !args.hasNext() ? warningSupplier(formattedMessage) : errorSupplier(formattedMessage);
	}
	
}
