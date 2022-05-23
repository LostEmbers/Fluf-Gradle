package net.fabricmc.mappings;

public class EntryTriple {
	private final String owner;
	private final String name;
	private final String desc;
	
	public EntryTriple(String owner, String name, String desc) {
		this.owner = owner;// 25
		this.name = name;// 26
		this.desc = desc;// 27
	}// 28
	
	public String getOwner() {
		return this.owner;// 31
	}
	
	public String getName() {
		return this.name;// 35
	}
	
	public String getDesc() {
		return this.desc;// 39
	}
}
