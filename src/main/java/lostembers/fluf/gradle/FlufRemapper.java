package lostembers.fluf.gradle;

import lostembers.fluf.gradle.util.Hierarchy;
import lostembers.fluf.gradle.util.mappings.FlufMappings;
import lostembers.fluf.gradle.util.mappings.MappingsClass;
import lostembers.fluf.gradle.util.mappings.MappingsField;
import lostembers.fluf.gradle.util.mappings.MappingsMethod;
import net.fabricmc.mappings.*;
import org.objectweb.asm.commons.Remapper;

import java.util.HashMap;
import java.util.Map;

public class FlufRemapper extends Remapper {
	public Hierarchy hierarchy;
	public Remapper parent;
	Mappings mappings;
	
	Map<String, String> classMap = new HashMap<>();
	Map<String, Map<String, String>> fieldMap = new HashMap<>();
	Map<String, Map<String, String>> methodMap = new HashMap<>();
	
	public FlufRemapper(FlufMappings mappings, boolean reverse) {
		this(mappings, reverse, false);
	}
	
	public FlufRemapper(FlufMappings mappings, boolean reverse, boolean areMappingsDumb) {
		this(mappings, reverse, areMappingsDumb, false);
	}
	
	boolean ignoreCNames = false;
	
	public FlufRemapper(FlufMappings mappings, boolean reverse, boolean areMappingsDumb, boolean ignoreClassNames) {
		ignoreCNames = ignoreClassNames;
		for (String obfName : mappings.obfNames()) {
			MappingsClass mappingsClass = mappings.get(obfName);
			String classLeft;
			{
				String left = classLeft = reverse ? mappingsClass.toObf() : mappingsClass.toMapped();
				String right = !reverse ? mappingsClass.toObf() : mappingsClass.toMapped();
				classMap.put(left, right);
			}
			for (String fieldName : mappingsClass.obfFieldNames()) {
				MappingsField field = mappingsClass.getField(fieldName);
				String left = reverse ? field.toObf() : field.toMapped();
				String right = !reverse ? field.toObf() : field.toMapped();
				String[] split;
				split = left.split("\\|");
				left = split[split.length - 1];
				split = right.split("\\|");
				right = split[split.length - 1];
				if (!fieldMap.containsKey(classLeft)) fieldMap.put(classLeft, new HashMap<>());
				Map<String, String> map = fieldMap.get(classLeft);
				map.put(left, right);
			}
		}
		for (String obfName : mappings.obfNames()) {
			MappingsClass mappingsClass = mappings.get(obfName);
			String classLeft = reverse ? mappingsClass.toObf() : mappingsClass.toMapped();
			for (String methodName : mappingsClass.obfMethodNames()) {
				MappingsMethod method = mappingsClass.getMethod(methodName);
				String left = reverse ? method.toObf() : method.toMapped();
				String right = !reverse ? method.toObf() : method.toMapped();
				if (!reverse ^ areMappingsDumb) left = remapDesc(left);
				else right = remapDesc(right);
				if (!methodMap.containsKey(classLeft)) methodMap.put(classLeft, new HashMap<>());
				Map<String, String> map = methodMap.get(classLeft);
				map.put(left, right);
			}
		}
	}
	
	boolean reverse = false;
	
	String[] obfNames;
	String[] mappedNames;
	
	protected String remapDesc(String method) {
		if (mappedNames == null) {
			obfNames = classMap.values().toArray(new String[0]);
			mappedNames = classMap.keySet().toArray(new String[0]);
		}
		reverse = true;
		String[] strings = method.split("\\(");
		strings[1] = mapMethodDesc("(" + strings[1]);
		reverse = false;
		return strings[0] + strings[1];
	}
	
