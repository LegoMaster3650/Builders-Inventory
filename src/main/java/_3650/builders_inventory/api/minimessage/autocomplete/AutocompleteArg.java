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
	
}
