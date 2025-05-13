package _3650.builders_inventory.util;

import java.util.ArrayList;

public class ArrayStack<T> {
	
	final ArrayList<T> list;
	int pointer = -1;
	
	public ArrayStack() {
		this.list = new ArrayList<>();
	}
	
	public ArrayStack(int initialCapacity) {
		this.list = new ArrayList<>(initialCapacity);
	}
	
	public void push(T t) {
		list.add(t);
		++pointer;
	}
	
	public T peek() {
		if (pointer < 0) return null;
		return list.get(pointer);
	}
	
	public T peek(int amount) {
		int i = pointer + 1 - amount;
		if (i < 0) return null;
		return list.get(i);
	}
	
	public T pop() {
		if (pointer < 0) return null;
		return list.remove(pointer--);
	}
	
	public int size() {
		return list.size();
	}
	
	public boolean isEmpty() {
		return list.isEmpty();
	}
	
	public void clear() {
		list.clear();
		pointer = -1;
	}
	
}
