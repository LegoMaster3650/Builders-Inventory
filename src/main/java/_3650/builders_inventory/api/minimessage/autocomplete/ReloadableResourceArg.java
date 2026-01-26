package _3650.builders_inventory.api.minimessage.autocomplete;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.resources.Identifier;

public abstract class ReloadableResourceArg implements AutocompleteArg {
	
	public static final ReloadableResourceArg KEYS = key();
	public static final ReloadableResourceArg LANG = key();
	public static final ReloadableResourceArg FONTS = resource();
	public static final ReloadableResourceArg ITEMS = plain();
	public static final ReloadableResourceArg ENTITIES = plain();
	public static final ReloadableResourceArg ATLASES = key();
	
	protected final AutocompleteArg.Filter filter;
	
	protected boolean loaded = false;
	
	private ReloadableResourceArg(AutocompleteArg.Filter filter) {
		this.filter = filter;
	}
	
	public static ReloadableResourceArg plain() {
		return new PlainArg(AutocompleteArg.Filter.id());
	}
	
	public static ReloadableResourceArg plain(AutocompleteArg.Filter filter) {
		return new PlainArg(filter);
	}
	
	public static ReloadableResourceArg key() {
		return new KeyArg(AutocompleteArg.Filter.key());
	}
	
	public static ReloadableResourceArg key(AutocompleteArg.Filter filter) {
		return new KeyArg(filter);
	}
	
	public static ReloadableResourceArg resource() {
		return new ResourceArg(AutocompleteArg.Filter.resource());
	}
	
	public static ReloadableResourceArg resource(AutocompleteArg.Filter filter) {
		return new ResourceArg(filter);
	}
	
	public boolean isLoaded() {
		return this.loaded;
	}
	
	public abstract void loadStr(ArrayList<String> strs);
	
	public abstract void loadId(ArrayList<Identifier> locs);
	
	private static class PlainArg extends ReloadableResourceArg {
		
		private PlainArg(AutocompleteArg.Filter filter) {
			super(filter);
		}
		
		private ArrayList<String> vals;
		
		@Override
		public List<String> find(String start) {
			if (!loaded) return List.of();
			if (start.isEmpty()) return vals;
			final String startLow = start.toLowerCase(Locale.ROOT);
			final ArrayList<String> result = new ArrayList<>();
			for (int i = 0; i < vals.size(); i++) {
				if (filter.hasMatch(vals.get(i), startLow)) result.add(vals.get(i));
			}
			return result;
		}
		
		@Override
		public List<String> findNonMatch(String start) {
			if (!loaded) return List.of();
			if (start.isEmpty()) return vals;
			final String startLow = start.toLowerCase(Locale.ROOT);
			final List<String> result = new ArrayList<>();
			for (int i = 0; i < vals.size(); i++) {
				final String key = vals.get(i);
				if (filter.hasMatch(key, startLow) && !(key.length() == start.length() && key.equals(start))) result.add(vals.get(i));
			}
			return result;
		}
		
		@Override
		public void loadStr(ArrayList<String> strs) {
			loaded = true;
			vals = strs;
		}
		
		@Override
		public void loadId(ArrayList<Identifier> ids) {
			throw new IllegalStateException("Cannot load Identifier values into String arg");
		}
		
	}
	
	private static class KeyArg extends ReloadableResourceArg {
		
		private KeyArg(AutocompleteArg.Filter filter) {
			super(filter);
		}
		
		private ArrayList<String> keys;
		private ArrayList<String> vals;
		
		@Override
		public List<String> find(String start) {
			if (!loaded) return List.of();
			if (start.isEmpty()) return vals;
			final String startLow = start.toLowerCase(Locale.ROOT);
			final ArrayList<String> result = new ArrayList<>();
			for (int i = 0; i < keys.size(); i++) {
				if (filter.hasMatch(keys.get(i), startLow)) result.add(vals.get(i));
			}
			return result;
		}
		
		@Override
		public List<String> findNonMatch(String start) {
			if (!loaded) return List.of();
			if (start.isEmpty()) return vals;
			final String startLow = start.toLowerCase(Locale.ROOT);
			final List<String> result = new ArrayList<>();
			for (int i = 0; i < keys.size(); i++) {
				final String key = keys.get(i);
				if (filter.hasMatch(key, startLow) && !(key.length() == start.length() && key.equals(start))) result.add(vals.get(i));
			}
			return result;
		}
		
		@Override
		public void loadStr(ArrayList<String> strs) {
			loaded = true;
			keys = new ArrayList<>(strs.size());
			vals = new ArrayList<>(strs.size());
			for (String str : strs) {
				keys.add(str.toLowerCase(Locale.ROOT));
				vals.add(str);
			}
		}
		
		@Override
		public void loadId(ArrayList<Identifier> ids) {
			throw new IllegalStateException("Cannot load Identifier values into String arg");
		}
		
	}
	
	private static class ResourceArg extends ReloadableResourceArg {
		
		private ResourceArg(AutocompleteArg.Filter filter) {
			super(filter);
		}
		
		private ArrayList<Identifier> keys;
		private ArrayList<String> vals;
		
		@Override
		public List<String> find(String start) {
			if (!loaded) return List.of();
			if (start.isEmpty()) return vals;
			final String startLow = start.toLowerCase(Locale.ROOT);
			final ArrayList<String> result = new ArrayList<>();
			final boolean namespaced = start.indexOf(':') > -1;
			for (int i = 0; i < keys.size(); i++) {
				final Identifier id = keys.get(i);
				if (namespaced) {
					if (filter.hasMatch(id.toString(), startLow)) result.add(vals.get(i));
				} else if (filter.hasMatch(id.getNamespace(), startLow)
						|| id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && filter.hasMatch(id.getPath(), startLow)) {
					result.add(vals.get(i));
				}
			}
			return result;
		}
		
		@Override
		public List<String> findNonMatch(String start) {
			if (!loaded) return List.of();
			if (start.isEmpty()) return vals;
			final String startLow = start.toLowerCase(Locale.ROOT);
			final ArrayList<String> result = new ArrayList<>();
			final boolean namespaced = start.indexOf(':') > -1;
			for (int i = 0; i < keys.size(); i++) {
				final Identifier id = keys.get(i);
				if (namespaced) {
					final String key = id.toString();
					if (filter.hasMatch(key, startLow) && !(key.length() == start.length() && key.equals(start))) result.add(vals.get(i));
				} else if (filter.hasMatch(id.getNamespace(), startLow)) {
					result.add(vals.get(i));
				} else if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && filter.hasMatch(id.getPath(), startLow)) {
					if (!(id.getPath().length() == start.length() && id.getPath().equals(start))) result.add(vals.get(i));
				}
			}
			return result;
		}
		
		@Override
		public void loadStr(ArrayList<String> strs) {
			throw new IllegalStateException("Cannot load String values into Identifier arg");
		}
		
		@Override
		public void loadId(ArrayList<Identifier> ids) {
			loaded = true;
			keys = ids;
			vals = new ArrayList<>(keys.size());
			for (Identifier id : ids) {
				vals.add(id.toString());
			}
		}
		
	}
	
}
