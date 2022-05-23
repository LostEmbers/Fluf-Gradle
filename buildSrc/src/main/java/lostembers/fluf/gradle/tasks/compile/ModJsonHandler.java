package lostembers.fluf.gradle.tasks.compile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lostembers.fluf.gradle.settings.Loader;
import lostembers.fluf.gradle.tasks.generic.FlufTask;
import lostembers.fluf.gradle.util.mappings.Mojmap;

import java.io.File;
import java.io.FileOutputStream;

public class ModJsonHandler extends FlufTask {
	public File buildDir = null;
	
	@Override
	public void run() {
		try {
			Gson gson = new GsonBuilder().setLenient().setPrettyPrinting().create();
			JsonObject modJson = gson.fromJson(Mojmap.readUrl(new File(buildDir + "/resources/main/fluf_mod.json").toURL().toString().replace("\\", "/")), JsonObject.class);
			System.out.println(modJson);
			File fabricModJson = new File(buildDir + "/resources/main/fabric.mod.json").getAbsoluteFile();
			if (fabricModJson.exists()) fabricModJson.delete();
			if (Loader.target.equals("fabric")) {
				JsonObject object = new JsonObject();
				object.addProperty("schemaVersion", 1);
				object.addProperty("id", modJson.get("id").getAsString());
				object.addProperty("name", modJson.get("name").getAsString());
				// TODO: support variable versions
				object.addProperty("version", modJson.get("version").getAsString());
				object.addProperty("license", modJson.get("license").getAsString());
				if (modJson.has("dependencies")) {
					JsonObject dependencies = new JsonObject();
					JsonObject obj = modJson.getAsJsonObject("dependencies");
					for (String s : obj.keySet()) {
						String depVersion = obj.get(s).getAsString();
						if (depVersion.equals("x")) {
							depVersion = "*";
						} else if (depVersion.endsWith(".x")) {
							depVersion = "~" + depVersion.replace(".x", "");
						} else if (depVersion.contains("-")) {
							if (depVersion.startsWith("(")) {
								// TODO
								throw new RuntimeException("NYI");
							} else {
								// TODO
								throw new RuntimeException("NYI");
							}
						}
						dependencies.addProperty(s, depVersion);
					}
					object.add("dependencies", dependencies);
				}
				
				if (modJson.has("side")) {
					if (modJson.get("side").getAsString().equals("any")) {
						object.addProperty("environment", "*");
					} else if (modJson.get("side").getAsString().equals("client") || modJson.get("side").getAsString().equals("server")) {
						throw new RuntimeException("NYI");
					} else {
						throw new RuntimeException("fluf_mod.json specifies an invalid side.");
					}
					// TODO
				} else {
					object.addProperty("environment", "*");
				}
				
				String txt = gson.toJson(object);
				FileOutputStream outputStream = new FileOutputStream(fabricModJson);
				outputStream.write(txt.getBytes());
				outputStream.close();
				outputStream.flush();
			}
			File flufModJson = new File(buildDir + "/resources/main/fluf_mod.json").getAbsoluteFile();
			if (flufModJson.exists()) flufModJson.delete();
		} catch (Throwable err) {
			System.err.println("Failed to read fluf_mod.json.");
			System.err.println("Does your mod json exist and follow conventions?");
		}
	}
}
