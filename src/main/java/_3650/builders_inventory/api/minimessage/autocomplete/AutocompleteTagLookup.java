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
	
	private SimpleStringArg nodes;
	private Object2ObjectOpenHashMap<String, Entry> entries;
	
	public AutocompleteTagLookup() {
		this.nodes = SimpleStringArg.of(List.of());
		this.entries = new Object2ObjectOpenHashMap<>();
	}
	
	public ACBuilder builder() {
		return new ACBuilder();
	}
	
	public List<String> suggestTag(String input) {
		return sort(input, this.nodes.findNonMatch(input));
	}
	
	public List<String> suggestArg(String tagName, int arg, @Nullable String prev, @NotNull String input) {
		Entry entry = this.entries.get(tagName);
		if (entry == null) return List.of();
		Suggestor suggest;
		if (entry.varargs != null) suggest = entry.varargs;
		else if (arg < entry.args.size()) suggest = entry.args.get(arg);
		else return List.of();
		return sort(input, suggest.suggest(prev, input));
	}
	
	private static List<String> sort(String input, List<String> strs) {
		if (strs.isEmpty()) return strs;
		final boolean isMc = strs.get(0).startsWith("minecraft:");
		final String lower = input.toLowerCase(Locale.ROOT);
		final String str = isMc ? "minecraft:" + lower : lower;
		ArrayList<String> sorted = new ArrayList<>(strs.size());
		ArrayList<String> nostart = new ArrayList<>(strs.size());
		for (String s : strs) {
			if (s.startsWith(str)) sorted.add(s);
			else nostart.add(s);
		}
		sorted.addAll(nostart);
		return sorted;
	}
	
	public class ACBuilder {
		
		private final ArrayList<String> vals = new ArrayList<>();
		private final Object2ObjectOpenHashMap<String, Entry> entries = new Object2ObjectOpenHashMap<>();
		
		private ACBuilder() {}
		
		public EBStage entry(String name) {
			return new EBStage(List.of(name));
		}
		
		public EBStage entry(String... names) {
			return new EBStage(List.of(names));
		}
		
		public EBStage entry(Collection<String> names) {
			return new EBStage(names);
		}
		
		public class EBStage {
			
			private final Collection<String> names;
			
			private EBStage(Collection<String> names) {
				this.names = names;
			}
			
			public ACBuilder build() {
				for (String name : names) {
					ACBuilder.this.entries.put(name, new Entry(List.of(), null));
					ACBuilder.this.vals.add(name);
				}
				return ACBuilder.this;
			}
			
			public ACBuilder build(Consumer<EntryBuilder> builder) {
				for (String name : names) {
					EntryBuilder b = new EntryBuilder();
					builder.accept(b);
					ACBuilder.this.entries.put(name, new Entry(b.args, b.varargs));
					ACBuilder.this.vals.add(name);
				}
				return ACBuilder.this;
			}
			
		}
		
		public void end() {
			AutocompleteTagLookup.this.nodes = SimpleStringArg.of(vals);
			AutocompleteTagLookup.this.entries = entries;
		}
		
	}
	
	private static class Entry {
		
		public final List<Suggestor> args;
		public final Suggestor varargs;
		
		private Entry(List<Suggestor> args, Suggestor varargs) {
			this.args = args;
			this.varargs = varargs;
		}
		
	}
	
	public static class EntryBuilder {
		
		private final List<Suggestor> args = new ArrayList<>();
		private Suggestor varargs = null;
		
		private EntryBuilder() {}
		
		public EntryBuilder emptyArg() {
			this.args.add(Suggestor.empty());
			return this;
		}
		
		public EntryBuilder arg(AutocompleteArg arg) {
			this.args.add(Suggestor.arg(arg));
			return this;
		}
		
		public EntryBuilder resource(AutocompleteArg arg) {
			this.args.add(Suggestor.resource(arg));
			this.args.add(Suggestor.resource(arg));
			return this;
		}
		
		public EntryBuilder arg(String arg) {
			this.args.add(Suggestor.literal(arg));
			return this;
		}
		
		public EntryBuilder arg(Suggestor arg) {
			this.args.add(arg);
			return this;
		}
		
		public EntryBuilder varArg(Suggestor arg) {
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
		
		public static Suggestor resource(AutocompleteArg arg) {
			return (prev, input) ->
			prev == null ? arg.findNonMatch(input) : arg.findNonMatch(prev + ':' + input).stream().map(s -> s.substring(prev.length() + 1)).toList();
		}
	}
	
}
