package lostembers.fluf.gradle.tasks.devenv.am;

public class AMEntry {
	public final String level;
	String className;
	boolean isField;
	String entryName;
	String descriptor;
	
	public AMEntry(String level, String className, String entryName, String descriptor) {
		this.level = level;
		this.className = className;
		this.isField = false;
		this.entryName = entryName;
		this.descriptor = descriptor;
	}
	
	public AMEntry(String level, String className, String entryName) {
		this.level = level;
		this.className = className;
		this.isField = true;
		this.entryName = entryName;
	}
	
	private boolean nameMatches(String name) {
		if (entryName.equals("*")) return true;
		return entryName.equals(name);
	}
	
	private boolean descMatches(String desc) {
		if (descriptor.equals("*")) return true;
		return desc.equals(descriptor);
	}
	
	public boolean matches(String className, String name, String descriptor) {
		if (descriptor == null && isField) {
			if (this.className == null) return nameMatches(name);
			return className.equals(this.className) && entryName.equals(className);
		} else if (descriptor != null && !isField) {
			if (this.className == null) return nameMatches(name) && descMatches(descriptor);
			return className.equals(this.className) && nameMatches(name) && descMatches(descriptor);
		}
		return false;
	}
}
