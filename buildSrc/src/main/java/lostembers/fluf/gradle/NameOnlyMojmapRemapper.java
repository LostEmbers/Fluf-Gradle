package lostembers.fluf.gradle;

import lostembers.fluf.gradle.util.mappings.FlufMappings;
import lostembers.fluf.gradle.util.mappings.MappingsClass;
import net.fabricmc.mappings.Mappings;
import org.objectweb.asm.commons.Remapper;

import java.util.HashMap;
import java.util.Map;

public class NameOnlyMojmapRemapper extends Remapper {
	Map<String, String> classMap = new HashMap<>();
	
	public NameOnlyMojmapRemapper(FlufMappings mappings, boolean reverse) {
		for (String obfName : mappings.obfNames()) {
			MappingsClass mappingsClass = mappings.get(obfName);
			String left = reverse ? mappingsClass.toObf() : mappingsClass.toMapped();
			String right = !reverse ? mappingsClass.toObf() : mappingsClass.toMapped();
			classMap.put(left, right);
		}
	}
	
	@Override
	public String map(String internalName) {
		if (classMap.containsKey(internalName)) return classMap.get(internalName);
		return super.map(internalName);
	}
}
