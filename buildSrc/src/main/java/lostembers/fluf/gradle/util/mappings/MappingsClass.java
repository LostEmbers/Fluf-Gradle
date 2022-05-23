package lostembers.fluf.gradle.util.mappings;

import java.util.HashMap;
import java.util.Set;

public class MappingsClass {
	String obf;
	String mapped;
	HashMap<String, MappingsMethod> methodsByObfName = new HashMap<>();
	HashMap<String, MappingsMethod> methodsByMappedName = new HashMap<>();
	HashMap<String, MappingsField> fieldsByObfName = new HashMap<>();
	HashMap<String, MappingsField> fieldsByMappedName = new HashMap<>();
	
	public MappingsClass(String obf, String mapped) {
		this.obf = obf;
		this.mapped = mapped;
	}
	
	public Set<String> obfFieldNames() {
		return fieldsByObfName.keySet();
	}
	
	public String toObf() {
		return obf;
	}
	
	public String toMapped() {
		return mapped;
	}
	
	public void addMethod(MappingsMethod method) {
		methodsByObfName.put(method.toObf(), method);
		methodsByMappedName.put(method.toMapped(), method);
	}
	
	public void addField(MappingsField field) {
		fieldsByObfName.put(field.obf, field);
		fieldsByMappedName.put(field.mapped, field);
	}
	
	public MappingsMethod getMethod(String s) {
		if (methodsByObfName.containsKey(s)) return methodsByObfName.get(s);
		if (methodsByMappedName.containsKey(s)) return methodsByMappedName.get(s);
		return null;
	}
	
	public MappingsField getField(String s) {
		if (fieldsByObfName.containsKey(s)) return fieldsByObfName.get(s);
		if (fieldsByMappedName.containsKey(s)) return fieldsByMappedName.get(s);
		return null;
	}
	
	public Set<String> obfMethodNames() {
		return methodsByObfName.keySet();
	}
}
