package _3650.builders_inventory.api.minimessage.autocomplete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SimpleStringArg implements AutocompleteArg {
	
	private final AutocompleteArg.Filter filter;
	private final ArrayList<String> vals;
	
	private SimpleStringArg(AutocompleteArg.Filter filter, ArrayList<String> vals) {
		this.filter = filter;
		this.vals = vals;
	}
	
	public static SimpleStringArg of(List<String> vals) {
		return SimpleStringArg.withFilter(vals, AutocompleteArg.Filter.id());
	}
	
	public static SimpleStringArg withFilter(List<String> vals, AutocompleteArg.Filter filter) {
		ArrayList<String> sorted = new ArrayList<>(vals);
		Collections.sort(sorted);
		return new SimpleStringArg(filter, sorted);
	}
	
	@Override
	public List<String> find(String start) {
		if (start.isEmpty()) return vals;
		final String startLow = start.toLowerCase(Locale.ROOT);
		final ArrayList<String> result = new ArrayList<>();
		for (String val : vals) {
			if (filter.hasMatch(val, startLow)) result.add(val);
		}
		return result;
	}
	
	@Override
	public List<String> findNonMatch(String start) {
		if (start.isEmpty()) return vals;
		final String startLow = start.toLowerCase(Locale.ROOT);
		final List<String> result = new ArrayList<>();
		for (String val : vals) {
			if (filter.hasMatch(val, startLow) && !(val.length() == start.length() && val.equals(start))) result.add(val);
		}
		return result;
	}
	
}
