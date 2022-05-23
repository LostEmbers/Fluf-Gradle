package lostembers.fluf.gradle.settings;

import groovy.lang.GroovyObjectSupport;
import lostembers.fluf.gradle.FlufProject;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

public abstract class Settings extends GroovyObjectSupport {
	public String mappings;
	public String version;
	public FlufProject project;
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getMappings() {
		return mappings;
	}
	
	public void setMappings(String mappings) {
		project.setMappings(mappings);
		this.mappings = mappings;
	}
	
	public Settings() {
	}
}
