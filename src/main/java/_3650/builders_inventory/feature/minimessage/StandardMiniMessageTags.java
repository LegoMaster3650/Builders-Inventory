package _3650.builders_inventory.feature.minimessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import _3650.builders_inventory.api.minimessage.autocomplete.ReloadableResourceArg;
import _3650.builders_inventory.api.minimessage.autocomplete.SimpleStringArg;
import _3650.builders_inventory.api.minimessage.autocomplete.AtlasSpriteSuggestor;
import _3650.builders_inventory.api.minimessage.autocomplete.AutocompleteTagLookup.ACBuilder;
import _3650.builders_inventory.api.minimessage.autocomplete.AutocompleteTagLookup.Suggestor;
import _3650.builders_inventory.api.minimessage.color.PrideFlagGradients;
import _3650.builders_inventory.api.util.StringDefinitions;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.ClickEvent;

public class StandardMiniMessageTags {
	
	public static void onRegisterTags(ACBuilder builder, @Nullable String server) {
		ArrayList<String> colors = StringDefinitions.list()
				.add("black")
				.add("dark_blue")
				.add("dark_green")
				.add("dark_aqua")
				.add("dark_red")
				.add("dark_purple")
				.add("gold")
				.add("gray")
				.add("grey")
				.add("dark_gray")
				.add("dark_grey")
				.add("blue")
				.add("green")
				.add("aqua")
				.add("red")
				.add("light_purple")
				.add("yellow")
				.add("white")
				.add("#")
				.finish();
		SimpleStringArg colorsArg = SimpleStringArg.of(colors);
		
		ArrayList<String> styles = StringDefinitions.list()
				.add("bold", "b")
				.add("italic", "em", "i")
				.add("underlined", "u")
				.add("strikethrough", "st")
				.add("obfuscated", "obf")
				.variant(str -> '!' + str)
				.finish();
		
		ArrayList<String> clickEvents = StringDefinitions.list()
				.add(add -> {
					for (var act : ClickEvent.Action.values()) {
						if (act.isAllowedFromServer() && act != ClickEvent.Action.SHOW_DIALOG) add.accept(act.getSerializedName());
					}
				})
				.finish();
		SimpleStringArg clickEventsArg = SimpleStringArg.of(clickEvents);
		
		ArrayList<String> hoverEvents = StringDefinitions.list()
				.add("show_text")
				.add("show_item")
				.add("show_entity")
				.finish();
		SimpleStringArg hoverEventsArg = SimpleStringArg.of(hoverEvents);
		
		SimpleStringArg prideFlagArg = SimpleStringArg.of(
				Arrays.stream(PrideFlagGradients.values())
				.map(flag -> flag.name())
				.collect(Collectors.toList()));
		
		builder
				.tag("color", "colour", "c").build(b -> b
						.arg(colorsArg)
				)
				.tag(colors).build()
				.tag("shadow").build(b -> b
						.arg(colorsArg)
				)
				.tag(styles).build()
				.tag("reset").build()
				.tag("click").build(b -> b
						.arg(clickEventsArg)
				)
				.tag("hover").build(b -> b
						.arg(hoverEventsArg)
						.arg((prev, input) -> switch (prev) {
						case "show_text" -> List.of();
						case "show_item" -> ReloadableResourceArg.ITEMS.findNonMatch(input);
						case "show_entity" -> ReloadableResourceArg.ENTITIES.findNonMatch(input);
						default -> List.of();
						})
				)
				.tag("key").build(b -> b
						.arg(ReloadableResourceArg.KEYS)
				)
				.tag("lang", "translate", "tr").build(b -> b
						.arg(ReloadableResourceArg.LANG)
				)
				.tag("lang_or", "translate_or", "tr_or").build(b -> b
						.arg(ReloadableResourceArg.LANG)
				)
				.tag("insert").build()
				.tag("rainbow").build(b -> b
						.arg("!")
				)
				.tag("gradient").build(b -> b
						.varArg(Suggestor.arg(colorsArg))
				)
				.tag("transition").build(b -> b
						.varArg(Suggestor.arg(colorsArg))
				)
				.tag("pride").build(b -> b
						.arg(prideFlagArg))
				.tag("font").build(b -> b
						.identifier(ReloadableResourceArg.FONTS)
				)
				.tag("sprite").build(b -> b
						.arg((prev, input) -> {
							var atlases = ReloadableResourceArg.ATLASES.findNonMatch(input);
							if (!atlases.isEmpty()) return atlases;
							var arg = AtlasSpriteSuggestor.INSTANCE.getAtlasArg(AtlasIds.BLOCKS);
							if (arg.isPresent()) return arg.get().findNonMatch(input);
							return List.of();
						})
						.arg(AtlasSpriteSuggestor.INSTANCE))
				.tag("head").build(b -> b
						.arg(Suggestor.playerList()))
				.tag("newline", "br").build()
				;
	}
	
}
