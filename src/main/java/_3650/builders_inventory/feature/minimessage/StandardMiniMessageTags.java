package _3650.builders_inventory.feature.minimessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import _3650.builders_inventory.api.minimessage.autocomplete.ReloadableResourceArg;
import _3650.builders_inventory.api.minimessage.autocomplete.SimpleStringArg;
import _3650.builders_inventory.api.minimessage.autocomplete.AutocompleteTagLookup.ACBuilder;
import _3650.builders_inventory.api.minimessage.autocomplete.AutocompleteTagLookup.Suggestor;
import _3650.builders_inventory.api.minimessage.color.PrideFlagGradients;
import net.minecraft.network.chat.ClickEvent;

public class StandardMiniMessageTags {
	
	public static void onRegisterTags(ACBuilder builder, @Nullable String server) {
		ArrayList<String> colors = list()
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
		
		ArrayList<String> styles = list()
				.add("bold", "b")
				.add("italic", "em", "i")
				.add("underlined", "u")
				.add("strikethrough", "st")
				.add("obfuscated", "obf")
				.transform(s -> '!' + s)
				.finish();
		
		ArrayList<String> clickEvents = list()
				.add(add -> {
					for (var act : ClickEvent.Action.values()) {
						if (act.isAllowedFromServer()) add.accept(act.getSerializedName());
					}
				})
				.finish();
		SimpleStringArg clickEventsArg = SimpleStringArg.of(clickEvents);
		
		ArrayList<String> hoverEvents = list()
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
				.entry("color", "colour", "c").build(b -> b
						.arg(colorsArg)
				)
				.entry(colors).build()
				.entry(styles).build()
				.entry("reset").build()
				.entry("click").build(b -> b
						.arg(clickEventsArg)
				)
				.entry("hover").build(b -> b
						.arg(hoverEventsArg)
						.arg((prev, input) -> switch (prev) {
						case "show_text" -> List.of();
						case "show_item" -> ReloadableResourceArg.ITEMS.findNonMatch(input);
						case "show_entity" -> ReloadableResourceArg.ENTITIES.findNonMatch(input);
						default -> List.of();
						})
				)
				.entry("key").build(b -> b
						.arg(ReloadableResourceArg.KEYS)
				)
				.entry("lang", "translate", "tr").build(b -> b
						.arg(ReloadableResourceArg.LANG)
				)
				.entry("lang_or", "translate_or", "tr_or").build(b -> b
						.arg(ReloadableResourceArg.LANG)
				)
				.entry("insert").build()
				.entry("rainbow").build(b -> b
						.arg("!")
				)
				.entry("gradient").build(b -> b
						.varArg(Suggestor.arg(colorsArg))
				)
				.entry("transition").build(b -> b
						.varArg(Suggestor.arg(colorsArg))
				)
				.entry("pride").build(b -> b
						.arg(prideFlagArg))
				.entry("font").build(b -> b
						.resource(ReloadableResourceArg.FONTS)
				)
				.entry("newline", "br").build()
//				.entry("space").build(b -> b
//						.emptyArg()
//				)
//				.entry("empty").build()
				;
	}
	
	/*
	 * String Definitions
	 */
	
	private static StringDefinitions list() {
		return new StringDefinitions();
	}
	
	private static class StringDefinitions {
		
		private StringDefinitions() {}
		
		private final ArrayList<String> list = new ArrayList<>();
		
		public StringDefinitions add(String s) {
			list.add(s);
			return this;
		}
		
		public StringDefinitions add(String... strs) {
			for (String s : strs) list.add(s);
			return this;
		}
		
//		public StringDefinitions add(List<String> strs) {
//			for (String s : strs) list.add(s);
//			return this;
//		}
		
		public StringDefinitions add(Consumer<Consumer<String>> appender) {
			appender.accept(list::add);
			return this;
		}
		
		public StringDefinitions transform(Function<String, String> func) {
			String[] transformed = new String[list.size()];
			for (int i = 0; i < list.size(); i++) transformed[i] = func.apply(list.get(i));
			list.ensureCapacity(list.size() * 2);
			for (String s : transformed) list.add(s);
			return this;
		}
		
		public ArrayList<String> finish() {
			return this.list;
		}
		
	}
	
}
