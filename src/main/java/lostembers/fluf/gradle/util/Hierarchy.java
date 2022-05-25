package lostembers.fluf.gradle.util;

import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.HashMap;

public class Hierarchy {
	HashMap<String, ArrayList<String>> inheritance = new HashMap<>();
	public Hierarchy inner;
	
	public void accept(ClassNode remapped) {
		ArrayList<String> list;
		inheritance.put(remapped.name, list = new ArrayList<>());
		list.add(remapped.superName);
		for (String anInterface : remapped.interfaces)
			list.add(anInterface);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (String s : inheritance.keySet()) {
			builder.append(s).append("\n");
			for (String s1 : inheritance.get(s))
				builder.append("\t").append(s1).append("\n");
		}
		return builder.toString();
	}
	
	public void read(String toString) {
		String name = null;
		ArrayList<String> strings = new ArrayList<>();
		for (String s : toString.split("\n")) {
			if (!s.startsWith("\t")) {
				if (name != null) inheritance.put(name, strings);
				name = s;
				strings = new ArrayList<>();
			} else {
				strings.add(s.trim());
			}
		}
		if (name != null) inheritance.put(name, strings);
	}
	
	public Hierarchy remap(Remapper remapper) {
		Hierarchy out = new Hierarchy();
		ArrayList<String> entry = new ArrayList<>();
		for (String s : inheritance.keySet()) {
			for (String s1 : inheritance.get(s)) entry.add(remapper.map(s1));
			out.add(remapper.map(s), entry);
			entry = new ArrayList<>();
		}
		if (inner != null)
			out.inner = inner.remap(remapper);
		return out;
	}
	
	public void add(String s, ArrayList<String> entry) {
		inheritance.put(s, entry);
	}
	
	public boolean contains(String owner) {
		return inheritance.containsKey(owner) || (inner != null && inner.contains(owner));
	}
	
	public ArrayList<String> get(String owner) {
		if (inheritance.containsKey(owner)) return inheritance.get(owner);
		return inner.get(owner);
	}
}
