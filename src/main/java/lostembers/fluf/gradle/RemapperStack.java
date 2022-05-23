package lostembers.fluf.gradle;

import org.objectweb.asm.commons.Remapper;

import java.util.ArrayList;

public class RemapperStack extends Remapper {
	ArrayList<Remapper> stack = new ArrayList<>();
	
	public void add(Remapper remapper) {
		stack.add(remapper);
	}
	
	@Override
	public String mapMethodName(String owner, String name, String descriptor) {
		for (Remapper remapper : stack) {
			name = remapper.mapMethodName(owner, name, descriptor);
			owner = remapper.map(owner);
			descriptor = remapper.mapMethodDesc(descriptor);
		}
		return name;
	}
	
	@Override
	public String mapFieldName(String owner, String name, String descriptor) {
		for (Remapper remapper : stack) {
			name = remapper.mapFieldName(owner, name, descriptor);
			owner = remapper.map(owner);
			descriptor = remapper.mapDesc(descriptor);
		}
		return name;
	}
	
	@Override
	public String map(String internalName) {
		for (Remapper remapper : stack)
			internalName = remapper.map(internalName);
		return internalName;
	}
}