	public FlufRemapper(Mappings mappings, boolean reverse) {
		this.mappings = mappings;
		for (ClassEntry classEntry : mappings.getClassEntries()) {
			String left = classEntry.get(reverse ? "official" : "intermediary");
			String right = classEntry.get(!reverse ? "official" : "intermediary");
			classMap.put(left, right);
		}
		for (FieldEntry fieldEntry : mappings.getFieldEntries()) {
			EntryTriple left = fieldEntry.get(reverse ? "official" : "intermediary");
			EntryTriple right = fieldEntry.get(!reverse ? "official" : "intermediary");
			if (!fieldMap.containsKey(left.getOwner())) fieldMap.put(left.getOwner(), new HashMap<>());
			Map<String, String> map = fieldMap.get(left.getOwner());
			map.put(left.getName(), right.getName());
		}
		for (MethodEntry methodEntry : mappings.getMethodEntries()) {
			EntryTriple left = methodEntry.get(reverse ? "official" : "intermediary");
			EntryTriple right = methodEntry.get(!reverse ? "official" : "intermediary");
			if (!methodMap.containsKey(left.getOwner())) methodMap.put(left.getOwner(), new HashMap<>());
			Map<String, String> map = methodMap.get(left.getOwner());
			map.put(left.getName() + left.getDesc(), right.getName() + right.getDesc());
		}
	}
	
	@Override
	public String mapType(String internalName) {
		return super.mapType(internalName);
	}
	
	@Override
	public String mapFieldName(String owner, String name, String descriptor) {
		if (owner.endsWith(";"))
			owner = owner.substring(1, owner.length() - 1);
		if (fieldMap.containsKey(owner)) {
			Map<String, String> map = fieldMap.get(owner);
			if (map.containsKey(name)) {
				String str = map.get(name);
//				if (str.startsWith("$SwitchMap$") && !descriptor.equals("[I"))
//					System.err.println("Field " + owner + "#" + name + " happened to have the same name as " + str);
//				else
					return str;
			}
		}
		if (hierarchy != null) {
			// TODO: check
			if (hierarchy.contains(owner)) {
				for (String s : hierarchy.get(owner)) {
					String str = mapFieldName(s, name, descriptor);
					if (!str.equals(name)) return str;
					if (parent instanceof RemapperStack) {
						for (Remapper mapper : ((RemapperStack) parent).mappers()) {
							s = mapper.map(s);
							str = mapFieldName(s, name, descriptor);
//							if (str.startsWith("$SwitchMap$") && !descriptor.equals("[I"))
//								System.err.println("Field " + owner + "#" + name + " happened to have the same name as " + str);
//							else
								if (!str.equals(name)) return str;
						}
					}
				}
			}
		}
		return super.mapFieldName(owner, name, descriptor);
	}
	
	@Override
	public String map(String internalName) {
		if (reverse) {
			for (int i = 0; i < obfNames.length; i++) {
				if (obfNames[i].equals(internalName)) {
					return mappedNames[i];
				}
			}
		}
		if (ignoreCNames) return internalName;
		if (classMap.containsKey(internalName)) return classMap.get(internalName);
		return super.map(internalName);
	}
	
	@Override
	// TODO: optimize?
	public String mapMethodName(String owner, String name, String descriptor) {
		if (methodMap.containsKey(owner)) {
			Map<String, String> map = methodMap.get(owner);
			if (map.containsKey(name + descriptor)) return map.get(name + descriptor).split("\\(")[0];
		}
		if (hierarchy != null) {
			// TODO: check
			if (hierarchy.contains(owner)) {
				for (String s : hierarchy.get(owner)) {
					String str = mapMethodName(s, name, descriptor);
					if (!str.equals(name)) return str;
					if (parent instanceof RemapperStack) {
						for (Remapper mapper : ((RemapperStack) parent).mappers()) {
							s = mapper.map(s);
							str = mapMethodName(s, name, descriptor);
							if (!str.equals(name)) return str;
						}
					}
				}
			}
		}
		return super.mapMethodName(owner, name, descriptor);
	}
}
