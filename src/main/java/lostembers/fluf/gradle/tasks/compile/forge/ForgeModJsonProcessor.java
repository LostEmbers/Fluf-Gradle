package lostembers.fluf.gradle.tasks.compile.forge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ForgeModJsonProcessor {
	public static void process(File buildDir, Gson gson, JsonObject modJson, File forgeModToml) throws IOException {
		StringBuilder tomlBuilder = new StringBuilder();
		tomlBuilder.append("modLoader=\"javafml\"\n"); // TODO: check this
		tomlBuilder.append("loaderVersion=\"[39,)\"\n"); // TODO: do something with this?
		tomlBuilder.append("license=").append(modJson.get("license")).append("\n");
		
		tomlBuilder.append("[[mods]]\n");
		tomlBuilder.append("modId=").append(modJson.get("id")).append("\n");
		tomlBuilder.append("version=").append(modJson.get("version")).append("\n"); // TODO: support variables in version number
		tomlBuilder.append("displayName=").append(modJson.get("name")).append("\n");
		if (modJson.has("author")) {
			tomlBuilder.append("authors=").append(modJson.get("author")).append("\n");
		}
		// TODO: author list
		tomlBuilder.append("description=").append(modJson.get("description")).append("\n");
		// TODO: dependencies
		
		MainClassFabricator.fabricateMain(buildDir, modJson.getAsJsonObject("forge"), modJson.getAsJsonObject("entries"), modJson.get("id").getAsString());
		
		forgeModToml.getParentFile().mkdirs();
		forgeModToml.createNewFile();
		FileOutputStream outputStream = new FileOutputStream(forgeModToml);
		outputStream.write(tomlBuilder.toString().getBytes());
	}
}
