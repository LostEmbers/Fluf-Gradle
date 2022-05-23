package lostembers.fluf.gradle.util.mappings;

public class MappingsMethod {
	String obf;
	String mapped;
	String obfDesc;
	String mappedDesc;
	
	public MappingsMethod(String obf, String mapped, String obfDesc, String mappedDesc) {
		this.obf = obf;
		this.mapped = mapped;
		this.obfDesc = obfDesc;
		this.mappedDesc = mappedDesc;
	}
	
	public String toObf() {
		return obf + obfDesc;
	}
	
	public String toMapped() {
		return mapped + mappedDesc;
	}
}
