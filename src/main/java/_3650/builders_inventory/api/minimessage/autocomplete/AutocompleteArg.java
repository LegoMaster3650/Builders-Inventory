package _3650.builders_inventory.api.minimessage.autocomplete;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public interface AutocompleteArg {
	
	public static final AutocompleteArg EMPTY = new AutocompleteArg() {
		@Override
		public List<String> findNonMatch(String start) {
			return List.of();
		}
		@Override
		public List<String> find(String start) {
			return List.of();
		}
	};
	
	public static AutocompleteArg single(String val) {
		return new AutocompleteArg() {
			@Override
			public List<String> findNonMatch(String start) {
				return StringUtils.startsWithIgnoreCase(val, start) && val.length() != start.length() ? List.of() : List.of(val);
			}
			@Override
			public List<String> find(String start) {
				return StringUtils.startsWithIgnoreCase(val, start) ? List.of() : List.of(val);
			}
		};
	}
	
	public List<String> find(String start);
	
	public List<String> findNonMatch(String start);
	
	@FunctionalInterface
	public static interface Filter {
		
		public boolean hasMatch(String key, String start);
		
		public static Filter id() {
			return new IDMatcher();
		}
		
		public static Filter key() {
			return new KeyMatcher();
		}
		
		public static Filter resource() {
			return new ResourceMatcher();
		}
		
	}
	
	static class IDMatcher implements Filter {
		@Override
		public boolean hasMatch(String key, String start) {
			for (int i = 0; !key.startsWith(start, i); ++i) {
				i = key.indexOf('_', i);
				if (i < 0) return false;
			}
			return true;
		}
	}
	
	static class KeyMatcher implements Filter {
		@Override
		public boolean hasMatch(String key, String start) {
			for (int i = 0; !key.startsWith(start, i); ++i) {
				int a = key.indexOf('.', i);
				int b = key.indexOf('_', i);
				i = Math.min(a, b);
				if (i < 0) i = Math.max(a, b);
				if (i < 0) return false;
			}
			return true;
		}
	}
	
	static class ResourceMatcher implements Filter {
		@Override
		public boolean hasMatch(String key, String start) {
			for (int i = 0; !key.startsWith(start, i); ++i) {
				int a = key.indexOf('/', i);
				int b = key.indexOf('_', i);
				i = Math.min(a, b);
				if (i < 0) i = Math.max(a, b);
				if (i < 0) return false;
			}
			return true;
		}
	}
	
}
