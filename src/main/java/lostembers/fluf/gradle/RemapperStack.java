package lostembers.fluf.gradle;

import lostembers.fluf.gradle.util.Hierarchy;
import org.objectweb.asm.commons.Remapper;

import java.util.ArrayList;

public class RemapperStack extends Remapper {
	Hierarchy hierarchy;
	
	ArrayList<Remapper> stack = new ArrayList<>();
	
	public void add(Remapper remapper) {
		stack.add(remapper);
		if (remapper instanceof FlufRemapper) {
			((FlufRemapper) remapper).parent = this;
			((FlufRemapper) remapper).hierarchy = hierarchy;
			if (hierarchy != null) hierarchy = hierarchy.remap(remapper);
		}
	}
	
	public Remapper[] mappers() {
		return stack.toArray(new Remapper[0]);
	}
	
	public void setHierarchy(Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
		for (Remapper remapper : stack) {
			if (remapper instanceof RemapperStack) {
				((RemapperStack) remapper).setHierarchy(hierarchy);
			} else if (remapper instanceof FlufRemapper) {
				((FlufRemapper) remapper).hierarchy = hierarchy;
			}
			hierarchy = hierarchy.remap(remapper);
		}
		this.hierarchy = hierarchy;
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
