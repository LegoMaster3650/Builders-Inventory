package _3650.builders_inventory.api.minimessage.autocomplete;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.resources.Identifier;

public abstract class ReloadableResourceArg implements AutocompleteArg {
	
	public static final ReloadableResourceArg KEYS = str();
	public static final ReloadableResourceArg LANG = str();
	public static final ReloadableResourceArg FONTS = loc();
	public static final ReloadableResourceArg ITEMS = plain();
	public static final ReloadableResourceArg ENTITIES = plain();
	public static final ReloadableResourceArg ATLASES = str();
	
	private ReloadableResourceArg() {}
	
	public static ReloadableResourceArg plain() {
		return new PlainArg();
	}
	
	public static ReloadableResourceArg str() {
		return new StringArg();
	}
	
	public static ReloadableResourceArg loc() {
		return new ResourceArg();
	}
	
	public abstract boolean isLoaded();
	
	public abstract void loadStr(ArrayList<String> strs);
	
	public abstract void loadId(ArrayList<Identifier> locs);
	
	private static class PlainArg extends ReloadableResourceArg {
		
		private ArrayList<String> vals;
		
		private boolean loaded = false;
		
		@Override
		public boolean isLoaded() {
			return loaded;
		}
		
		@Override
		public List<String> find(String start) {
			if (!loaded) return List.of();
			if (start.isEmpty()) return vals;
			final String startLow = start.toLowerCase(Locale.ROOT);
			final ArrayList<String> result = new ArrayList<>();
			for (int i = 0; i < vals.size(); i++) {
				if (segmentMatches(vals.get(i), startLow)) result.add(vals.get(i));
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
				if (segmentMatches(key, startLow) && !(key.length() == start.length() && key.equals(start))) result.add(vals.get(i));
			}
			return result;
		}
		
		private static boolean segmentMatches(String key, String start) {
			for (int i = 0; !key.startsWith(start, i); ++i) {
				i = key.indexOf('_', i);
				if (i < 0) return false;
			}
			return true;
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
	
	private static class StringArg extends ReloadableResourceArg {
		
		private ArrayList<String> keys;
		private ArrayList<String> vals;
		
		private boolean loaded = false;
		
		@Override
		public boolean isLoaded() {
			return loaded;
		}
		
		@Override
		public List<String> find(String start) {
			if (!loaded) return List.of();
			if (start.isEmpty()) return vals;
			final String startLow = start.toLowerCase(Locale.ROOT);
			final ArrayList<String> result = new ArrayList<>();
			for (int i = 0; i < keys.size(); i++) {
				if (segmentMatches(keys.get(i), startLow)) result.add(vals.get(i));
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
				if (segmentMatches(key, startLow) && !(key.length() == start.length() && key.equals(start))) result.add(vals.get(i));
			}
			return result;
		}
		
		private static boolean segmentMatches(String key, String start) {
			for (int i = 0; !key.startsWith(start, i); ++i) {
				int a = key.indexOf('.', i);
				int b = key.indexOf('_', i);
				i = Math.min(a, b);
				if (i < 0) i = Math.max(a, b);
				if (i < 0) return false;
			}
			return true;
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
		
		private ArrayList<Identifier> keys;
		private ArrayList<String> vals;
		
		private boolean loaded = false;
		
		@Override
		public boolean isLoaded() {
			return loaded;
		}
		
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
					if (segmentMatches(id.toString(), startLow)) result.add(vals.get(i));
				} else if (segmentMatches(id.getNamespace(), startLow)
						|| id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && segmentMatches(id.getPath(), startLow)) {
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
					if (segmentMatches(key, startLow) && !(key.length() == start.length() && key.equals(start))) result.add(vals.get(i));
				} else if (segmentMatches(id.getNamespace(), startLow)) {
					result.add(vals.get(i));
				} else if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && segmentMatches(id.getPath(), startLow)) {
					if (!(id.getPath().length() == start.length() && id.getPath().equals(start))) result.add(vals.get(i));
				}
			}
			return result;
		}
		
		private static boolean segmentMatches(String key, String start) {
			for (int i = 0; !key.startsWith(start, i); ++i) {
				int a = key.indexOf('/', i);
				int b = key.indexOf('_', i);
				i = Math.min(a, b);
				if (i < 0) i = Math.max(a, b);
				if (i < 0) return false;
			}
			return true;
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
