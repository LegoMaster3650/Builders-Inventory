package _3650.builders_inventory.api.minimessage.autocomplete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SimpleStringArg implements AutocompleteArg {
	
	private final ArrayList<String> vals;
	
	private SimpleStringArg(ArrayList<String> vals) {
		this.vals = vals;
	}
	
	public static SimpleStringArg of(List<String> vals) {
		ArrayList<String> sorted = new ArrayList<>(vals);
		Collections.sort(sorted);
		return new SimpleStringArg(sorted);
	}
	
	@Override
	public List<String> find(String start) {
		if (start.isEmpty()) return vals;
		final String startLow = start.toLowerCase(Locale.ROOT);
		final ArrayList<String> result = new ArrayList<>();
		for (String val : vals) {
			if (segmentMatches(val, startLow)) result.add(val);
		}
		return result;
	}
	
	@Override
	public List<String> findNonMatch(String start) {
		if (start.isEmpty()) return vals;
		final String startLow = start.toLowerCase(Locale.ROOT);
		final List<String> result = new ArrayList<>();
		for (String val : vals) {
			if (segmentMatches(val, startLow) && !(val.length() == start.length() && val.equals(start))) result.add(val);
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
	
}
