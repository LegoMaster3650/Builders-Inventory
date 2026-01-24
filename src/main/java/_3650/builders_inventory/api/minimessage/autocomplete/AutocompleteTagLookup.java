package _3650.builders_inventory.api.minimessage.autocomplete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class AutocompleteTagLookup {
	
	private SimpleStringArg tags;
	private Object2ObjectOpenHashMap<String, TagArgs> tagArgs;
	
	public AutocompleteTagLookup() {
		this.tags = SimpleStringArg.of(List.of());
		this.tagArgs = new Object2ObjectOpenHashMap<>();
	}
	
	public ACBuilder builder() {
		return new ACBuilder();
	}
	
	public List<String> suggestTag(String input) {
		return prioritySort(input, this.tags.findNonMatch(input));
	}
	
	public List<String> suggestArg(String tagName, int arg, @Nullable String prev, @NotNull String input) {
		TagArgs tag = this.tagArgs.get(tagName);
		if (tag == null) return List.of();
		Suggestor suggestor;
		if (tag.varargs != null) suggestor = tag.varargs;
		else if (arg < tag.args.size()) suggestor = tag.args.get(arg);
		else return List.of();
		return prioritySort(input, suggestor.suggest(prev, input));
	}
	
	private static List<String> prioritySort(String input, List<String> suggestions) {
		if (suggestions.isEmpty()) return suggestions;
		final boolean isMc = suggestions.get(0).startsWith("minecraft:");
		final String lower = input.toLowerCase(Locale.ROOT);
		final String prefix = isMc ? "minecraft:" + lower : lower;
		ArrayList<String> sorted = new ArrayList<>(suggestions.size());
		ArrayList<String> nostart = new ArrayList<>(suggestions.size());
		for (String suggestion : suggestions) {
			if (suggestion.startsWith(prefix)) sorted.add(suggestion);
			else nostart.add(suggestion);
		}
		sorted.addAll(nostart);
		return sorted;
	}
	
	public class ACBuilder {
		
		private final ArrayList<String> tags = new ArrayList<>();
		private final Object2ObjectOpenHashMap<String, TagArgs> tagArgs = new Object2ObjectOpenHashMap<>();
		
		private ACBuilder() {}
		
		public TagBuilder tag(String name) {
			return new TagBuilder(List.of(name));
		}
		
		public TagBuilder tag(String... names) {
			return new TagBuilder(List.of(names));
		}
		
		public TagBuilder tag(Collection<String> names) {
			return new TagBuilder(names);
		}
		
		public class TagBuilder {
			
			private final Collection<String> names;
			
			private TagBuilder(Collection<String> names) {
				this.names = names;
			}
			
			public ACBuilder build() {
				for (String name : names) {
					ACBuilder.this.tagArgs.put(name, new TagArgs(List.of(), null));
					ACBuilder.this.tags.add(name);
				}
				return ACBuilder.this;
			}
			
			public ACBuilder build(Consumer<ArgBuilder> builder) {
				for (String name : names) {
					ArgBuilder argBuilder = new ArgBuilder();
					builder.accept(argBuilder);
					ACBuilder.this.tagArgs.put(name, new TagArgs(argBuilder.args, argBuilder.varargs));
					ACBuilder.this.tags.add(name);
				}
				return ACBuilder.this;
			}
			
		}
		
		public void end() {
			AutocompleteTagLookup.this.tags = SimpleStringArg.of(tags);
			AutocompleteTagLookup.this.tagArgs = tagArgs;
		}
		
	}
	
	private static class TagArgs {
		
		public final List<Suggestor> args;
		public final Suggestor varargs;
		
		private TagArgs(List<Suggestor> args, Suggestor varargs) {
			this.args = args;
			this.varargs = varargs;
		}
		
	}
	
	public static class ArgBuilder {
		
		private final List<Suggestor> args = new ArrayList<>();
		private Suggestor varargs = null;
		
		private ArgBuilder() {}
		
		public ArgBuilder emptyArg() {
			this.args.add(Suggestor.empty());
			return this;
		}
		
		public ArgBuilder arg(AutocompleteArg arg) {
			this.args.add(Suggestor.arg(arg));
			return this;
		}
		
		public ArgBuilder identifier(AutocompleteArg arg) {
			this.args.add(Suggestor.identifier(arg));
			this.args.add(Suggestor.identifier(arg));
			return this;
		}
		
		public ArgBuilder arg(String arg) {
			this.args.add(Suggestor.literal(arg));
			return this;
		}
		
		public ArgBuilder arg(Suggestor arg) {
			this.args.add(arg);
			return this;
		}
		
		public ArgBuilder varArg(Suggestor arg) {
			this.varargs = arg;
			return this;
		}
		
	}
	
	@FunctionalInterface
	public static interface Suggestor {
		public List<String> suggest(@Nullable String prev, @NotNull String input);
		
		public static Suggestor empty() {
			return (prev, input) -> List.of();
		}
		
		public static Suggestor literal(String text) {
			return (prev, input) -> input.isEmpty() || text.startsWith(input) ? List.of(text) : List.of();
		}
		
		public static Suggestor arg(AutocompleteArg arg) {
			return (prev, input) -> arg.findNonMatch(input);
		}
		
		public static Suggestor identifier(AutocompleteArg arg) {
			return (prev, input) ->
			prev == null ? arg.findNonMatch(input) : arg.findNonMatch(prev + ':' + input).stream().map(s -> s.substring(prev.length() + 1)).toList();
		}
	}
	
}
