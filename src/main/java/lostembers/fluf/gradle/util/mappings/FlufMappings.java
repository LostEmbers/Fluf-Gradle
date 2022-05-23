package lostembers.fluf.gradle.util.mappings;

import java.util.HashMap;
import java.util.Set;

public class FlufMappings {
	HashMap<String, MappingsClass> classesByObf = new HashMap<>();
	HashMap<String, MappingsClass> classesByMapped = new HashMap<>();
	
	public Set<String> obfNames() {
		return classesByObf.keySet();
	}
	
	public void addClass(MappingsClass mappingsClass) {
		classesByObf.put(mappingsClass.toObf(), mappingsClass);
		classesByMapped.put(mappingsClass.toMapped(), mappingsClass);
	}
	
	public MappingsClass get(String secondaryName) {
		if (classesByMapped.containsKey(secondaryName)) return classesByMapped.get(secondaryName);
		if (classesByObf.containsKey(secondaryName)) return classesByObf.get(secondaryName);
		return null;
	}
}
