package net.fabricmc.mappings;

import java.io.IOException;
import java.io.InputStream;

public final class MappingsProvider {
	private MappingsProvider() {
	}// 25
	
	public static Mappings createEmptyMappings() {
//		return DummyMappings.INSTANCE;// 28
		return null;
	}
	
	public static Mappings readTinyMappings(InputStream stream) throws IOException {
		return readTinyMappings(stream, true);// 32
	}
	
	public static Mappings readTinyMappings(InputStream stream, boolean saveMemoryUsage) throws IOException {
//		return new TinyMappings(stream, (MappedStringDeduplicator)(saveMemoryUsage ? new MapBased() : MappedStringDeduplicator.EMPTY));// 36
		return null;
	}
}
