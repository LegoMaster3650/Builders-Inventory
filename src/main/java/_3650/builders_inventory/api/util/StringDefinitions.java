package _3650.builders_inventory.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class StringDefinitions {
	
	private final ArrayList<String> list;
	
	private StringDefinitions(ArrayList<String> list) {
		this.list = list;
	}
	
	public static StringDefinitions list(ArrayList<String> list) {
		return new StringDefinitions(list);
	}
	
	public static StringDefinitions list() {
		return new StringDefinitions(new ArrayList<>());
	}
	
	public StringDefinitions add(String s) {
		list.add(s);
		return this;
	}
	
	public StringDefinitions add(String... strs) {
		for (String s : strs) list.add(s);
		return this;
	}
	
	public StringDefinitions add(List<String> strs) {
		for (String s : strs) list.add(s);
		return this;
	}
	
	public StringDefinitions add(Consumer<Consumer<String>> appender) {
		appender.accept(list::add);
		return this;
	}
	
	public StringDefinitions variant(Function<String, String> func) {
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
