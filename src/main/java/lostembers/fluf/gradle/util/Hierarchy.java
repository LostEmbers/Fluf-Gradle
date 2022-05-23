package lostembers.fluf.gradle.util;

import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.HashMap;

public class Hierarchy {
	HashMap<String, ArrayList<String>> inheritance = new HashMap<>();
	
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
}
