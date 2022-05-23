package lostembers.fluf.gradle.util.mappings;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class Mojmap {
	public static final String url = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
	public static final Gson gson = new Gson();
	
	public static FlufMappings parseMappings(String version) {
		File fl = new File("fluf_gradle/cache/" + version + "_mojmap.txt").getAbsoluteFile();
		File fl1 = new File("fluf_gradle/cache/" + version + ".json").getAbsoluteFile();
		String url = Mojmap.url;
		try {
			if (!fl.exists()) {
				fl.getParentFile().mkdirs();
				fl.createNewFile();
				String txt = readUrl(url);
				JsonObject object = gson.fromJson(txt, JsonObject.class);
				JsonArray array = object.getAsJsonArray("versions");
				
				url = null;
				for (JsonElement element : array) {
					if (element.getAsJsonObject().getAsJsonPrimitive("id").getAsString().equals(version)) {
						url = element.getAsJsonObject().getAsJsonPrimitive("url").getAsString();
						break;
					}
				}
				if (url == null) throw new RuntimeException("Could not find an entry for " + version);
				txt = readUrl(url);
				
				{
					FileOutputStream outputStream = new FileOutputStream(fl1);
					outputStream.write(txt.getBytes());
					outputStream.close();
					outputStream.flush();
				}
				
				object = gson.fromJson(txt, JsonObject.class);
				object = object.get("downloads").getAsJsonObject();
				object = object.get("client_mappings").getAsJsonObject();
				url = object.get("url").getAsString();
				
				txt = readUrl(url);
				// TODO: gzip
				FileOutputStream outputStream = new FileOutputStream(fl);
				outputStream.write(txt.getBytes());
				outputStream.close();
				outputStream.flush();
			}
			url = fl.toURL().toString();
		} catch (Throwable ignored) {
		}
		String txt = readUrl(url);
		return $parseMappings(txt);
	}
	
	private static FlufMappings $parseMappings(String text) {
		FlufMappings mappings = new FlufMappings();
		MappingsClass mappingsClass = null;
		MappingsMethod mappingsMethod = null;
		MappingsField mappingsField = null;
		for (String line : text.split("\n")) {
			if (line.startsWith("#")) continue;
			if (!line.startsWith("\t") && !line.startsWith(" ")) {
				String[] split = line.split(" -> ");
				String mojName = split[0].replace(".", "/");
				String obfName = split[1].replace(".", "/");
				if (obfName.endsWith(":")) obfName = obfName.substring(0, obfName.length() - 1);
				if (mappingsField != null) mappingsClass.addField(mappingsField);
				if (mappingsMethod != null) mappingsClass.addMethod(mappingsMethod);
				if (mappingsClass != null) mappings.addClass(mappingsClass);
				mappingsClass = new MappingsClass(obfName, mojName);
			} else if (line.contains("(")) {
				// method
				String[] split;
				if (line.contains(":")) split = line.split(":");
				else split = new String[]{line.trim()};
				line = split[split.length - 1];
				split = line.split(" -> ");
				String obfName = split[1];
				String left = split[0];
				split = left.split(" ");
				String returnType = split[0];
				String mojName = split[1];
				split = mojName.split("\\(");
				mojName = split[0];
				String desc = split[1];
				desc = "(" + mojmapIsDumb(desc) + ")";
				desc = desc + mojmapIsStillDumb(returnType);
				if (mappingsMethod != null) mappingsClass.addMethod(mappingsMethod);
				mappingsMethod = new MappingsMethod(obfName, mojName, desc, desc);
			} else {
				line = line.trim();
				String[] split = line.split(" -> ");
				String mojName = split[0];
				String obfName = split[1];
				split = mojName.split(" ");
				String desc = mojmapIsStillDumb(split[0]);
				mojName = split[1];
				if (mappingsField != null) mappingsClass.addField(mappingsField);
				mappingsField = new MappingsField(obfName, mojName, desc);
			}
		}
		if (mappingsField != null) mappingsClass.addField(mappingsField);
		if (mappingsMethod != null) mappingsClass.addMethod(mappingsMethod);
		if (mappingsClass != null) mappings.addClass(mappingsClass);
		return mappings;
	}
	
	public static String mojmapIsDumb(String desc) {
		if (desc.startsWith(")")) return "";
		StringBuilder out = new StringBuilder();
		String[] split = null;
		desc = desc.replace(")", "");
		if (desc.contains(",")) split = desc.split(",");
		else split = new String[]{desc};
		for (String s : split) {
			out.append(mojmapIsStillDumb(s));
		}
		return out.toString();
	}
	
	public static String mojmapIsStillDumb(String s) {
		if (s.equals("int")) return "I";
		else if (s.equals("long")) return "J";
		else if (s.equals("void")) return "V";
		else if (s.equals("byte")) return "B";
		else if (s.equals("short")) return "S";
		else if (s.equals("char")) return "C";
		else if (s.equals("float")) return "F";
		else if (s.equals("double")) return "D";
		else if (s.equals("boolean")) return "Z";
		else if (s.startsWith("[")) return "[" + mojmapIsDumb(s.substring(1));
		else return "L" + s.replace(".", "/") + ";";
	}
	
	public static String readUrl(String url) {
		try {
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
		} catch (Throwable ignored) {
			ignored.printStackTrace();
			throw new RuntimeException("send help");
		}
	}
	
	public static String downloadMappings(String version) {
		try {
			return null; // TODO:
		} catch (Throwable err) {
			return null;
		}
	}
}
