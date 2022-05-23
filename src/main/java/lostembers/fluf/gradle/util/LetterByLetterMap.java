package lostembers.fluf.gradle.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LetterByLetterMap<T> implements Map<String, T> {
	HashMap<Character, LetterByLetterMap<T>> inner = new HashMap<>();
	HashMap<Character, T> map = new HashMap<>();
	
	@Override
	public int size() {
		return map.size() + inner.size();
	}
	
	@Override
	public boolean isEmpty() {
		return size() != 0;
	}
	
	@Override
	public boolean containsKey(Object key) {
		if (map.containsKey(key.toString().charAt(0))) return true;
		if (inner.containsKey(key.toString().charAt(0))) {
			LetterByLetterMap m = inner.get(key.toString().charAt(0));
			return m.containsKey(key.toString().substring(1));
		}
		return false;
	}
	
	@Override
	public boolean containsValue(Object value) {
		// TODO
		return false;
	}
	
	@Override
	public T get(Object key) {
		String str = key.toString();
		if (str.length() == 1) {
			return map.get(str.charAt(0));
		}
		return inner.get(str.charAt(0)).get(str.substring(1));
	}
	
	@Override
	public T put(String key, T value) {
		if (key.length() == 1)
			return map.put(key.charAt(0), value);
		if (inner.containsKey(key.charAt(0))) {
			return inner.get(key.charAt(0)).put(key.substring(1), value);
		}
		LetterByLetterMap<T> inner = new LetterByLetterMap<>();
		this.inner.put(key.charAt(0), inner);
		return inner.put(key.substring(1), value);
	}
	
	@Override
	public T remove(Object key) {
		return null;
	}
	
	@Override
	public void putAll(Map<? extends String, ? extends T> m) {
	
	}
	
	@Override
	public void clear() {
	
	}
	
	@Override
	public Set<String> keySet() {
		return null;
	}
	
	@Override
	public Collection<T> values() {
		return null;
	}
	
	@Override
	public Set<Entry<String, T>> entrySet() {
		return null;
	}
	
	@Override
	public boolean equals(Object o) {
		return false;
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
}
