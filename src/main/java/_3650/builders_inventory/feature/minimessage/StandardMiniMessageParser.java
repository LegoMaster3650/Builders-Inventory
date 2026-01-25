package _3650.builders_inventory.feature.minimessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

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
import _3650.builders_inventory.api.minimessage.tags.TaggedLiteral;
import _3650.builders_inventory.api.minimessage.tags.Translatable;
import _3650.builders_inventory.api.minimessage.tags.TranslatableFallback;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class StandardMiniMessageParser implements MiniMessageTagParser {
	
	@Override
	public boolean parseTag(MiniMessageTagOutput output, MiniMessageParser parser, String argString, String name, ArgData args, @Nullable String server) throws InvalidMiniMessage {
		if (name.charAt(0) == '#') {
			// hex color tag
			try {
				int color = Integer.parseInt(name.substring(1), 16);
				if (color > 0xFFFFFF) throw invalid("Color %s must be less than #FFFFFF", name);
				output.push(new ColorFormat(argString, name, TextColor.fromRgb(color)));
				return true;
			} catch (NumberFormatException e) {
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw invalid("%s is not a valid hex color", name);
			} catch (IndexOutOfBoundsException e) {
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw invalid("Color cannot be empty");
			}
		}
		switch (name.toLowerCase()) {
		case "color":
		case "colour":
		case "c":
		{
			String colorName = args.requireQuiet();
			if (colorName.isEmpty()) {
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw invalid("Color name cannot be empty");
			}
			var color = MiniMessageParser.parseColor(colorName);
			if (color.isEmpty()) {
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw invalid("%s is not a valid color", colorName);
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
				else throw invalid("%s is not a valid color, but should be! Please report this bug!", name);
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
			if (args.size > 0) throw new InvalidMiniMessage(String.format("Tag %s does not accept arguments", name));
			while (parser.ctx != parser.root) {
				Branch b = output.popUnclosed();
				output.append(b);
			}
			output.append(HiddenLiteral.tag(name));
			return true;
		case "click":
		{
			String action = args.requireQuiet();
			String value = MiniMessageParser.quoteArg(args.requireQuiet());
			String actLower = action.toLowerCase();
			for (var act : ClickEvent.Action.values()) {
				if (act.getSerializedName().equals(actLower) && act.isAllowedFromServer()) {
					output.push(new ClickFormat(argString, name, new ClickEvent(act, value)));
					return true;
				}
			}
			return false;
		}
		case "hover":
		{
			String action = args.requireQuiet();
			switch (action.toLowerCase(Locale.ROOT)) {
			case "show_text":
			{
				String arg = args.requireQuiet();
				output.push(new HoverFormat(argString, name, HoverFormat.text(
						parser.nodeArg(arg)
						)));
				return true;
			}
			case "show_item":
			{
				ResourceLocation id;
				try {
					id = ResourceLocation.parse(MiniMessageParser.quoteArg(args.requireQuiet()));
				} catch (ResourceLocationException e) {
					if (output == MiniMessageTagOutput.SINK && !args.hasNext()) return false;
					else throw invalid(e.getMessage());
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
					else throw invalid("%s is not a valid item count", countArg);
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
				
				if (parser.registryOps.isEmpty()) throw invalid("Item components need a world open to resolve");
				
				final DataComponentPatch.Builder components = DataComponentPatch.builder();
				final ReferenceArraySet<DataComponentType<?>> keys = new ReferenceArraySet<>();
				try {
					while (args.hasNext()) {
						String key = MiniMessageParser.quoteArg(args.next());
						if (key.isEmpty()) {
							if (output == MiniMessageTagOutput.SINK && !args.hasNext()) break;
							else throw invalid("Cannot have empty component type");
						}
						if (key.charAt(0) == '!') {
							// remove component
							String rmKey = key.substring(1);
							if (rmKey.isEmpty()) return false;
							
							ResourceLocation loc = ResourceLocation.parse(rmKey);
							DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(loc);
							if (type == null) {
								if (output == MiniMessageTagOutput.SINK && !args.hasNext()) break;
								else throw invalid("Invalid component type %s", key);
							}
							if (type.isTransient()) throw invalid("Component type %s is client-only", rmKey);
							if (!keys.add(type)) throw invalid("Component %s is already specified in this tag", rmKey);
							
							components.remove(type);
						} else {
							// component
							ResourceLocation loc = ResourceLocation.parse(key);
							DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(loc);
							if (type == null) {
								if (output == MiniMessageTagOutput.SINK && !args.hasNext()) break;
								else throw invalid("Invalid component type %s", key);
							}
							if (type.isTransient()) throw invalid("Component type %s is client-only", key);
							if (!keys.add(type)) throw invalid("Component %s is already specified in this tag", key);
							
							String value = args.requireQuiet();
							Tag tag;
							try {
								tag = new TagParser(new StringReader(value)).readValue();
							} catch (CommandSyntaxException e) {
								throw invalid("Invalid component NBT for %s: %s", key, value);
							}
							if (!parser.parseComponent(type, tag, components)) throw invalid("Invalid component value for %s: %s", key, value);
						}
					}
				} catch (ResourceLocationException e) {
					if (output == MiniMessageTagOutput.SINK && !args.hasNext()) return false;
					else throw invalid(e.getMessage());
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
					id = ResourceLocation.parse(MiniMessageParser.quoteArg(args.requireQuiet()));
				} catch (ResourceLocationException e) {
					if (output == MiniMessageTagOutput.SINK && !args.hasNext()) return false;
					else throw invalid(e.getMessage());
				}
				EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElseThrow(invalidSup(output, args, "%s is not a valid entity ID", id.toString()));
				
				String uuidStr = args.requireQuiet();
				UUID uuid;
				try {
					uuid = UUID.fromString(uuidStr);
				} catch (IllegalArgumentException e) {
					if (output == MiniMessageTagOutput.SINK && !args.hasNext()) return false;
					else throw invalid("%s is not a valid UUID", uuidStr);
				}
				if (!args.hasNext()) {
					output.push(new HoverFormat(argString, name, HoverFormat.entity(type, uuid)));
					return true;
				}
				
				output.push(new HoverFormat(argString, name, HoverFormat.entity(type, uuid, parser.nodeArg(args.next()))));
				return true;
			}
			default:
				if (args.hasNext()) throw invalid("%s is not a valid hover event", action);
				return false;
			}
		}
		case "key":
		{
			String key = args.requireQuiet();
			output.append(new Keybind(argString, key));
			return true;
		}
		case "lang":
		case "translate":
		case "tr":
		{
			String key = args.requireQuiet();
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
			String key = args.requireQuiet();
			String fallback = args.requireQuiet();
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
			String text = args.requireQuiet();
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
					throw invalid("%s is not a valid number", arg);
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
							if (phase < -1.0 || phase > 1.0) throw invalid("Phase %s must be between -1 and 1 (including those numbers)", phase);
						} catch (NumberFormatException e) {
							if (output == MiniMessageTagOutput.SINK && !args.hasNext()) return false;
							else throw invalid("%s is not a valid color", arg);
						}
					}
					return false;
				}
			}
			if (colors.size() == 1) throw invalid("Gradient must have more than 1 color, but only has %s", colors.get(0));
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
							if (phase < -1.0 || phase > 1.0) throw invalid("Phase %s must be between -1 and 1 (including those numbers)", phase);
						} catch (NumberFormatException e) {
							if (output == MiniMessageTagOutput.SINK && !args.hasNext()) return false;
							else throw invalid("%s is not a valid color", arg);
						}
					}
					return false;
				}
			}
			if (colors.size() == 1) {
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw invalid("Transition must have more than 1 color, but only has %s", colors.get(0));
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
						if (phase < -1.0 || phase > 1.0) throw invalid("Phase %s must be between -1 and 1 (including those numbers)", phase);
					} catch (NumberFormatException e) {
						if (output == MiniMessageTagOutput.SINK) return false;
						else throw invalid("%s is not an available pride flag gradient"); // "_ is not a valid pride flag" probably wouldn't be the best way to word it
					}
				}
			}
			output.push(new GradientFormat(argString, name, flag.colors, phase));
			return true;
		}
		case "font":
		{
			try {
				if (args.hasNext()) {
					String arg1 = args.next();
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
				}
			} catch (ResourceLocationException e) {
				if (output == MiniMessageTagOutput.SINK) return false;
				else throw invalid(e.getMessage());
			}
			return false;
		}
		case "newline":
		case "br":
			output.append(new TaggedLiteral(argString, "\n"));
			return true;
		default:
			return false;
		}
	}
	
//	private static InvalidMiniMessage invalid() {
//		return new InvalidMiniMessage();
//	}
	
	private static InvalidMiniMessage invalid(String error) {
		return new InvalidMiniMessage(error);
	}
	
	private static InvalidMiniMessage invalid(String error, Object... formatArgs) {
		return new InvalidMiniMessage(String.format(error, formatArgs));
	}
	
	private static Supplier<InvalidMiniMessage> invalidSup() {
		return () -> new InvalidMiniMessage();
	}
	
//	private static Supplier<InvalidMiniMessage> invalidSup(String error) {
//		return () -> new InvalidMiniMessage(error);
//	}
	
	private static Supplier<InvalidMiniMessage> invalidSup(MiniMessageTagOutput output, ArgData args, String error, Object... formatArgs) {
		final String format = String.format(error, formatArgs);
		return output == MiniMessageTagOutput.SINK && !args.hasNext() ? invalidSup() : () -> new InvalidMiniMessage(format);
	}
	
}
