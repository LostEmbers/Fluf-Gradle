package lostembers.fluf.gradle.util.mappings;

public class MappingsField {
	String obf;
	String mapped;
	String obfDesc;
	
	public MappingsField(String obf, String mapped, String obfDesc) {
		this.obf = obf;
		this.mapped = mapped;
		this.obfDesc = obfDesc;
	}
	
	public String toObf() {
		return obfDesc + "|" + obf;
	}
	
	public String toMapped() {
		return obfDesc + "|" + mapped;
	}
}
