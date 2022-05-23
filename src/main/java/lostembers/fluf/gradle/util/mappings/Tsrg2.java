package lostembers.fluf.gradle.util.mappings;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class Tsrg2 {
	private static final String url = "https://raw.githubusercontent.com/MinecraftForge/MCPConfig/master/versions/release/$version$/joined.tsrg";
	
	public static FlufMappings parseMappings(String version) {
		// TODO: cache
		String mappingsFile = downloadMappings(version);
		FlufMappings mappings = new FlufMappings();
		MappingsClass mappingsClass = null;
		MappingsMethod method = null;
		MappingsField field = null;
		for (String string : mappingsFile.split("\n")) {
			if (!string.startsWith("\t")) {
				if (string.equals("tsrg2 obf srg id")) continue;
				if (string.startsWith("\t\t")) continue;
				string = string.replace("\t", "");
				String[] str = string.split(" ");
				String obf = str[0];
				String descOrName = str[1];
				if (descOrName.contains("/")) {
					// class
					if (method != null) mappingsClass.addMethod(method);
					if (mappingsClass != null) mappings.addClass(mappingsClass);
					method = null;
					field = null;
					mappingsClass = new MappingsClass(obf, descOrName);
				}
			} else {
				if (string.startsWith("\t\t")) continue;
				string = string.replace("\t", "");
				String[] str = string.split(" ");
				if (str.length <= 2) continue;
				String obf = str[0];
				String descOrName = str[1];
				if (descOrName.startsWith("f_") || str.length <= 3) {
					// field
					if (field != null) mappingsClass.addField(field);
					field = new MappingsField(obf, descOrName, "");
				} else {
					// method
					if (method != null) mappingsClass.addMethod(method);
					String name = str[2];
					method = new MappingsMethod(obf, name, descOrName, descOrName /* TODO */);
				}
			}
		}
		return mappings;
	}
	
	public static String downloadMappings(String version) {
		try {
			String url = Tsrg2.url.replace("$version$", version);
			URL u = new URL(url);
			URLConnection connection = u.openConnection();
			InputStream stream = connection.getInputStream();
			int b = 0;
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			while ((b = stream.read()) != -1) {
				outputStream.write(b);
//				int availible = stream.available();
//				if (availible != 0) {
//					byte[] bytes = new byte[availible];
//					stream.read(bytes);
//					outputStream.write(bytes);
//				}
			}
			String txt = outputStream.toString();
			stream.close();
			outputStream.close();
			outputStream.flush();
			return txt;
		} catch (Throwable err) {
			return null;
		}
	}
}
